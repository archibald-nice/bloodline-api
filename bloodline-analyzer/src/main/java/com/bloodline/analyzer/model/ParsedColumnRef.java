package com.bloodline.analyzer.model;

import java.util.Objects;

public class ParsedColumnRef {
    private String appId;
    private String tableName;
    private String columnName;
    private String sqlSignature;
    private String sqlPreview;
    private String operationType;
    private String operationDetail;
    private String sourceLocation;

    public ParsedColumnRef() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedColumnRef that = (ParsedColumnRef) o;
        return Objects.equals(appId, that.appId) &&
                Objects.equals(tableName, that.tableName) &&
                Objects.equals(columnName, that.columnName) &&
                Objects.equals(sqlSignature, that.sqlSignature) &&
                Objects.equals(operationDetail, that.operationDetail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, tableName, columnName, sqlSignature, operationDetail);
    }
}
