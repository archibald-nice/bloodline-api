package com.bloodline.service.controller;

import com.bloodline.domain.entity.LineageIndex;
import com.bloodline.service.service.LineageIndexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LineageIndexController {

    private final LineageIndexService lineageIndexService;

    public LineageIndexController(LineageIndexService lineageIndexService) {
        this.lineageIndexService = lineageIndexService;
    }

    @GetMapping("/schemas/{schemaId}/tables/{tableName}/indexes")
    public ResponseEntity<List<LineageIndex>> getIndexes(@PathVariable Long schemaId, @PathVariable String tableName) {
        return ResponseEntity.ok(lineageIndexService.listByTable(schemaId, tableName));
    }

    @PostMapping("/schemas/{schemaId}/tables/{tableName}/indexes")
    public ResponseEntity<LineageIndex> createIndex(@PathVariable Long schemaId, @PathVariable String tableName, @RequestBody LineageIndex index) {
        index.setSchemaId(schemaId);
        index.setTableName(tableName);
        return ResponseEntity.ok(lineageIndexService.create("dept_01", index, null));
    }
}
