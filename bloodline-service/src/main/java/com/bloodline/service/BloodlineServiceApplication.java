package com.bloodline.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.bloodline")
@MapperScan("com.bloodline.domain.mapper")
@EnableScheduling
public class BloodlineServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BloodlineServiceApplication.class, args);
    }
}
