package com.bloodline.service.controller;

import com.bloodline.domain.entity.Datasource;
import com.bloodline.domain.entity.DatasourceSchema;
import com.bloodline.service.service.DatasourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DatasourceController {

    private final DatasourceService datasourceService;

    public DatasourceController(DatasourceService datasourceService) {
        this.datasourceService = datasourceService;
    }

    @GetMapping("/applications/{appId}/datasources")
    public ResponseEntity<List<Datasource>> getDatasources(@PathVariable String appId) {
        return ResponseEntity.ok(datasourceService.listByApp("dept_01", appId));
    }

    @PostMapping("/applications/{appId}/datasources")
    public ResponseEntity<Datasource> createDatasource(@PathVariable String appId, @RequestBody Datasource ds) {
        ds.setAppId(appId);
        return ResponseEntity.ok(datasourceService.create("dept_01", ds));
    }

    @GetMapping("/datasources/{datasourceId}/schemas")
    public ResponseEntity<List<DatasourceSchema>> getSchemas(@PathVariable Long datasourceId) {
        return ResponseEntity.ok(datasourceService.listSchemas(datasourceId));
    }

    @PostMapping("/datasources/{datasourceId}/schemas")
    public ResponseEntity<DatasourceSchema> createSchema(@PathVariable Long datasourceId, @RequestBody DatasourceSchema schema) {
        schema.setDatasourceId(datasourceId);
        return ResponseEntity.ok(datasourceService.createSchema("dept_01", schema));
    }
}
