package com.bloodline.service.service;

import com.bloodline.domain.entity.LineageSnapshot;
import com.bloodline.domain.mapper.LineageSnapshotMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ConflictAnalyzer {

    private final LineageSnapshotMapper snapshotMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConflictAnalyzer(LineageSnapshotMapper snapshotMapper) {
        this.snapshotMapper = snapshotMapper;
    }

    public ConflictReport analyze(Long baseSnapshotId, Long compareSnapshotId) {
        if (baseSnapshotId == null || compareSnapshotId == null) {
            throw new IllegalArgumentException("Snapshot IDs are required");
        }

        LineageSnapshot base = snapshotMapper.findById(baseSnapshotId);
        LineageSnapshot compare = snapshotMapper.findById(compareSnapshotId);

        if (base == null || compare == null) {
            throw new IllegalArgumentException("Snapshot not found");
        }

        List<SnapshotEdge> baseEdges = parseEdgesData(base.getEdgesData());
        List<SnapshotEdge> compareEdges = parseEdgesData(compare.getEdgesData());

        Set<String> baseSet = new HashSet<>();
        for (SnapshotEdge e : baseEdges) {
            baseSet.add(e.getSourceId() + "::" + e.getTargetId() + "::" + e.getRelationType());
        }

        Set<String> compareSet = new HashSet<>();
        for (SnapshotEdge e : compareEdges) {
            compareSet.add(e.getSourceId() + "::" + e.getTargetId() + "::" + e.getRelationType());
        }

        Set<String> added = new HashSet<>(compareSet);
        added.removeAll(baseSet);

        Set<String> removed = new HashSet<>(baseSet);
        removed.removeAll(compareSet);

        List<Conflict> conflicts = new ArrayList<>();

        for (String sig : added) {
            String[] parts = sig.split("::", 3);
            Conflict c = new Conflict();
            c.setType(detectNodeType(parts[0], parts[1]));
            c.setNodeId(parts[0]);
            c.setSeverity("MEDIUM");
            c.setChangeType("ADDED");
            c.setDetail(parts[0] + " → " + parts[1] + " (" + parts[2] + ")");
            conflicts.add(c);
        }

        for (String sig : removed) {
            String[] parts = sig.split("::", 3);
            Conflict c = new Conflict();
            c.setType(detectNodeType(parts[0], parts[1]));
            c.setNodeId(parts[0]);
            c.setSeverity("HIGH");
            c.setChangeType("REMOVED");
            c.setDetail(parts[0] + " → " + parts[1] + " (" + parts[2] + ")");
            conflicts.add(c);
        }

        conflicts.sort((a, b) -> severityWeight(b.getSeverity()) - severityWeight(a.getSeverity()));

        int high = (int) conflicts.stream().filter(c -> "HIGH".equals(c.getSeverity())).count();
        int medium = (int) conflicts.stream().filter(c -> "MEDIUM".equals(c.getSeverity())).count();
        int low = (int) conflicts.stream().filter(c -> "LOW".equals(c.getSeverity())).count();

        ConflictReport report = new ConflictReport();
        report.setConflicts(conflicts);
        report.setSummary(new ConflictSummary(conflicts.size(), high, medium, low));
        return report;
    }

    private List<SnapshotEdge> parseEdgesData(String edgesData) {
        if (edgesData == null || edgesData.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(edgesData, new TypeReference<List<SnapshotEdge>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String detectNodeType(String sourceId, String targetId) {
        if (sourceId.contains(":")) return "TABLE";
        if (targetId.contains(":")) return "TABLE";
        return "FIELD";
    }

    private int severityWeight(String severity) {
        if ("HIGH".equals(severity)) return 3;
        if ("MEDIUM".equals(severity)) return 2;
        if ("LOW".equals(severity)) return 1;
        return 0;
    }

    public static class SnapshotEdge {
        private String sourceId;
        private String targetId;
        private String relationType;

        public String getSourceId() { return sourceId; }
        public void setSourceId(String sourceId) { this.sourceId = sourceId; }
        public String getTargetId() { return targetId; }
        public void setTargetId(String targetId) { this.targetId = targetId; }
        public String getRelationType() { return relationType; }
        public void setRelationType(String relationType) { this.relationType = relationType; }
    }

    public static class ConflictReport {
        private List<Conflict> conflicts;
        private ConflictSummary summary;

        public List<Conflict> getConflicts() { return conflicts; }
        public void setConflicts(List<Conflict> conflicts) { this.conflicts = conflicts; }
        public ConflictSummary getSummary() { return summary; }
        public void setSummary(ConflictSummary summary) { this.summary = summary; }
    }

    public static class Conflict {
        private String type;
        private String nodeId;
        private String severity;
        private String changeType;
        private String detail;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getNodeId() { return nodeId; }
        public void setNodeId(String nodeId) { this.nodeId = nodeId; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }
        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
    }

    public static class ConflictSummary {
        private int totalConflicts;
        private int highCount;
        private int mediumCount;
        private int lowCount;

        public ConflictSummary(int total, int high, int medium, int low) {
            this.totalConflicts = total;
            this.highCount = high;
            this.mediumCount = medium;
            this.lowCount = low;
        }

        public int getTotalConflicts() { return totalConflicts; }
        public int getHighCount() { return highCount; }
        public int getMediumCount() { return mediumCount; }
        public int getLowCount() { return lowCount; }
    }
}
