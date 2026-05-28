package com.bloodline.service.controller;

import com.bloodline.service.service.ImpactAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ImpactAnalysisController {
    private final ImpactAnalysisService impactAnalysisService;

    public ImpactAnalysisController(ImpactAnalysisService impactAnalysisService) {
        this.impactAnalysisService = impactAnalysisService;
    }

    @PostMapping("/impact-analysis")
    public ResponseEntity<ImpactAnalysisService.ImpactReport> analyzeImpact(
            @RequestBody ImpactAnalysisService.ImpactRequest request) {
        return ResponseEntity.ok(impactAnalysisService.analyze(request));
    }
}
