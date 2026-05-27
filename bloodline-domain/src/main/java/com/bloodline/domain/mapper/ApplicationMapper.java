package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.Application;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApplicationMapper {

    @Insert("INSERT INTO application(tenant_id, app_id, app_name, git_url, default_branch, language) " +
            "VALUES(#{tenantId}, #{appId}, #{appName}, #{gitUrl}, #{defaultBranch}, #{language})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Application app);

    @Select("SELECT * FROM application WHERE tenant_id = #{tenantId} AND app_id = #{appId}")
    Application findByAppId(@Param("tenantId") String tenantId, @Param("appId") String appId);

    @Select("SELECT * FROM application WHERE tenant_id = #{tenantId} AND git_url = #{gitUrl}")
    Application findByGitUrl(@Param("tenantId") String tenantId, @Param("gitUrl") String gitUrl);

    @Select("SELECT * FROM application WHERE tenant_id = #{tenantId}")
    List<Application> findByTenant(@Param("tenantId") String tenantId);

    @Update("UPDATE application SET app_name = #{appName}, git_url = #{gitUrl}, default_branch = #{defaultBranch}, language = #{language} WHERE tenant_id = #{tenantId} AND app_id = #{appId}")
    int update(Application app);

    @Delete("DELETE FROM application WHERE tenant_id = #{tenantId} AND app_id = #{appId}")
    int deleteByAppId(@Param("tenantId") String tenantId, @Param("appId") String appId);
}
