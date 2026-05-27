package com.bloodline.service.controller;

import com.bloodline.domain.entity.LineageEdge;
import com.bloodline.service.service.LineageQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lineage")
public class LineageController {
    private final LineageQueryService lineageQueryService;

    public LineageController(LineageQueryService lineageQueryService) {
        this.lineageQueryService = lineageQueryService;
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

    @GetMapping("/tables/{tableName}/apps")
    public ResponseEntity<List<String>> getAppsUsingTable(@PathVariable String tableName) {
        return ResponseEntity.ok(lineageQueryService.getAppsUsingTable("dept_01", tableName));
    }
}
