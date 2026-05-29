CREATE TABLE IF NOT EXISTS application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(32) NOT NULL,
    app_id VARCHAR(64) NOT NULL,
    app_name VARCHAR(128),
    git_url VARCHAR(256),
    default_branch VARCHAR(64) DEFAULT 'release_sit',
    language VARCHAR(32) DEFAULT 'java',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_app (tenant_id, app_id),
    INDEX idx_tenant (tenant_id)
);

CREATE TABLE IF NOT EXISTS project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(32) NOT NULL,
    project_code VARCHAR(32) NOT NULL,
    project_name VARCHAR(128),
    baseline_branch VARCHAR(64) DEFAULT 'release_sit',
    dev_branch VARCHAR(64),
    status TINYINT DEFAULT 0 COMMENT '0:dev 1:ST 2:UAT 3:released',
    created_by VARCHAR(32),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_code (tenant_id, project_code),
    INDEX idx_tenant_status (tenant_id, status)
);

CREATE TABLE IF NOT EXISTS analysis_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(32) NOT NULL,
    project_id BIGINT,
    app_id VARCHAR(64) NOT NULL,
    branch VARCHAR(64) NOT NULL,
    commit_sha VARCHAR(64),
    trigger_type TINYINT COMMENT '1:webhook 2:manual 3:scheduled',
    status TINYINT DEFAULT 0 COMMENT '0:pending 1:running 2:completed 3:failed 4:timeout 5:stale',
    scheduler_job_id VARCHAR(64),
    result_summary TEXT,
    error_msg TEXT,
    started_at DATETIME,
    completed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_app (tenant_id, app_id),
    INDEX idx_status_created (status, created_at)
);

CREATE TABLE IF NOT EXISTS lineage_edge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(32) NOT NULL,
    app_id VARCHAR(64) NOT NULL,
    target_app_id VARCHAR(64),
    target_type VARCHAR(32) NOT NULL COMMENT 'SERVICE/TABLE/API_ENDPOINT/DATABASE',
    target_name VARCHAR(128) NOT NULL,
    target_detail VARCHAR(256),
    relation_type VARCHAR(32) NOT NULL COMMENT 'CALLS/HTTP_CALLS/QUERIES/REFERENCES',
    branch VARCHAR(64) NOT NULL DEFAULT 'release_sit',
    project_id VARCHAR(32),
    confidence DECIMAL(3,2) DEFAULT 1.00,
    source_type VARCHAR(16) DEFAULT 'AST' COMMENT 'AST/RUNTIME',
    commit_sha VARCHAR(64),
    detected_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_app (tenant_id, app_id),
    INDEX idx_tenant_target (tenant_id, target_type, target_name),
    INDEX idx_tenant_branch (tenant_id, branch),
    INDEX idx_target_app (tenant_id, target_app_id),
    INDEX idx_project (project_id)
);

CREATE TABLE IF NOT EXISTS project_app (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(32) NOT NULL,
    project_id BIGINT NOT NULL,
    app_id VARCHAR(64) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_project_app (tenant_id, project_id, app_id),
    INDEX idx_project (tenant_id, project_id),
    INDEX idx_app (tenant_id, app_id)
);

CREATE TABLE IF NOT EXISTS lineage_column_ref (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_id VARCHAR(64) NOT NULL,
    table_name VARCHAR(128) NOT NULL,
    column_name VARCHAR(128) NOT NULL,
    sql_signature VARCHAR(64) NOT NULL,
    sql_preview VARCHAR(512),
    operation_type VARCHAR(16) NOT NULL,
    operation_detail VARCHAR(32),
    source_location VARCHAR(256),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_app_table_col (app_id, table_name, column_name),
    INDEX idx_sql_sig (sql_signature),
    INDEX idx_table_col (table_name, column_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Field-level lineage reference records';

-- ============================================================
-- Schema & Index Layer (added 2026-05-29)
-- ============================================================

CREATE TABLE IF NOT EXISTS datasource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(32) NOT NULL,
    app_id VARCHAR(64) NOT NULL,
    datasource_code VARCHAR(64) NOT NULL COMMENT 'Unique code within app',
    datasource_name VARCHAR(128),
    db_type VARCHAR(32) NOT NULL COMMENT 'mysql|oracle|postgresql|hive|clickhouse|tidb',
    db_version VARCHAR(32),
    jdbc_url VARCHAR(512),
    catalog VARCHAR(128) COMMENT 'For PostgreSQL/Greenplum',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_app_code (tenant_id, app_id, datasource_code),
    INDEX idx_app (tenant_id, app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS datasource_schema (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(32) NOT NULL,
    datasource_id BIGINT NOT NULL,
    schema_name VARCHAR(128) NOT NULL COMMENT 'MySQL:database_name, Oracle:user_name',
    schema_alias VARCHAR(128),
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_datasource_schema (datasource_id, schema_name),
    INDEX idx_datasource (datasource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS lineage_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(32) NOT NULL,
    schema_id BIGINT NOT NULL,
    table_name VARCHAR(128) NOT NULL,
    index_name VARCHAR(128) NOT NULL,
    index_type VARCHAR(32) COMMENT 'BTREE|HASH|GIN|GIST|FULLTEXT|BITMAP|PRIMARY',
    is_unique BOOLEAN DEFAULT FALSE,
    is_primary BOOLEAN DEFAULT FALSE,
    index_columns VARCHAR(512) COMMENT 'Comma-separated for display',
    definition TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_schema_table_idx (schema_id, table_name, index_name),
    INDEX idx_schema_table (schema_id, table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS lineage_index_column (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    index_id BIGINT NOT NULL,
    column_name VARCHAR(128) NOT NULL,
    column_order INT DEFAULT 1,
    is_descending BOOLEAN DEFAULT FALSE,
    UNIQUE KEY uk_idx_col (index_id, column_name),
    INDEX idx_column (index_id, column_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add schema_id to lineage_column_ref (nullable for migration)
ALTER TABLE lineage_column_ref
    ADD COLUMN schema_id BIGINT AFTER app_id,
    ADD INDEX idx_schema_table_col (schema_id, table_name, column_name);
