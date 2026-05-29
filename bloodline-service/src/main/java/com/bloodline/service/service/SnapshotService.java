package com.bloodline.service.service;

import com.bloodline.common.context.TenantContext;
import com.bloodline.domain.entity.LineageSnapshot;
import com.bloodline.domain.mapper.LineageEdgeV2Mapper;
import com.bloodline.domain.mapper.LineageSnapshotMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SnapshotService {

    private final LineageEdgeV2Mapper edgeMapper;
    private final LineageSnapshotMapper snapshotMapper;

    public SnapshotService(LineageEdgeV2Mapper edgeMapper, LineageSnapshotMapper snapshotMapper) {
        this.edgeMapper = edgeMapper;
        this.snapshotMapper = snapshotMapper;
    }

    @Transactional
    public LineageSnapshot createSnapshot(String snapshotName, String snapshotType, String refId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set.");
        }

        int edgeCount = countActiveEdges(tenantId);

        LineageSnapshot snapshot = new LineageSnapshot();
        snapshot.setTenantId(tenantId);
        snapshot.setSnapshotName(snapshotName);
        snapshot.setSnapshotType(snapshotType);
        snapshot.setRefId(refId);
        snapshot.setEdgeCount(edgeCount);
        snapshot.setNodeCount(0);
        snapshotMapper.insert(snapshot);

        return snapshot;
    }

    private int countActiveEdges(Long tenantId) {
        // MVP simplified: no dedicated COUNT query yet
        return 0;
    }

    public LineageSnapshot getSnapshot(Long snapshotId) {
        return snapshotMapper.findById(snapshotId);
    }

    public List<LineageSnapshot> listSnapshots() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set.");
        }
        return snapshotMapper.findByTenant(tenantId);
    }
}
