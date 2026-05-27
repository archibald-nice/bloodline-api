package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.ProjectAppRelation;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ProjectAppRelationMapper {
    @Insert("INSERT INTO project_app(tenant_id, project_id, app_id) VALUES(#{tenantId}, #{projectId}, #{appId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProjectAppRelation relation);

    @Delete("DELETE FROM project_app WHERE tenant_id = #{tenantId} AND project_id = #{projectId} AND app_id = #{appId}")
    int delete(@Param("tenantId") String tenantId, @Param("projectId") Long projectId, @Param("appId") String appId);

    @Select("SELECT app_id FROM project_app WHERE tenant_id = #{tenantId} AND project_id = #{projectId}")
    List<String> findAppIdsByProject(@Param("tenantId") String tenantId, @Param("projectId") Long projectId);

    @Select("SELECT * FROM project_app WHERE tenant_id = #{tenantId} AND app_id = #{appId}")
    List<ProjectAppRelation> findByApp(@Param("tenantId") String tenantId, @Param("appId") String appId);
}
