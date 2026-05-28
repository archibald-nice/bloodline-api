package com.bloodline.analyzer.model;

public class ParsedRelation {
    private String relationType;
    private String targetType;
    private String targetName;
    private String targetDetail;
    private String targetAppId;
    private double confidence = 1.0;
    private String sqlSignature;
    private String sqlPreview;
    private String sourceLocation;

    public ParsedRelation() {}

    public ParsedRelation(String relationType, String targetType, String targetName) {
        this.relationType = relationType;
        this.targetType = targetType;
        this.targetName = targetName;
    }

    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public String getTargetDetail() { return targetDetail; }
    public void setTargetDetail(String targetDetail) { this.targetDetail = targetDetail; }

    public String getTargetAppId() { return targetAppId; }
    public void setTargetAppId(String targetAppId) { this.targetAppId = targetAppId; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getSqlSignature() { return sqlSignature; }
    public void setSqlSignature(String sqlSignature) { this.sqlSignature = sqlSignature; }

    public String getSqlPreview() { return sqlPreview; }
    public void setSqlPreview(String sqlPreview) { this.sqlPreview = sqlPreview; }

    public String getSourceLocation() { return sourceLocation; }
    public void setSourceLocation(String sourceLocation) { this.sourceLocation = sourceLocation; }
}
