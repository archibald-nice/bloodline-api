package com.bloodline.domain.mapper;

import com.bloodline.domain.entity.LineageIndexColumn;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LineageIndexColumnMapper {

    @Insert("INSERT INTO lineage_index_column(index_id, column_name, column_order, is_descending) " +
            "VALUES(#{indexId}, #{columnName}, #{columnOrder}, #{isDescending})")
    int insert(LineageIndexColumn ic);

    @Select("SELECT * FROM lineage_index_column WHERE index_id = #{indexId}")
    List<LineageIndexColumn> findByIndex(@Param("indexId") Long indexId);

    @Delete("DELETE FROM lineage_index_column WHERE index_id = #{indexId}")
    int deleteByIndex(@Param("indexId") Long indexId);
}
