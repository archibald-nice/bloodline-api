package com.bloodline.service.service;

import com.bloodline.common.context.TenantContext;
import com.bloodline.domain.entity.LineageSnapshot;
import com.bloodline.domain.mapper.LineageSnapshotMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SnapshotService {

    private final LineageSnapshotMapper snapshotMapper;

    public SnapshotService(LineageSnapshotMapper snapshotMapper) {
        this.snapshotMapper = snapshotMapper;
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
        // TODO: implement actual count query when edge table is ready
        return 0;
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
