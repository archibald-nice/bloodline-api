package com.bloodline.service.service;

import com.bloodline.domain.entity.AnalysisTask;
import com.bloodline.domain.mapper.AnalysisTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisTaskService {

    private final AnalysisTaskMapper analysisTaskMapper;
    private final AnalysisService analysisService;

    public AnalysisTaskService(AnalysisTaskMapper analysisTaskMapper, AnalysisService analysisService) {
        this.analysisTaskMapper = analysisTaskMapper;
        this.analysisService = analysisService;
    }

    @Transactional
    public Long submitTask(String tenantId, Long projectId, String appId, String branch,
                           String commitSha, Integer triggerType) {
        AnalysisTask task = new AnalysisTask();
        task.setTenantId(tenantId);
        task.setProjectId(projectId);
        task.setAppId(appId);
        task.setBranch(branch);
        task.setCommitSha(commitSha);
        task.setTriggerType(triggerType);
        task.setStatus(0); // pending
        analysisTaskMapper.insert(task);
        return task.getId();
    }

    @Transactional
    public void executeTask(Long taskId) {
        AnalysisTask task = analysisTaskMapper.findById(taskId);
        if (task == null || task.getStatus() != 0) {
            return;
        }

        analysisTaskMapper.updateStatusAndStartedAt(taskId, 1); // running

        try {
            analysisService.analyzeJavaSource(
                task.getTenantId(),
                task.getAppId(),
                task.getBranch(),
                task.getProjectId() != null ? task.getProjectId().toString() : null,
                ""
            );

            AnalysisTask completedTask = new AnalysisTask();
            completedTask.setId(taskId);
            completedTask.setStatus(2); // completed
            completedTask.setResultSummary("Analysis completed successfully");
            analysisTaskMapper.updateStatus(completedTask);
        } catch (Exception e) {
            AnalysisTask failedTask = new AnalysisTask();
            failedTask.setId(taskId);
            failedTask.setStatus(3); // failed
            failedTask.setErrorMsg(e.getMessage());
            analysisTaskMapper.updateStatus(failedTask);
        }
    }

    public AnalysisTask getTaskStatus(Long taskId) {
        return analysisTaskMapper.findById(taskId);
    }
}
