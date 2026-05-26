package com.bloodline.service.service;

import com.bloodline.domain.entity.Project;
import com.bloodline.domain.mapper.ProjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public Project create(Project project) {
        projectMapper.insert(project);
        return project;
    }

    public Project findByCode(String tenantId, String projectCode) {
        return projectMapper.findByCode(tenantId, projectCode);
    }

    public List<Project> listByTenant(String tenantId) {
        return projectMapper.findByTenant(tenantId);
    }
}
