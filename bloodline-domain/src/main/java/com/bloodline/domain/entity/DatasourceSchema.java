package com.bloodline.domain.entity;

import java.time.LocalDateTime;

public class DatasourceSchema {
    private Long id;
    private String tenantId;
    private Long datasourceId;
    private String schemaName;
    private String schemaAlias;
    private String description;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getDatasourceId() { return datasourceId; }
    public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }
    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }
    public String getSchemaAlias() { return schemaAlias; }
    public void setSchemaAlias(String schemaAlias) { this.schemaAlias = schemaAlias; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
