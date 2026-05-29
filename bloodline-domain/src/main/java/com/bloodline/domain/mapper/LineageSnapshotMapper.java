package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.LineageSnapshot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineageSnapshotMapper {
    LineageSnapshot findById(@Param("id") Long id);
    List<LineageSnapshot> findByTenant(@Param("tenantId") Long tenantId);
    List<LineageSnapshot> findByType(@Param("tenantId") Long tenantId, @Param("snapshotType") String snapshotType);
    int insert(LineageSnapshot snapshot);
    int updateCounts(@Param("id") Long id, @Param("edgeCount") int edgeCount, @Param("nodeCount") int nodeCount);
    int deleteById(@Param("id") Long id);
}
