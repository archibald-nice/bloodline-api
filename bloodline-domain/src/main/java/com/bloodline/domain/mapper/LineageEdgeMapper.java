package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.LineageEdge;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LineageEdgeMapper {

    @Insert("INSERT INTO lineage_edge(tenant_id, app_id, target_app_id, target_type, target_name, " +
            "target_detail, relation_type, branch, project_id, confidence, source_type, commit_sha) " +
            "VALUES(#{tenantId}, #{appId}, #{targetAppId}, #{targetType}, #{targetName}, " +
            "#{targetDetail}, #{relationType}, #{branch}, #{projectId}, #{confidence}, #{sourceType}, #{commitSha})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LineageEdge edge);

    @Insert("<script>" +
            "INSERT INTO lineage_edge(tenant_id, app_id, target_app_id, target_type, target_name, " +
            "target_detail, relation_type, branch, project_id, confidence, source_type, commit_sha) VALUES " +
            "<foreach collection='edges' item='e' separator=','>" +
            "(#{e.tenantId}, #{e.appId}, #{e.targetAppId}, #{e.targetType}, #{e.targetName}, " +
            "#{e.targetDetail}, #{e.relationType}, #{e.branch}, #{e.projectId}, #{e.confidence}, #{e.sourceType}, #{e.commitSha})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("edges") List<LineageEdge> edges);

    @Select("SELECT * FROM lineage_edge WHERE tenant_id = #{tenantId} AND app_id = #{appId}")
    List<LineageEdge> findByApp(@Param("tenantId") String tenantId, @Param("appId") String appId);

    @Select("SELECT DISTINCT app_id FROM lineage_edge WHERE tenant_id = #{tenantId} AND target_app_id = #{appId}")
    List<String> findUpstreamApps(@Param("tenantId") String tenantId, @Param("appId") String appId);

    @Select("SELECT DISTINCT app_id FROM lineage_edge WHERE tenant_id = #{tenantId} AND target_type = #{targetType} AND target_name = #{targetName}")
    List<String> findAppsUsingTarget(@Param("tenantId") String tenantId, @Param("targetType") String targetType, @Param("targetName") String targetName);

    @Delete("DELETE FROM lineage_edge WHERE tenant_id = #{tenantId} AND app_id = #{appId} AND branch = #{branch} AND (project_id = #{projectId} OR (project_id IS NULL AND #{projectId} IS NULL))")
    int deleteByAppBranch(@Param("tenantId") String tenantId, @Param("appId") String appId,
                          @Param("branch") String branch, @Param("projectId") String projectId);
}
