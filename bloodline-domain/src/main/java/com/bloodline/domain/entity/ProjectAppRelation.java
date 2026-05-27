package com.bloodline.domain.entity;

import java.time.LocalDateTime;

public class ProjectAppRelation {
    private Long id;
    private String tenantId;
    private Long projectId;
    private String appId;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
