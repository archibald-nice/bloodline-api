package com.bloodline.etl.parser;

import com.bloodline.etl.model.LineageEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpenLineageEventParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LineageEvent parse(File jsonFile) throws IOException {
        JsonNode root = objectMapper.readTree(jsonFile);
        return parseNode(root);
    }

    public LineageEvent parse(String jsonContent) throws IOException {
        JsonNode root = objectMapper.readTree(jsonContent);
        return parseNode(root);
    }

    private LineageEvent parseNode(JsonNode root) {
        LineageEvent event = new LineageEvent();
        event.setEventType(root.path("eventType").asText());
        event.setEventTime(root.path("eventTime").asText());
        event.setRunId(root.path("run").path("runId").asText());
        event.setJobName(root.path("job").path("name").asText());
        event.setJobNamespace(root.path("job").path("namespace").asText());
        event.setInputs(parseInputs(root.path("inputs")));
        event.setOutputs(parseOutputs(root.path("outputs")));

        if (event.getRunId() == null || event.getRunId().isEmpty()
            || event.getJobName() == null || event.getJobName().isEmpty()
            || event.getJobNamespace() == null || event.getJobNamespace().isEmpty()) {
            throw new IllegalArgumentException("Missing required fields: runId, job.name, or job.namespace");
        }

        return event;
    }

    private List<LineageEvent.DatasetInput> parseInputs(JsonNode inputsNode) {
        List<LineageEvent.DatasetInput> inputs = new ArrayList<>();
        if (inputsNode != null && inputsNode.isArray()) {
            for (JsonNode node : inputsNode) {
                LineageEvent.DatasetInput input = new LineageEvent.DatasetInput();
                input.setNamespace(node.path("namespace").asText());
                input.setName(node.path("name").asText());
                inputs.add(input);
            }
        }
        return inputs;
    }

    private List<LineageEvent.DatasetOutput> parseOutputs(JsonNode outputsNode) {
        List<LineageEvent.DatasetOutput> outputs = new ArrayList<>();
        if (outputsNode != null && outputsNode.isArray()) {
            for (JsonNode node : outputsNode) {
                LineageEvent.DatasetOutput output = new LineageEvent.DatasetOutput();
                output.setNamespace(node.path("namespace").asText());
                output.setName(node.path("name").asText());
                outputs.add(output);
            }
        }
        return outputs;
    }
}
