package com.bloodline.service.service;

import com.bloodline.domain.entity.LineageEdgeV2;
import com.bloodline.domain.entity.LineageNode;
import com.bloodline.domain.mapper.LineageEdgeV2Mapper;
import com.bloodline.domain.mapper.LineageNodeMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TraceLineageCorrelator {

    private final LineageEdgeV2Mapper edgeMapper;
    private final LineageNodeMapper nodeMapper;

    public TraceLineageCorrelator(LineageEdgeV2Mapper edgeMapper, LineageNodeMapper nodeMapper) {
        this.edgeMapper = edgeMapper;
        this.nodeMapper = nodeMapper;
    }

    public List<CorrelationResult> correlate(Long tenantId, List<SkyWalkingTraceQueryService.TraceSpan> spans) {
        List<CorrelationResult> results = new ArrayList<>();
        List<LineageNode> allNodes = nodeMapper.findByType(tenantId, null);

        for (SkyWalkingTraceQueryService.TraceSpan span : spans) {
            MatchResult match = findBestMatch(span.getEndpoint(), allNodes);
            CorrelationResult result = new CorrelationResult();
            result.setTraceId(span.getTraceId());
            result.setEndpoint(span.getEndpoint());
            result.setDuration(span.getDuration());
            result.setError(span.isError());

            if (match != null) {
                result.setMatchedNodeId(match.node.getNodeId());
                result.setMatchedNodeType(match.node.getNodeType());
                result.setMatchType(match.type);
                result.setConfidence(match.confidence);
                result.setLineageMissing(false);

                // Check if this trace endpoint has lineage edges
                List<LineageEdgeV2> edges = edgeMapper.findBySource(tenantId, match.node.getNodeId());
                if (edges == null || edges.isEmpty()) {
                    edges = edgeMapper.findByTarget(tenantId, match.node.getNodeId());
                }
                result.setHasLineage(edges != null && !edges.isEmpty());
            } else {
                result.setLineageMissing(true);
                result.setHasLineage(false);
                result.setMatchType("NONE");
                result.setConfidence(0.0);
            }

            results.add(result);
        }

        return results;
    }

    private MatchResult findBestMatch(String endpoint, List<LineageNode> nodes) {
        if (endpoint == null || endpoint.isEmpty()) {
            return null;
        }
        String normalizedEndpoint = endpoint.toLowerCase();

        // 1. Exact match
        for (LineageNode node : nodes) {
            if (endpoint.equals(node.getNodeId()) || endpoint.equals(node.getNodeName())) {
                return new MatchResult(node, "EXACT", 1.0);
            }
        }

        // 2. Contains match (endpoint contains node name or vice versa)
        for (LineageNode node : nodes) {
            if (node.getNodeName() != null) {
                String normalizedName = node.getNodeName().toLowerCase();
                if (normalizedEndpoint.contains(normalizedName) || normalizedName.contains(normalizedEndpoint)) {
                    return new MatchResult(node, "DOMAIN", 0.8);
                }
            }
        }

        // 3. Fuzzy match (simple Levenshtein-like similarity)
        MatchResult best = null;
        double bestScore = 0.0;
        for (LineageNode node : nodes) {
            if (node.getNodeName() != null) {
                double score = similarity(endpoint, node.getNodeName());
                if (score > bestScore && score >= 0.6) {
                    bestScore = score;
                    best = new MatchResult(node, "FUZZY", score);
                }
            }
        }
        return best;
    }

    private double similarity(String a, String b) {
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        int distance = levenshtein(a.toLowerCase(), b.toLowerCase());
        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    private static class MatchResult {
        final LineageNode node;
        final String type;
        final double confidence;
        MatchResult(LineageNode node, String type, double confidence) {
            this.node = node;
            this.type = type;
            this.confidence = confidence;
        }
    }

    public static class CorrelationResult {
        private String traceId;
        private String endpoint;
        private int duration;
        private boolean error;
        private String matchedNodeId;
        private String matchedNodeType;
        private String matchType;
        private double confidence;
        private boolean lineageMissing;
        private boolean hasLineage;

        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        public boolean isError() { return error; }
        public void setError(boolean error) { this.error = error; }
        public String getMatchedNodeId() { return matchedNodeId; }
        public void setMatchedNodeId(String matchedNodeId) { this.matchedNodeId = matchedNodeId; }
        public String getMatchedNodeType() { return matchedNodeType; }
        public void setMatchedNodeType(String matchedNodeType) { this.matchedNodeType = matchedNodeType; }
        public String getMatchType() { return matchType; }
        public void setMatchType(String matchType) { this.matchType = matchType; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public boolean isLineageMissing() { return lineageMissing; }
        public void setLineageMissing(boolean lineageMissing) { this.lineageMissing = lineageMissing; }
        public boolean isHasLineage() { return hasLineage; }
        public void setHasLineage(boolean hasLineage) { this.hasLineage = hasLineage; }
    }
}
