package com.bloodline.service.service;

import com.bloodline.common.context.TenantContext;
import com.bloodline.domain.entity.LineageEdgeV2;
import com.bloodline.domain.entity.LineageNode;
import com.bloodline.domain.mapper.LineageEdgeV2Mapper;
import com.bloodline.domain.mapper.LineageNodeMapper;
import com.bloodline.etl.exporter.OpenLineageExporter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenLineageExportService {

    private final LineageEdgeV2Mapper edgeMapper;
    private final LineageNodeMapper nodeMapper;
    private final OpenLineageExporter exporter;

    public OpenLineageExportService(LineageEdgeV2Mapper edgeMapper,
                                     LineageNodeMapper nodeMapper) {
        this.edgeMapper = edgeMapper;
        this.nodeMapper = nodeMapper;
        this.exporter = new OpenLineageExporter();
    }

    /**
     * Export all active lineage edges for the current tenant as an OpenLineage RunEvent.
     */
    public String exportCurrentTenant(String jobName, String jobNamespace) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        List<LineageEdgeV2> edges = edgeMapper.findBySource(tenantId, "");
        // Also get edges from other sources — collect all unique edges
        // For simplicity, we export a representative set per source node
        // A full export would iterate all nodes
        return exportByTenant(tenantId, jobName, jobNamespace);
    }

    /**
     * Export lineage edges for a specific tenant.
     */
    public String exportByTenant(Long tenantId, String jobName, String jobNamespace) {
        // Fetch all active edges for tenant
        // Since findBySource requires a sourceId, we need a different approach
        // For MVP: export edges from known seed nodes or use a limited set
        List<LineageNode> nodes = nodeMapper.findByType(tenantId, "TABLE");
        Map<Long, LineageEdgeV2> uniqueEdges = new HashMap<>();

        for (LineageNode node : nodes) {
            List<LineageEdgeV2> outgoing = edgeMapper.findBySource(tenantId, node.getNodeId());
            if (outgoing != null) {
                for (LineageEdgeV2 edge : outgoing) {
                    uniqueEdges.put(edge.getId(), edge);
                }
            }
            List<LineageEdgeV2> incoming = edgeMapper.findByTarget(tenantId, node.getNodeId());
            if (incoming != null) {
                for (LineageEdgeV2 edge : incoming) {
                    uniqueEdges.put(edge.getId(), edge);
                }
            }
        }

        List<LineageEdgeV2> allEdges = new java.util.ArrayList<>(uniqueEdges.values());

        Map<String, LineageNode> nodeCache = new HashMap<>();
        java.util.function.Function<String, LineageNode> nodeLookup = nodeId -> {
            return nodeCache.computeIfAbsent(nodeId, id -> nodeMapper.findByNodeId(tenantId, id));
        };

        return exporter.exportRunEvent(jobName, jobNamespace, allEdges, nodeLookup);
    }

    /**
     * Export lineage starting from specific node IDs.
     */
    public String exportByNodes(Long tenantId, List<String> nodeIds, String jobName, String jobNamespace) {
        Map<Long, LineageEdgeV2> uniqueEdges = new HashMap<>();

        for (String nodeId : nodeIds) {
            List<LineageEdgeV2> outgoing = edgeMapper.findBySource(tenantId, nodeId);
            if (outgoing != null) {
                for (LineageEdgeV2 edge : outgoing) {
                    uniqueEdges.put(edge.getId(), edge);
                }
            }
            List<LineageEdgeV2> incoming = edgeMapper.findByTarget(tenantId, nodeId);
            if (incoming != null) {
                for (LineageEdgeV2 edge : incoming) {
                    uniqueEdges.put(edge.getId(), edge);
                }
            }
        }

        List<LineageEdgeV2> allEdges = new java.util.ArrayList<>(uniqueEdges.values());

        Map<String, LineageNode> nodeCache = new HashMap<>();
        java.util.function.Function<String, LineageNode> nodeLookup = nodeId -> {
            return nodeCache.computeIfAbsent(nodeId, id -> nodeMapper.findByNodeId(tenantId, id));
        };

        return exporter.exportRunEvent(jobName, jobNamespace, allEdges, nodeLookup);
    }
}
