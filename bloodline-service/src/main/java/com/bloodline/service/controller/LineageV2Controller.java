package com.bloodline.service.controller;

import com.bloodline.common.context.TenantContext;
import com.bloodline.domain.entity.LineageEdgeV2;
import com.bloodline.domain.mapper.LineageEdgeV2Mapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/lineage")
public class LineageV2Controller {

    private final LineageEdgeV2Mapper edgeMapper;

    public LineageV2Controller(LineageEdgeV2Mapper edgeMapper) {
        this.edgeMapper = edgeMapper;
    }

    @GetMapping("/nodes/{nodeId}/upstream")
    public ResponseEntity<List<LineageEdgeV2>> getUpstream(
            @PathVariable String nodeId,
            @RequestParam(defaultValue = "3") int maxDepth) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set.");
        }
        return ResponseEntity.ok(edgeMapper.findUpstreamRecursive(tenantId, nodeId, maxDepth));
    }

    @GetMapping("/nodes/{nodeId}/downstream")
    public ResponseEntity<List<LineageEdgeV2>> getDownstream(
            @PathVariable String nodeId,
            @RequestParam(defaultValue = "3") int maxDepth) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set.");
        }
        return ResponseEntity.ok(edgeMapper.findDownstreamRecursive(tenantId, nodeId, maxDepth));
    }

    @GetMapping("/graph")
    public ResponseEntity<Map<String, Object>> getGraph(
            @RequestParam String nodeId,
            @RequestParam(defaultValue = "3") int maxDepth) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set.");
        }
        List<LineageEdgeV2> upstream = edgeMapper.findUpstreamRecursive(tenantId, nodeId, maxDepth);
        List<LineageEdgeV2> downstream = edgeMapper.findDownstreamRecursive(tenantId, nodeId, maxDepth);

        Map<String, Object> result = new HashMap<>();
        result.put("nodeId", nodeId);
        result.put("upstream", upstream);
        result.put("downstream", downstream);
        return ResponseEntity.ok(result);
    }
}
