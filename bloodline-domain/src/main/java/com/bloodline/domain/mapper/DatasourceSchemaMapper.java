package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.DatasourceSchema;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DatasourceSchemaMapper {

    @Insert("INSERT INTO datasource_schema(tenant_id, datasource_id, schema_name, schema_alias, description) " +
            "VALUES(#{tenantId}, #{datasourceId}, #{schemaName}, #{schemaAlias}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DatasourceSchema schema);

    @Select("SELECT * FROM datasource_schema WHERE datasource_id = #{datasourceId}")
    List<DatasourceSchema> findByDatasource(@Param("datasourceId") Long datasourceId);

    @Select("SELECT * FROM datasource_schema WHERE id = #{id}")
    DatasourceSchema findById(@Param("id") Long id);

    @Delete("DELETE FROM datasource_schema WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
