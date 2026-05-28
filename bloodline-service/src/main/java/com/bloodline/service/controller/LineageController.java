package com.bloodline.service.controller;

import com.bloodline.domain.entity.LineageColumnRef;
import com.bloodline.domain.entity.LineageEdge;
import com.bloodline.service.service.LineageColumnRefService;
import com.bloodline.service.service.LineageQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/lineage")
public class LineageController {
    private final LineageQueryService lineageQueryService;
    private final LineageColumnRefService lineageColumnRefService;

    public LineageController(LineageQueryService lineageQueryService, LineageColumnRefService lineageColumnRefService) {
        this.lineageQueryService = lineageQueryService;
        this.lineageColumnRefService = lineageColumnRefService;
    }

    @GetMapping("/apps/{appId}/upstream")
    public ResponseEntity<List<LineageEdge>> getUpstream(
            @PathVariable String appId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String projectBranch,
            @RequestParam(required = false) String projectId) {
        if (branch != null && projectBranch != null && projectId != null) {
            return ResponseEntity.ok(lineageQueryService.getUpstreamWithOverlay("dept_01", appId, branch, projectBranch, projectId));
        }
        return ResponseEntity.ok(lineageQueryService.getUpstream("dept_01", appId));
    }

    @GetMapping("/apps/{appId}/downstream")
    public ResponseEntity<List<String>> getDownstream(@PathVariable String appId) {
        return ResponseEntity.ok(lineageQueryService.getDownstreamAppIds("dept_01", appId));
    }

    @GetMapping("/apps/{appId}/graph")
    public ResponseEntity<LineageQueryService.LineageGraph> getGraph(
            @PathVariable String appId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String projectBranch,
            @RequestParam(required = false) String projectId) {
        if (branch != null && projectBranch != null && projectId != null) {
            return ResponseEntity.ok(lineageQueryService.getLineageGraphWithOverlay("dept_01", appId, branch, projectBranch, projectId));
        }
        return ResponseEntity.ok(lineageQueryService.getLineageGraph("dept_01", appId));
    }

    @GetMapping("/apps/{appId}/upstream/recursive")
    public ResponseEntity<List<LineageEdge>> getUpstreamRecursive(
            @PathVariable String appId,
            @RequestParam(defaultValue = "5") int maxDepth) {
        return ResponseEntity.ok(lineageQueryService.getUpstreamRecursive("dept_01", appId, maxDepth));
    }

    @GetMapping("/apps/{appId}/downstream/recursive")
    public ResponseEntity<List<String>> getDownstreamRecursive(
            @PathVariable String appId,
            @RequestParam(defaultValue = "5") int maxDepth) {
        return ResponseEntity.ok(lineageQueryService.getDownstreamRecursive("dept_01", appId, maxDepth));
    }

    @GetMapping("/apps/{appId}/tables")
    public ResponseEntity<List<String>> getTablesByApp(@PathVariable String appId) {
        return ResponseEntity.ok(lineageColumnRefService.findTablesByApp(appId));
    }

    @GetMapping("/tables/{tableName}/apps")
    public ResponseEntity<List<String>> getAppsUsingTable(@PathVariable String tableName) {
        return ResponseEntity.ok(lineageQueryService.getAppsUsingTable("dept_01", tableName));
    }

    @GetMapping("/tables")
    public ResponseEntity<List<String>> getAllTables() {
        return ResponseEntity.ok(lineageColumnRefService.findAllTables());
    }

    @GetMapping("/tables/{tableName}/columns")
    public ResponseEntity<List<String>> getTableColumns(@PathVariable String tableName) {
        return ResponseEntity.ok(lineageColumnRefService.findColumnsByTable(tableName));
    }

    @GetMapping("/apps/{appId}/fields")
    public ResponseEntity<Map<String, Object>> getFieldLineage(
            @PathVariable String appId,
            @RequestParam String tableName,
            @RequestParam String columnName,
            @RequestParam(defaultValue = "2") int depth) {

        List<LineageColumnRef> refs = lineageColumnRefService.findByColumn(tableName, columnName);

        Set<Map<String, Object>> nodes = new LinkedHashSet<>();
        Set<Map<String, Object>> edges = new LinkedHashSet<>();

        String columnId = tableName + "." + columnName;

        // Add the target column node
        Map<String, Object> columnNode = new LinkedHashMap<>();
        columnNode.put("type", "column");
        columnNode.put("id", columnId);
        columnNode.put("name", columnName);
        columnNode.put("table", tableName);
        nodes.add(columnNode);

        // Track which columns have been processed to avoid infinite loops
        Set<String> processedColumns = new HashSet<>();
        processedColumns.add(columnId);

        // Process refs: add app nodes and app-to-column edges
        for (LineageColumnRef ref : refs) {
            String refAppId = ref.getAppId();

            Map<String, Object> appNode = new LinkedHashMap<>();
            appNode.put("type", "app");
            appNode.put("id", refAppId);
            appNode.put("name", refAppId);
            nodes.add(appNode);

            Map<String, Object> appEdge = new LinkedHashMap<>();
            appEdge.put("source", refAppId);
            appEdge.put("target", columnId);
            appEdge.put("relation", "QUERIES");
            edges.add(appEdge);

            // Find co-occurring columns via SQL signature (up to depth)
            if (depth > 1 && ref.getSqlSignature() != null) {
                List<LineageColumnRef> coRefs = lineageColumnRefService.findBySqlSignature(ref.getSqlSignature());
                for (LineageColumnRef coRef : coRefs) {
                    String coColumnId = coRef.getTableName() + "." + coRef.getColumnName();
                    if (columnId.equals(coColumnId)) {
                        continue; // skip self
                    }

                    Map<String, Object> coColumnNode = new LinkedHashMap<>();
                    coColumnNode.put("type", "column");
                    coColumnNode.put("id", coColumnId);
                    coColumnNode.put("name", coRef.getColumnName());
                    coColumnNode.put("table", coRef.getTableName());
                    nodes.add(coColumnNode);

                    Map<String, Object> coEdge = new LinkedHashMap<>();
                    coEdge.put("source", columnId);
                    coEdge.put("target", coColumnId);
                    coEdge.put("relation", "CO_OCCUR");
                    coEdge.put("sqlSignature", ref.getSqlSignature());
                    edges.add(coEdge);

                    // Add app node for co-occurring column's app
                    Map<String, Object> coAppNode = new LinkedHashMap<>();
                    coAppNode.put("type", "app");
                    coAppNode.put("id", coRef.getAppId());
                    coAppNode.put("name", coRef.getAppId());
                    nodes.add(coAppNode);

                    Map<String, Object> coAppEdge = new LinkedHashMap<>();
                    coAppEdge.put("source", coRef.getAppId());
                    coAppEdge.put("target", coColumnId);
                    coAppEdge.put("relation", "QUERIES");
                    edges.add(coAppEdge);
                }
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("appId", appId);
        response.put("tableName", tableName);
        response.put("columnName", columnName);
        response.put("depth", depth);
        response.put("nodes", new ArrayList<>(nodes));
        response.put("edges", new ArrayList<>(edges));

        return ResponseEntity.ok(response);
    }
}
