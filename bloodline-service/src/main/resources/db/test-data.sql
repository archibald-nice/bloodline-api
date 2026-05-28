-- Bloodline Test Data
-- Run this after schema.sql to populate the database with ~100 records
-- for a realistic e-commerce microservices lineage graph.

-- Use default tenant
delete from lineage_edge where tenant_id = 'dept_01';
delete from project_app where tenant_id = 'dept_01';
delete from analysis_task where tenant_id = 'dept_01';
delete from application where tenant_id = 'dept_01';
delete from project where tenant_id = 'dept_01';

-- ============================================================
-- 1. APPLICATIONS (20 microservices)
-- ============================================================
INSERT INTO application (tenant_id, app_id, app_name, git_url, default_branch, language) VALUES
('dept_01', 'gateway-service', 'API Gateway', 'https://github.com/acme/gateway-service.git', 'release_sit', 'java'),
('dept_01', 'user-service', 'User Service', 'https://github.com/acme/user-service.git', 'release_sit', 'java'),
('dept_01', 'order-service', 'Order Service', 'https://github.com/acme/order-service.git', 'release_sit', 'java'),
('dept_01', 'payment-service', 'Payment Service', 'https://github.com/acme/payment-service.git', 'release_sit', 'java'),
('dept_01', 'inventory-service', 'Inventory Service', 'https://github.com/acme/inventory-service.git', 'release_sit', 'java'),
('dept_01', 'product-service', 'Product Service', 'https://github.com/acme/product-service.git', 'release_sit', 'java'),
('dept_01', 'cart-service', 'Shopping Cart Service', 'https://github.com/acme/cart-service.git', 'release_sit', 'java'),
('dept_01', 'search-service', 'Search Service', 'https://github.com/acme/search-service.git', 'release_sit', 'java'),
('dept_01', 'recommendation-service', 'Recommendation Engine', 'https://github.com/acme/recommendation-service.git', 'release_sit', 'java'),
('dept_01', 'notification-service', 'Notification Service', 'https://github.com/acme/notification-service.git', 'release_sit', 'java'),
('dept_01', 'logistics-service', 'Logistics Service', 'https://github.com/acme/logistics-service.git', 'release_sit', 'java'),
('dept_01', 'warehouse-service', 'Warehouse Service', 'https://github.com/acme/warehouse-service.git', 'release_sit', 'java'),
('dept_01', 'supplier-service', 'Supplier Service', 'https://github.com/acme/supplier-service.git', 'release_sit', 'java'),
('dept_01', 'review-service', 'Review Service', 'https://github.com/acme/review-service.git', 'release_sit', 'java'),
('dept_01', 'coupon-service', 'Coupon Service', 'https://github.com/acme/coupon-service.git', 'release_sit', 'java'),
('dept_01', 'promotion-service', 'Promotion Service', 'https://github.com/acme/promotion-service.git', 'release_sit', 'java'),
('dept_01', 'member-service', 'Member Service', 'https://github.com/acme/member-service.git', 'release_sit', 'java'),
('dept_01', 'analytics-service', 'Analytics Service', 'https://github.com/acme/analytics-service.git', 'release_sit', 'java'),
('dept_01', 'report-service', 'Report Service', 'https://github.com/acme/report-service.git', 'release_sit', 'java'),
('dept_01', 'admin-service', 'Admin Portal Service', 'https://github.com/acme/admin-service.git', 'release_sit', 'java'),
('dept_01', 'auth-service', 'Authentication Service', 'https://github.com/acme/auth-service.git', 'release_sit', 'java'),
('dept_01', 'price-service', 'Pricing Service', 'https://github.com/acme/price-service.git', 'release_sit', 'java');

-- ============================================================
-- 2. PROJECTS (5 projects)
-- ============================================================
INSERT INTO project (tenant_id, project_code, project_name, baseline_branch, dev_branch, status, created_by) VALUES
('dept_01', 'ECOMMERCE', 'E-Commerce Platform', 'release_sit', 'feature/v2-checkout', 1, 'admin'),
('dept_01', 'PROMO', 'Promotion System', 'release_sit', 'feature/black-friday', 0, 'admin'),
('dept_01', 'LOGISTICS', 'Logistics & Supply Chain', 'release_sit', 'feature/same-day', 2, 'admin'),
('dept_01', 'ANALYTICS', 'Data Analytics Suite', 'release_sit', 'feature/realtime', 1, 'admin'),
('dept_01', 'ADMIN', 'Admin Management Console', 'release_sit', 'feature/rbac-v2', 0, 'admin');

-- ============================================================
-- 3. PROJECT_APP relations
-- ============================================================
INSERT INTO project_app (tenant_id, project_id, app_id) VALUES
('dept_01', 1, 'gateway-service'),
('dept_01', 1, 'user-service'),
('dept_01', 1, 'order-service'),
('dept_01', 1, 'payment-service'),
('dept_01', 1, 'inventory-service'),
('dept_01', 1, 'product-service'),
('dept_01', 1, 'cart-service'),
('dept_01', 1, 'search-service'),
('dept_01', 1, 'review-service'),
('dept_01', 1, 'auth-service'),
('dept_01', 1, 'price-service'),
('dept_01', 2, 'coupon-service'),
('dept_01', 2, 'promotion-service'),
('dept_01', 2, 'product-service'),
('dept_01', 2, 'order-service'),
('dept_01', 3, 'logistics-service'),
('dept_01', 3, 'warehouse-service'),
('dept_01', 3, 'supplier-service'),
('dept_01', 3, 'inventory-service'),
('dept_01', 3, 'order-service'),
('dept_01', 4, 'analytics-service'),
('dept_01', 4, 'report-service'),
('dept_01', 4, 'recommendation-service'),
('dept_01', 4, 'search-service'),
('dept_01', 4, 'user-service'),
('dept_01', 4, 'order-service'),
('dept_01', 5, 'admin-service'),
('dept_01', 5, 'auth-service'),
('dept_01', 5, 'user-service'),
('dept_01', 5, 'member-service');

-- ============================================================
-- 4. ANALYSIS_TASKS (15 tasks)
-- ============================================================
INSERT INTO analysis_task (tenant_id, project_id, app_id, branch, commit_sha, trigger_type, status, result_summary, started_at, completed_at) VALUES
('dept_01', 1, 'gateway-service', 'release_sit', 'a1b2c3d', 1, 2, 'Parsed 42 files, found 18 edges', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY + INTERVAL 5 MINUTE),
('dept_01', 1, 'order-service', 'release_sit', 'e4f5g6h', 1, 2, 'Parsed 128 files, found 35 edges', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY + INTERVAL 12 MINUTE),
('dept_01', 1, 'user-service', 'release_sit', 'i7j8k9l', 2, 2, 'Parsed 86 files, found 22 edges', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 8 MINUTE),
('dept_01', 1, 'product-service', 'release_sit', 'm0n1o2p', 1, 3, 'Parse error at ProductMapper.xml:47', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 2 MINUTE),
('dept_01', 2, 'promotion-service', 'feature/black-friday', 'q3r4s5t', 2, 1, NULL, NOW() - INTERVAL 30 MINUTE, NULL),
('dept_01', 2, 'coupon-service', 'feature/black-friday', 'u6v7w8x', 2, 0, NULL, NULL, NULL),
('dept_01', 3, 'logistics-service', 'release_sit', 'y9z0a1b', 1, 2, 'Parsed 95 files, found 28 edges', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 10 MINUTE),
('dept_01', 3, 'warehouse-service', 'release_sit', 'c2d3e4f', 1, 2, 'Parsed 67 files, found 19 edges', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 7 MINUTE),
('dept_01', 4, 'analytics-service', 'feature/realtime', 'g5h6i7j', 2, 4, 'Timeout after 600s', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY + INTERVAL 600 SECOND),
('dept_01', 4, 'recommendation-service', 'release_sit', 'k8l9m0n', 3, 2, 'Parsed 112 files, found 31 edges', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY + INTERVAL 15 MINUTE),
('dept_01', 4, 'report-service', 'release_sit', 'o1p2q3r', 3, 2, 'Parsed 54 files, found 14 edges', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY + INTERVAL 6 MINUTE),
('dept_01', 5, 'admin-service', 'feature/rbac-v2', 's4t5u6v', 2, 1, NULL, NOW() - INTERVAL 10 MINUTE, NULL),
('dept_01', 1, 'cart-service', 'release_sit', 'w7x8y9z', 1, 2, 'Parsed 48 files, found 16 edges', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY + INTERVAL 5 MINUTE),
('dept_01', 1, 'search-service', 'release_sit', 'a2b3c4d', 1, 2, 'Parsed 73 files, found 24 edges', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY + INTERVAL 9 MINUTE),
('dept_01', 1, 'payment-service', 'release_sit', 'e5f6g7h', 1, 2, 'Parsed 91 files, found 27 edges', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY + INTERVAL 11 MINUTE);

-- ============================================================
-- 5. LINEAGE_EDGES (service call chains & DB dependencies)
-- ============================================================

-- Gateway upstream calls
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'gateway-service', 'user-service', 'SERVICE', 'user-service', 'Dubbo @Reference UserApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'gateway-service', 'order-service', 'SERVICE', 'order-service', 'Dubbo @Reference OrderApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'gateway-service', 'product-service', 'SERVICE', 'product-service', 'Dubbo @Reference ProductApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'gateway-service', 'cart-service', 'SERVICE', 'cart-service', 'Dubbo @Reference CartApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'gateway-service', 'search-service', 'SERVICE', 'search-service', 'Dubbo @Reference SearchApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'gateway-service', 'auth-service', 'SERVICE', 'auth-service', 'Dubbo @Reference AuthApi', 'CALLS', 'release_sit', 1.00);

-- User service dependencies
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'user-service', NULL, 'TABLE', 't_user', 'SELECT * FROM t_user WHERE id = ?', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'user-service', NULL, 'TABLE', 't_user_address', 'INSERT INTO t_user_address', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'user-service', NULL, 'TABLE', 't_user_profile', 'UPDATE t_user_profile', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'user-service', 'auth-service', 'SERVICE', 'auth-service', 'Feign validateToken', 'HTTP_CALLS', 'release_sit', 1.00),
('dept_01', 'user-service', 'member-service', 'SERVICE', 'member-service', 'Dubbo @Reference MemberApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'user-service', 'notification-service', 'SERVICE', 'notification-service', 'Dubbo @Reference NotifyApi', 'CALLS', 'release_sit', 0.95);

-- Auth service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'auth-service', NULL, 'TABLE', 't_auth_token', 'SELECT token FROM t_auth_token', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'auth-service', NULL, 'TABLE', 't_auth_permission', 'JOIN t_auth_permission', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'auth-service', 'user-service', 'SERVICE', 'user-service', 'Feign getUserById', 'HTTP_CALLS', 'release_sit', 1.00);

-- Member service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'member-service', NULL, 'TABLE', 't_member_level', 'SELECT level FROM t_member_level', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'member-service', NULL, 'TABLE', 't_member_points', 'UPDATE t_member_points', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'member-service', 'user-service', 'SERVICE', 'user-service', 'Dubbo @Reference UserApi', 'CALLS', 'release_sit', 1.00);

-- Order service dependencies
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'order-service', NULL, 'TABLE', 't_order', 'INSERT INTO t_order', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'order-service', NULL, 'TABLE', 't_order_item', 'INSERT INTO t_order_item', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'order-service', NULL, 'TABLE', 't_order_status_log', 'INSERT INTO t_order_status_log', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'order-service', 'user-service', 'SERVICE', 'user-service', 'Dubbo @Reference UserApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'order-service', 'inventory-service', 'SERVICE', 'inventory-service', 'Dubbo @Reference InventoryApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'order-service', 'payment-service', 'SERVICE', 'payment-service', 'Dubbo @Reference PaymentApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'order-service', 'coupon-service', 'SERVICE', 'coupon-service', 'Feign validateCoupon', 'HTTP_CALLS', 'release_sit', 1.00),
('dept_01', 'order-service', 'price-service', 'SERVICE', 'price-service', 'Dubbo @Reference PriceApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'order-service', 'promotion-service', 'SERVICE', 'promotion-service', 'Feign applyPromotion', 'HTTP_CALLS', 'release_sit', 0.90);

-- Payment service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'payment-service', NULL, 'TABLE', 't_payment', 'INSERT INTO t_payment', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'payment-service', NULL, 'TABLE', 't_payment_channel', 'SELECT * FROM t_payment_channel', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'payment-service', NULL, 'TABLE', 't_refund', 'INSERT INTO t_refund', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'payment-service', 'order-service', 'SERVICE', 'order-service', 'Dubbo @Reference OrderApi.updateStatus', 'CALLS', 'release_sit', 1.00),
('dept_01', 'payment-service', 'notification-service', 'SERVICE', 'notification-service', 'Dubbo @Reference NotifyApi', 'CALLS', 'release_sit', 0.95);

-- Inventory service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'inventory-service', NULL, 'TABLE', 't_inventory', 'UPDATE t_inventory SET stock', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'inventory-service', NULL, 'TABLE', 't_inventory_log', 'INSERT INTO t_inventory_log', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'inventory-service', NULL, 'TABLE', 't_sku', 'SELECT sku FROM t_sku', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'inventory-service', 'warehouse-service', 'SERVICE', 'warehouse-service', 'Dubbo @Reference WarehouseApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'inventory-service', 'product-service', 'SERVICE', 'product-service', 'Dubbo @Reference ProductApi', 'CALLS', 'release_sit', 1.00);

-- Product service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'product-service', NULL, 'TABLE', 't_product', 'SELECT * FROM t_product', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'product-service', NULL, 'TABLE', 't_product_category', 'JOIN t_product_category', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'product-service', NULL, 'TABLE', 't_product_spu', 'SELECT spu FROM t_product_spu', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'product-service', 'inventory-service', 'SERVICE', 'inventory-service', 'Dubbo @Reference InventoryApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'product-service', 'price-service', 'SERVICE', 'price-service', 'Dubbo @Reference PriceApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'product-service', 'search-service', 'SERVICE', 'search-service', 'Dubbo @Reference SearchApi.indexProduct', 'CALLS', 'release_sit', 0.85);

-- Cart service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'cart-service', NULL, 'TABLE', 't_cart', 'SELECT * FROM t_cart WHERE user_id', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'cart-service', NULL, 'TABLE', 't_cart_item', 'INSERT INTO t_cart_item', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'cart-service', 'product-service', 'SERVICE', 'product-service', 'Dubbo @Reference ProductApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'cart-service', 'price-service', 'SERVICE', 'price-service', 'Dubbo @Reference PriceApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'cart-service', 'inventory-service', 'SERVICE', 'inventory-service', 'Dubbo @Reference InventoryApi.checkStock', 'CALLS', 'release_sit', 1.00),
('dept_01', 'cart-service', 'coupon-service', 'SERVICE', 'coupon-service', 'Feign listAvailableCoupons', 'HTTP_CALLS', 'release_sit', 0.90);

-- Search service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'search-service', NULL, 'TABLE', 't_search_index', 'UPDATE t_search_index', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'search-service', 'product-service', 'SERVICE', 'product-service', 'Dubbo @Reference ProductApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'search-service', 'recommendation-service', 'SERVICE', 'recommendation-service', 'Dubbo @Reference RecommendApi', 'CALLS', 'release_sit', 0.85);

-- Recommendation service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'recommendation-service', NULL, 'TABLE', 't_user_behavior', 'SELECT behavior FROM t_user_behavior', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'recommendation-service', NULL, 'TABLE', 't_product_embedding', 'SELECT embedding FROM t_product_embedding', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'recommendation-service', 'user-service', 'SERVICE', 'user-service', 'Dubbo @Reference UserApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'recommendation-service', 'product-service', 'SERVICE', 'product-service', 'Dubbo @Reference ProductApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'recommendation-service', 'analytics-service', 'SERVICE', 'analytics-service', 'Dubbo @Reference AnalyticsApi.trackEvent', 'CALLS', 'release_sit', 0.80);

-- Notification service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'notification-service', NULL, 'TABLE', 't_notification', 'INSERT INTO t_notification', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'notification-service', NULL, 'TABLE', 't_notification_template', 'SELECT template FROM t_notification_template', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'notification-service', 'user-service', 'SERVICE', 'user-service', 'Dubbo @Reference UserApi.getContact', 'CALLS', 'release_sit', 1.00);

-- Logistics service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'logistics-service', NULL, 'TABLE', 't_shipment', 'INSERT INTO t_shipment', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'logistics-service', NULL, 'TABLE', 't_delivery_route', 'SELECT route FROM t_delivery_route', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'logistics-service', 'order-service', 'SERVICE', 'order-service', 'Dubbo @Reference OrderApi.updateStatus', 'CALLS', 'release_sit', 1.00),
('dept_01', 'logistics-service', 'warehouse-service', 'SERVICE', 'warehouse-service', 'Dubbo @Reference WarehouseApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'logistics-service', 'notification-service', 'SERVICE', 'notification-service', 'Dubbo @Reference NotifyApi.sendDelivery', 'CALLS', 'release_sit', 0.95);

-- Warehouse service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'warehouse-service', NULL, 'TABLE', 't_warehouse', 'SELECT * FROM t_warehouse', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'warehouse-service', NULL, 'TABLE', 't_stock_allocation', 'UPDATE t_stock_allocation', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'warehouse-service', 'inventory-service', 'SERVICE', 'inventory-service', 'Dubbo @Reference InventoryApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'warehouse-service', 'supplier-service', 'SERVICE', 'supplier-service', 'Dubbo @Reference SupplierApi', 'CALLS', 'release_sit', 1.00);

-- Supplier service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'supplier-service', NULL, 'TABLE', 't_supplier', 'SELECT * FROM t_supplier', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'supplier-service', NULL, 'TABLE', 't_purchase_order', 'INSERT INTO t_purchase_order', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'supplier-service', 'warehouse-service', 'SERVICE', 'warehouse-service', 'Dubbo @Reference WarehouseApi', 'CALLS', 'release_sit', 1.00);

-- Review service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'review-service', NULL, 'TABLE', 't_review', 'INSERT INTO t_review', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'review-service', NULL, 'TABLE', 't_review_image', 'INSERT INTO t_review_image', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'review-service', 'user-service', 'SERVICE', 'user-service', 'Dubbo @Reference UserApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'review-service', 'product-service', 'SERVICE', 'product-service', 'Dubbo @Reference ProductApi.updateRating', 'CALLS', 'release_sit', 1.00),
('dept_01', 'review-service', 'analytics-service', 'SERVICE', 'analytics-service', 'Dubbo @Reference AnalyticsApi.trackReview', 'CALLS', 'release_sit', 0.80);

-- Coupon service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'coupon-service', NULL, 'TABLE', 't_coupon', 'SELECT * FROM t_coupon', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'coupon-service', NULL, 'TABLE', 't_coupon_usage', 'INSERT INTO t_coupon_usage', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'coupon-service', 'user-service', 'SERVICE', 'user-service', 'Dubbo @Reference UserApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'coupon-service', 'promotion-service', 'SERVICE', 'promotion-service', 'Dubbo @Reference PromotionApi', 'CALLS', 'release_sit', 1.00);

-- Promotion service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'promotion-service', NULL, 'TABLE', 't_promotion', 'SELECT * FROM t_promotion', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'promotion-service', NULL, 'TABLE', 't_promotion_rule', 'SELECT rule FROM t_promotion_rule', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'promotion-service', 'product-service', 'SERVICE', 'product-service', 'Dubbo @Reference ProductApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'promotion-service', 'price-service', 'SERVICE', 'price-service', 'Dubbo @Reference PriceApi', 'CALLS', 'release_sit', 1.00);

-- Price service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'price-service', NULL, 'TABLE', 't_price', 'SELECT price FROM t_price', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'price-service', NULL, 'TABLE', 't_price_history', 'INSERT INTO t_price_history', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'price-service', 'product-service', 'SERVICE', 'product-service', 'Dubbo @Reference ProductApi', 'CALLS', 'release_sit', 1.00);

-- Analytics service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'analytics-service', NULL, 'TABLE', 't_event', 'INSERT INTO t_event', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'analytics-service', NULL, 'TABLE', 't_event_aggregation', 'SELECT * FROM t_event_aggregation', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'analytics-service', 'user-service', 'SERVICE', 'user-service', 'Dubbo @Reference UserApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'analytics-service', 'order-service', 'SERVICE', 'order-service', 'Dubbo @Reference OrderApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'analytics-service', 'report-service', 'SERVICE', 'report-service', 'Dubbo @Reference ReportApi', 'CALLS', 'release_sit', 0.85);

-- Report service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'report-service', NULL, 'TABLE', 't_report', 'INSERT INTO t_report', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'report-service', NULL, 'TABLE', 't_report_schedule', 'SELECT schedule FROM t_report_schedule', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'report-service', 'analytics-service', 'SERVICE', 'analytics-service', 'Dubbo @Reference AnalyticsApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'report-service', 'order-service', 'SERVICE', 'order-service', 'Dubbo @Reference OrderApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'report-service', 'product-service', 'SERVICE', 'product-service', 'Dubbo @Reference ProductApi', 'CALLS', 'release_sit', 1.00);

-- Admin service
INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, confidence) VALUES
('dept_01', 'admin-service', NULL, 'TABLE', 't_admin_operation_log', 'INSERT INTO t_admin_operation_log', 'QUERIES', 'release_sit', 1.00),
('dept_01', 'admin-service', 'auth-service', 'SERVICE', 'auth-service', 'Dubbo @Reference AuthApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'admin-service', 'user-service', 'SERVICE', 'user-service', 'Dubbo @Reference UserApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'admin-service', 'order-service', 'SERVICE', 'order-service', 'Dubbo @Reference OrderApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'admin-service', 'product-service', 'SERVICE', 'product-service', 'Dubbo @Reference ProductApi', 'CALLS', 'release_sit', 1.00),
('dept_01', 'admin-service', 'report-service', 'SERVICE', 'report-service', 'Dubbo @Reference ReportApi', 'CALLS', 'release_sit', 1.00);

-- Field-level lineage sample data
INSERT INTO lineage_column_ref (app_id, table_name, column_name, sql_signature, sql_preview, operation_type, operation_detail, source_location) VALUES
('order-service', 'orders', 'order_id', 'f001', 'SELECT order_id, user_id, amount, status FROM orders', 'SELECT', 'READ', 'OrderMapper.java:15'),
('order-service', 'orders', 'user_id', 'f001', 'SELECT order_id, user_id, amount, status FROM orders', 'SELECT', 'READ', 'OrderMapper.java:15'),
('order-service', 'orders', 'amount', 'f001', 'SELECT order_id, user_id, amount, status FROM orders', 'SELECT', 'READ', 'OrderMapper.java:15'),
('order-service', 'orders', 'status', 'f001', 'SELECT order_id, user_id, amount, status FROM orders', 'SELECT', 'READ', 'OrderMapper.java:15'),
('order-service', 'orders', 'amount', 'f002', 'UPDATE orders SET status = ? WHERE amount > 100', 'UPDATE', 'WHERE', 'OrderMapper.java:32'),
('order-service', 'orders', 'status', 'f003', 'INSERT INTO orders (order_id, user_id, amount, status) VALUES (?, ?, ?, ?)', 'INSERT', 'WRITE', 'OrderMapper.java:48'),
('price-service', 'orders', 'amount', 'f001', 'SELECT order_id, user_id, amount, status FROM orders', 'SELECT', 'READ', 'PriceMapper.java:22'),
('price-service', 'orders', 'discount_rate', 'f004', 'SELECT amount, discount_rate FROM orders WHERE status = 1', 'SELECT', 'READ', 'PriceMapper.java:38'),
('price-service', 'orders', 'amount', 'f004', 'SELECT amount, discount_rate FROM orders WHERE status = 1', 'SELECT', 'READ', 'PriceMapper.java:38'),
('price-service', 'orders', 'status', 'f004', 'SELECT amount, discount_rate FROM orders WHERE status = 1', 'SELECT', 'WHERE', 'PriceMapper.java:38'),
('inventory-service', 'orders', 'status', 'f005', 'SELECT status, COUNT(*) FROM orders GROUP BY status', 'SELECT', 'READ', 'InventoryMapper.java:10'),
('report-service', 'report', 'total_amount', 'f006', 'INSERT INTO report (user_id, total_amount) SELECT user_id, SUM(amount) FROM orders GROUP BY user_id', 'INSERT', 'WRITE', 'ReportMapper.java:55'),
('report-service', 'orders', 'user_id', 'f006', 'INSERT INTO report (user_id, total_amount) SELECT user_id, SUM(amount) FROM orders GROUP BY user_id', 'SELECT', 'READ', 'ReportMapper.java:55'),
('report-service', 'orders', 'amount', 'f006', 'INSERT INTO report (user_id, total_amount) SELECT user_id, SUM(amount) FROM orders GROUP BY user_id', 'SELECT', 'READ', 'ReportMapper.java:55');
