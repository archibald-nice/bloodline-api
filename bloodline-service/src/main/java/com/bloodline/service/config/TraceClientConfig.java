package com.bloodline.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class TraceClientConfig {

    @Value("${skywalking.url:}")
    private String skywalkingUrl;

    @Value("${jaeger.url:}")
    private String jaegerUrl;

    @Bean("traceRestTemplate")
    public RestTemplate traceRestTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String getSkywalkingUrl() {
        return skywalkingUrl;
    }

    public String getJaegerUrl() {
        return jaegerUrl;
    }

    public boolean hasSkywalking() {
        return skywalkingUrl != null && !skywalkingUrl.isEmpty();
    }

    public boolean hasJaeger() {
        return jaegerUrl != null && !jaegerUrl.isEmpty();
    }
}
