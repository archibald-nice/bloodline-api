package com.bloodline.service.job;

import com.bloodline.domain.entity.AnalysisTask;
import com.bloodline.domain.mapper.AnalysisTaskMapper;
import com.bloodline.service.service.AnalysisTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnalysisJobExecutor {

    private static final int TIMEOUT_MINUTES = 10;

    private final AnalysisTaskMapper analysisTaskMapper;
    private final AnalysisTaskService analysisTaskService;

    public AnalysisJobExecutor(AnalysisTaskMapper analysisTaskMapper, AnalysisTaskService analysisTaskService) {
        this.analysisTaskMapper = analysisTaskMapper;
        this.analysisTaskService = analysisTaskService;
    }

    @Scheduled(fixedDelay = 30000)
    public void processPendingTasks() {
        // First: mark any long-running tasks as timed out
        analysisTaskService.markTimedOutTasks(TIMEOUT_MINUTES);

        // Then: process pending tasks
        List<AnalysisTask> pendingTasks = analysisTaskMapper.findPendingTasks("dept_01", 10);
        for (AnalysisTask task : pendingTasks) {
            analysisTaskService.executeTask(task.getId());
            checkAndReTriggerStale(task);
        }
    }

    private void checkAndReTriggerStale(AnalysisTask completedTask) {
        AnalysisTask staleTask = analysisTaskMapper.findLatestStaleTask(
                completedTask.getTenantId(),
                completedTask.getAppId(),
                completedTask.getBranch()
        );
        if (staleTask != null) {
            analysisTaskMapper.updateStatusAndStartedAt(staleTask.getId(), 0);
        }
    }
}
