package com.bloodline.service.service;

import com.bloodline.domain.entity.ProjectAppRelation;
import com.bloodline.domain.mapper.ProjectAppRelationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProjectAppRelationService {
    private final ProjectAppRelationMapper relationMapper;

    public ProjectAppRelationService(ProjectAppRelationMapper relationMapper) {
        this.relationMapper = relationMapper;
    }

    @Transactional
    public void bindApp(String tenantId, Long projectId, String appId) {
        ProjectAppRelation relation = new ProjectAppRelation();
        relation.setTenantId(tenantId);
        relation.setProjectId(projectId);
        relation.setAppId(appId);
        relationMapper.insert(relation);
    }

    @Transactional
    public void unbindApp(String tenantId, Long projectId, String appId) {
        relationMapper.delete(tenantId, projectId, appId);
    }

    public List<String> listAppIdsByProject(String tenantId, Long projectId) {
        return relationMapper.findAppIdsByProject(tenantId, projectId);
    }
}
