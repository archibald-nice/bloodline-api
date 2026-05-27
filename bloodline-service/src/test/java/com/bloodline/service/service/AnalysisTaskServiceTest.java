package com.bloodline.service.service;

import com.bloodline.domain.entity.AnalysisTask;
import com.bloodline.domain.entity.Application;
import com.bloodline.domain.mapper.AnalysisTaskMapper;
import com.bloodline.domain.mapper.ApplicationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisTaskServiceTest {

    @Mock
    private AnalysisTaskMapper analysisTaskMapper;

    @Mock
    private AnalysisService analysisService;

    @Mock
    private DistributedLockService lockService;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private GitHubCodeFetchService codeFetchService;

    @InjectMocks
    private AnalysisTaskService analysisTaskService;

    @Test
    void shouldSubmitTaskWithPendingStatus() {
        when(analysisTaskMapper.insert(any(AnalysisTask.class))).thenAnswer(invocation -> {
            AnalysisTask task = invocation.getArgument(0);
            task.setId(100L);
            return 1;
        });

        Long taskId = analysisTaskService.submitTask("dept_01", 1L, "app1", "release_sit", "abc123", 2);

        assertThat(taskId).isEqualTo(100L);
        ArgumentCaptor<AnalysisTask> captor = ArgumentCaptor.forClass(AnalysisTask.class);
        verify(analysisTaskMapper).insert(captor.capture());
        AnalysisTask task = captor.getValue();
        assertThat(task.getTenantId()).isEqualTo("dept_01");
        assertThat(task.getProjectId()).isEqualTo(1L);
        assertThat(task.getAppId()).isEqualTo("app1");
        assertThat(task.getBranch()).isEqualTo("release_sit");
        assertThat(task.getCommitSha()).isEqualTo("abc123");
        assertThat(task.getTriggerType()).isEqualTo(2);
        assertThat(task.getStatus()).isEqualTo(0);
    }

    @Test
    void shouldExecuteTaskAndMarkCompleted() throws Exception {
        AnalysisTask pendingTask = new AnalysisTask();
        pendingTask.setId(1L);
        pendingTask.setStatus(0);
        pendingTask.setTenantId("dept_01");
        pendingTask.setAppId("app1");
        pendingTask.setBranch("release_sit");
        pendingTask.setProjectId(1L);
        when(analysisTaskMapper.findById(1L)).thenReturn(pendingTask);
        when(lockService.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);

        Application app = new Application();
        app.setAppId("app1");
        app.setGitUrl("https://github.com/acme/app1.git");
        when(applicationMapper.findByAppId("dept_01", "app1")).thenReturn(app);

        File mockDir = new File("/tmp/repo");
        doReturn(mockDir).when(codeFetchService).cloneRepository(anyString(), anyString(), any());
        when(codeFetchService.ensembleSourceFiles(mockDir)).thenReturn(Collections.emptyList());

        analysisTaskService.executeTask(1L);

        verify(analysisTaskMapper).updateStatusAndStartedAt(1L, 1);
        verify(analysisService).analyzeSourceFiles(anyString(), anyString(), anyString(), anyString(), any());
        verify(codeFetchService).cleanup(mockDir);

        ArgumentCaptor<AnalysisTask> captor = ArgumentCaptor.forClass(AnalysisTask.class);
        verify(analysisTaskMapper).updateStatus(captor.capture());
        AnalysisTask completedTask = captor.getValue();
        assertThat(completedTask.getId()).isEqualTo(1L);
        assertThat(completedTask.getStatus()).isEqualTo(2);
        assertThat(completedTask.getResultSummary()).isEqualTo("Analysis completed successfully");
    }

    @Test
    void shouldMarkFailedWhenAnalysisThrowsException() throws Exception {
        AnalysisTask pendingTask = new AnalysisTask();
        pendingTask.setId(1L);
        pendingTask.setStatus(0);
        pendingTask.setTenantId("dept_01");
        pendingTask.setAppId("app1");
        pendingTask.setBranch("release_sit");
        pendingTask.setProjectId(1L);
        when(analysisTaskMapper.findById(1L)).thenReturn(pendingTask);
        when(lockService.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);

        Application app = new Application();
        app.setAppId("app1");
        app.setGitUrl("https://github.com/acme/app1.git");
        when(applicationMapper.findByAppId("dept_01", "app1")).thenReturn(app);

        File mockDir = new File("/tmp/repo");
        doReturn(mockDir).when(codeFetchService).cloneRepository(anyString(), anyString(), any());
        when(codeFetchService.ensembleSourceFiles(mockDir)).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("Parse error")).when(analysisService).analyzeSourceFiles(anyString(), anyString(), anyString(), anyString(), any());

        analysisTaskService.executeTask(1L);

        verify(codeFetchService).cleanup(mockDir);

        ArgumentCaptor<AnalysisTask> captor = ArgumentCaptor.forClass(AnalysisTask.class);
        verify(analysisTaskMapper).updateStatus(captor.capture());
        AnalysisTask failedTask = captor.getValue();
        assertThat(failedTask.getStatus()).isEqualTo(3);
        assertThat(failedTask.getErrorMsg()).isEqualTo("Parse error");
    }

    @Test
    void shouldNotExecuteNonPendingTask() {
        AnalysisTask runningTask = new AnalysisTask();
        runningTask.setId(1L);
        runningTask.setStatus(1);
        when(analysisTaskMapper.findById(1L)).thenReturn(runningTask);

        analysisTaskService.executeTask(1L);

        verify(analysisTaskMapper, never()).updateStatusAndStartedAt(anyLong(), anyInt());
        verify(analysisService, never()).analyzeSourceFiles(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void shouldMarkFailedWhenApplicationHasNoGitUrl() {
        AnalysisTask pendingTask = new AnalysisTask();
        pendingTask.setId(1L);
        pendingTask.setStatus(0);
        pendingTask.setTenantId("dept_01");
        pendingTask.setAppId("app1");
        pendingTask.setBranch("release_sit");
        pendingTask.setProjectId(1L);
        when(analysisTaskMapper.findById(1L)).thenReturn(pendingTask);
        when(lockService.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);

        Application app = new Application();
        app.setAppId("app1");
        when(applicationMapper.findByAppId("dept_01", "app1")).thenReturn(app);

        analysisTaskService.executeTask(1L);

        ArgumentCaptor<AnalysisTask> captor = ArgumentCaptor.forClass(AnalysisTask.class);
        verify(analysisTaskMapper).updateStatus(captor.capture());
        AnalysisTask failedTask = captor.getValue();
        assertThat(failedTask.getStatus()).isEqualTo(3);
        assertThat(failedTask.getErrorMsg()).isEqualTo("Application has no git_url configured");
    }

    @Test
    void shouldReturnTaskStatus() {
        AnalysisTask task = new AnalysisTask();
        task.setId(1L);
        task.setStatus(2);
        when(analysisTaskMapper.findById(1L)).thenReturn(task);

        AnalysisTask result = analysisTaskService.getTaskStatus(1L);

        assertThat(result.getStatus()).isEqualTo(2);
    }
}
