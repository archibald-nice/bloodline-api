package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.LineageNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineageNodeMapper {
    LineageNode findByNodeId(@Param("tenantId") Long tenantId, @Param("nodeId") String nodeId);
    List<LineageNode> findByType(@Param("tenantId") Long tenantId, @Param("nodeType") String nodeType);
    List<LineageNode> findByIds(@Param("tenantId") Long tenantId, @Param("nodeIds") List<String> nodeIds);
    int insert(LineageNode node);
    int insertBatch(@Param("nodes") List<LineageNode> nodes);
    int update(LineageNode node);
    int softDelete(@Param("tenantId") Long tenantId, @Param("nodeId") String nodeId);
}
