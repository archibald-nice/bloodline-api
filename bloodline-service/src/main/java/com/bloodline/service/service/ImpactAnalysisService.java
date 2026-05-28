package com.bloodline.service.service;

import com.bloodline.domain.entity.LineageColumnRef;
import com.bloodline.domain.mapper.LineageColumnRefMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ImpactAnalysisService {

    private final LineageColumnRefMapper columnRefMapper;

    public ImpactAnalysisService(LineageColumnRefMapper columnRefMapper) {
        this.columnRefMapper = columnRefMapper;
    }

    public ImpactReport analyze(ImpactRequest request) {
        ImpactReport report = new ImpactReport();

        if (request == null || request.getChanges() == null || request.getChanges().isEmpty()) {
            report.setSummary(new Summary(0, 0, 0));
            report.setAppsAffected(Collections.emptyList());
            report.setCrossFieldRelations(Collections.emptyList());
            return report;
        }

        // Step 1: For each change, query refs by (tableName, columnName)
        Set<String> allAffectedAppIds = new LinkedHashSet<>();
        Set<String> allRelatedFields = new LinkedHashSet<>();
        Set<String> allSqlSignatures = new LinkedHashSet<>();

        // Group refs by appId for building AppImpact
        Map<String, List<LineageColumnRef>> refsByApp = new LinkedHashMap<>();

        // Track which columns were directly changed (for coOccurredColumns filtering)
        Set<String> changedFields = new LinkedHashSet<>();

        for (ChangeItem change : request.getChanges()) {
            String fieldKey = change.getTableName() + "." + change.getColumnName();
            changedFields.add(fieldKey);

            List<LineageColumnRef> refs = columnRefMapper.findByColumn(change.getTableName(), change.getColumnName());
            for (LineageColumnRef ref : refs) {
                allAffectedAppIds.add(ref.getAppId());
                allSqlSignatures.add(ref.getSqlSignature());
                refsByApp.computeIfAbsent(ref.getAppId(), k -> new ArrayList<>()).add(ref);
            }
        }

        // Collect all related fields from the SQLs involved, and add co-occurring refs to refsByApp
        for (String sqlSignature : allSqlSignatures) {
            List<LineageColumnRef> sqlRefs = columnRefMapper.findBySqlSignature(sqlSignature);
            for (LineageColumnRef ref : sqlRefs) {
                allRelatedFields.add(ref.getTableName() + "." + ref.getColumnName());
                refsByApp.computeIfAbsent(ref.getAppId(), k -> new ArrayList<>()).add(ref);
            }
        }

        // Step 2: Build appsAffected list
        List<AppImpact> appsAffected = new ArrayList<>();
        for (Map.Entry<String, List<LineageColumnRef>> entry : refsByApp.entrySet()) {
            String appId = entry.getKey();
            List<LineageColumnRef> appRefs = entry.getValue();

            AppImpact appImpact = new AppImpact();
            appImpact.setAppId(appId);
            appImpact.setAppName(appId); // appName not available from mapper, use appId as fallback

            // Group by table.column to build ColumnImpact (affectedColumns)
            Map<String, List<LineageColumnRef>> refsByColumn = appRefs.stream()
                    .collect(Collectors.groupingBy(r -> r.getTableName() + "." + r.getColumnName(), LinkedHashMap::new, Collectors.toList()));

            List<ColumnImpact> affectedColumns = new ArrayList<>();
            List<ColumnImpact> coOccurredColumns = new ArrayList<>();

            for (Map.Entry<String, List<LineageColumnRef>> colEntry : refsByColumn.entrySet()) {
                String columnKey = colEntry.getKey();
                List<LineageColumnRef> colRefs = colEntry.getValue();

                ColumnImpact columnImpact = new ColumnImpact();
                String[] parts = columnKey.split("\\.", 2);
                columnImpact.setTableName(parts[0]);
                columnImpact.setColumnName(parts[1]);
                columnImpact.setRelation(colRefs.get(0).getOperationDetail());
                columnImpact.setSqlSignatures(colRefs.stream()
                        .map(LineageColumnRef::getSqlSignature)
                        .distinct()
                        .collect(Collectors.toList()));

                if (changedFields.contains(columnKey)) {
                    affectedColumns.add(columnImpact);
                } else {
                    coOccurredColumns.add(columnImpact);
                }
            }

            appImpact.setAffectedColumns(affectedColumns);
            appImpact.setCoOccurredColumns(coOccurredColumns);
            appsAffected.add(appImpact);
        }

        // Step 3: Build crossFieldRelations list
        List<CrossFieldRelation> crossFieldRelations = new ArrayList<>();
        for (String sqlSignature : allSqlSignatures) {
            List<LineageColumnRef> sqlRefs = columnRefMapper.findBySqlSignature(sqlSignature);
            if (sqlRefs.isEmpty()) {
                continue;
            }

            CrossFieldRelation crossFieldRelation = new CrossFieldRelation();
            crossFieldRelation.setSqlSignature(sqlSignature);
            crossFieldRelation.setSqlPreview(sqlRefs.get(0).getSqlPreview());

            List<String> columns = sqlRefs.stream()
                    .map(r -> r.getTableName() + "." + r.getColumnName())
                    .distinct()
                    .collect(Collectors.toList());
            crossFieldRelation.setColumns(columns);

            List<String> appsInvolved = sqlRefs.stream()
                    .map(LineageColumnRef::getAppId)
                    .distinct()
                    .collect(Collectors.toList());
            crossFieldRelation.setAppsInvolved(appsInvolved);

            crossFieldRelations.add(crossFieldRelation);
        }

        // Step 4: Build summary
        Summary summary = new Summary(
                allAffectedAppIds.size(),
                allRelatedFields.size(),
                allSqlSignatures.size()
        );

        report.setSummary(summary);
        report.setAppsAffected(appsAffected);
        report.setCrossFieldRelations(crossFieldRelations);

        return report;
    }

    // ==================== DTOs ====================

    public static class ImpactRequest {
        private List<ChangeItem> changes;

        public ImpactRequest() {
        }

        public ImpactRequest(List<ChangeItem> changes) {
            this.changes = changes;
        }

        public List<ChangeItem> getChanges() {
            return changes;
        }

        public void setChanges(List<ChangeItem> changes) {
            this.changes = changes;
        }
    }

    public static class ChangeItem {
        private String appId;
        private String tableName;
        private String columnName;
        private String changeType;

        public ChangeItem() {
        }

        public ChangeItem(String appId, String tableName, String columnName, String changeType) {
            this.appId = appId;
            this.tableName = tableName;
            this.columnName = columnName;
            this.changeType = changeType;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getChangeType() {
            return changeType;
        }

        public void setChangeType(String changeType) {
            this.changeType = changeType;
        }
    }

    public static class ImpactReport {
        private Summary summary;
        private List<AppImpact> appsAffected;
        private List<CrossFieldRelation> crossFieldRelations;

        public Summary getSummary() {
            return summary;
        }

        public void setSummary(Summary summary) {
            this.summary = summary;
        }

        public List<AppImpact> getAppsAffected() {
            return appsAffected;
        }

        public void setAppsAffected(List<AppImpact> appsAffected) {
            this.appsAffected = appsAffected;
        }

        public List<CrossFieldRelation> getCrossFieldRelations() {
            return crossFieldRelations;
        }

        public void setCrossFieldRelations(List<CrossFieldRelation> crossFieldRelations) {
            this.crossFieldRelations = crossFieldRelations;
        }
    }

    public static class Summary {
        private int totalAppsAffected;
        private int totalFieldsRelated;
        private int totalSqlsInvolved;

        public Summary() {
        }

        public Summary(int totalAppsAffected, int totalFieldsRelated, int totalSqlsInvolved) {
            this.totalAppsAffected = totalAppsAffected;
            this.totalFieldsRelated = totalFieldsRelated;
            this.totalSqlsInvolved = totalSqlsInvolved;
        }

        public int getTotalAppsAffected() {
            return totalAppsAffected;
        }

        public void setTotalAppsAffected(int totalAppsAffected) {
            this.totalAppsAffected = totalAppsAffected;
        }

        public int getTotalFieldsRelated() {
            return totalFieldsRelated;
        }

        public void setTotalFieldsRelated(int totalFieldsRelated) {
            this.totalFieldsRelated = totalFieldsRelated;
        }

        public int getTotalSqlsInvolved() {
            return totalSqlsInvolved;
        }

        public void setTotalSqlsInvolved(int totalSqlsInvolved) {
            this.totalSqlsInvolved = totalSqlsInvolved;
        }
    }

    public static class AppImpact {
        private String appId;
        private String appName;
        private List<ColumnImpact> affectedColumns;
        private List<ColumnImpact> coOccurredColumns;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public List<ColumnImpact> getAffectedColumns() {
            return affectedColumns;
        }

        public void setAffectedColumns(List<ColumnImpact> affectedColumns) {
            this.affectedColumns = affectedColumns;
        }

        public List<ColumnImpact> getCoOccurredColumns() {
            return coOccurredColumns;
        }

        public void setCoOccurredColumns(List<ColumnImpact> coOccurredColumns) {
            this.coOccurredColumns = coOccurredColumns;
        }
    }

    public static class ColumnImpact {
        private String tableName;
        private String columnName;
        private String relation;
        private List<String> sqlSignatures;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getRelation() {
            return relation;
        }

        public void setRelation(String relation) {
            this.relation = relation;
        }

        public List<String> getSqlSignatures() {
            return sqlSignatures;
        }

        public void setSqlSignatures(List<String> sqlSignatures) {
            this.sqlSignatures = sqlSignatures;
        }
    }

    public static class CrossFieldRelation {
        private String sqlSignature;
        private String sqlPreview;
        private List<String> columns;
        private List<String> appsInvolved;

        public String getSqlSignature() {
            return sqlSignature;
        }

        public void setSqlSignature(String sqlSignature) {
            this.sqlSignature = sqlSignature;
        }

        public String getSqlPreview() {
            return sqlPreview;
        }

        public void setSqlPreview(String sqlPreview) {
            this.sqlPreview = sqlPreview;
        }

        public List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            this.columns = columns;
        }

        public List<String> getAppsInvolved() {
            return appsInvolved;
        }

        public void setAppsInvolved(List<String> appsInvolved) {
            this.appsInvolved = appsInvolved;
        }
    }
}
