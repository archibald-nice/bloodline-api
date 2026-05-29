package com.bloodline.domain.entity;

public class LineageIndexColumn {
    private Long id;
    private Long indexId;
    private String columnName;
    private Integer columnOrder;
    private Boolean isDescending;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIndexId() { return indexId; }
    public void setIndexId(Long indexId) { this.indexId = indexId; }
    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }
    public Integer getColumnOrder() { return columnOrder; }
    public void setColumnOrder(Integer columnOrder) { this.columnOrder = columnOrder; }
    public Boolean getIsDescending() { return isDescending; }
    public void setIsDescending(Boolean descending) { isDescending = descending; }
}
