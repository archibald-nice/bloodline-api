package com.bloodline.service.controller;

import com.bloodline.service.service.OpenLineageExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/openlineage")
public class OpenLineageExportController {

    private final OpenLineageExportService exportService;

    public OpenLineageExportController(OpenLineageExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/export")
    public ResponseEntity<Map<String, String>> exportLineage(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobNamespace,
            @RequestBody(required = false) ExportRequest request) {

        String actualJobName = jobName != null ? jobName : "bloodline-export";
        String actualJobNamespace = jobNamespace != null ? jobNamespace : "bloodline";

        String json;
        if (request != null && request.getNodeIds() != null && !request.getNodeIds().isEmpty()) {
            json = exportService.exportByNodes(
                    request.getTenantId(), request.getNodeIds(), actualJobName, actualJobNamespace);
        } else {
            json = exportService.exportCurrentTenant(actualJobName, actualJobNamespace);
        }

        return ResponseEntity.ok(Collections.singletonMap("runEvent", json));
    }

    public static class ExportRequest {
        private Long tenantId;
        private List<String> nodeIds;
        private List<String> relationTypes;

        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public List<String> getNodeIds() { return nodeIds; }
        public void setNodeIds(List<String> nodeIds) { this.nodeIds = nodeIds; }
        public List<String> getRelationTypes() { return relationTypes; }
        public void setRelationTypes(List<String> relationTypes) { this.relationTypes = relationTypes; }
    }
}
