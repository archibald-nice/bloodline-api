package com.bloodline.etl.exporter;

import com.bloodline.domain.entity.LineageEdgeV2;
import com.bloodline.domain.entity.LineageNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenLineageExporterTest {

    private final OpenLineageExporter exporter = new OpenLineageExporter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testExportRunEventWithInputsAndOutputs() throws Exception {
        LineageEdgeV2 edge1 = createEdge("warehouse:orders", "TABLE", "etl:daily-etl", "JOB", "QUERIES");
        LineageEdgeV2 edge2 = createEdge("etl:daily-etl", "JOB", "warehouse:orders_summary", "TABLE", "POPULATES");
        List<LineageEdgeV2> edges = Arrays.asList(edge1, edge2);

        String json = exporter.exportRunEvent("daily-export", "bloodline", edges, this::mockNodeLookup);

        JsonNode root = objectMapper.readTree(json);

        // Verify top-level fields
        assertThat(root.path("eventType").asText()).isEqualTo("COMPLETE");
        assertThat(root.path("eventTime").asText()).isNotEmpty();
        assertThat(root.path("run").path("runId").asText()).isNotEmpty();
        assertThat(root.path("job").path("namespace").asText()).isEqualTo("bloodline");
        assertThat(root.path("job").path("name").asText()).isEqualTo("daily-export");

        // Verify inputs contain the queried table
        JsonNode inputs = root.path("inputs");
        assertThat(inputs.isArray()).isTrue();
        assertThat(inputs.size()).isGreaterThanOrEqualTo(1);
        assertThat(inputs.get(0).path("namespace").asText()).isEqualTo("warehouse");
        assertThat(inputs.get(0).path("name").asText()).isEqualTo("warehouse:orders");

        // Verify outputs contain the populated table
        JsonNode outputs = root.path("outputs");
        assertThat(outputs.isArray()).isTrue();
        assertThat(outputs.size()).isGreaterThanOrEqualTo(1);

        // Verify producer facet
        assertThat(root.path("producer").path("name").asText()).isEqualTo("bloodline");
    }

    @Test
    void testExportRunEventWithEmptyEdges() throws Exception {
        String json = exporter.exportRunEvent("empty-export", "bloodline", Collections.emptyList(), n -> null);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.path("eventType").asText()).isEqualTo("COMPLETE");
        assertThat(root.path("inputs").isArray()).isTrue();
        assertThat(root.path("inputs").size()).isEqualTo(0);
        assertThat(root.path("outputs").isArray()).isTrue();
        assertThat(root.path("outputs").size()).isEqualTo(0);
    }

    @Test
    void testExportRunEventContainsBloodlineFacet() throws Exception {
        LineageEdgeV2 edge = createEdge("warehouse:orders", "TABLE", "etl:daily-etl", "JOB", "QUERIES");
        String json = exporter.exportRunEvent("test", "bloodline", Collections.singletonList(edge), this::mockNodeLookup);

        JsonNode root = objectMapper.readTree(json);
        JsonNode input = root.path("inputs").get(0);
        JsonNode facet = input.path("facets").path("bloodline");

        assertThat(facet.path("nodeType").asText()).isEqualTo("TABLE");
        assertThat(facet.path("relationType").asText()).isEqualTo("QUERIES");
    }

    private LineageEdgeV2 createEdge(String sourceId, String sourceType, String targetId, String targetType, String relationType) {
        LineageEdgeV2 edge = new LineageEdgeV2();
        edge.setSourceId(sourceId);
        edge.setSourceType(sourceType);
        edge.setTargetId(targetId);
        edge.setTargetType(targetType);
        edge.setRelationType(relationType);
        return edge;
    }

    private LineageNode mockNodeLookup(String nodeId) {
        LineageNode node = new LineageNode();
        node.setNodeId(nodeId);
        node.setDomain("warehouse");
        return node;
    }
}
