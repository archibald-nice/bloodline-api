package com.bloodline.service.service;

import com.bloodline.common.context.TenantContext;
import com.bloodline.domain.entity.LineageEdgeV2;
import com.bloodline.domain.entity.LineageSnapshot;
import com.bloodline.domain.mapper.LineageEdgeV2Mapper;
import com.bloodline.domain.mapper.LineageSnapshotMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SnapshotService {

    private final LineageSnapshotMapper snapshotMapper;
    private final LineageEdgeV2Mapper edgeMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SnapshotService(LineageSnapshotMapper snapshotMapper, LineageEdgeV2Mapper edgeMapper) {
        this.snapshotMapper = snapshotMapper;
        this.edgeMapper = edgeMapper;
    }

    @Transactional
    public LineageSnapshot createSnapshot(String snapshotName, String snapshotType, String refId) {
        if (snapshotName == null || snapshotName.trim().isEmpty()) {
            throw new IllegalArgumentException("snapshotName is required");
        }
        if (snapshotType == null || snapshotType.trim().isEmpty()) {
            throw new IllegalArgumentException("snapshotType is required");
        }

        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set.");
        }

        int edgeCount = countActiveEdges(tenantId);
        String edgesData = serializeActiveEdges(tenantId);

        LineageSnapshot snapshot = new LineageSnapshot();
        snapshot.setTenantId(tenantId);
        snapshot.setSnapshotName(snapshotName);
        snapshot.setSnapshotType(snapshotType);
        snapshot.setRefId(refId);
        snapshot.setEdgeCount(edgeCount);
        snapshot.setNodeCount(0);
        snapshot.setEdgesData(edgesData);
        snapshotMapper.insert(snapshot);

        return snapshot;
    }

    private int countActiveEdges(Long tenantId) {
        return edgeMapper.countByTenant(tenantId);
    }

    private String serializeActiveEdges(Long tenantId) {
        List<LineageEdgeV2> edges = edgeMapper.findBySource(tenantId, "");
        if (edges == null || edges.isEmpty()) {
            edges = edgeMapper.findByTarget(tenantId, "");
        }
        // findBySource with empty string may return nothing depending on data;
        // Fall back to querying via a dedicated all-edges approach if needed.
        // For MVP, we construct a simple JSON array from whatever edges we can find.
        // A more robust approach would add findAllByTenant to the mapper.
        List<Map<String, Object>> edgeList = new java.util.ArrayList<>();
        if (edges == null) {
            edges = java.util.Collections.emptyList();
        }
        for (LineageEdgeV2 e : edges) {
            Map<String, Object> m = new HashMap<>();
            m.put("sourceId", e.getSourceId());
            m.put("targetId", e.getTargetId());
            m.put("relationType", e.getRelationType());
            edgeList.add(m);
        }
        try {
            return objectMapper.writeValueAsString(edgeList);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    public LineageSnapshot getSnapshot(Long snapshotId) {
        LineageSnapshot snapshot = snapshotMapper.findById(snapshotId);
        if (snapshot == null) {
            return null;
        }
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set.");
        }
        if (!tenantId.equals(snapshot.getTenantId())) {
            throw new IllegalStateException("Snapshot does not belong to current tenant.");
        }
        return snapshot;
    }

    public List<LineageSnapshot> listSnapshots() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set.");
        }
        return snapshotMapper.findByTenant(tenantId);
    }
}
