package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMapper {

    @Insert("INSERT INTO project(tenant_id, project_code, project_name, baseline_branch, dev_branch, status, created_by) " +
            "VALUES(#{tenantId}, #{projectCode}, #{projectName}, #{baselineBranch}, #{devBranch}, #{status}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Project project);

    @Select("SELECT * FROM project WHERE tenant_id = #{tenantId} AND project_code = #{projectCode}")
    Project findByCode(@Param("tenantId") String tenantId, @Param("projectCode") String projectCode);

    @Select("SELECT * FROM project WHERE tenant_id = #{tenantId}")
    List<Project> findByTenant(@Param("tenantId") String tenantId);

    @Update("UPDATE project SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
