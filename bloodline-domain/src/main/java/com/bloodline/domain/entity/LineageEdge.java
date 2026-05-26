package com.bloodline.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LineageEdge {
    private Long id;
    private String tenantId;
    private String appId;
    private String targetAppId;
    private String targetType;
    private String targetName;
    private String targetDetail;
    private String relationType;
    private String branch;
    private String projectId;
    private BigDecimal confidence;
    private String sourceType;
    private String commitSha;
    private LocalDateTime detectedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getTargetAppId() { return targetAppId; }
    public void setTargetAppId(String targetAppId) { this.targetAppId = targetAppId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public String getTargetDetail() { return targetDetail; }
    public void setTargetDetail(String targetDetail) { this.targetDetail = targetDetail; }

    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getCommitSha() { return commitSha; }
    public void setCommitSha(String commitSha) { this.commitSha = commitSha; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
}
