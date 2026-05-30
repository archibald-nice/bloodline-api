package com.bloodline.service.service;

import com.bloodline.service.config.TraceClientConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SkyWalkingTraceQueryService {

    private final RestTemplate restTemplate;
    private final TraceClientConfig traceConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SkyWalkingTraceQueryService(RestTemplate traceRestTemplate, TraceClientConfig traceConfig) {
        this.restTemplate = traceRestTemplate;
        this.traceConfig = traceConfig;
    }

    public List<TraceSpan> queryRecentTraces(String serviceName, int minutes) {
        if (!traceConfig.hasSkywalking()) {
            return Collections.emptyList();
        }

        String url = traceConfig.getSkywalkingUrl() + "/trace/graphql";
        String query = buildTraceQuery(serviceName, minutes);

        try {
            String response = restTemplate.postForObject(url, query, String.class);
            return parseTraceResponse(response);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String buildTraceQuery(String serviceName, int minutes) {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (minutes * 60 * 1000L);
        return "{\"query\":\"query queryBasicTraces { queryBasicTraces(condition: {serviceName: \\\"" + serviceName
                + "\\\", traceState: ALL, queryDuration: {start: \\\"" + startTime
                + "\\\", end: \\\"" + endTime
                + "\\\", step: MINUTE}, paging: {pageNum: 1, pageSize: 20}}) { traces { traceId endpointNames duration start isError } total } }\"}";
    }

    private List<TraceSpan> parseTraceResponse(String response) {
        List<TraceSpan> spans = new ArrayList<>();
        if (response == null || response.isEmpty()) {
            return spans;
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.path("data").path("queryBasicTraces").path("traces");
            if (data.isArray()) {
                for (JsonNode trace : data) {
                    TraceSpan span = new TraceSpan();
                    span.setTraceId(trace.path("traceId").asText());
                    span.setEndpoint(trace.path("endpointNames").isArray() && trace.path("endpointNames").size() > 0
                            ? trace.path("endpointNames").get(0).asText() : "");
                    span.setDuration(trace.path("duration").asInt());
                    span.setError(trace.path("isError").asBoolean());
                    spans.add(span);
                }
            }
        } catch (IOException e) {
            // ignore parse error
        }
        return spans;
    }

    public static class TraceSpan {
        private String traceId;
        private String endpoint;
        private int duration;
        private boolean error;

        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        public boolean isError() { return error; }
        public void setError(boolean error) { this.error = error; }
    }
}
