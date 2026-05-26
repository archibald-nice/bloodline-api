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
    public ResponseEntity<List<LineageEdge>> getUpstream(@PathVariable String appId) {
        return ResponseEntity.ok(lineageQueryService.getUpstream("dept_01", appId));
    }

    @GetMapping("/apps/{appId}/downstream")
    public ResponseEntity<List<String>> getDownstream(@PathVariable String appId) {
        return ResponseEntity.ok(lineageQueryService.getDownstreamAppIds("dept_01", appId));
    }

    @GetMapping("/apps/{appId}/graph")
    public ResponseEntity<LineageQueryService.LineageGraph> getGraph(@PathVariable String appId) {
        return ResponseEntity.ok(lineageQueryService.getLineageGraph("dept_01", appId));
    }

    @GetMapping("/tables/{tableName}/apps")
    public ResponseEntity<List<String>> getAppsUsingTable(@PathVariable String tableName) {
        return ResponseEntity.ok(lineageQueryService.getAppsUsingTable("dept_01", tableName));
    }
}
