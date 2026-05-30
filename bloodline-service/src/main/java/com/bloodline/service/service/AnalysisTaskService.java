package com.bloodline.service.service;

import com.bloodline.domain.entity.AnalysisTask;
import com.bloodline.domain.entity.Application;
import com.bloodline.domain.mapper.AnalysisTaskMapper;
import com.bloodline.domain.mapper.ApplicationMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
public class AnalysisTaskService {

    private static final long LOCK_TTL_SECONDS = 600; // 10 minutes

    private final AnalysisTaskMapper analysisTaskMapper;
    private final AnalysisService analysisService;
    private final DistributedLockService lockService;
    private final ApplicationMapper applicationMapper;
    private final GitHubCodeFetchService codeFetchService;

    public AnalysisTaskService(AnalysisTaskMapper analysisTaskMapper,
                               AnalysisService analysisService,
                               DistributedLockService lockService,
                               ApplicationMapper applicationMapper,
                               GitHubCodeFetchService codeFetchService) {
        this.analysisTaskMapper = analysisTaskMapper;
        this.analysisService = analysisService;
        this.lockService = lockService;
        this.applicationMapper = applicationMapper;
        this.codeFetchService = codeFetchService;
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

        boolean locked = lockService.tryLock(task.getAppId(), task.getBranch(), LOCK_TTL_SECONDS);
        if (!locked) {
            analysisTaskMapper.updateStatusAndStartedAt(taskId, 5); // stale
            return;
        }

        File repoDir = null;
        try {
            analysisTaskMapper.updateStatusAndStartedAt(taskId, 1); // running

            try {
                Application app = applicationMapper.findByAppId(task.getTenantId(), task.getAppId());
                if (app == null || app.getGitUrl() == null) {
                    throw new IllegalStateException("Application has no git_url configured");
                }

                repoDir = codeFetchService.cloneRepository(app.getGitUrl(), task.getBranch(), task.getCommitSha());
                List<GitHubCodeFetchService.SourceFile> files = codeFetchService.ensembleSourceFiles(repoDir);

                analysisService.analyzeSourceFiles(
                        task.getTenantId(),
                        task.getAppId(),
                        task.getBranch(),
                        task.getProjectId() != null ? task.getProjectId().toString() : null,
                        files
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
        } finally {
            lockService.unlock(task.getAppId(), task.getBranch());
            if (repoDir != null) {
                codeFetchService.cleanup(repoDir);
            }
        }
    }

    @Transactional
    public void markTimedOutTasks(int timeoutMinutes) {
        for (AnalysisTask task : analysisTaskMapper.findRunningTasksOlderThan(timeoutMinutes)) {
            AnalysisTask timeoutTask = new AnalysisTask();
            timeoutTask.setId(task.getId());
            timeoutTask.setStatus(4); // timeout
            timeoutTask.setErrorMsg("Task exceeded " + timeoutMinutes + " minutes timeout");
            analysisTaskMapper.updateStatus(timeoutTask);
        }
    }

    public AnalysisTask getTaskStatus(Long taskId) {
        return analysisTaskMapper.findById(taskId);
    }

    public List<AnalysisTask> listTasks(String tenantId, Integer limit) {
        return analysisTaskMapper.findByTenant(tenantId, limit);
    }

    /**
     * Execute task asynchronously (immediate trigger).
     * Returns true if execution was submitted, false if task not found or already processed.
     */
    @Async("analysisTaskExecutor")
    public boolean executeAsync(Long taskId) {
        AnalysisTask task = analysisTaskMapper.findById(taskId);
        if (task == null || task.getStatus() != 0) {
            return false;
        }
        try {
            executeTask(taskId);
            return true;
        } catch (Exception e) {
            AnalysisTask failedTask = new AnalysisTask();
            failedTask.setId(taskId);
            failedTask.setStatus(3);
            failedTask.setErrorMsg("Immediate execution failed: " + e.getMessage());
            analysisTaskMapper.updateStatus(failedTask);
            return false;
        }
    }
}
