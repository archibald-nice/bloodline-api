-- ============================================
-- V2 Mock Data for Frontend Verification
-- Scenario: E-commerce data warehouse lineage
-- ============================================

-- Prerequisites: default tenant (id=1) must exist
-- Run after: schema-v2-changes.sql

SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------
-- Tenants (for TenantManager page)
-- --------------------------------------------
INSERT IGNORE INTO tenant (id, tenant_code, tenant_name, status, config) VALUES
(1, 'dept_01', 'Data Platform Team', 1, '{"region":"cn-north"}'),
(2, 'dept_02', 'BI Analytics Team', 1, '{"region":"cn-south"}'),
(3, 'dept_03', 'Archived Team', 0, NULL);

-- --------------------------------------------
-- Lineage Nodes (for LineageGraphV2 page)
-- --------------------------------------------
INSERT IGNORE INTO lineage_node (tenant_id, node_id, node_type, node_name, domain, owner, description, properties) VALUES
-- Source tables (tenant 1)
(1, 'warehouse:orders', 'TABLE', 'orders', 'warehouse', 'alice', 'Order transaction table', '{"db":"mysql","rows":"10M"}'),
(1, 'warehouse:users',  'TABLE', 'users',  'warehouse', 'alice', 'User profile table',      '{"db":"mysql","rows":"5M"}'),
(1, 'warehouse:products','TABLE','products','warehouse','bob',  'Product catalog table',   '{"db":"mysql","rows":"1M"}'),
-- ETL jobs (tenant 1)
(1, 'etl:daily-etl',    'JOB',   'daily-etl',    'etl', 'charlie', 'Daily ETL aggregation job', '{"schedule":"0 2 * * *"}'),
(1, 'etl:hourly-report','JOB',   'hourly-report','etl', 'charlie', 'Hourly report generation',  '{"schedule":"0 * * * *"}'),
-- Result tables (tenant 1)
(1, 'warehouse:orders_summary', 'TABLE', 'orders_summary', 'warehouse', 'bob', 'Aggregated order stats', '{"db":"clickhouse"}'),
(1, 'warehouse:daily_report',   'TABLE', 'daily_report',   'warehouse', 'bob', 'Daily business report',  '{"db":"clickhouse"}'),
-- Tenant 2 nodes
(2, 'bi:user_behavior', 'TABLE', 'user_behavior', 'bi', 'dave', 'User behavior analysis', NULL),
(2, 'bi:conversion_funnel', 'TABLE', 'conversion_funnel', 'bi', 'dave', 'Conversion funnel', NULL);

-- --------------------------------------------
-- Lineage Edges V2 (for LineageGraphV2 page)
-- --------------------------------------------
-- Chain: orders -> etl-daily -> orders_summary -> etl-hourly -> daily_report
--        users  -> etl-daily
--        products -> etl-daily
INSERT IGNORE INTO lineage_edge_v2 (tenant_id, source_id, source_type, target_id, target_type, relation_type, properties, version, is_deleted, created_by) VALUES
(1, 'warehouse:orders',   'TABLE', 'etl:daily-etl',     'JOB', 'QUERIES',   NULL, 1, 0, 'mock'),
(1, 'warehouse:users',    'TABLE', 'etl:daily-etl',     'JOB', 'QUERIES',   NULL, 1, 0, 'mock'),
(1, 'warehouse:products', 'TABLE', 'etl:daily-etl',     'JOB', 'QUERIES',   NULL, 1, 0, 'mock'),
(1, 'etl:daily-etl',      'JOB',   'warehouse:orders_summary', 'TABLE', 'POPULATES', NULL, 1, 0, 'mock'),
(1, 'warehouse:orders_summary', 'TABLE', 'etl:hourly-report', 'JOB', 'QUERIES',   NULL, 1, 0, 'mock'),
(1, 'etl:hourly-report',  'JOB',   'warehouse:daily_report',   'TABLE', 'POPULATES', NULL, 1, 0, 'mock'),
-- Tenant 2 edges
(2, 'bi:user_behavior',     'TABLE', 'bi:conversion_funnel', 'TABLE', 'QUERIES', NULL, 1, 0, 'mock');

-- --------------------------------------------
-- Snapshots (for SnapshotManager & ConflictAnalysis pages)
-- Snapshot A (baseline): 6 edges
-- Snapshot B (after change): 5 edges (removed daily_report edge, added a new one)
-- --------------------------------------------
INSERT INTO lineage_snapshot (tenant_id, snapshot_name, snapshot_type, ref_id, edge_count, node_count, edges_data, created_by) VALUES
(1, 'baseline-v1', 'TAG', 'v1.0.0', 6, 7, '[
  {"sourceId":"warehouse:orders","targetId":"etl:daily-etl","relationType":"QUERIES"},
  {"sourceId":"warehouse:users","targetId":"etl:daily-etl","relationType":"QUERIES"},
  {"sourceId":"warehouse:products","targetId":"etl:daily-etl","relationType":"QUERIES"},
  {"sourceId":"etl:daily-etl","targetId":"warehouse:orders_summary","relationType":"POPULATES"},
  {"sourceId":"warehouse:orders_summary","targetId":"etl:hourly-report","relationType":"QUERIES"},
  {"sourceId":"etl:hourly-report","targetId":"warehouse:daily_report","relationType":"POPULATES"}
]', 'mock'),
(1, 'after-refactor-v2', 'BRANCH', 'feat/refactor-2026', 5, 7, '[
  {"sourceId":"warehouse:orders","targetId":"etl:daily-etl","relationType":"QUERIES"},
  {"sourceId":"warehouse:users","targetId":"etl:daily-etl","relationType":"QUERIES"},
  {"sourceId":"warehouse:products","targetId":"etl:daily-etl","relationType":"QUERIES"},
  {"sourceId":"etl:daily-etl","targetId":"warehouse:orders_summary","relationType":"POPULATES"},
  {"sourceId":"warehouse:orders_summary","targetId":"etl:hourly-report","relationType":"QUERIES"}
]', 'mock');

SET FOREIGN_KEY_CHECKS = 1;
