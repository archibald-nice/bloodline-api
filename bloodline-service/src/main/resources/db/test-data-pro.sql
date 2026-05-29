-- ============================================================
-- Bloodline Pro Test Data Generator
-- Generates: 100 apps, ~2000 tables, ~46,000 column refs
-- Usage: mysql -u root -p bloodline < test-data-pro.sql
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELIMITER $$

DROP PROCEDURE IF EXISTS generate_pro_test_data$$

CREATE PROCEDURE generate_pro_test_data()
BEGIN
    DECLARE v_app_idx INT DEFAULT 1;
    DECLARE v_table_idx INT DEFAULT 1;
    DECLARE v_col_idx INT DEFAULT 1;
    DECLARE v_ref_idx INT DEFAULT 1;
    DECLARE v_app_id VARCHAR(64);
    DECLARE v_table_name VARCHAR(128);
    DECLARE v_col_name VARCHAR(128);
    DECLARE v_sql_sig VARCHAR(64);
    DECLARE v_num_cols INT;
    DECLARE v_op_type VARCHAR(16);
    DECLARE v_op_detail VARCHAR(32);
    DECLARE v_sql_preview VARCHAR(512);
    DECLARE v_source_loc VARCHAR(256);
    DECLARE v_target_app_idx INT;
    DECLARE v_target_table_idx INT;
    DECLARE v_target_table_name VARCHAR(128);
    DECLARE v_target_col_name VARCHAR(128);
    DECLARE v_cross_ref_count INT;
    DECLARE v_total_cols INT DEFAULT 0;

    -- Table name pool (20 distinct business domains)
    DECLARE v_table_suffix VARCHAR(64);

    -- Cleanup existing pro test data
    DELETE FROM lineage_column_ref WHERE app_id LIKE 'pro_app_%';
    DELETE FROM application WHERE tenant_id = 'dept_01' AND app_id LIKE 'pro_app_%';

    -- ============================================================
    -- Phase 1: Generate 100 Applications
    -- ============================================================
    SET v_app_idx = 1;
    app_loop: WHILE v_app_idx <= 100 DO
        SET v_app_id = CONCAT('pro_app_', LPAD(v_app_idx, 3, '0'));

        INSERT INTO application (tenant_id, app_id, app_name, git_url, default_branch, language)
        VALUES ('dept_01', v_app_id,
                CONCAT('ProService-', v_app_idx),
                CONCAT('https://github.com/company/', v_app_id, '.git'),
                'release_sit',
                'java');

        SET v_app_idx = v_app_idx + 1;
    END WHILE app_loop;

    -- ============================================================
    -- Phase 2: Generate Tables & Columns (self-owned)
    -- Each app owns 20 tables
    -- 90% tables: 20 cols, 10% tables: 50 cols
    -- ============================================================
    SET v_app_idx = 1;
    app_table_loop: WHILE v_app_idx <= 100 DO
        SET v_app_id = CONCAT('pro_app_', LPAD(v_app_idx, 3, '0'));
        SET v_table_idx = 1;

        table_loop: WHILE v_table_idx <= 20 DO
            -- Pick table suffix from 20 business domains
            SET v_table_suffix = ELT(v_table_idx,
                'orders', 'users', 'products', 'payments',
                'inventory', 'shipments', 'notifications', 'audit_logs',
                'configurations', 'reports', 'categories', 'suppliers',
                'customers', 'addresses', 'coupons', 'reviews',
                'refunds', 'invoices', 'transactions', 'balances'
            );

            SET v_table_name = CONCAT(v_app_id, '_', v_table_suffix);

            -- 10% tables (every 10th) have 50 columns
            IF v_table_idx % 10 = 0 THEN
                SET v_num_cols = 50;
            ELSE
                SET v_num_cols = 20;
            END IF;

            SET v_col_idx = 1;
            col_loop: WHILE v_col_idx <= v_num_cols DO
                SET v_col_name = CASE v_col_idx
                    WHEN 1 THEN 'id'
                    WHEN 2 THEN 'created_at'
                    WHEN 3 THEN 'updated_at'
                    WHEN 4 THEN 'created_by'
                    WHEN 5 THEN 'updated_by'
                    WHEN 6 THEN 'deleted'
                    WHEN 7 THEN 'tenant_id'
                    WHEN 8 THEN 'version'
                    ELSE CONCAT('field_', LPAD(v_col_idx - 8, 3, '0'))
                END;

                SET v_sql_sig = SUBSTRING(MD5(CONCAT(v_app_id, v_table_name, v_col_name,
                    v_table_idx, v_col_idx, RAND())), 1, 32);

                SET v_op_type = ELT((v_col_idx % 4) + 1, 'SELECT', 'INSERT', 'UPDATE', 'DELETE');
                SET v_op_detail = ELT((v_col_idx % 5) + 1, 'READ', 'WRITE', 'WHERE', 'JOIN', 'ORDER_BY');

                SET v_sql_preview = CASE v_op_type
                    WHEN 'SELECT' THEN CONCAT('SELECT ', v_col_name, ' FROM ', v_table_name, ' WHERE id = ?')
                    WHEN 'INSERT' THEN CONCAT('INSERT INTO ', v_table_name, ' (', v_col_name, ') VALUES (?)')
                    WHEN 'UPDATE' THEN CONCAT('UPDATE ', v_table_name, ' SET ', v_col_name, ' = ? WHERE id = ?')
                    ELSE CONCAT('DELETE FROM ', v_table_name, ' WHERE ', v_col_name, ' = ?')
                END;

                SET v_source_loc = CONCAT(v_app_id, '/mapper/', v_table_suffix, 'Mapper.xml:', v_col_idx * 10 + 5);

                INSERT INTO lineage_column_ref
                    (app_id, table_name, column_name, sql_signature, sql_preview,
                     operation_type, operation_detail, source_location)
                VALUES (v_app_id, v_table_name, v_col_name, v_sql_sig, v_sql_preview,
                        v_op_type, v_op_detail, v_source_loc);

                SET v_total_cols = v_total_cols + 1;
                SET v_col_idx = v_col_idx + 1;
            END WHILE col_loop;

            SET v_table_idx = v_table_idx + 1;
        END WHILE table_loop;

        SET v_app_idx = v_app_idx + 1;
    END WHILE app_table_loop;

    -- ============================================================
    -- Phase 3: Cross-application references
    -- Each app references ~50 columns from other apps (10% of avg)
    -- This simulates real-world microservice inter-dependencies
    -- ============================================================
    SET v_app_idx = 1;
    cross_loop: WHILE v_app_idx <= 100 DO
        SET v_app_id = CONCAT('pro_app_', LPAD(v_app_idx, 3, '0'));
        SET v_ref_idx = 1;
        SET v_cross_ref_count = 50;

        cross_ref_loop: WHILE v_ref_idx <= v_cross_ref_count DO
            -- Pick a random different app
            SET v_target_app_idx = FLOOR(1 + RAND() * 100);
            IF v_target_app_idx = v_app_idx THEN
                SET v_target_app_idx = IF(v_target_app_idx = 100, 1, v_target_app_idx + 1);
            END IF;

            SET v_target_table_idx = FLOOR(1 + RAND() * 20);
            SET v_target_table_name = CONCAT('pro_app_', LPAD(v_target_app_idx, 3, '0'), '_',
                ELT(v_target_table_idx,
                    'orders', 'users', 'products', 'payments',
                    'inventory', 'shipments', 'notifications', 'audit_logs',
                    'configurations', 'reports', 'categories', 'suppliers',
                    'customers', 'addresses', 'coupons', 'reviews',
                    'refunds', 'invoices', 'transactions', 'balances'
                )
            );

            SET v_target_col_name = ELT(FLOOR(1 + RAND() * 8),
                'id', 'created_at', 'updated_at', 'created_by',
                'updated_by', 'deleted', 'tenant_id', 'status'
            );

            SET v_sql_sig = SUBSTRING(MD5(CONCAT(v_app_id, v_target_table_name, v_target_col_name,
                v_ref_idx, RAND())), 1, 32);

            SET v_op_type = ELT((v_ref_idx % 4) + 1, 'SELECT', 'INSERT', 'UPDATE', 'DELETE');
            SET v_op_detail = 'READ';

            SET v_sql_preview = CONCAT('SELECT * FROM ', v_target_table_name,
                ' WHERE ', v_target_col_name, ' = ?');
            SET v_source_loc = CONCAT(v_app_id, '/service/FeignClient.java:', v_ref_idx * 5);

            INSERT IGNORE INTO lineage_column_ref
                (app_id, table_name, column_name, sql_signature, sql_preview,
                 operation_type, operation_detail, source_location)
            VALUES (v_app_id, v_target_table_name, v_target_col_name, v_sql_sig, v_sql_preview,
                    v_op_type, v_op_detail, v_source_loc);

            SET v_ref_idx = v_ref_idx + 1;
        END WHILE cross_ref_loop;

        SET v_app_idx = v_app_idx + 1;
    END WHILE cross_loop;

    -- ============================================================
    -- Summary
    -- ============================================================
    SELECT CONCAT('Applications: ', COUNT(*)) AS count FROM application WHERE tenant_id = 'dept_01' AND app_id LIKE 'pro_app_%';
    SELECT CONCAT('Distinct Tables: ', COUNT(DISTINCT table_name)) AS count FROM lineage_column_ref WHERE app_id LIKE 'pro_app_%';
    SELECT CONCAT('Distinct Columns: ', COUNT(DISTINCT CONCAT(table_name, '.', column_name))) AS count FROM lineage_column_ref WHERE app_id LIKE 'pro_app_%';
    SELECT CONCAT('Total Column References: ', COUNT(*)) AS count FROM lineage_column_ref WHERE app_id LIKE 'pro_app_%';

END$$

DELIMITER ;

-- Execute
CALL generate_pro_test_data();

-- Clean up
DROP PROCEDURE IF EXISTS generate_pro_test_data;

SET FOREIGN_KEY_CHECKS = 1;
