package com.bloodline.etl.parser;

import com.bloodline.etl.model.LineageEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenLineageEventParserTest {

    private final OpenLineageEventParser parser = new OpenLineageEventParser();

    @Test
    void testParseValidRunEvent() throws Exception {
        String json = "{\n" +
            "  \"eventType\": \"COMPLETE\",\n" +
            "  \"eventTime\": \"2026-05-30T10:00:00Z\",\n" +
            "  \"run\": {\"runId\": \"run-123\"},\n" +
            "  \"job\": {\"namespace\": \"etl\", \"name\": \"daily-etl\"},\n" +
            "  \"inputs\": [{\"namespace\": \"warehouse\", \"name\": \"orders\"}],\n" +
            "  \"outputs\": [{\"namespace\": \"warehouse\", \"name\": \"orders_summary\"}]\n" +
            "}";

        LineageEvent event = parser.parse(json);

        assertEquals("COMPLETE", event.getEventType());
        assertEquals("run-123", event.getRunId());
        assertEquals("daily-etl", event.getJobName());
        assertEquals(1, event.getInputs().size());
        assertEquals("orders", event.getInputs().get(0).getName());
        assertEquals(1, event.getOutputs().size());
        assertEquals("orders_summary", event.getOutputs().get(0).getName());
    }

    @Test
    void testParseEmptyInputsOutputs() throws Exception {
        String json = "{\"eventType\": \"START\", \"run\": {\"runId\": \"r\"}, \"job\": {\"namespace\": \"n\", \"name\": \"j\"}, \"inputs\": [], \"outputs\": []}";
        LineageEvent event = parser.parse(json);
        assertTrue(event.getInputs().isEmpty());
        assertTrue(event.getOutputs().isEmpty());
    }
}
