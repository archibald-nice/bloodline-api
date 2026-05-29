package com.bloodline.service.controller;

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
        Long baseId = request.get("baseSnapshotId");
        Long compareId = request.get("compareSnapshotId");

        if (baseId == null || compareId == null) {
            return ResponseEntity.badRequest().build();
        }

        ConflictReport report = conflictAnalyzer.analyze(baseId, compareId);
        return ResponseEntity.ok(report);
    }
}
