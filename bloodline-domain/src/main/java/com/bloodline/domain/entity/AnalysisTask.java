package com.bloodline.domain.entity;

import java.time.LocalDateTime;

public class AnalysisTask {
    private Long id;
    private String tenantId;
    private Long projectId;
    private String appId;
    private String branch;
    private String commitSha;
    private Integer triggerType;
    private Integer status;
    private String schedulerJobId;
    private String resultSummary;
    private String errorMsg;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String lineageType;
    private Long snapshotId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getCommitSha() { return commitSha; }
    public void setCommitSha(String commitSha) { this.commitSha = commitSha; }

    public Integer getTriggerType() { return triggerType; }
    public void setTriggerType(Integer triggerType) { this.triggerType = triggerType; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getSchedulerJobId() { return schedulerJobId; }
    public void setSchedulerJobId(String schedulerJobId) { this.schedulerJobId = schedulerJobId; }

    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getLineageType() { return lineageType; }
    public void setLineageType(String lineageType) { this.lineageType = lineageType; }
    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
}
