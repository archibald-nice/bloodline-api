package com.bloodline.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

@Configuration
public class SeedDataConfig {

    private static final Logger log = LoggerFactory.getLogger(SeedDataConfig.class);

    @Bean
    public CommandLineRunner seedData(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM application WHERE tenant_id = 'dept_01'");
                int count = 0;
                if (rs.next()) {
                    count = rs.getInt(1);
                }
                rs.close();

                if (count == 0) {
                    log.info("No test data found. Seeding database with sample microservices lineage data...");
                    ClassPathResource resource = new ClassPathResource("db/test-data.sql");
                    if (resource.exists()) {
                        String sql = new BufferedReader(
                                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                                .lines()
                                .collect(Collectors.joining("\n"));

                        // Split by semicolon and execute each statement
                        String[] statements = sql.split(";");
                        int executed = 0;
                        for (String s : statements) {
                            String trimmed = s.trim();
                            if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("/*") || trimmed.startsWith("delete") || trimmed.startsWith("DELETE")) {
                                continue;
                            }
                            try {
                                stmt.execute(trimmed);
                                executed++;
                            } catch (Exception e) {
                                log.warn("Failed to execute statement: {}... — {}", trimmed.substring(0, Math.min(60, trimmed.length())), e.getMessage());
                            }
                        }
                        log.info("Database seeded successfully. Executed {} INSERT statements.", executed);
                    } else {
                        log.warn("Seed data file not found: db/test-data.sql");
                    }
                } else {
                    log.info("Database already contains {} application(s). Skipping seed.", count);
                }
            }
        };
    }
}
