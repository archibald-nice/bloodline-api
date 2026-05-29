package com.bloodline.service.controller;

import com.bloodline.common.context.TenantContext;
import com.bloodline.service.service.ConflictAnalyzer;
import com.bloodline.service.service.ConflictAnalyzer.ConflictReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/lineage/conflict")
public class ConflictController {

    private final ConflictAnalyzer conflictAnalyzer;

    public ConflictController(ConflictAnalyzer conflictAnalyzer) {
        this.conflictAnalyzer = conflictAnalyzer;
    }

    @PostMapping("/analyze")
    public ResponseEntity<ConflictReport> analyze(
            @RequestBody Map<String, Long> request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set.");
        }

        Long baseId = request.get("baseSnapshotId");
        Long compareId = request.get("compareSnapshotId");

        if (baseId == null || compareId == null || baseId <= 0 || compareId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        ConflictReport report = conflictAnalyzer.analyze(baseId, compareId);
        return ResponseEntity.ok(report);
    }
}
