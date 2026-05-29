package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.LineageIndex;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LineageIndexMapper {

    @Insert("INSERT INTO lineage_index(tenant_id, schema_id, table_name, index_name, index_type, is_unique, is_primary, index_columns, definition) " +
            "VALUES(#{tenantId}, #{schemaId}, #{tableName}, #{indexName}, #{indexType}, #{isUnique}, #{isPrimary}, #{indexColumns}, #{definition})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LineageIndex index);

    @Select("SELECT * FROM lineage_index WHERE schema_id = #{schemaId} AND table_name = #{tableName}")
    List<LineageIndex> findByTable(@Param("schemaId") Long schemaId, @Param("tableName") String tableName);

    @Select("SELECT * FROM lineage_index WHERE schema_id = #{schemaId} AND table_name = #{tableName} AND index_name = #{indexName}")
    LineageIndex findByName(@Param("schemaId") Long schemaId, @Param("tableName") String tableName, @Param("indexName") String indexName);

    @Delete("DELETE FROM lineage_index WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
