package com.bloodline.service.controller;

import com.bloodline.common.context.TenantContext;
import com.bloodline.domain.entity.LineageSnapshot;
import com.bloodline.service.service.SnapshotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/lineage/snapshots")
public class LineageSnapshotController {

    private final SnapshotService snapshotService;

    public LineageSnapshotController(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    @GetMapping
    public ResponseEntity<List<LineageSnapshot>> list() {
        requireTenant();
        return ResponseEntity.ok(snapshotService.listSnapshots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LineageSnapshot> get(@PathVariable Long id) {
        requireTenant();
        LineageSnapshot snapshot = snapshotService.getSnapshot(id);
        if (snapshot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(snapshot);
    }

    @PostMapping
    public ResponseEntity<LineageSnapshot> create(@RequestBody Map<String, String> request) {
        requireTenant();
        String name = request.get("snapshotName");
        String type = request.get("snapshotType");
        if (name == null || name.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String refId = request.get("refId");
        LineageSnapshot snapshot = snapshotService.createSnapshot(name, type, refId);
        return ResponseEntity.ok(snapshot);
    }

    private Long requireTenant() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set.");
        }
        return tenantId;
    }
}
