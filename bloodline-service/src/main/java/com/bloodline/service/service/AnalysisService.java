package com.bloodline.service.service;

import com.bloodline.analyzer.model.ParsedRelation;
import com.bloodline.analyzer.parser.JavaSourceParser;
import com.bloodline.domain.entity.LineageEdge;
import com.bloodline.domain.mapper.LineageEdgeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalysisService {
    private final LineageEdgeMapper lineageEdgeMapper;
    private final JavaSourceParser parser;

    public AnalysisService(LineageEdgeMapper lineageEdgeMapper, JavaSourceParser parser) {
        this.lineageEdgeMapper = lineageEdgeMapper;
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

    private void saveRelations(String tenantId, String appId, String branch, String projectId, List<ParsedRelation> relations) {
        if (relations.isEmpty()) return;

        lineageEdgeMapper.deleteByAppBranch(tenantId, appId, branch, projectId);

        List<LineageEdge> edges = relations.stream().map(rel -> {
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
}
