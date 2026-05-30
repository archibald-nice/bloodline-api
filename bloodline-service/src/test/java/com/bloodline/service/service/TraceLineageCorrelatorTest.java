package com.bloodline.service.service;

import com.bloodline.domain.entity.LineageEdgeV2;
import com.bloodline.domain.entity.LineageNode;
import com.bloodline.domain.mapper.LineageEdgeV2Mapper;
import com.bloodline.domain.mapper.LineageNodeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceLineageCorrelatorTest {

    @Mock
    private LineageEdgeV2Mapper edgeMapper;

    @Mock
    private LineageNodeMapper nodeMapper;

    @InjectMocks
    private TraceLineageCorrelator correlator;

    @Test
    void testExactMatch() {
        LineageNode node = createNode("order-service", "SERVICE", "order-service");
        when(nodeMapper.findByType(1L, null)).thenReturn(Collections.singletonList(node));
        when(edgeMapper.findBySource(1L, "order-service")).thenReturn(
                Collections.singletonList(createEdge("order-service", "user-service")));

        SkyWalkingTraceQueryService.TraceSpan span = createSpan("trace-1", "order-service", 100, false);
        List<TraceLineageCorrelator.CorrelationResult> results = correlator.correlate(1L, Collections.singletonList(span));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMatchType()).isEqualTo("EXACT");
        assertThat(results.get(0).getConfidence()).isEqualTo(1.0);
        assertThat(results.get(0).isLineageMissing()).isFalse();
        assertThat(results.get(0).isHasLineage()).isTrue();
    }

    @Test
    void testDomainMatch() {
        LineageNode node = createNode("warehouse:orders", "TABLE", "orders");
        when(nodeMapper.findByType(1L, null)).thenReturn(Collections.singletonList(node));
        when(edgeMapper.findBySource(1L, "warehouse:orders")).thenReturn(Collections.emptyList());
        when(edgeMapper.findByTarget(1L, "warehouse:orders")).thenReturn(Collections.emptyList());

        SkyWalkingTraceQueryService.TraceSpan span = createSpan("trace-1", "api/orders", 100, false);
        List<TraceLineageCorrelator.CorrelationResult> results = correlator.correlate(1L, Collections.singletonList(span));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMatchType()).isEqualTo("DOMAIN");
        assertThat(results.get(0).getConfidence()).isGreaterThan(0.7);
        assertThat(results.get(0).isLineageMissing()).isFalse();
    }

    @Test
    void testNoMatch() {
        when(nodeMapper.findByType(1L, null)).thenReturn(Collections.emptyList());

        SkyWalkingTraceQueryService.TraceSpan span = createSpan("trace-1", "unknown-service", 100, false);
        List<TraceLineageCorrelator.CorrelationResult> results = correlator.correlate(1L, Collections.singletonList(span));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMatchType()).isEqualTo("NONE");
        assertThat(results.get(0).isLineageMissing()).isTrue();
        assertThat(results.get(0).isHasLineage()).isFalse();
    }

    @Test
    void testMissingLineageDetection() {
        LineageNode node = createNode("new-service", "SERVICE", "new-service");
        when(nodeMapper.findByType(1L, null)).thenReturn(Collections.singletonList(node));
        when(edgeMapper.findBySource(1L, "new-service")).thenReturn(Collections.emptyList());
        when(edgeMapper.findByTarget(1L, "new-service")).thenReturn(Collections.emptyList());

        SkyWalkingTraceQueryService.TraceSpan span = createSpan("trace-1", "new-service", 100, false);
        List<TraceLineageCorrelator.CorrelationResult> results = correlator.correlate(1L, Collections.singletonList(span));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMatchType()).isEqualTo("EXACT");
        assertThat(results.get(0).isLineageMissing()).isFalse();
        assertThat(results.get(0).isHasLineage()).isFalse(); // matched node but no edges
    }

    private LineageNode createNode(String nodeId, String nodeType, String nodeName) {
        LineageNode node = new LineageNode();
        node.setNodeId(nodeId);
        node.setNodeType(nodeType);
        node.setNodeName(nodeName);
        return node;
    }

    private LineageEdgeV2 createEdge(String sourceId, String targetId) {
        LineageEdgeV2 edge = new LineageEdgeV2();
        edge.setSourceId(sourceId);
        edge.setTargetId(targetId);
        edge.setRelationType("CALLS");
        return edge;
    }

    private SkyWalkingTraceQueryService.TraceSpan createSpan(String traceId, String endpoint, int duration, boolean error) {
        SkyWalkingTraceQueryService.TraceSpan span = new SkyWalkingTraceQueryService.TraceSpan();
        span.setTraceId(traceId);
        span.setEndpoint(endpoint);
        span.setDuration(duration);
        span.setError(error);
        return span;
    }
}
