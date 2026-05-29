package com.bloodline.etl.service;

import com.bloodline.common.context.TenantContext;
import com.bloodline.domain.entity.LineageEdgeV2;
import com.bloodline.domain.entity.LineageNode;
import com.bloodline.domain.mapper.LineageEdgeV2Mapper;
import com.bloodline.domain.mapper.LineageNodeMapper;
import com.bloodline.etl.model.LineageEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LineageIngestionService {

    private final LineageEdgeV2Mapper edgeMapper;
    private final LineageNodeMapper nodeMapper;

    public LineageIngestionService(LineageEdgeV2Mapper edgeMapper, LineageNodeMapper nodeMapper) {
        this.edgeMapper = edgeMapper;
        this.nodeMapper = nodeMapper;
    }

    @Transactional
    public void ingest(LineageEvent event) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set. Cannot ingest lineage event.");
        }

        String jobNodeId = event.getJobNamespace() + ":" + event.getJobName();
        ensureNode(tenantId, jobNodeId, "JOB", event.getJobName(), event.getJobNamespace());

        for (LineageEvent.DatasetInput input : event.getInputs()) {
            String tableNodeId = input.getNamespace() + ":" + input.getName();
            ensureNode(tenantId, tableNodeId, "TABLE", input.getName(), input.getNamespace());
            insertEdge(tenantId, tableNodeId, "TABLE", jobNodeId, "JOB", "QUERIES");
        }

        for (LineageEvent.DatasetOutput output : event.getOutputs()) {
            String tableNodeId = output.getNamespace() + ":" + output.getName();
            ensureNode(tenantId, tableNodeId, "TABLE", output.getName(), output.getNamespace());
            insertEdge(tenantId, jobNodeId, "JOB", tableNodeId, "TABLE", "POPULATES");
        }
    }

    private void ensureNode(Long tenantId, String nodeId, String nodeType, String nodeName, String domain) {
        LineageNode existing = nodeMapper.findByNodeId(tenantId, nodeId);
        if (existing == null) {
            LineageNode node = new LineageNode();
            node.setTenantId(tenantId);
            node.setNodeId(nodeId);
            node.setNodeType(nodeType);
            node.setNodeName(nodeName);
            node.setDomain(domain);
            nodeMapper.insert(node);
        }
    }

    private void insertEdge(Long tenantId, String sourceId, String sourceType, String targetId, String targetType, String relationType) {
        if (edgeExists(tenantId, sourceId, targetId, relationType)) {
            return;
        }
        LineageEdgeV2 edge = new LineageEdgeV2();
        edge.setTenantId(tenantId);
        edge.setSourceId(sourceId);
        edge.setSourceType(sourceType);
        edge.setTargetId(targetId);
        edge.setTargetType(targetType);
        edge.setRelationType(relationType);
        edge.setVersion(1);
        edgeMapper.insert(edge);
    }

    private boolean edgeExists(Long tenantId, String sourceId, String targetId, String relationType) {
        List<LineageEdgeV2> existing = edgeMapper.findBySource(tenantId, sourceId);
        if (existing != null) {
            for (LineageEdgeV2 edge : existing) {
                if (targetId.equals(edge.getTargetId()) && relationType.equals(edge.getRelationType())
                    && Integer.valueOf(0).equals(edge.getIsDeleted())) {
                    return true;
                }
            }
        }
        return false;
    }
}
