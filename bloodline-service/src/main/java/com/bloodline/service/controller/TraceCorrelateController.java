package com.bloodline.service.controller;

import com.bloodline.common.context.TenantContext;
import com.bloodline.service.service.SkyWalkingTraceQueryService;
import com.bloodline.service.service.TraceLineageCorrelator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/trace")
public class TraceCorrelateController {

    private final SkyWalkingTraceQueryService traceQueryService;
    private final TraceLineageCorrelator correlator;

    public TraceCorrelateController(SkyWalkingTraceQueryService traceQueryService,
                                     TraceLineageCorrelator correlator) {
        this.traceQueryService = traceQueryService;
        this.correlator = correlator;
    }

    @PostMapping("/correlate")
    public ResponseEntity<Map<String, Object>> correlate(
            @RequestParam String serviceName,
            @RequestParam(defaultValue = "30") int minutes,
            @RequestHeader(value = "X-Tenant-ID", required = false) Long tenantId) {

        Long actualTenantId = tenantId != null ? tenantId : TenantContext.getCurrentTenant();
        if (actualTenantId == null) {
            actualTenantId = 1L; // default tenant fallback
        }

        List<SkyWalkingTraceQueryService.TraceSpan> spans = traceQueryService.queryRecentTraces(serviceName, minutes);
        List<TraceLineageCorrelator.CorrelationResult> results = correlator.correlate(actualTenantId, spans);

        long missingCount = results.stream().filter(TraceLineageCorrelator.CorrelationResult::isLineageMissing).count();
        long matchedCount = results.stream().filter(r -> !r.isLineageMissing()).count();

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("serviceName", serviceName);
        response.put("timeWindowMinutes", minutes);
        response.put("totalTraces", spans.size());
        response.put("matched", matchedCount);
        response.put("missingLineage", missingCount);
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/correlate/status")
    public ResponseEntity<Map<String, Object>> status() {
        boolean skywalkingAvailable = traceQueryService.queryRecentTraces("test", 1) != null;
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("skywalkingConfigured", skywalkingAvailable);
        status.put("status", skywalkingAvailable ? "AVAILABLE" : "NOT_CONFIGURED");
        return ResponseEntity.ok(status);
    }
}
