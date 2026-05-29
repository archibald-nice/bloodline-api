package com.bloodline.domain.entity;

import java.time.LocalDateTime;

public class LineageEdgeHistory {
    private Long id;
    private Long edgeId;
    private Long tenantId;
    private String sourceId;
    private String sourceType;
    private String targetId;
    private String targetType;
    private String relationType;
    private String properties;
    private Integer version;
    private LocalDateTime snapshotAt;

    // getters and setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEdgeId() { return edgeId; }
    public void setEdgeId(Long edgeId) { this.edgeId = edgeId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public String getProperties() { return properties; }
    public void setProperties(String properties) { this.properties = properties; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getSnapshotAt() { return snapshotAt; }
    public void setSnapshotAt(LocalDateTime snapshotAt) { this.snapshotAt = snapshotAt; }
}
