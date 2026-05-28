package com.bloodline.service.service;

import com.bloodline.analyzer.model.ParsedRelation;
import com.bloodline.analyzer.parser.JavaSourceParser;
import com.bloodline.domain.entity.LineageColumnRef;
import com.bloodline.domain.entity.LineageEdge;
import com.bloodline.domain.mapper.LineageColumnRefMapper;
import com.bloodline.domain.mapper.LineageEdgeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);
    private final LineageEdgeMapper lineageEdgeMapper;
    private final LineageColumnRefMapper columnRefMapper;
    private final JavaSourceParser parser;

    public AnalysisService(LineageEdgeMapper lineageEdgeMapper, LineageColumnRefMapper columnRefMapper, JavaSourceParser parser) {
        this.lineageEdgeMapper = lineageEdgeMapper;
        this.columnRefMapper = columnRefMapper;
        this.parser = parser;
    }

    @Transactional
    public void analyzeJavaSource(String tenantId, String appId, String branch, String projectId, String sourceCode) {
        List<ParsedRelation> relations = parser.parseJavaFile(sourceCode);
        saveRelations(tenantId, appId, branch, projectId, relations);
    }

    @Transactional
    public void analyzeMyBatisXml(String tenantId, String appId, String branch, String projectId, String xmlContent) {
        List<ParsedRelation> relations = parser.parseMyBatisXml(xmlContent);
        saveRelations(tenantId, appId, branch, projectId, relations);
    }

    @Transactional
    public void analyzeSourceFiles(String tenantId, String appId, String branch, String projectId,
                                   List<GitHubCodeFetchService.SourceFile> files) {
        List<ParsedRelation> allRelations = new ArrayList<>();
        for (GitHubCodeFetchService.SourceFile file : files) {
            try {
                if (file.getRelativePath().endsWith(".java")) {
                    allRelations.addAll(parser.parseJavaFile(file.getContent()));
                } else if (file.getRelativePath().endsWith(".xml")) {
                    allRelations.addAll(parser.parseMyBatisXml(file.getContent()));
                }
            } catch (Exception e) {
                logger.warn("Failed to parse file: {}", file.getRelativePath(), e);
            }
        }
        saveRelations(tenantId, appId, branch, projectId, allRelations);
    }

    private void saveRelations(String tenantId, String appId, String branch, String projectId, List<ParsedRelation> relations) {
        if (relations.isEmpty()) return;

        List<ParsedRelation> edgeRelations = new ArrayList<>();
        List<ParsedRelation> columnRelations = new ArrayList<>();

        for (ParsedRelation rel : relations) {
            if ("COLUMN".equals(rel.getTargetType())) {
                columnRelations.add(rel);
            } else {
                edgeRelations.add(rel);
            }
        }

        // Save lineage edges (APP and TABLE types)
        lineageEdgeMapper.deleteByAppBranch(tenantId, appId, branch, projectId);

        if (!edgeRelations.isEmpty()) {
            List<LineageEdge> edges = edgeRelations.stream().map(rel -> {
                LineageEdge edge = new LineageEdge();
                edge.setTenantId(tenantId);
                edge.setAppId(appId);
                edge.setTargetAppId(rel.getTargetAppId());
                edge.setTargetType(rel.getTargetType());
                edge.setTargetName(rel.getTargetName());
                edge.setTargetDetail(rel.getTargetDetail());
                edge.setRelationType(rel.getRelationType());
                edge.setBranch(branch);
                edge.setProjectId(projectId);
                edge.setConfidence(BigDecimal.valueOf(rel.getConfidence()));
                edge.setSourceType("AST");
                return edge;
            }).collect(Collectors.toList());

            lineageEdgeMapper.batchInsert(edges);
        }

        // Save column refs (COLUMN type)
        columnRefMapper.deleteByApp(appId);

        if (!columnRelations.isEmpty()) {
            List<LineageColumnRef> columnRefs = columnRelations.stream().map(rel -> {
                LineageColumnRef ref = new LineageColumnRef();
                ref.setAppId(appId);

                String targetName = rel.getTargetName();
                String tableName = null;
                String columnName = targetName;
                if (targetName != null && targetName.contains(".")) {
                    int dotIndex = targetName.lastIndexOf('.');
                    tableName = targetName.substring(0, dotIndex);
                    columnName = targetName.substring(dotIndex + 1);
                }

                ref.setTableName(tableName);
                ref.setColumnName(columnName);
                ref.setSqlSignature(rel.getSqlSignature());
                ref.setSqlPreview(rel.getSqlPreview());
                ref.setOperationType(rel.getTargetAppId());
                ref.setOperationDetail(rel.getTargetDetail());
                ref.setSourceLocation(rel.getSourceLocation());
                return ref;
            }).collect(Collectors.toList());

            columnRefMapper.batchInsert(columnRefs);
        }
    }
}
