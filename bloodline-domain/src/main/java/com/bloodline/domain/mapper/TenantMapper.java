package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.Tenant;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TenantMapper {
    Tenant findById(@Param("id") Long id);
    Tenant findByCode(@Param("tenantCode") String tenantCode);
    List<Tenant> findAll();
    int insert(Tenant tenant);
    int update(Tenant tenant);
}
