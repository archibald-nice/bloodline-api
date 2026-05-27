package com.bloodline.service.service;

import com.bloodline.domain.entity.LineageEdge;
import com.bloodline.domain.mapper.LineageEdgeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LineageQueryService {
    private static final int DEFAULT_RECURSION_DEPTH = 5;

    private final LineageEdgeMapper lineageEdgeMapper;

    public LineageQueryService(LineageEdgeMapper lineageEdgeMapper) {
        this.lineageEdgeMapper = lineageEdgeMapper;
    }

    public List<LineageEdge> getUpstream(String tenantId, String appId) {
        return lineageEdgeMapper.findByApp(tenantId, appId);
    }

    public List<String> getDownstreamAppIds(String tenantId, String appId) {
        return lineageEdgeMapper.findUpstreamApps(tenantId, appId);
    }

    public List<String> getAppsUsingTable(String tenantId, String tableName) {
        return lineageEdgeMapper.findAppsUsingTarget(tenantId, "TABLE", tableName);
    }

    public LineageGraph getLineageGraph(String tenantId, String appId) {
        LineageGraph graph = new LineageGraph();
        graph.setAppId(appId);
        graph.setUpstream(getUpstream(tenantId, appId));
        graph.setDownstreamAppIds(getDownstreamAppIds(tenantId, appId));
        return graph;
    }

    public List<LineageEdge> getUpstreamRecursive(String tenantId, String appId, int maxDepth) {
        return lineageEdgeMapper.findUpstreamRecursive(tenantId, appId, maxDepth);
    }

    public List<String> getDownstreamRecursive(String tenantId, String appId, int maxDepth) {
        return lineageEdgeMapper.findDownstreamRecursive(tenantId, appId, maxDepth);
    }

    public List<LineageEdge> getUpstreamWithOverlay(String tenantId, String appId,
                                                     String baselineBranch, String projectBranch,
                                                     String projectId) {
        return lineageEdgeMapper.findByAppWithBranchOverlay(
                tenantId, appId, baselineBranch, projectBranch, projectId);
    }

    public LineageGraph getLineageGraphWithOverlay(String tenantId, String appId,
                                                    String baselineBranch, String projectBranch,
                                                    String projectId) {
        LineageGraph graph = new LineageGraph();
        graph.setAppId(appId);
        graph.setUpstream(getUpstreamWithOverlay(tenantId, appId, baselineBranch, projectBranch, projectId));
        graph.setDownstreamAppIds(getDownstreamAppIds(tenantId, appId));
        return graph;
    }

    public static class LineageGraph {
        private String appId;
        private List<LineageEdge> upstream;
        private List<String> downstreamAppIds;

        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }

        public List<LineageEdge> getUpstream() { return upstream; }
        public void setUpstream(List<LineageEdge> upstream) { this.upstream = upstream; }

        public List<String> getDownstreamAppIds() { return downstreamAppIds; }
        public void setDownstreamAppIds(List<String> downstreamAppIds) { this.downstreamAppIds = downstreamAppIds; }
    }
}
