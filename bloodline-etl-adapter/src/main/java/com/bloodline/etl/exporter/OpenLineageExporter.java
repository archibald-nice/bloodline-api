package com.bloodline.etl.exporter;

import com.bloodline.domain.entity.LineageEdgeV2;
import com.bloodline.domain.entity.LineageNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Converts Bloodline internal lineage data to OpenLineage RunEvent JSON format.
 */
public class OpenLineageExporter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Export edges as an OpenLineage RunEvent COMPLETE event.
     *
     * @param jobName       export job name
     * @param jobNamespace  export job namespace (e.g. "bloodline")
     * @param edges         list of active lineage edges
     * @param nodeLookup    function to resolve node details by nodeId
     * @return JSON string representing an OpenLineage RunEvent
     */
    public String exportRunEvent(String jobName, String jobNamespace,
                                  List<LineageEdgeV2> edges,
                                  java.util.function.Function<String, LineageNode> nodeLookup) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("eventType", "COMPLETE");
        root.put("eventTime", Instant.now().toString());

        ObjectNode run = root.putObject("run");
        run.put("runId", UUID.randomUUID().toString());

        ObjectNode job = root.putObject("job");
        job.put("namespace", jobNamespace);
        job.put("name", jobName);

        ArrayNode inputs = root.putArray("inputs");
        ArrayNode outputs = root.putArray("outputs");

        for (LineageEdgeV2 edge : edges) {
            if (isInputEdge(edge)) {
                inputs.add(buildDataset(edge.getSourceId(), edge.getSourceType(), edge.getRelationType(), nodeLookup));
            } else if (isOutputEdge(edge)) {
                outputs.add(buildDataset(edge.getTargetId(), edge.getTargetType(), edge.getRelationType(), nodeLookup));
            }
            // For bi-directional or query edges, include both as context
            if ("QUERIES".equals(edge.getRelationType())) {
                inputs.add(buildDataset(edge.getSourceId(), edge.getSourceType(), edge.getRelationType(), nodeLookup));
                outputs.add(buildDataset(edge.getTargetId(), edge.getTargetType(), edge.getRelationType(), nodeLookup));
            }
        }

        // Add producer facet
        ObjectNode producer = root.putObject("producer");
        producer.put("name", "bloodline");
        producer.put("version", "2.0.0");

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize OpenLineage RunEvent", e);
        }
    }

    private boolean isInputEdge(LineageEdgeV2 edge) {
        return "QUERIES".equals(edge.getRelationType()) || "READS".equals(edge.getRelationType());
    }

    private boolean isOutputEdge(LineageEdgeV2 edge) {
        return "POPULATES".equals(edge.getRelationType()) || "WRITES".equals(edge.getRelationType());
    }

    private ObjectNode buildDataset(String nodeId, String nodeType, String relationType,
                                     java.util.function.Function<String, LineageNode> nodeLookup) {
        ObjectNode dataset = objectMapper.createObjectNode();

        String namespace = "bloodline";
        String name = nodeId;

        LineageNode node = nodeLookup.apply(nodeId);
        if (node != null && node.getDomain() != null && !node.getDomain().isEmpty()) {
            namespace = node.getDomain();
        }

        dataset.put("namespace", namespace);
        dataset.put("name", name);

        ObjectNode facets = dataset.putObject("facets");
        ObjectNode bloodlineFacet = facets.putObject("bloodline");
        bloodlineFacet.put("nodeType", nodeType);
        bloodlineFacet.put("relationType", relationType);

        return dataset;
    }
}
