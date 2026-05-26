package com.bloodline.service.controller;

import com.bloodline.domain.entity.AnalysisTask;
import com.bloodline.service.service.AnalysisService;
import com.bloodline.service.service.AnalysisTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;
    private final AnalysisTaskService analysisTaskService;

    public AnalysisController(AnalysisService analysisService, AnalysisTaskService analysisTaskService) {
        this.analysisService = analysisService;
        this.analysisTaskService = analysisTaskService;
    }

    @PostMapping("/java")
    public ResponseEntity<Void> analyzeJava(@RequestBody Map<String, String> body) {
        String tenantId = "dept_01";
        analysisService.analyzeJavaSource(
            tenantId,
            body.get("appId"),
            body.getOrDefault("branch", "release_sit"),
            body.get("projectId"),
            body.get("sourceCode")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mybatis-xml")
    public ResponseEntity<Void> analyzeMyBatisXml(@RequestBody Map<String, String> body) {
        String tenantId = "dept_01";
        analysisService.analyzeMyBatisXml(
            tenantId,
            body.get("appId"),
            body.getOrDefault("branch", "release_sit"),
            body.get("projectId"),
            body.get("xmlContent")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tasks")
    public ResponseEntity<Map<String, Long>> submitTask(@RequestBody Map<String, String> body) {
        String tenantId = "dept_01";
        Long taskId = analysisTaskService.submitTask(
            tenantId,
            body.get("projectId") != null ? Long.valueOf(body.get("projectId")) : null,
            body.get("appId"),
            body.getOrDefault("branch", "release_sit"),
            body.get("commitSha"),
            body.get("triggerType") != null ? Integer.valueOf(body.get("triggerType")) : 2
        );
        return ResponseEntity.ok(Collections.singletonMap("taskId", taskId));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<AnalysisTask> getTaskStatus(@PathVariable Long taskId) {
        AnalysisTask task = analysisTaskService.getTaskStatus(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Long>> receiveWebhook(@RequestBody Map<String, Object> payload) {
        String tenantId = "dept_01";
        String appId = (String) payload.getOrDefault("appId", "unknown");
        String branch = (String) payload.getOrDefault("branch", "release_sit");
        String commitSha = (String) payload.get("commitSha");

        Long taskId = analysisTaskService.submitTask(
            tenantId,
            null,
            appId,
            branch,
            commitSha,
            1 // webhook trigger
        );
        return ResponseEntity.ok(Collections.singletonMap("taskId", taskId));
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, List<Long>>> batchAnalyze(@RequestBody Map<String, Object> body) {
        String tenantId = "dept_01";
        @SuppressWarnings("unchecked")
        List<String> appIds = (List<String>) body.get("appIds");
        String branch = (String) body.getOrDefault("branch", "release_sit");
        Long projectId = body.get("projectId") != null ? Long.valueOf(body.get("projectId").toString()) : null;

        List<Long> taskIds = new java.util.ArrayList<>();
        for (String appId : appIds) {
            Long taskId = analysisTaskService.submitTask(tenantId, projectId, appId, branch, null, 2);
            taskIds.add(taskId);
        }
        return ResponseEntity.ok(Collections.singletonMap("taskIds", taskIds));
    }
}
