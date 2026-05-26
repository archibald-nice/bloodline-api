package com.bloodline.service.service;

import com.bloodline.analyzer.model.ParsedRelation;
import com.bloodline.analyzer.parser.JavaSourceParser;
import com.bloodline.domain.entity.LineageEdge;
import com.bloodline.domain.mapper.LineageEdgeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private LineageEdgeMapper lineageEdgeMapper;

    @Mock
    private JavaSourceParser parser;

    @InjectMocks
    private AnalysisService analysisService;

    @Test
    void shouldDeleteOldEdgesAndInsertNewOnes() {
        ParsedRelation rel = new ParsedRelation("CALLS", "SERVICE", "UserService");
        rel.setTargetAppId("user-service");
        when(parser.parseJavaFile(anyString())).thenReturn(Collections.singletonList(rel));

        analysisService.analyzeJavaSource("dept_01", "app1", "release_sit", "proj1", "source code");

        verify(lineageEdgeMapper).deleteByAppBranch("dept_01", "app1", "release_sit", "proj1");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<LineageEdge>> captor = ArgumentCaptor.forClass(List.class);
        verify(lineageEdgeMapper).batchInsert(captor.capture());

        List<LineageEdge> edges = captor.getValue();
        assertThat(edges).hasSize(1);
        assertThat(edges.get(0).getTenantId()).isEqualTo("dept_01");
        assertThat(edges.get(0).getAppId()).isEqualTo("app1");
        assertThat(edges.get(0).getRelationType()).isEqualTo("CALLS");
        assertThat(edges.get(0).getTargetType()).isEqualTo("SERVICE");
        assertThat(edges.get(0).getTargetName()).isEqualTo("UserService");
        assertThat(edges.get(0).getTargetAppId()).isEqualTo("user-service");
        assertThat(edges.get(0).getBranch()).isEqualTo("release_sit");
        assertThat(edges.get(0).getProjectId()).isEqualTo("proj1");
        assertThat(edges.get(0).getSourceType()).isEqualTo("AST");
    }

    @Test
    void shouldDoNothingWhenParserReturnsEmptyList() {
        when(parser.parseJavaFile(anyString())).thenReturn(Collections.emptyList());

        analysisService.analyzeJavaSource("dept_01", "app1", "release_sit", "proj1", "source code");

        verify(lineageEdgeMapper, never()).deleteByAppBranch(anyString(), anyString(), anyString(), anyString());
        verify(lineageEdgeMapper, never()).batchInsert(any());
    }

    @Test
    void shouldHandleMultipleRelations() {
        ParsedRelation rel1 = new ParsedRelation("CALLS", "SERVICE", "UserService");
        ParsedRelation rel2 = new ParsedRelation("QUERIES", "TABLE", "user");
        rel2.setTargetDetail("SELECT");
        when(parser.parseJavaFile(anyString())).thenReturn(Arrays.asList(rel1, rel2));

        analysisService.analyzeJavaSource("dept_01", "app1", "release_sit", "proj1", "source code");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<LineageEdge>> captor = ArgumentCaptor.forClass(List.class);
        verify(lineageEdgeMapper).batchInsert(captor.capture());

        List<LineageEdge> edges = captor.getValue();
        assertThat(edges).hasSize(2);
    }

    @Test
    void shouldParseMyBatisXmlAndSaveRelations() {
        ParsedRelation rel = new ParsedRelation("QUERIES", "TABLE", "user");
        rel.setTargetDetail("SELECT");
        when(parser.parseMyBatisXml(anyString())).thenReturn(Collections.singletonList(rel));

        analysisService.analyzeMyBatisXml("dept_01", "app1", "release_sit", "proj1", "<mapper>...</mapper>");

        verify(lineageEdgeMapper).deleteByAppBranch("dept_01", "app1", "release_sit", "proj1");
        verify(lineageEdgeMapper).batchInsert(any());
    }

    @Test
    void shouldDoNothingWhenMyBatisXmlParserReturnsEmptyList() {
        when(parser.parseMyBatisXml(anyString())).thenReturn(Collections.emptyList());

        analysisService.analyzeMyBatisXml("dept_01", "app1", "release_sit", "proj1", "<mapper></mapper>");

        verify(lineageEdgeMapper, never()).deleteByAppBranch(anyString(), anyString(), anyString(), anyString());
        verify(lineageEdgeMapper, never()).batchInsert(any());
    }
}
