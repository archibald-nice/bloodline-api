package com.bloodline.service.service;

import com.bloodline.domain.entity.LineageColumnRef;
import com.bloodline.domain.mapper.LineageColumnRefMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LineageColumnRefService {

    private final LineageColumnRefMapper lineageColumnRefMapper;

    public LineageColumnRefService(LineageColumnRefMapper lineageColumnRefMapper) {
        this.lineageColumnRefMapper = lineageColumnRefMapper;
    }

    public List<LineageColumnRef> findByApp(String appId) {
        return lineageColumnRefMapper.findByApp(appId);
    }

    public List<LineageColumnRef> findByColumn(String tableName, String columnName) {
        return lineageColumnRefMapper.findByColumn(tableName, columnName);
    }

    public List<String> findAppsByColumn(String tableName, String columnName) {
        return lineageColumnRefMapper.findAppsByColumn(tableName, columnName);
    }

    public List<LineageColumnRef> findBySqlSignature(String sqlSignature) {
        return lineageColumnRefMapper.findBySqlSignature(sqlSignature);
    }

    public List<String> findAllTables() {
        return lineageColumnRefMapper.findAllTables();
    }

    public List<String> findColumnsByTable(String tableName) {
        return lineageColumnRefMapper.findColumnsByTable(tableName);
    }

    public List<String> findTablesByApp(String appId) {
        return lineageColumnRefMapper.findTablesByApp(appId);
    }
}
