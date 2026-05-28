package com.bloodline.domain.entity;

import java.time.LocalDateTime;

public class LineageColumnRef {
    private Long id;
    private String appId;
    private String tableName;
    private String columnName;
    private String sqlSignature;
    private String sqlPreview;
    private String operationType;
    private String operationDetail;
    private String sourceLocation;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public String getSqlSignature() { return sqlSignature; }
    public void setSqlSignature(String sqlSignature) { this.sqlSignature = sqlSignature; }

    public String getSqlPreview() { return sqlPreview; }
    public void setSqlPreview(String sqlPreview) { this.sqlPreview = sqlPreview; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getOperationDetail() { return operationDetail; }
    public void setOperationDetail(String operationDetail) { this.operationDetail = operationDetail; }

    public String getSourceLocation() { return sourceLocation; }
    public void setSourceLocation(String sourceLocation) { this.sourceLocation = sourceLocation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
