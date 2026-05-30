package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.LineageEdgeV2;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineageEdgeV2Mapper {
    LineageEdgeV2 findById(@Param("id") Long id);
    List<LineageEdgeV2> findBySource(@Param("tenantId") Long tenantId, @Param("sourceId") String sourceId);
    List<LineageEdgeV2> findByTarget(@Param("tenantId") Long tenantId, @Param("targetId") String targetId);
    List<LineageEdgeV2> findUpstreamRecursive(@Param("tenantId") Long tenantId, @Param("targetId") String targetId, @Param("maxDepth") int maxDepth);
    List<LineageEdgeV2> findDownstreamRecursive(@Param("tenantId") Long tenantId, @Param("sourceId") String sourceId, @Param("maxDepth") int maxDepth);
    int insert(LineageEdgeV2 edge);
    int insertBatch(@Param("edges") List<LineageEdgeV2> edges);
    int softDeleteById(@Param("id") Long id);
    int softDeleteBySourceTarget(@Param("tenantId") Long tenantId, @Param("sourceId") String sourceId, @Param("targetId") String targetId);
    int updateVersion(@Param("id") Long id, @Param("version") int version);
    int countByTenant(@Param("tenantId") Long tenantId);
}
