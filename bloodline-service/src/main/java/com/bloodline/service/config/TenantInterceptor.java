package com.bloodline.service.config;

import com.bloodline.common.context.TenantContext;
import com.bloodline.domain.entity.Tenant;
import com.bloodline.domain.mapper.TenantMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantMapper tenantMapper;
    private static final String HEADER_TENANT_ID = "X-Tenant-ID";

    public TenantInterceptor(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantIdHeader = request.getHeader(HEADER_TENANT_ID);
        Long tenantId = null;

        if (tenantIdHeader != null && !tenantIdHeader.isEmpty()) {
            try {
                tenantId = Long.parseLong(tenantIdHeader);
            } catch (NumberFormatException e) {
                // Fall through to default
            }
        }

        // Default tenant for backward compatibility during migration
        if (tenantId == null) {
            tenantId = 1L;
        }

        // Validate tenant exists and is active
        Tenant tenant = tenantMapper.findById(tenantId);
        if (tenant == null || tenant.getStatus() == null || tenant.getStatus() != 1) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        TenantContext.setCurrentTenant(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
