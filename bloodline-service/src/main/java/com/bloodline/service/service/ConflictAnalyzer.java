package com.bloodline.service.service;

import com.bloodline.domain.mapper.LineageEdgeV2Mapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConflictAnalyzer {

    private final LineageEdgeV2Mapper edgeMapper;

    public ConflictAnalyzer(LineageEdgeV2Mapper edgeMapper) {
        this.edgeMapper = edgeMapper;
    }

    public ConflictReport analyze(Long baseSnapshotId, Long compareSnapshotId) {
        if (baseSnapshotId == null || compareSnapshotId == null) {
            throw new IllegalArgumentException("Snapshot IDs are required");
        }
        ConflictReport report = new ConflictReport();
        report.setConflicts(new ArrayList<>());
        report.setSummary(new ConflictSummary(0, 0, 0, 0));
        return report;
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
        private int baseVersion;
        private int compareVersion;
        private String severity;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getNodeId() { return nodeId; }
        public void setNodeId(String nodeId) { this.nodeId = nodeId; }
        public int getBaseVersion() { return baseVersion; }
        public void setBaseVersion(int baseVersion) { this.baseVersion = baseVersion; }
        public int getCompareVersion() { return compareVersion; }
        public void setCompareVersion(int compareVersion) { this.compareVersion = compareVersion; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
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
