package com.bloodline.service.service;

import com.bloodline.domain.entity.LineageIndex;
import com.bloodline.domain.entity.LineageIndexColumn;
import com.bloodline.domain.mapper.LineageIndexMapper;
import com.bloodline.domain.mapper.LineageIndexColumnMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LineageIndexService {

    private final LineageIndexMapper indexMapper;
    private final LineageIndexColumnMapper indexColumnMapper;

    public LineageIndexService(LineageIndexMapper indexMapper, LineageIndexColumnMapper indexColumnMapper) {
        this.indexMapper = indexMapper;
        this.indexColumnMapper = indexColumnMapper;
    }

    public List<LineageIndex> listByTable(Long schemaId, String tableName) {
        return indexMapper.findByTable(schemaId, tableName);
    }

    @Transactional
    public LineageIndex create(String tenantId, LineageIndex index, List<LineageIndexColumn> columns) {
        index.setTenantId(tenantId);
        indexMapper.insert(index);
        if (columns != null) {
            for (LineageIndexColumn col : columns) {
                col.setIndexId(index.getId());
                indexColumnMapper.insert(col);
            }
        }
        return index;
    }

    public List<LineageIndexColumn> listColumns(Long indexId) {
        return indexColumnMapper.findByIndex(indexId);
    }
}
