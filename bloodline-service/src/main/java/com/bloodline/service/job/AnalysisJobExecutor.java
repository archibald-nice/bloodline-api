package com.bloodline.service.job;

import com.bloodline.domain.entity.AnalysisTask;
import com.bloodline.domain.mapper.AnalysisTaskMapper;
import com.bloodline.service.service.AnalysisTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnalysisJobExecutor {

    private final AnalysisTaskMapper analysisTaskMapper;
    private final AnalysisTaskService analysisTaskService;

    public AnalysisJobExecutor(AnalysisTaskMapper analysisTaskMapper, AnalysisTaskService analysisTaskService) {
        this.analysisTaskMapper = analysisTaskMapper;
        this.analysisTaskService = analysisTaskService;
    }

    @Scheduled(fixedDelay = 30000)
    public void processPendingTasks() {
        List<AnalysisTask> pendingTasks = analysisTaskMapper.findPendingTasks("dept_01", 10);
        for (AnalysisTask task : pendingTasks) {
            analysisTaskService.executeTask(task.getId());
        }
    }
}
