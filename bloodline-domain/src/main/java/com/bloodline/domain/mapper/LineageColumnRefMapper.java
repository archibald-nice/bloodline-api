package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.LineageColumnRef;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LineageColumnRefMapper {

    @Insert("INSERT INTO lineage_column_ref(app_id, table_name, column_name, sql_signature, sql_preview, " +
            "operation_type, operation_detail, source_location) " +
            "VALUES(#{appId}, #{tableName}, #{columnName}, #{sqlSignature}, #{sqlPreview}, " +
            "#{operationType}, #{operationDetail}, #{sourceLocation})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LineageColumnRef ref);

    @Insert("<script>" +
            "INSERT INTO lineage_column_ref(app_id, table_name, column_name, sql_signature, sql_preview, " +
            "operation_type, operation_detail, source_location) VALUES " +
            "<foreach collection='refs' item='r' separator=','>" +
            "(#{r.appId}, #{r.tableName}, #{r.columnName}, #{r.sqlSignature}, #{r.sqlPreview}, " +
            "#{r.operationType}, #{r.operationDetail}, #{r.sourceLocation})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("refs") List<LineageColumnRef> refs);

    @Select("SELECT * FROM lineage_column_ref WHERE app_id = #{appId}")
    List<LineageColumnRef> findByApp(@Param("appId") String appId);

    @Select("SELECT * FROM lineage_column_ref WHERE table_name = #{tableName} AND column_name = #{columnName}")
    List<LineageColumnRef> findByColumn(@Param("tableName") String tableName, @Param("columnName") String columnName);

    @Select("SELECT DISTINCT app_id FROM lineage_column_ref WHERE table_name = #{tableName} AND column_name = #{columnName}")
    List<String> findAppsByColumn(@Param("tableName") String tableName, @Param("columnName") String columnName);

    @Select("SELECT * FROM lineage_column_ref WHERE sql_signature = #{sqlSignature}")
    List<LineageColumnRef> findBySqlSignature(@Param("sqlSignature") String sqlSignature);

    @Delete("DELETE FROM lineage_column_ref WHERE app_id = #{appId}")
    int deleteByApp(@Param("appId") String appId);

    @Select("SELECT DISTINCT table_name FROM lineage_column_ref ORDER BY table_name")
    List<String> findAllTables();

    @Select("SELECT DISTINCT table_name FROM lineage_column_ref WHERE app_id = #{appId} ORDER BY table_name")
    List<String> findTablesByApp(@Param("appId") String appId);

    @Select("SELECT DISTINCT column_name FROM lineage_column_ref WHERE table_name = #{tableName} ORDER BY column_name")
    List<String> findColumnsByTable(@Param("tableName") String tableName);
}
