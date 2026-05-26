package com.bloodline.service.service;

import com.bloodline.domain.entity.Application;
import com.bloodline.domain.mapper.ApplicationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {
    private final ApplicationMapper applicationMapper;

    public ApplicationService(ApplicationMapper applicationMapper) {
        this.applicationMapper = applicationMapper;
    }

    public Application create(Application app) {
        applicationMapper.insert(app);
        return app;
    }

    public Application findByAppId(String tenantId, String appId) {
        return applicationMapper.findByAppId(tenantId, appId);
    }

    public List<Application> listByTenant(String tenantId) {
        return applicationMapper.findByTenant(tenantId);
    }
}
