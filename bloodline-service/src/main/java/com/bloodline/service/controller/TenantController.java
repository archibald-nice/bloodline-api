package com.bloodline.service.controller;

import com.bloodline.domain.entity.Tenant;
import com.bloodline.domain.mapper.TenantMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/tenants")
public class TenantController {

    private final TenantMapper tenantMapper;

    public TenantController(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> list() {
        return ResponseEntity.ok(tenantMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> get(@PathVariable Long id) {
        Tenant tenant = tenantMapper.findById(id);
        if (tenant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tenant);
    }

    @PostMapping
    public ResponseEntity<Tenant> create(@RequestBody Tenant tenant) {
        tenantMapper.insert(tenant);
        return ResponseEntity.ok(tenant);
    }
}
