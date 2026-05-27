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
