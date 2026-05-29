package com.bloodline.etl.model;

import java.util.List;
import java.util.Map;

public class LineageEvent {
    private String eventType;
    private String eventTime;
    private String runId;
    private String jobName;
    private String jobNamespace;
    private List<DatasetInput> inputs;
    private List<DatasetOutput> outputs;
    private Map<String, Object> runFacets;

    public static class DatasetInput {
        private String namespace;
        private String name;
        private Map<String, Object> facets;

        public String getNamespace() { return namespace; }
        public void setNamespace(String namespace) { this.namespace = namespace; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Map<String, Object> getFacets() { return facets; }
        public void setFacets(Map<String, Object> facets) { this.facets = facets; }
    }

    public static class DatasetOutput {
        private String namespace;
        private String name;
        private Map<String, Object> facets;

        public String getNamespace() { return namespace; }
        public void setNamespace(String namespace) { this.namespace = namespace; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Map<String, Object> getFacets() { return facets; }
        public void setFacets(Map<String, Object> facets) { this.facets = facets; }
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public String getJobNamespace() { return jobNamespace; }
    public void setJobNamespace(String jobNamespace) { this.jobNamespace = jobNamespace; }
    public List<DatasetInput> getInputs() { return inputs; }
    public void setInputs(List<DatasetInput> inputs) { this.inputs = inputs; }
    public List<DatasetOutput> getOutputs() { return outputs; }
    public void setOutputs(List<DatasetOutput> outputs) { this.outputs = outputs; }
    public Map<String, Object> getRunFacets() { return runFacets; }
    public void setRunFacets(Map<String, Object> runFacets) { this.runFacets = runFacets; }
}
