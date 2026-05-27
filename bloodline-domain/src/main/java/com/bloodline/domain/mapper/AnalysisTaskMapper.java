package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.AnalysisTask;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnalysisTaskMapper {

    @Insert("INSERT INTO analysis_task(tenant_id, project_id, app_id, branch, commit_sha, trigger_type, status, scheduler_job_id) " +
            "VALUES(#{tenantId}, #{projectId}, #{appId}, #{branch}, #{commitSha}, #{triggerType}, #{status}, #{schedulerJobId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AnalysisTask task);

    @Select("SELECT * FROM analysis_task WHERE tenant_id = #{tenantId} AND app_id = #{appId} ORDER BY created_at DESC LIMIT 1")
    AnalysisTask findLatestByApp(@Param("tenantId") String tenantId, @Param("appId") String appId);

    @Update("UPDATE analysis_task SET status = #{status}, completed_at = NOW(), result_summary = #{resultSummary}, error_msg = #{errorMsg} WHERE id = #{id}")
    int updateStatus(AnalysisTask task);

    @Select("SELECT * FROM analysis_task WHERE id = #{id}")
    AnalysisTask findById(@Param("id") Long id);

    @Select("SELECT * FROM analysis_task WHERE tenant_id = #{tenantId} ORDER BY created_at DESC LIMIT #{limit}")
    List<AnalysisTask> findByTenant(@Param("tenantId") String tenantId, @Param("limit") Integer limit);

    @Select("SELECT * FROM analysis_task WHERE tenant_id = #{tenantId} AND status = 0 ORDER BY created_at ASC LIMIT #{limit}")
    List<AnalysisTask> findPendingTasks(@Param("tenantId") String tenantId, @Param("limit") Integer limit);

    @Update("UPDATE analysis_task SET status = #{status}, started_at = NOW() WHERE id = #{id}")
    int updateStatusAndStartedAt(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT * FROM analysis_task WHERE status = 1 AND started_at < DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE)")
    List<AnalysisTask> findRunningTasksOlderThan(@Param("minutes") int minutes);

    @Select("SELECT * FROM analysis_task WHERE tenant_id = #{tenantId} AND app_id = #{appId} AND branch = #{branch} AND status = 5 ORDER BY created_at DESC LIMIT 1")
    AnalysisTask findLatestStaleTask(@Param("tenantId") String tenantId, @Param("appId") String appId, @Param("branch") String branch);
}
