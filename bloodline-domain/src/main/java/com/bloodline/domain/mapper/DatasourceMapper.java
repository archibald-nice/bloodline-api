package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.Datasource;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DatasourceMapper {

    @Insert("INSERT INTO datasource(tenant_id, app_id, datasource_code, datasource_name, db_type, db_version, jdbc_url, catalog) " +
            "VALUES(#{tenantId}, #{appId}, #{datasourceCode}, #{datasourceName}, #{dbType}, #{dbVersion}, #{jdbcUrl}, #{catalog})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Datasource ds);

    @Select("SELECT * FROM datasource WHERE tenant_id = #{tenantId} AND app_id = #{appId}")
    List<Datasource> findByTenantAndApp(@Param("tenantId") String tenantId, @Param("appId") String appId);

    @Select("SELECT * FROM datasource WHERE id = #{id}")
    Datasource findById(@Param("id") Long id);

    @Delete("DELETE FROM datasource WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
