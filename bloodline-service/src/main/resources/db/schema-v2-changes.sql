-- ============================================================
-- V2 Schema Changes: Generic Lineage Graph, Versioning, Snapshots, and Tenants
-- ============================================================

-- 1. tenant table
CREATE TABLE IF NOT EXISTS tenant (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_code     VARCHAR(64) NOT NULL UNIQUE COMMENT '租户编码',
    tenant_name     VARCHAR(128) NOT NULL COMMENT '租户名称',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '1-启用 0-禁用',
    config          JSON COMMENT '租户级配置',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- Insert default tenant (idempotent)
INSERT IGNORE INTO tenant (tenant_code, tenant_name, status) VALUES ('dept_01', 'Default Department', 1);

-- 2. lineage_node table
CREATE TABLE IF NOT EXISTS lineage_node (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id       BIGINT NOT NULL,
    node_id         VARCHAR(128) NOT NULL COMMENT '节点唯一标识',
    node_type       VARCHAR(32) NOT NULL COMMENT 'TABLE|JOB|FIELD|API',
    node_name       VARCHAR(256) NOT NULL COMMENT '显示名称',
    domain          VARCHAR(64) COMMENT '所属域',
    owner           VARCHAR(64) COMMENT '负责人',
    description     TEXT COMMENT '描述',
    properties      JSON COMMENT '扩展属性',
    is_deleted      TINYINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_node (tenant_id, node_id),
    INDEX idx_tenant_type (tenant_id, node_type, is_deleted),
    INDEX idx_domain (tenant_id, domain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血缘节点表';

-- 3. lineage_edge_v2 table
CREATE TABLE IF NOT EXISTS lineage_edge_v2 (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id       BIGINT NOT NULL COMMENT '租户ID',
    source_id       VARCHAR(128) NOT NULL COMMENT '源节点ID',
    source_type     VARCHAR(32) NOT NULL COMMENT '源类型：TABLE|JOB|FIELD|API',
    target_id       VARCHAR(128) NOT NULL COMMENT '目标节点ID',
    target_type     VARCHAR(32) NOT NULL COMMENT '目标类型',
    relation_type   VARCHAR(32) NOT NULL COMMENT '关系类型：QUERIES|POPULATES|CALLS',
    properties      JSON COMMENT '扩展属性',
    version         INT NOT NULL DEFAULT 1 COMMENT '版本号',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(64) COMMENT '创建者',
    INDEX idx_tenant_source (tenant_id, source_id, is_deleted),
    INDEX idx_tenant_target (tenant_id, target_id, is_deleted),
    INDEX idx_tenant_version (tenant_id, version),
    INDEX idx_relation_type (relation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血缘边主表v2';

-- 4. lineage_edge_history table
CREATE TABLE IF NOT EXISTS lineage_edge_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    edge_id         BIGINT NOT NULL COMMENT '关联lineage_edge_v2.id',
    tenant_id       BIGINT NOT NULL,
    source_id       VARCHAR(128) NOT NULL,
    source_type     VARCHAR(32) NOT NULL,
    target_id       VARCHAR(128) NOT NULL,
    target_type     VARCHAR(32) NOT NULL,
    relation_type   VARCHAR(32) NOT NULL,
    properties      JSON,
    version         INT NOT NULL,
    snapshot_at     TIMESTAMP NOT NULL COMMENT '快照时间点',
    INDEX idx_edge_version (edge_id, version),
    INDEX idx_tenant_snapshot (tenant_id, snapshot_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血缘边历史快照';

-- 5. lineage_snapshot table
CREATE TABLE IF NOT EXISTS lineage_snapshot (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id       BIGINT NOT NULL,
    snapshot_name   VARCHAR(128) NOT NULL COMMENT '快照名称',
    snapshot_type   VARCHAR(32) NOT NULL COMMENT 'BRANCH|TAG|MANUAL',
    ref_id          VARCHAR(64) COMMENT '关联引用（如Git commit hash）',
    edge_count      INT NOT NULL DEFAULT 0,
    node_count      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(64),
    INDEX idx_tenant_type (tenant_id, snapshot_type),
    INDEX idx_ref (ref_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血缘版本快照';

-- 6. ALTER analysis_task (idempotent via stored procedure, MySQL 8 doesn't support ADD COLUMN IF NOT EXISTS)
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS AddAnalysisTaskColumns()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'bloodline' AND TABLE_NAME = 'analysis_task' AND COLUMN_NAME = 'lineage_type'
    ) THEN
        ALTER TABLE analysis_task ADD COLUMN lineage_type VARCHAR(32) COMMENT '任务类型：CODE|ETL|OPENLINEAGE';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'bloodline' AND TABLE_NAME = 'analysis_task' AND COLUMN_NAME = 'snapshot_id'
    ) THEN
        ALTER TABLE analysis_task ADD COLUMN snapshot_id BIGINT COMMENT '关联快照ID';
    END IF;
END //
DELIMITER ;

CALL AddAnalysisTaskColumns();

DROP PROCEDURE IF EXISTS AddAnalysisTaskColumns;
