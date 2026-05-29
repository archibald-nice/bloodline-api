-- ============================================
-- V1 → V2 Data Migration
-- Migrate existing app-level lineage edges to generic node-level model
-- ============================================

-- Prerequisites:
-- 1. Default tenant must exist: INSERT INTO tenant (tenant_code, tenant_name, status) VALUES ('dept_01', 'Default Department', 1);
-- 2. All new v2 tables must be created (schema-v2-changes.sql applied)

SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------
-- Step 1: Migrate lineage_edge → lineage_edge_v2
-- Mapping:
--   source_app_id  → source_id (as-is, treated as node id)
--   target_app_id  → target_id (as-is, treated as node id)
--   relation_type  → relation_type (preserved)
--   tenant_id      → 1 (default tenant during migration)
--   version        → 1
--   is_deleted     → 0
-- --------------------------------------------

INSERT INTO lineage_edge_v2 (
    tenant_id, source_id, source_type, target_id, target_type,
    relation_type, properties, version, is_deleted, created_at, updated_at, created_by
)
SELECT
    1 AS tenant_id,
    le.source_app_id AS source_id,
    'TABLE' AS source_type,  -- Best-effort: legacy edges are mostly table/service level
    le.target_app_id AS target_id,
    'TABLE' AS target_type,
    le.relation_type,
    JSON_OBJECT('legacyEdgeId', le.id, 'migratedAt', NOW()) AS properties,
    1 AS version,
    0 AS is_deleted,
    le.created_at,
    le.updated_at,
    'migration' AS created_by
FROM lineage_edge le
WHERE le.is_deleted = 0;

-- --------------------------------------------
-- Step 2: Create lineage_node entries for all unique source/target nodes
-- This ensures node-level queries work correctly
-- --------------------------------------------

INSERT INTO lineage_node (tenant_id, node_id, node_type, node_name, domain, is_deleted, created_at, updated_at)
SELECT DISTINCT
    1 AS tenant_id,
    e.source_id AS node_id,
    e.source_type AS node_type,
    e.source_id AS node_name,
    NULL AS domain,
    0 AS is_deleted,
    NOW() AS created_at,
    NOW() AS updated_at
FROM lineage_edge_v2 e
WHERE e.tenant_id = 1
  AND NOT EXISTS (
      SELECT 1 FROM lineage_node n WHERE n.tenant_id = 1 AND n.node_id = e.source_id
  );

INSERT INTO lineage_node (tenant_id, node_id, node_type, node_name, domain, is_deleted, created_at, updated_at)
SELECT DISTINCT
    1 AS tenant_id,
    e.target_id AS node_id,
    e.target_type AS node_type,
    e.target_id AS node_name,
    NULL AS domain,
    0 AS is_deleted,
    NOW() AS created_at,
    NOW() AS updated_at
FROM lineage_edge_v2 e
WHERE e.tenant_id = 1
  AND NOT EXISTS (
      SELECT 1 FROM lineage_node n WHERE n.tenant_id = 1 AND n.node_id = e.target_id
  );

-- --------------------------------------------
-- Step 3: Migrate existing analysis_task lineage_type and snapshot_id
-- These columns are nullable, so no data transformation needed
-- --------------------------------------------

-- The ALTER TABLE in schema-v2-changes.sql already added the columns with defaults.
-- If you need to backfill lineage_type based on existing data patterns:
-- UPDATE analysis_task SET lineage_type = 'CODE' WHERE snapshot_id IS NULL;

-- --------------------------------------------
-- Step 4: Create initial snapshot for migration baseline
-- This captures the migrated state for future conflict analysis
-- --------------------------------------------

INSERT INTO lineage_snapshot (tenant_id, snapshot_name, snapshot_type, ref_id, edge_count, node_count, edges_data, created_by)
SELECT
    1 AS tenant_id,
    'v1-migration-baseline' AS snapshot_name,
    'MANUAL' AS snapshot_type,
    CONCAT('migration-', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s')) AS ref_id,
    (SELECT COUNT(*) FROM lineage_edge_v2 WHERE tenant_id = 1 AND is_deleted = 0) AS edge_count,
    (SELECT COUNT(*) FROM lineage_node WHERE tenant_id = 1 AND is_deleted = 0) AS node_count,
    (SELECT JSON_ARRAYAGG(
        JSON_OBJECT('sourceId', source_id, 'targetId', target_id, 'relationType', relation_type)
    ) FROM lineage_edge_v2 WHERE tenant_id = 1 AND is_deleted = 0) AS edges_data,
    'migration' AS created_by;

SET FOREIGN_KEY_CHECKS = 1;
