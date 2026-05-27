package com.bloodline.service.service;

import com.bloodline.domain.entity.LineageEdge;
import com.bloodline.domain.mapper.LineageEdgeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LineageQueryServiceTest {

    @Mock
    private LineageEdgeMapper lineageEdgeMapper;

    @InjectMocks
    private LineageQueryService lineageQueryService;

    @Test
    void shouldReturnUpstreamEdges() {
        LineageEdge edge1 = new LineageEdge();
        edge1.setAppId("app1");
        edge1.setTargetName("user-service");
        LineageEdge edge2 = new LineageEdge();
        edge2.setAppId("app1");
        edge2.setTargetName("order-service");
        when(lineageEdgeMapper.findByApp("dept_01", "app1")).thenReturn(Arrays.asList(edge1, edge2));

        List<LineageEdge> result = lineageQueryService.getUpstream("dept_01", "app1");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(LineageEdge::getTargetName)
                .containsExactly("user-service", "order-service");
        verify(lineageEdgeMapper).findByApp("dept_01", "app1");
    }

    @Test
    void shouldReturnDownstreamAppIds() {
        when(lineageEdgeMapper.findUpstreamApps("dept_01", "app1"))
                .thenReturn(Arrays.asList("app2", "app3"));

        List<String> result = lineageQueryService.getDownstreamAppIds("dept_01", "app1");

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("app2", "app3");
        verify(lineageEdgeMapper).findUpstreamApps("dept_01", "app1");
    }

    @Test
    void shouldReturnAppsUsingTable() {
        when(lineageEdgeMapper.findAppsUsingTarget("dept_01", "TABLE", "user"))
                .thenReturn(Arrays.asList("app1", "app2"));

        List<String> result = lineageQueryService.getAppsUsingTable("dept_01", "user");

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("app1", "app2");
        verify(lineageEdgeMapper).findAppsUsingTarget("dept_01", "TABLE", "user");
    }

    @Test
    void shouldBuildLineageGraph() {
        LineageEdge edge = new LineageEdge();
        edge.setAppId("app1");
        edge.setTargetName("user-service");
        when(lineageEdgeMapper.findByApp("dept_01", "app1")).thenReturn(Collections.singletonList(edge));
        when(lineageEdgeMapper.findUpstreamApps("dept_01", "app1"))
                .thenReturn(Collections.singletonList("app2"));

        LineageQueryService.LineageGraph graph = lineageQueryService.getLineageGraph("dept_01", "app1");

        assertThat(graph.getAppId()).isEqualTo("app1");
        assertThat(graph.getUpstream()).hasSize(1);
        assertThat(graph.getUpstream().get(0).getTargetName()).isEqualTo("user-service");
        assertThat(graph.getDownstreamAppIds()).hasSize(1);
        assertThat(graph.getDownstreamAppIds().get(0)).isEqualTo("app2");
    }

    @Test
    void shouldHandleEmptyResults() {
        when(lineageEdgeMapper.findByApp(anyString(), anyString())).thenReturn(Collections.emptyList());
        when(lineageEdgeMapper.findUpstreamApps(anyString(), anyString())).thenReturn(Collections.emptyList());

        LineageQueryService.LineageGraph graph = lineageQueryService.getLineageGraph("dept_01", "app1");

        assertThat(graph.getAppId()).isEqualTo("app1");
        assertThat(graph.getUpstream()).isEmpty();
        assertThat(graph.getDownstreamAppIds()).isEmpty();
    }

    @Test
    void shouldGetRecursiveUpstream() {
        LineageEdge edge1 = new LineageEdge();
        edge1.setAppId("app1");
        edge1.setTargetAppId("app2");
        LineageEdge edge2 = new LineageEdge();
        edge2.setAppId("app2");
        edge2.setTargetAppId("app3");
        when(lineageEdgeMapper.findUpstreamRecursive("dept_01", "app1", 5))
                .thenReturn(Arrays.asList(edge1, edge2));

        List<LineageEdge> result = lineageQueryService.getUpstreamRecursive("dept_01", "app1", 5);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(LineageEdge::getTargetAppId)
                .containsExactly("app2", "app3");
        verify(lineageEdgeMapper).findUpstreamRecursive("dept_01", "app1", 5);
    }

    @Test
    void shouldGetRecursiveDownstream() {
        when(lineageEdgeMapper.findDownstreamRecursive("dept_01", "app1", 5))
                .thenReturn(Arrays.asList("app2", "app3"));

        List<String> result = lineageQueryService.getDownstreamRecursive("dept_01", "app1", 5);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("app2", "app3");
        verify(lineageEdgeMapper).findDownstreamRecursive("dept_01", "app1", 5);
    }

    @Test
    void shouldQueryWithBranchOverlay() {
        LineageEdge edge = new LineageEdge();
        edge.setAppId("app1");
        edge.setTargetName("user-service");
        when(lineageEdgeMapper.findByAppWithBranchOverlay("dept_01", "app1", "release_sit", "feature_x", "proj_123"))
                .thenReturn(Collections.singletonList(edge));

        List<LineageEdge> result = lineageQueryService.getUpstreamWithOverlay("dept_01", "app1", "release_sit", "feature_x", "proj_123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetName()).isEqualTo("user-service");
        verify(lineageEdgeMapper).findByAppWithBranchOverlay("dept_01", "app1", "release_sit", "feature_x", "proj_123");
    }
}
