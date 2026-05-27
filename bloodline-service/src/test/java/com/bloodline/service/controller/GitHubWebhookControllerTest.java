package com.bloodline.service.controller;

import com.bloodline.domain.entity.Application;
import com.bloodline.domain.mapper.ApplicationMapper;
import com.bloodline.service.service.AnalysisTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GitHubWebhookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalysisTaskService analysisTaskService;

    @Mock
    private ApplicationMapper applicationMapper;

    @BeforeEach
    void setUp() {
        GitHubWebhookController controller = new GitHubWebhookController(analysisTaskService, applicationMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldAcceptValidPushEvent() throws Exception {
        Application app = new Application();
        app.setAppId("order-service");
        app.setGitUrl("https://github.com/acme/order-service.git");
        when(applicationMapper.findByGitUrl("dept_01", "https://github.com/acme/order-service.git"))
                .thenReturn(app);
        when(analysisTaskService.submitTask(any(), any(), eq("order-service"), any(), any(), any()))
                .thenReturn(123L);

        String payload = "{" +
                "\"ref\":\"refs/heads/main\"," +
                "\"after\":\"abc123def456\"," +
                "\"repository\":{\"clone_url\":\"https://github.com/acme/order-service.git\"}" +
                "}";

        mockMvc.perform(post("/api/v1/github/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "push")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(123));
    }

    @Test
    void shouldRejectNonPushEvents() throws Exception {
        mockMvc.perform(post("/api/v1/github/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
