package com.bloodline.domain.entity;

import java.time.LocalDateTime;

public class LineageSnapshot {
    private Long id;
    private Long tenantId;
    private String snapshotName;
    private String snapshotType;
    private String refId;
    private Integer edgeCount;
    private Integer nodeCount;
    private String edgesData;
    private LocalDateTime createdAt;
    private String createdBy;

    // getters and setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getSnapshotName() { return snapshotName; }
    public void setSnapshotName(String snapshotName) { this.snapshotName = snapshotName; }
    public String getSnapshotType() { return snapshotType; }
    public void setSnapshotType(String snapshotType) { this.snapshotType = snapshotType; }
    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }
    public Integer getEdgeCount() { return edgeCount; }
    public void setEdgeCount(Integer edgeCount) { this.edgeCount = edgeCount; }
    public Integer getNodeCount() { return nodeCount; }
    public void setNodeCount(Integer nodeCount) { this.nodeCount = nodeCount; }
    public String getEdgesData() { return edgesData; }
    public void setEdgesData(String edgesData) { this.edgesData = edgesData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
