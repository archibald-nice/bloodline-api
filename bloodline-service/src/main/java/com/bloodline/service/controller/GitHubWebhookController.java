package com.bloodline.service.controller;

import com.bloodline.domain.entity.Application;
import com.bloodline.domain.mapper.ApplicationMapper;
import com.bloodline.service.service.AnalysisTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/github")
public class GitHubWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(GitHubWebhookController.class);

    private final AnalysisTaskService analysisTaskService;
    private final ApplicationMapper applicationMapper;
    private final boolean immediateTriggerEnabled;

    public GitHubWebhookController(AnalysisTaskService analysisTaskService,
                                   ApplicationMapper applicationMapper,
                                   @Value("${analysis.immediate-trigger:false}") boolean immediateTriggerEnabled) {
        this.analysisTaskService = analysisTaskService;
        this.applicationMapper = applicationMapper;
        this.immediateTriggerEnabled = immediateTriggerEnabled;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody Map<String, Object> payload) {

        if (!"push".equals(eventType)) {
            return ResponseEntity.badRequest().build();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
        if (repository == null) {
            return ResponseEntity.badRequest().build();
        }

        String cloneUrl = (String) repository.get("clone_url");
        String branch = extractBranchName((String) payload.get("ref"));
        String commitSha = (String) payload.get("after");

        if (cloneUrl == null || branch == null) {
            return ResponseEntity.badRequest().build();
        }

        Application app = applicationMapper.findByGitUrl("dept_01", cloneUrl);
        if (app == null) {
            logger.warn("No application registered for git url: {}", cloneUrl);
            return ResponseEntity.notFound().build();
        }

        Long taskId = analysisTaskService.submitTask(
                "dept_01", null, app.getAppId(), branch, commitSha, 1
        );

        boolean triggered = false;
        if (immediateTriggerEnabled) {
            triggered = analysisTaskService.executeAsync(taskId);
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("taskId", taskId);
        result.put("immediateTrigger", immediateTriggerEnabled);
        result.put("triggered", triggered);
        return ResponseEntity.ok(result);
    }

    private String extractBranchName(String ref) {
        if (ref == null || !ref.startsWith("refs/heads/")) {
            return null;
        }
        return ref.substring("refs/heads/".length());
    }
}
