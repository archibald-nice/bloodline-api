package com.bloodline.service.service;

import com.bloodline.domain.entity.Datasource;
import com.bloodline.domain.entity.DatasourceSchema;
import com.bloodline.domain.mapper.DatasourceMapper;
import com.bloodline.domain.mapper.DatasourceSchemaMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasourceService {

    private final DatasourceMapper datasourceMapper;
    private final DatasourceSchemaMapper schemaMapper;

    public DatasourceService(DatasourceMapper datasourceMapper, DatasourceSchemaMapper schemaMapper) {
        this.datasourceMapper = datasourceMapper;
        this.schemaMapper = schemaMapper;
    }

    public List<Datasource> listByApp(String tenantId, String appId) {
        return datasourceMapper.findByTenantAndApp(tenantId, appId);
    }

    public Datasource create(String tenantId, Datasource ds) {
        ds.setTenantId(tenantId);
        datasourceMapper.insert(ds);
        return ds;
    }

    public List<DatasourceSchema> listSchemas(Long datasourceId) {
        return schemaMapper.findByDatasource(datasourceId);
    }

    public DatasourceSchema createSchema(String tenantId, DatasourceSchema schema) {
        schema.setTenantId(tenantId);
        schemaMapper.insert(schema);
        return schema;
    }
}
