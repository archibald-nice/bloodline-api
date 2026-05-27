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

    @Select("<script>" +
            "WITH RECURSIVE upstream AS ( " +
            "  SELECT * FROM lineage_edge WHERE tenant_id = #{tenantId} AND app_id = #{appId} " +
            "  UNION ALL " +
            "  SELECT e.* FROM lineage_edge e " +
            "  INNER JOIN upstream u ON e.app_id = u.target_app_id " +
            "  WHERE e.tenant_id = #{tenantId} " +
            ") " +
            "SELECT * FROM upstream LIMIT #{limit}" +
            "</script>")
    List<LineageEdge> findUpstreamRecursive(@Param("tenantId") String tenantId,
                                            @Param("appId") String appId,
                                            @Param("limit") int limit);

    @Select("<script>" +
            "WITH RECURSIVE downstream AS ( " +
            "  SELECT app_id, target_app_id, relation_type, target_type, target_name FROM lineage_edge " +
            "  WHERE tenant_id = #{tenantId} AND target_app_id = #{appId} " +
            "  UNION ALL " +
            "  SELECT e.app_id, e.target_app_id, e.relation_type, e.target_type, e.target_name " +
            "  FROM lineage_edge e " +
            "  INNER JOIN downstream d ON e.target_app_id = d.app_id " +
            "  WHERE e.tenant_id = #{tenantId} " +
            ") " +
            "SELECT DISTINCT app_id FROM downstream LIMIT #{limit}" +
            "</script>")
    List<String> findDownstreamRecursive(@Param("tenantId") String tenantId,
                                         @Param("appId") String appId,
                                         @Param("limit") int limit);

    @Select("<script>" +
            "SELECT e.* FROM lineage_edge e " +
            "WHERE e.tenant_id = #{tenantId} AND e.app_id = #{appId} " +
            "AND ( " +
            "  (e.branch = #{baselineBranch} AND e.project_id IS NULL) " +
            "  OR " +
            "  (e.branch = #{projectBranch} AND e.project_id = #{projectId}) " +
            ") " +
            "ORDER BY e.project_id IS NULL" +
            "</script>")
    List<LineageEdge> findByAppWithBranchOverlay(@Param("tenantId") String tenantId,
                                                  @Param("appId") String appId,
                                                  @Param("baselineBranch") String baselineBranch,
                                                  @Param("projectBranch") String projectBranch,
                                                  @Param("projectId") String projectId);
}
