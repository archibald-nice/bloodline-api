#!/usr/bin/env python3
"""
Bloodline 电商业务 Mock 数据生成器
生成真实微服务、真实表结构、真实 SQL 引用关系，用于 MVP Demo
"""

import random
import hashlib
import mysql.connector
from datetime import datetime, timedelta

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'dev123456',
    'database': 'bloodline',
    'charset': 'utf8mb4'
}

def random_sha():
    return hashlib.md5(str(random.random()).encode()).hexdigest()[:16]

def random_time_ago(days_max):
    return datetime.now() - timedelta(days=random.randint(1, days_max))

def sql_signature(sql):
    return hashlib.md5(sql.encode()).hexdigest()[:16]

# ============ 服务定义 ============
SERVICES = [
    {"app_id": "gateway-service",      "app_name": "API网关服务",   "desc": "统一API网关，路由转发、鉴权、限流"},
    {"app_id": "order-service",        "app_name": "订单中心服务",  "desc": "订单生命周期管理：下单、支付、发货、完成"},
    {"app_id": "payment-service",      "app_name": "支付中心服务",  "desc": "支付渠道接入：支付、退款、对账"},
    {"app_id": "inventory-service",    "app_name": "库存服务",      "desc": "库存管理：预占、扣减、释放、补货"},
    {"app_id": "user-service",         "app_name": "用户中心服务",  "desc": "用户注册、登录、信息管理"},
    {"app_id": "logistics-service",    "app_name": "物流服务",      "desc": "物流跟踪，快递公司对接"},
    {"app_id": "product-service",      "app_name": "商品服务",      "desc": "商品、SKU、品类、品牌管理"},
    {"app_id": "cart-service",         "app_name": "购物车服务",    "desc": "加购、改数量、选中、删除"},
    {"app_id": "coupon-service",       "app_name": "优惠券服务",    "desc": "优惠券创建、发放、核销"},
    {"app_id": "notification-service", "app_name": "通知服务",      "desc": "短信、推送、邮件通知"},
    {"app_id": "audit-service",        "app_name": "审计服务",      "desc": "业务操作审计日志"},
    {"app_id": "finance-service",      "app_name": "财务服务",      "desc": "日结、对账、财务报表"},
    {"app_id": "search-service",       "app_name": "搜索服务",      "desc": "商品搜索、搜索日志"},
    {"app_id": "report-service",       "app_name": "报表服务",      "desc": "运营报表、数据导出"},
    {"app_id": "admin-service",        "app_name": "运营后台服务",  "desc": "运营管理后台"},
]

SERVICE_IDS = [s["app_id"] for s in SERVICES]

# ============ 项目定义 ============
PROJECTS = [
    {"code": "ORDER",   "name": "订单交易域",   "dev_branch": "feature/order-v2", "status": 1},
    {"code": "PRODUCT", "name": "商品供应链域", "dev_branch": "feature/product-v2", "status": 1},
    {"code": "COMMON",  "name": "公共服务域",   "dev_branch": None, "status": 2},
]

# 服务归属项目映射
SERVICE_PROJECT = {
    "gateway-service": "COMMON",
    "order-service": "ORDER",
    "payment-service": "ORDER",
    "inventory-service": "PRODUCT",
    "user-service": "COMMON",
    "logistics-service": "ORDER",
    "product-service": "PRODUCT",
    "cart-service": "ORDER",
    "coupon-service": "ORDER",
    "notification-service": "COMMON",
    "audit-service": "COMMON",
    "finance-service": "COMMON",
    "search-service": "PRODUCT",
    "report-service": "COMMON",
    "admin-service": "COMMON",
}

# ============ 表结构定义 ============
TABLES = {
    "order-service": {
        "t_order": [
            ("order_id", "订单ID"),
            ("user_id", "用户ID"),
            ("order_state", "订单状态:0待支付1已支付2已发货3已完成4已取消"),
            ("total_amount", "订单总金额"),
            ("pay_amount", "实付金额"),
            ("discount_amount", "优惠金额"),
            ("freight_amount", "运费金额"),
            ("receiver_name", "收货人姓名"),
            ("receiver_phone", "收货人电话"),
            ("receiver_address", "收货详细地址"),
            ("coupon_id", "优惠券ID"),
            ("pay_time", "支付时间"),
            ("delivery_time", "发货时间"),
            ("finish_time", "完成时间"),
            ("create_time", "创建时间"),
            ("update_time", "更新时间"),
        ],
        "t_order_item": [
            ("item_id", "明细ID"),
            ("order_id", "订单ID"),
            ("sku_id", "SKU ID"),
            ("product_id", "商品ID"),
            ("product_name", "商品名称"),
            ("sku_spec", "SKU规格"),
            ("quantity", "购买数量"),
            ("unit_price", "单价"),
            ("subtotal", "小计金额"),
            ("create_time", "创建时间"),
        ],
        "t_order_status_log": [
            ("log_id", "日志ID"),
            ("order_id", "订单ID"),
            ("from_state", "变更前状态"),
            ("to_state", "变更后状态"),
            ("operator_id", "操作人ID"),
            ("remark", "备注"),
            ("create_time", "创建时间"),
        ],
    },
    "payment-service": {
        "t_payment_record": [
            ("record_id", "记录ID"),
            ("order_id", "订单ID"),
            ("user_id", "用户ID"),
            ("pay_channel", "支付渠道:WX/ALIPAY/UNION"),
            ("pay_amount", "支付金额"),
            ("pay_state", "支付状态:0待支付1成功2失败3关闭"),
            ("transaction_id", "第三方流水号"),
            ("third_party_id", "第三方订单号"),
            ("expire_time", "过期时间"),
            ("pay_time", "支付时间"),
            ("create_time", "创建时间"),
        ],
        "t_payment_refund": [
            ("refund_id", "退款ID"),
            ("order_id", "订单ID"),
            ("record_id", "支付记录ID"),
            ("refund_amount", "退款金额"),
            ("refund_reason", "退款原因"),
            ("refund_state", "退款状态:0申请中1成功2拒绝"),
            ("audit_state", "审核状态"),
            ("create_time", "创建时间"),
        ],
        "t_payment_channel": [
            ("channel_id", "渠道ID"),
            ("channel_code", "渠道编码"),
            ("channel_name", "渠道名称"),
            ("channel_type", "渠道类型"),
            ("fee_rate", "费率"),
            ("status", "状态"),
            ("create_time", "创建时间"),
        ],
    },
    "inventory-service": {
        "t_inventory": [
            ("sku_id", "SKU ID"),
            ("product_id", "商品ID"),
            ("warehouse_id", "仓库ID"),
            ("stock_qty", "库存总量"),
            ("available_qty", "可用库存"),
            ("reserved_qty", "预占库存"),
            ("locked_qty", "锁定库存"),
            ("alert_threshold", "预警阈值"),
            ("create_time", "创建时间"),
            ("update_time", "更新时间"),
        ],
        "t_inventory_log": [
            ("log_id", "日志ID"),
            ("sku_id", "SKU ID"),
            ("change_qty", "变更数量"),
            ("before_qty", "变更前数量"),
            ("after_qty", "变更后数量"),
            ("change_type", "变更类型:ADD/DEDUCT/RELEASE"),
            ("biz_type", "业务类型:ORDER/RETURN/ADJUST"),
            ("biz_id", "业务单号"),
            ("operator_id", "操作人ID"),
            ("create_time", "创建时间"),
        ],
        "t_warehouse": [
            ("warehouse_id", "仓库ID"),
            ("warehouse_code", "仓库编码"),
            ("warehouse_name", "仓库名称"),
            ("province", "省份"),
            ("city", "城市"),
            ("address", "详细地址"),
            ("contact_name", "联系人"),
            ("contact_phone", "联系电话"),
            ("status", "状态"),
            ("create_time", "创建时间"),
        ],
    },
    "user-service": {
        "t_user": [
            ("user_id", "用户ID"),
            ("username", "用户名"),
            ("phone", "手机号"),
            ("email", "邮箱"),
            ("password_hash", "密码哈希"),
            ("user_state", "用户状态:0禁用1正常"),
            ("user_level", "用户等级"),
            ("register_channel", "注册渠道"),
            ("create_time", "创建时间"),
            ("update_time", "更新时间"),
        ],
        "t_user_address": [
            ("address_id", "地址ID"),
            ("user_id", "用户ID"),
            ("receiver_name", "收货人"),
            ("receiver_phone", "收货电话"),
            ("province", "省份"),
            ("city", "城市"),
            ("district", "区县"),
            ("detail_address", "详细地址"),
            ("is_default", "是否默认"),
            ("create_time", "创建时间"),
        ],
        "t_user_level": [
            ("level_id", "等级ID"),
            ("level_name", "等级名称"),
            ("level_code", "等级编码"),
            ("min_score", "最小积分"),
            ("max_score", "最大积分"),
            ("discount_rate", "折扣率"),
            ("create_time", "创建时间"),
        ],
    },
    "logistics-service": {
        "t_logistics": [
            ("logistics_id", "物流单ID"),
            ("order_id", "订单ID"),
            ("logistics_state", "物流状态:0待发货1已发货2运输中3已签收"),
            ("carrier_code", "承运商编码"),
            ("carrier_name", "承运商名称"),
            ("tracking_no", "运单号"),
            ("send_time", "发货时间"),
            ("receive_time", "签收时间"),
            ("create_time", "创建时间"),
        ],
        "t_logistics_detail": [
            ("detail_id", "明细ID"),
            ("logistics_id", "物流单ID"),
            ("node_time", "节点时间"),
            ("node_desc", "节点描述"),
            ("operator", "操作人"),
            ("location", "地点"),
            ("create_time", "创建时间"),
        ],
        "t_carrier": [
            ("carrier_id", "承运商ID"),
            ("carrier_code", "承运商编码"),
            ("carrier_name", "承运商名称"),
            ("contact_phone", "联系电话"),
            ("api_url", "API地址"),
            ("status", "状态"),
            ("create_time", "创建时间"),
        ],
    },
    "product-service": {
        "t_product": [
            ("product_id", "商品ID"),
            ("product_name", "商品名称"),
            ("category_id", "类目ID"),
            ("brand_id", "品牌ID"),
            ("spu_code", "SPU编码"),
            ("sale_state", "销售状态:0下架1上架"),
            ("audit_state", "审核状态:0待审1通过2拒绝"),
            ("create_time", "创建时间"),
            ("update_time", "更新时间"),
        ],
        "t_sku": [
            ("sku_id", "SKU ID"),
            ("product_id", "商品ID"),
            ("sku_code", "SKU编码"),
            ("sku_name", "SKU名称"),
            ("sku_spec", "SKU规格"),
            ("sale_price", "销售价"),
            ("market_price", "市场价"),
            ("cost_price", "成本价"),
            ("weight", "重量"),
            ("sale_state", "销售状态"),
            ("create_time", "创建时间"),
        ],
        "t_category": [
            ("category_id", "类目ID"),
            ("parent_id", "父类目ID"),
            ("category_name", "类目名称"),
            ("category_level", "类目级别"),
            ("sort_order", "排序"),
            ("status", "状态"),
            ("create_time", "创建时间"),
        ],
        "t_brand": [
            ("brand_id", "品牌ID"),
            ("brand_name", "品牌名称"),
            ("brand_code", "品牌编码"),
            ("logo_url", "LOGO地址"),
            ("status", "状态"),
            ("create_time", "创建时间"),
        ],
    },
    "cart-service": {
        "t_cart_item": [
            ("item_id", "购物车项ID"),
            ("user_id", "用户ID"),
            ("sku_id", "SKU ID"),
            ("product_name", "商品名称"),
            ("sku_spec", "SKU规格"),
            ("quantity", "数量"),
            ("unit_price", "单价"),
            ("selected", "是否选中"),
            ("create_time", "创建时间"),
            ("update_time", "更新时间"),
        ],
    },
    "coupon-service": {
        "t_coupon": [
            ("coupon_id", "优惠券ID"),
            ("coupon_name", "优惠券名称"),
            ("coupon_type", "类型:FULL_REDUCE/DISCOUNT"),
            ("discount_amount", "优惠金额"),
            ("discount_percent", "折扣比例"),
            ("min_order_amount", "最低订单金额"),
            ("max_discount_amount", "最大优惠金额"),
            ("total_count", "总数量"),
            ("remain_count", "剩余数量"),
            ("valid_start", "有效开始"),
            ("valid_end", "有效结束"),
            ("status", "状态"),
            ("create_time", "创建时间"),
        ],
        "t_user_coupon": [
            ("user_coupon_id", "用户优惠券ID"),
            ("user_id", "用户ID"),
            ("coupon_id", "优惠券ID"),
            ("use_state", "使用状态:0未使用1已使用2已过期"),
            ("use_time", "使用时间"),
            ("order_id", "订单ID"),
            ("valid_start", "有效开始"),
            ("valid_end", "有效结束"),
            ("create_time", "创建时间"),
        ],
    },
    "notification-service": {
        "t_notification": [
            ("notification_id", "通知ID"),
            ("user_id", "用户ID"),
            ("notify_type", "通知类型:ORDER/PAYMENT/LOGISTICS"),
            ("notify_title", "通知标题"),
            ("notify_content", "通知内容"),
            ("channel", "渠道:SMS/PUSH/EMAIL"),
            ("send_state", "发送状态"),
            ("read_state", "读取状态"),
            ("create_time", "创建时间"),
            ("send_time", "发送时间"),
            ("read_time", "读取时间"),
        ],
        "t_notify_template": [
            ("template_id", "模板ID"),
            ("template_code", "模板编码"),
            ("template_name", "模板名称"),
            ("channel", "渠道"),
            ("content_template", "内容模板"),
            ("status", "状态"),
            ("create_time", "创建时间"),
        ],
    },
    "audit-service": {
        "t_audit_log": [
            ("log_id", "日志ID"),
            ("biz_type", "业务类型"),
            ("biz_id", "业务ID"),
            ("action", "操作"),
            ("operator_id", "操作人ID"),
            ("operator_name", "操作人姓名"),
            ("before_state", "变更前状态"),
            ("after_state", "变更后状态"),
            ("detail_json", "详情JSON"),
            ("create_time", "创建时间"),
        ],
    },
    "finance-service": {
        "t_finance_daily": [
            ("daily_id", "日结ID"),
            ("biz_date", "业务日期"),
            ("order_count", "订单笔数"),
            ("order_amount", "订单金额"),
            ("pay_count", "支付笔数"),
            ("pay_amount", "支付金额"),
            ("refund_count", "退款笔数"),
            ("refund_amount", "退款金额"),
            ("fee_amount", "手续费金额"),
            ("net_amount", "净收入"),
            ("create_time", "创建时间"),
        ],
        "t_finance_reconcile": [
            ("reconcile_id", "对账ID"),
            ("biz_date", "业务日期"),
            ("channel_code", "渠道编码"),
            ("channel_name", "渠道名称"),
            ("third_amount", "第三方金额"),
            ("system_amount", "系统金额"),
            ("diff_amount", "差异金额"),
            ("reconcile_state", "对账状态"),
            ("create_time", "创建时间"),
        ],
    },
    "search-service": {
        "t_search_log": [
            ("log_id", "日志ID"),
            ("user_id", "用户ID"),
            ("keyword", "关键词"),
            ("search_type", "搜索类型"),
            ("result_count", "结果数量"),
            ("click_count", "点击数量"),
            ("create_time", "创建时间"),
        ],
    },
    "report-service": {
        "t_report_template": [
            ("template_id", "模板ID"),
            ("template_code", "模板编码"),
            ("template_name", "模板名称"),
            ("report_type", "报表类型"),
            ("data_source", "数据源"),
            ("status", "状态"),
            ("create_time", "创建时间"),
        ],
    },
    "admin-service": {
        "t_admin_user": [
            ("admin_id", "管理员ID"),
            ("username", "用户名"),
            ("real_name", "真实姓名"),
            ("role_code", "角色编码"),
            ("dept_code", "部门编码"),
            ("status", "状态"),
            ("create_time", "创建时间"),
        ],
        "t_admin_role": [
            ("role_id", "角色ID"),
            ("role_code", "角色编码"),
            ("role_name", "角色名称"),
            ("permissions", "权限列表"),
            ("status", "状态"),
            ("create_time", "创建时间"),
        ],
    },
    "gateway-service": {},
}


# ============ 字段级血缘引用（核心：真实SQL + 跨服务引用） ============
# 每条记录: (app_id, table_name, column_name, sql_preview, operation_type, operation_detail)
COLUMN_REFS = [
    # --- payment-service 引用 t_order (支付核心链路) ---
    ("payment-service", "t_order", "order_state",
     "SELECT order_state, pay_amount, total_amount, user_id FROM t_order WHERE order_id = ?",
     "SELECT", "支付前查询订单状态和金额"),
    ("payment-service", "t_order", "pay_amount",
     "SELECT order_state, pay_amount, total_amount, user_id FROM t_order WHERE order_id = ?",
     "SELECT", "支付前查询应付金额"),
    ("payment-service", "t_order", "total_amount",
     "SELECT order_state, pay_amount, total_amount, user_id FROM t_order WHERE order_id = ?",
     "SELECT", "支付前查询订单总金额"),
    ("payment-service", "t_order", "user_id",
     "SELECT order_state, pay_amount, total_amount, user_id FROM t_order WHERE order_id = ?",
     "SELECT", "支付前查询下单用户"),
    ("payment-service", "t_order", "order_state",
     "UPDATE t_order SET order_state = 1, pay_time = NOW() WHERE order_id = ? AND order_state = 0",
     "UPDATE", "支付成功回调：更新订单为已支付"),
    ("payment-service", "t_order", "pay_time",
     "UPDATE t_order SET order_state = 1, pay_time = NOW() WHERE order_id = ? AND order_state = 0",
     "UPDATE", "支付成功回调：记录支付时间"),
    # payment-service 引用自身 t_payment_record
    ("payment-service", "t_payment_record", "pay_state",
     "SELECT pay_state, pay_amount FROM t_payment_record WHERE order_id = ? AND pay_state = 0",
     "SELECT", "查询待支付记录"),
    ("payment-service", "t_payment_record", "pay_amount",
     "SELECT pay_state, pay_amount FROM t_payment_record WHERE order_id = ? AND pay_state = 0",
     "SELECT", "查询待支付金额"),

    # --- logistics-service 引用 t_order (发货链路) ---
    ("logistics-service", "t_order", "receiver_name",
     "SELECT receiver_name, receiver_phone, receiver_address, order_state FROM t_order WHERE order_id = ?",
     "SELECT", "创建物流单：查询收货人姓名"),
    ("logistics-service", "t_order", "receiver_phone",
     "SELECT receiver_name, receiver_phone, receiver_address, order_state FROM t_order WHERE order_id = ?",
     "SELECT", "创建物流单：查询收货电话"),
    ("logistics-service", "t_order", "receiver_address",
     "SELECT receiver_name, receiver_phone, receiver_address, order_state FROM t_order WHERE order_id = ?",
     "SELECT", "创建物流单：查询收货地址"),
    ("logistics-service", "t_order", "order_state",
     "SELECT receiver_name, receiver_phone, receiver_address, order_state FROM t_order WHERE order_id = ?",
     "SELECT", "创建物流单：校验订单已支付"),
    ("logistics-service", "t_order", "order_state",
     "UPDATE t_order SET order_state = 2, delivery_time = NOW() WHERE order_id = ? AND order_state = 1",
     "UPDATE", "发货完成：更新订单为已发货"),
    ("logistics-service", "t_order", "delivery_time",
     "UPDATE t_order SET order_state = 2, delivery_time = NOW() WHERE order_id = ? AND order_state = 1",
     "UPDATE", "发货完成：记录发货时间"),
    # logistics-service 引用 t_carrier
    ("logistics-service", "t_carrier", "carrier_code",
     "SELECT carrier_code, carrier_name, api_url FROM t_carrier WHERE status = 1",
     "SELECT", "查询可用承运商"),
    ("logistics-service", "t_carrier", "carrier_name",
     "SELECT carrier_code, carrier_name, api_url FROM t_carrier WHERE status = 1",
     "SELECT", "查询承运商名称"),

    # --- notification-service 引用 t_order (通知链路) ---
    ("notification-service", "t_order", "order_id",
     "SELECT order_id, user_id, order_state, total_amount, pay_time FROM t_order WHERE order_id = ?",
     "SELECT", "订单状态变更通知：查询订单信息"),
    ("notification-service", "t_order", "user_id",
     "SELECT order_id, user_id, order_state, total_amount, pay_time FROM t_order WHERE order_id = ?",
     "SELECT", "订单通知：查询用户ID"),
    ("notification-service", "t_order", "order_state",
     "SELECT order_id, user_id, order_state, total_amount, pay_time FROM t_order WHERE order_id = ?",
     "SELECT", "订单通知：查询当前状态"),
    ("notification-service", "t_order", "total_amount",
     "SELECT order_id, user_id, order_state, total_amount, pay_time FROM t_order WHERE order_id = ?",
     "SELECT", "订单通知：展示订单金额"),
    ("notification-service", "t_order", "pay_time",
     "SELECT order_id, user_id, order_state, total_amount, pay_time FROM t_order WHERE order_id = ?",
     "SELECT", "支付成功通知：展示支付时间"),
    # notification-service 引用 t_payment_record
    ("notification-service", "t_payment_record", "pay_state",
     "SELECT pay_state, pay_amount, pay_channel FROM t_payment_record WHERE order_id = ?",
     "SELECT", "支付结果通知：查询支付状态"),
    ("notification-service", "t_payment_record", "pay_amount",
     "SELECT pay_state, pay_amount, pay_channel FROM t_payment_record WHERE order_id = ?",
     "SELECT", "支付结果通知：展示支付金额"),
    ("notification-service", "t_payment_record", "pay_channel",
     "SELECT pay_state, pay_amount, pay_channel FROM t_payment_record WHERE order_id = ?",
     "SELECT", "支付结果通知：展示支付渠道"),
    # notification-service 引用 t_user
    ("notification-service", "t_user", "phone",
     "SELECT phone, email FROM t_user WHERE user_id = ?",
     "SELECT", "通知：查询用户手机号发送短信"),
    ("notification-service", "t_user", "email",
     "SELECT phone, email FROM t_user WHERE user_id = ?",
     "SELECT", "通知：查询用户邮箱发送邮件"),
    # notification-service 引用 t_logistics
    ("notification-service", "t_logistics", "logistics_state",
     "SELECT logistics_state, tracking_no, carrier_name FROM t_logistics WHERE order_id = ?",
     "SELECT", "物流通知：查询物流状态"),
    ("notification-service", "t_logistics", "tracking_no",
     "SELECT logistics_state, tracking_no, carrier_name FROM t_logistics WHERE order_id = ?",
     "SELECT", "物流通知：展示运单号"),

    # --- coupon-service 引用 t_order (优惠券核销链路) ---
    ("coupon-service", "t_order", "total_amount",
     "SELECT total_amount, user_id, coupon_id FROM t_order WHERE order_id = ?",
     "SELECT", "核销优惠券：校验订单金额"),
    ("coupon-service", "t_order", "user_id",
     "SELECT total_amount, user_id, coupon_id FROM t_order WHERE order_id = ?",
     "SELECT", "核销优惠券：校验用户身份"),
    ("coupon-service", "t_order", "coupon_id",
     "SELECT total_amount, user_id, coupon_id FROM t_order WHERE order_id = ?",
     "SELECT", "核销优惠券：查询使用的优惠券"),
    # coupon-service 引用 t_user_coupon
    ("coupon-service", "t_user_coupon", "use_state",
     "SELECT use_state, valid_start, valid_end FROM t_user_coupon WHERE user_id = ? AND coupon_id = ?",
     "SELECT", "校验优惠券是否可用"),
    ("coupon-service", "t_user_coupon", "valid_start",
     "SELECT use_state, valid_start, valid_end FROM t_user_coupon WHERE user_id = ? AND coupon_id = ?",
     "SELECT", "校验优惠券有效期开始"),
    ("coupon-service", "t_user_coupon", "valid_end",
     "SELECT use_state, valid_start, valid_end FROM t_user_coupon WHERE user_id = ? AND coupon_id = ?",
     "SELECT", "校验优惠券有效期结束"),
    ("coupon-service", "t_user_coupon", "use_state",
     "UPDATE t_user_coupon SET use_state = 1, use_time = NOW(), order_id = ? WHERE user_coupon_id = ?",
     "UPDATE", "核销优惠券：更新为已使用"),

    # --- inventory-service 引用 t_order_item (库存扣减链路) ---
    ("inventory-service", "t_order_item", "sku_id",
     "SELECT sku_id, quantity FROM t_order_item WHERE order_id = ?",
     "SELECT", "扣减库存：查询订单SKU"),
    ("inventory-service", "t_order_item", "quantity",
     "SELECT sku_id, quantity FROM t_order_item WHERE order_id = ?",
     "SELECT", "扣减库存：查询购买数量"),
    # inventory-service 引用 t_inventory
    ("inventory-service", "t_inventory", "stock_qty",
     "UPDATE t_inventory SET stock_qty = stock_qty - ?, reserved_qty = reserved_qty + ? WHERE sku_id = ?",
     "UPDATE", "下单扣减库存"),
    ("inventory-service", "t_inventory", "reserved_qty",
     "UPDATE t_inventory SET stock_qty = stock_qty - ?, reserved_qty = reserved_qty + ? WHERE sku_id = ?",
     "UPDATE", "下单预占库存"),
    ("inventory-service", "t_inventory", "stock_qty",
     "SELECT stock_qty, available_qty FROM t_inventory WHERE sku_id = ?",
     "SELECT", "查询库存余量"),
    ("inventory-service", "t_inventory", "available_qty",
     "SELECT stock_qty, available_qty FROM t_inventory WHERE sku_id = ?",
     "SELECT", "查询可用库存"),
    # 发货释放预占
    ("inventory-service", "t_inventory", "reserved_qty",
     "UPDATE t_inventory SET reserved_qty = reserved_qty - ? WHERE sku_id = ?",
     "UPDATE", "发货释放预占库存"),

    # --- finance-service 引用 t_payment_record (财务对账链路) ---
    ("finance-service", "t_payment_record", "pay_amount",
     "SELECT pay_amount, pay_state, transaction_id, pay_channel FROM t_payment_record WHERE order_id = ?",
     "SELECT", "财务对账：查询支付金额"),
    ("finance-service", "t_payment_record", "pay_state",
     "SELECT pay_amount, pay_state, transaction_id, pay_channel FROM t_payment_record WHERE order_id = ?",
     "SELECT", "财务对账：查询支付状态"),
    ("finance-service", "t_payment_record", "transaction_id",
     "SELECT pay_amount, pay_state, transaction_id, pay_channel FROM t_payment_record WHERE order_id = ?",
     "SELECT", "财务对账：查询第三方流水号"),
    ("finance-service", "t_payment_record", "pay_channel",
     "SELECT pay_amount, pay_state, transaction_id, pay_channel FROM t_payment_record WHERE order_id = ?",
     "SELECT", "财务对账：查询支付渠道"),
    # 日结统计
    ("finance-service", "t_payment_record", "pay_amount",
     "SELECT SUM(pay_amount), COUNT(*) FROM t_payment_record WHERE pay_state = 'SUCCESS' AND pay_time BETWEEN ? AND ?",
     "SELECT", "日结：统计支付金额"),
    ("finance-service", "t_payment_record", "pay_state",
     "SELECT SUM(pay_amount), COUNT(*) FROM t_payment_record WHERE pay_state = 'SUCCESS' AND pay_time BETWEEN ? AND ?",
     "SELECT", "日结：统计成功笔数"),
    ("finance-service", "t_payment_record", "pay_time",
     "SELECT SUM(pay_amount), COUNT(*) FROM t_payment_record WHERE pay_state = 'SUCCESS' AND pay_time BETWEEN ? AND ?",
     "SELECT", "日结：按时间范围统计"),
    # finance-service 引用 t_order
    ("finance-service", "t_order", "total_amount",
     "SELECT SUM(total_amount), COUNT(*) FROM t_order WHERE create_time BETWEEN ? AND ? AND order_state = 3",
     "SELECT", "财务：统计已完成订单金额"),
    ("finance-service", "t_order", "order_state",
     "SELECT SUM(total_amount), COUNT(*) FROM t_order WHERE create_time BETWEEN ? AND ? AND order_state = 3",
     "SELECT", "财务：统计已完成订单数量"),
    ("finance-service", "t_order", "create_time",
     "SELECT SUM(total_amount), COUNT(*) FROM t_order WHERE create_time BETWEEN ? AND ? AND order_state = 3",
     "SELECT", "财务：按时间范围统计订单"),
    # finance-service 引用 t_payment_refund
    ("finance-service", "t_payment_refund", "refund_amount",
     "SELECT SUM(refund_amount), COUNT(*) FROM t_payment_refund WHERE refund_state = 1 AND create_time BETWEEN ? AND ?",
     "SELECT", "财务：统计退款金额"),
    ("finance-service", "t_payment_refund", "refund_state",
     "SELECT SUM(refund_amount), COUNT(*) FROM t_payment_refund WHERE refund_state = 1 AND create_time BETWEEN ? AND ?",
     "SELECT", "财务：统计退款笔数"),

    # --- audit-service 引用 t_payment_record (审计链路) ---
    ("audit-service", "t_payment_record", "pay_state",
     "SELECT record_id, order_id, pay_amount, pay_state, transaction_id FROM t_payment_record WHERE order_id = ?",
     "SELECT", "审计：查询支付记录"),
    ("audit-service", "t_payment_record", "pay_amount",
     "SELECT record_id, order_id, pay_amount, pay_state, transaction_id FROM t_payment_record WHERE order_id = ?",
     "SELECT", "审计：查询支付金额"),
    ("audit-service", "t_payment_record", "transaction_id",
     "SELECT record_id, order_id, pay_amount, pay_state, transaction_id FROM t_payment_record WHERE order_id = ?",
     "SELECT", "审计：查询第三方流水"),
    ("audit-service", "t_payment_record", "refund_state",
     "SELECT refund_id, refund_amount, refund_state, audit_state FROM t_payment_refund WHERE order_id = ?",
     "SELECT", "审计：查询退款状态"),
    ("audit-service", "t_payment_record", "refund_amount",
     "SELECT refund_id, refund_amount, refund_state, audit_state FROM t_payment_refund WHERE order_id = ?",
     "SELECT", "审计：查询退款金额"),
    # audit-service 引用 t_order
    ("audit-service", "t_order", "order_state",
     "SELECT order_id, order_state, user_id, total_amount FROM t_order WHERE order_id = ?",
     "SELECT", "审计：查询订单信息"),
    ("audit-service", "t_order", "user_id",
     "SELECT order_id, order_state, user_id, total_amount FROM t_order WHERE order_id = ?",
     "SELECT", "审计：查询订单用户"),
    ("audit-service", "t_order", "total_amount",
     "SELECT order_id, order_state, user_id, total_amount FROM t_order WHERE order_id = ?",
     "SELECT", "审计：查询订单金额"),
    # audit-service 引用 t_inventory_log
    ("audit-service", "t_inventory_log", "change_type",
     "SELECT log_id, sku_id, change_qty, change_type, biz_id FROM t_inventory_log WHERE biz_id = ?",
     "SELECT", "审计：查询库存变更记录"),
    ("audit-service", "t_inventory_log", "change_qty",
     "SELECT log_id, sku_id, change_qty, change_type, biz_id FROM t_inventory_log WHERE biz_id = ?",
     "SELECT", "审计：查询库存变更数量"),

    # --- report-service 引用多个表 (报表统计) ---
    ("report-service", "t_order", "order_state",
     "SELECT COUNT(*), SUM(total_amount) FROM t_order WHERE create_time BETWEEN ? AND ? AND order_state = ?",
     "SELECT", "订单报表：按状态统计"),
    ("report-service", "t_order", "total_amount",
     "SELECT COUNT(*), SUM(total_amount) FROM t_order WHERE create_time BETWEEN ? AND ? AND order_state = ?",
     "SELECT", "订单报表：按金额统计"),
    ("report-service", "t_order", "create_time",
     "SELECT COUNT(*), SUM(total_amount) FROM t_order WHERE create_time BETWEEN ? AND ? AND order_state = ?",
     "SELECT", "订单报表：按时间范围统计"),
    ("report-service", "t_payment_record", "pay_amount",
     "SELECT COUNT(*), SUM(pay_amount) FROM t_payment_record WHERE pay_time BETWEEN ? AND ? AND pay_state = ?",
     "SELECT", "支付报表：统计支付金额"),
    ("report-service", "t_payment_record", "pay_state",
     "SELECT COUNT(*), SUM(pay_amount) FROM t_payment_record WHERE pay_time BETWEEN ? AND ? AND pay_state = ?",
     "SELECT", "支付报表：统计支付笔数"),
    ("report-service", "t_payment_record", "pay_time",
     "SELECT COUNT(*), SUM(pay_amount) FROM t_payment_record WHERE pay_time BETWEEN ? AND ? AND pay_state = ?",
     "SELECT", "支付报表：按时间范围统计"),
    ("report-service", "t_inventory", "stock_qty",
     "SELECT sku_id, stock_qty, available_qty FROM t_inventory WHERE alert_threshold > available_qty",
     "SELECT", "库存预警报表：查询低库存SKU"),
    ("report-service", "t_inventory", "available_qty",
     "SELECT sku_id, stock_qty, available_qty FROM t_inventory WHERE alert_threshold > available_qty",
     "SELECT", "库存预警报表：查询可用库存不足"),
    ("report-service", "t_user", "user_state",
     "SELECT user_state, COUNT(*) FROM t_user WHERE create_time BETWEEN ? AND ? GROUP BY user_state",
     "SELECT", "用户报表：按状态统计"),
    ("report-service", "t_user", "create_time",
     "SELECT user_state, COUNT(*) FROM t_user WHERE create_time BETWEEN ? AND ? GROUP BY user_state",
     "SELECT", "用户报表：按注册时间统计"),

    # --- cart-service 引用 t_sku (加购链路) ---
    ("cart-service", "t_sku", "sale_price",
     "SELECT sale_price, product_name, sku_spec, sale_state FROM t_sku WHERE sku_id = ?",
     "SELECT", "加购：查询SKU价格"),
    ("cart-service", "t_sku", "product_name",
     "SELECT sale_price, product_name, sku_spec, sale_state FROM t_sku WHERE sku_id = ?",
     "SELECT", "加购：查询商品名称"),
    ("cart-service", "t_sku", "sku_spec",
     "SELECT sale_price, product_name, sku_spec, sale_state FROM t_sku WHERE sku_id = ?",
     "SELECT", "加购：查询SKU规格"),
    ("cart-service", "t_sku", "sale_state",
     "SELECT sale_price, product_name, sku_spec, sale_state FROM t_sku WHERE sku_id = ?",
     "SELECT", "加购：查询销售状态"),
    # cart-service 引用 t_inventory 校验库存
    ("cart-service", "t_inventory", "stock_qty",
     "SELECT stock_qty FROM t_inventory WHERE sku_id = ?",
     "SELECT", "购物车：校验库存余量"),
    # cart-service 引用 t_product
    ("cart-service", "t_product", "sale_state",
     "SELECT sale_state, audit_state FROM t_product WHERE product_id = ?",
     "SELECT", "购物车：校验商品上架状态"),

    # --- search-service 引用 product 表 (搜索链路) ---
    ("search-service", "t_product", "product_name",
     "SELECT product_id, product_name, category_id, brand_id, sale_state FROM t_product WHERE sale_state = 1",
     "SELECT", "搜索：查询上架商品列表"),
    ("search-service", "t_product", "category_id",
     "SELECT product_id, product_name, category_id, brand_id, sale_state FROM t_product WHERE sale_state = 1",
     "SELECT", "搜索：查询商品分类"),
    ("search-service", "t_product", "brand_id",
     "SELECT product_id, product_name, category_id, brand_id, sale_state FROM t_product WHERE sale_state = 1",
     "SELECT", "搜索：查询商品品牌"),
    ("search-service", "t_product", "sale_state",
     "SELECT product_id, product_name, category_id, brand_id, sale_state FROM t_product WHERE sale_state = 1",
     "SELECT", "搜索：只展示上架商品"),
    ("search-service", "t_sku", "sale_price",
     "SELECT sku_id, product_id, sale_price, sku_spec FROM t_sku WHERE product_id = ?",
     "SELECT", "搜索：查询SKU价格"),
    ("search-service", "t_sku", "sku_spec",
     "SELECT sku_id, product_id, sale_price, sku_spec FROM t_sku WHERE product_id = ?",
     "SELECT", "搜索：查询SKU规格"),
    ("search-service", "t_category", "category_name",
     "SELECT category_id, category_name FROM t_category WHERE status = 1 ORDER BY sort_order",
     "SELECT", "搜索：查询类目列表"),

    # --- admin-service 引用多个表 (运营管理) ---
    ("admin-service", "t_order", "order_id",
     "SELECT * FROM t_order WHERE order_id = ?",
     "SELECT", "运营：查询订单详情"),
    ("admin-service", "t_order", "order_state",
     "SELECT * FROM t_order WHERE order_state = ? ORDER BY create_time DESC LIMIT ?",
     "SELECT", "运营：按状态筛选订单"),
    ("admin-service", "t_order", "create_time",
     "SELECT * FROM t_order WHERE create_time BETWEEN ? AND ? ORDER BY create_time DESC",
     "SELECT", "运营：按时间筛选订单"),
    ("admin-service", "t_order", "total_amount",
     "SELECT * FROM t_order WHERE total_amount BETWEEN ? AND ? ORDER BY create_time DESC",
     "SELECT", "运营：按金额筛选订单"),
    ("admin-service", "t_payment_record", "pay_state",
     "SELECT * FROM t_payment_record WHERE pay_state = ? ORDER BY create_time DESC LIMIT ?",
     "SELECT", "运营：查询支付记录"),
    ("admin-service", "t_payment_record", "pay_channel",
     "SELECT * FROM t_payment_record WHERE pay_channel = ? ORDER BY create_time DESC",
     "SELECT", "运营：按渠道筛选支付"),
    ("admin-service", "t_product", "sale_state",
     "SELECT * FROM t_product WHERE sale_state = ? ORDER BY create_time DESC",
     "SELECT", "运营：查询商品列表"),
    ("admin-service", "t_product", "audit_state",
     "UPDATE t_product SET audit_state = ?, sale_state = ? WHERE product_id = ?",
     "UPDATE", "运营：审核商品上架"),
    ("admin-service", "t_product", "sale_state",
     "UPDATE t_product SET audit_state = ?, sale_state = ? WHERE product_id = ?",
     "UPDATE", "运营：修改商品销售状态"),
    ("admin-service", "t_user", "user_state",
     "SELECT * FROM t_user WHERE user_state = ? ORDER BY create_time DESC LIMIT ?",
     "SELECT", "运营：查询用户列表"),
    ("admin-service", "t_user", "user_level",
     "SELECT * FROM t_user WHERE user_level = ? ORDER BY create_time DESC",
     "SELECT", "运营：按等级筛选用户"),
    ("admin-service", "t_inventory", "stock_qty",
     "SELECT * FROM t_inventory WHERE stock_qty < alert_threshold ORDER BY stock_qty",
     "SELECT", "运营：查询低库存预警"),
    ("admin-service", "t_coupon", "status",
     "SELECT * FROM t_coupon WHERE status = ? ORDER BY create_time DESC",
     "SELECT", "运营：查询优惠券列表"),

    # --- user-service 自身引用 ---
    ("user-service", "t_user", "user_state",
     "SELECT user_id, username, phone, email, user_state FROM t_user WHERE user_id = ?",
     "SELECT", "查询用户信息"),
    ("user-service", "t_user", "phone",
     "SELECT user_id, username, phone, email, user_state FROM t_user WHERE user_id = ?",
     "SELECT", "查询用户手机号"),
    ("user-service", "t_user", "email",
     "SELECT user_id, username, phone, email, user_state FROM t_user WHERE user_id = ?",
     "SELECT", "查询用户邮箱"),
    ("user-service", "t_user", "username",
     "SELECT user_id, username, phone, email, user_state FROM t_user WHERE user_id = ?",
     "SELECT", "查询用户名"),
    ("user-service", "t_user", "user_level",
     "SELECT user_level FROM t_user WHERE user_id = ?",
     "SELECT", "查询用户等级"),
    ("user-service", "t_user_address", "receiver_name",
     "SELECT address_id, receiver_name, receiver_phone, detail_address, is_default FROM t_user_address WHERE user_id = ?",
     "SELECT", "查询用户地址"),
    ("user-service", "t_user_address", "receiver_phone",
     "SELECT address_id, receiver_name, receiver_phone, detail_address, is_default FROM t_user_address WHERE user_id = ?",
     "SELECT", "查询地址电话"),
    ("user-service", "t_user_address", "is_default",
     "SELECT address_id, receiver_name, receiver_phone, detail_address, is_default FROM t_user_address WHERE user_id = ?",
     "SELECT", "查询默认地址"),

    # --- order-service 引用其他表 (下单链路) ---
    ("order-service", "t_inventory", "stock_qty",
     "SELECT stock_qty, available_qty FROM t_inventory WHERE sku_id = ?",
     "SELECT", "下单前校验库存"),
    ("order-service", "t_inventory", "available_qty",
     "SELECT stock_qty, available_qty FROM t_inventory WHERE sku_id = ?",
     "SELECT", "下单前查询可用库存"),
    ("order-service", "t_user_coupon", "use_state",
     "SELECT user_coupon_id, use_state, valid_end FROM t_user_coupon WHERE user_id = ? AND coupon_id = ?",
     "SELECT", "下单查询优惠券状态"),
    ("order-service", "t_user_coupon", "valid_end",
     "SELECT user_coupon_id, use_state, valid_end FROM t_user_coupon WHERE user_id = ? AND coupon_id = ?",
     "SELECT", "下单校验优惠券有效期"),
    ("order-service", "t_sku", "sale_price",
     "SELECT sale_price, product_name, sku_spec, sale_state FROM t_sku WHERE sku_id = ?",
     "SELECT", "下单查询SKU价格"),
    ("order-service", "t_sku", "product_name",
     "SELECT sale_price, product_name, sku_spec, sale_state FROM t_sku WHERE sku_id = ?",
     "SELECT", "下单查询商品名称"),
    ("order-service", "t_sku", "sale_state",
     "SELECT sale_price, product_name, sku_spec, sale_state FROM t_sku WHERE sku_id = ?",
     "SELECT", "下单校验SKU销售状态"),
    ("order-service", "t_user_address", "receiver_name",
     "SELECT receiver_name, receiver_phone, detail_address FROM t_user_address WHERE address_id = ?",
     "SELECT", "下单查询收货人"),
    ("order-service", "t_user_address", "receiver_phone",
     "SELECT receiver_name, receiver_phone, detail_address FROM t_user_address WHERE address_id = ?",
     "SELECT", "下单查询收货电话"),
    ("order-service", "t_user_address", "detail_address",
     "SELECT receiver_name, receiver_phone, detail_address FROM t_user_address WHERE address_id = ?",
     "SELECT", "下单查询收货地址"),
    # order-service 更新自身表
    ("order-service", "t_order", "order_state",
     "UPDATE t_order SET order_state = 4, update_time = NOW() WHERE order_id = ? AND order_state = 0",
     "UPDATE", "订单取消：更新状态"),
    ("order-service", "t_order", "update_time",
     "UPDATE t_order SET order_state = 4, update_time = NOW() WHERE order_id = ? AND order_state = 0",
     "UPDATE", "订单取消：更新时间"),
    ("order-service", "t_order_status_log", "from_state",
     "INSERT INTO t_order_status_log (order_id, from_state, to_state, operator_id, remark) VALUES (?, ?, ?, ?, ?)",
     "INSERT", "记录订单状态变更日志"),
    ("order-service", "t_order_status_log", "to_state",
     "INSERT INTO t_order_status_log (order_id, from_state, to_state, operator_id, remark) VALUES (?, ?, ?, ?, ?)",
     "INSERT", "记录订单状态变更日志"),
]


# ============ 应用间调用拓扑（LineageEdge） ============
LINEAGE_EDGES = [
    # 网关层调用
    ("gateway-service", "order-service",        "HTTP_CALLS", "API路由: /api/v1/orders/**",     0.98),
    ("gateway-service", "payment-service",      "HTTP_CALLS", "API路由: /api/v1/payments/**",   0.98),
    ("gateway-service", "user-service",         "HTTP_CALLS", "API路由: /api/v1/users/**",      0.98),
    ("gateway-service", "product-service",      "HTTP_CALLS", "API路由: /api/v1/products/**",   0.98),
    ("gateway-service", "cart-service",         "HTTP_CALLS", "API路由: /api/v1/cart/**",       0.98),
    ("gateway-service", "coupon-service",       "HTTP_CALLS", "API路由: /api/v1/coupons/**",    0.98),
    ("gateway-service", "search-service",       "HTTP_CALLS", "API路由: /api/v1/search/**",     0.98),
    ("gateway-service", "logistics-service",    "HTTP_CALLS", "API路由: /api/v1/logistics/**",  0.95),
    ("gateway-service", "notification-service", "HTTP_CALLS", "API路由: /api/v1/notifications/**", 0.95),
    ("gateway-service", "admin-service",        "HTTP_CALLS", "API路由: /api/v1/admin/**",      0.95),

    # 订单域内部调用
    ("order-service", "payment-service",      "HTTP_CALLS", "创建支付单", 0.95),
    ("order-service", "inventory-service",    "HTTP_CALLS", "扣减库存", 0.95),
    ("order-service", "logistics-service",    "HTTP_CALLS", "创建物流单", 0.92),
    ("order-service", "coupon-service",       "HTTP_CALLS", "核销优惠券", 0.92),
    ("order-service", "notification-service", "HTTP_CALLS", "发送订单通知", 0.90),
    ("order-service", "user-service",         "HTTP_CALLS", "查询用户信息", 0.90),

    # 支付回调链路
    ("payment-service", "order-service",        "HTTP_CALLS", "支付成功回调: 更新订单状态", 0.98),
    ("payment-service", "finance-service",      "HTTP_CALLS", "通知财务入账", 0.92),
    ("payment-service", "audit-service",        "HTTP_CALLS", "记录支付审计日志", 0.90),
    ("payment-service", "notification-service", "HTTP_CALLS", "发送支付结果通知", 0.90),

    # 库存链路
    ("inventory-service", "product-service",      "HTTP_CALLS", "查询SKU信息", 0.92),
    ("inventory-service", "order-service",        "HTTP_CALLS", "库存扣减结果回调", 0.90),

    # 购物车链路
    ("cart-service", "product-service",   "HTTP_CALLS", "查询商品信息", 0.92),
    ("cart-service", "inventory-service", "HTTP_CALLS", "查询库存余量", 0.92),
    ("cart-service", "user-service",      "HTTP_CALLS", "查询用户信息", 0.90),

    # 优惠券链路
    ("coupon-service", "order-service",        "HTTP_CALLS", "校验订单金额", 0.92),
    ("coupon-service", "notification-service", "HTTP_CALLS", "发送优惠券通知", 0.88),

    # 物流链路
    ("logistics-service", "order-service",        "HTTP_CALLS", "查询收货信息", 0.95),
    ("logistics-service", "notification-service", "HTTP_CALLS", "发送物流状态通知", 0.90),

    # 通知链路
    ("notification-service", "user-service", "HTTP_CALLS", "查询用户联系方式", 0.95),

    # 财务链路
    ("finance-service", "payment-service", "HTTP_CALLS", "查询支付记录对账", 0.95),
    ("finance-service", "order-service",   "HTTP_CALLS", "查询订单统计数据", 0.92),
    ("finance-service", "audit-service",   "HTTP_CALLS", "审计财务数据", 0.88),

    # 审计链路
    ("audit-service", "payment-service", "HTTP_CALLS", "查询支付记录", 0.92),
    ("audit-service", "order-service",   "HTTP_CALLS", "查询订单信息", 0.90),
    ("audit-service", "inventory-service", "HTTP_CALLS", "查询库存变更", 0.88),

    # 报表链路
    ("report-service", "order-service",   "HTTP_CALLS", "查询订单报表数据", 0.95),
    ("report-service", "payment-service", "HTTP_CALLS", "查询支付报表数据", 0.95),
    ("report-service", "inventory-service", "HTTP_CALLS", "查询库存报表数据", 0.92),
    ("report-service", "user-service",    "HTTP_CALLS", "查询用户报表数据", 0.90),
    ("report-service", "product-service", "HTTP_CALLS", "查询商品报表数据", 0.90),

    # 搜索链路
    ("search-service", "product-service", "HTTP_CALLS", "查询商品索引数据", 0.95),
    ("search-service", "inventory-service", "HTTP_CALLS", "查询库存过滤", 0.88),

    # 运营后台链路
    ("admin-service", "order-service",   "HTTP_CALLS", "运营查询订单", 0.95),
    ("admin-service", "payment-service", "HTTP_CALLS", "运营查询支付", 0.95),
    ("admin-service", "product-service", "HTTP_CALLS", "运营查询商品", 0.95),
    ("admin-service", "user-service",    "HTTP_CALLS", "运营查询用户", 0.92),
    ("admin-service", "inventory-service", "HTTP_CALLS", "运营查询库存", 0.92),
    ("admin-service", "coupon-service",  "HTTP_CALLS", "运营查询优惠券", 0.90),
    ("admin-service", "report-service",  "HTTP_CALLS", "运营生成报表", 0.90),
]

# ============ 索引定义 (每个表的主键 + 业务索引) ============
INDEX_DEFS = {
    "t_order": [
        ("PRIMARY", "PRIMARY", True, True, ["order_id"]),
        ("idx_user_id", "BTREE", False, False, ["user_id"]),
        ("idx_order_state", "BTREE", False, False, ["order_state", "create_time"]),
        ("idx_create_time", "BTREE", False, False, ["create_time"]),
    ],
    "t_order_item": [
        ("PRIMARY", "PRIMARY", True, True, ["item_id"]),
        ("idx_order_id", "BTREE", False, False, ["order_id"]),
        ("idx_sku_id", "BTREE", False, False, ["sku_id"]),
    ],
    "t_order_status_log": [
        ("PRIMARY", "PRIMARY", True, True, ["log_id"]),
        ("idx_order_id", "BTREE", False, False, ["order_id", "create_time"]),
    ],
    "t_payment_record": [
        ("PRIMARY", "PRIMARY", True, True, ["record_id"]),
        ("idx_order_id", "BTREE", False, False, ["order_id"]),
        ("idx_pay_state", "BTREE", False, False, ["pay_state", "pay_time"]),
        ("idx_transaction_id", "BTREE", False, False, ["transaction_id"]),
    ],
    "t_payment_refund": [
        ("PRIMARY", "PRIMARY", True, True, ["refund_id"]),
        ("idx_order_id", "BTREE", False, False, ["order_id"]),
        ("idx_record_id", "BTREE", False, False, ["record_id"]),
    ],
    "t_payment_channel": [
        ("PRIMARY", "PRIMARY", True, True, ["channel_id"]),
        ("idx_channel_code", "BTREE", True, False, ["channel_code"]),
    ],
    "t_inventory": [
        ("PRIMARY", "PRIMARY", True, True, ["sku_id"]),
        ("idx_warehouse", "BTREE", False, False, ["warehouse_id"]),
        ("idx_alert", "BTREE", False, False, ["alert_threshold", "available_qty"]),
    ],
    "t_inventory_log": [
        ("PRIMARY", "PRIMARY", True, True, ["log_id"]),
        ("idx_sku_id", "BTREE", False, False, ["sku_id", "create_time"]),
        ("idx_biz_id", "BTREE", False, False, ["biz_id", "biz_type"]),
    ],
    "t_warehouse": [
        ("PRIMARY", "PRIMARY", True, True, ["warehouse_id"]),
        ("idx_warehouse_code", "BTREE", True, False, ["warehouse_code"]),
    ],
    "t_user": [
        ("PRIMARY", "PRIMARY", True, True, ["user_id"]),
        ("idx_phone", "BTREE", True, False, ["phone"]),
        ("idx_user_state", "BTREE", False, False, ["user_state", "create_time"]),
    ],
    "t_user_address": [
        ("PRIMARY", "PRIMARY", True, True, ["address_id"]),
        ("idx_user_id", "BTREE", False, False, ["user_id", "is_default"]),
    ],
    "t_user_level": [
        ("PRIMARY", "PRIMARY", True, True, ["level_id"]),
        ("idx_level_code", "BTREE", True, False, ["level_code"]),
    ],
    "t_logistics": [
        ("PRIMARY", "PRIMARY", True, True, ["logistics_id"]),
        ("idx_order_id", "BTREE", False, False, ["order_id"]),
        ("idx_tracking_no", "BTREE", True, False, ["tracking_no"]),
    ],
    "t_logistics_detail": [
        ("PRIMARY", "PRIMARY", True, True, ["detail_id"]),
        ("idx_logistics_id", "BTREE", False, False, ["logistics_id", "node_time"]),
    ],
    "t_carrier": [
        ("PRIMARY", "PRIMARY", True, True, ["carrier_id"]),
        ("idx_carrier_code", "BTREE", True, False, ["carrier_code"]),
    ],
    "t_product": [
        ("PRIMARY", "PRIMARY", True, True, ["product_id"]),
        ("idx_category_id", "BTREE", False, False, ["category_id", "sale_state"]),
        ("idx_spu_code", "BTREE", True, False, ["spu_code"]),
        ("idx_sale_state", "BTREE", False, False, ["sale_state", "create_time"]),
    ],
    "t_sku": [
        ("PRIMARY", "PRIMARY", True, True, ["sku_id"]),
        ("idx_product_id", "BTREE", False, False, ["product_id", "sale_state"]),
        ("idx_sku_code", "BTREE", True, False, ["sku_code"]),
    ],
    "t_category": [
        ("PRIMARY", "PRIMARY", True, True, ["category_id"]),
        ("idx_parent_id", "BTREE", False, False, ["parent_id", "sort_order"]),
    ],
    "t_brand": [
        ("PRIMARY", "PRIMARY", True, True, ["brand_id"]),
        ("idx_brand_code", "BTREE", True, False, ["brand_code"]),
    ],
    "t_cart_item": [
        ("PRIMARY", "PRIMARY", True, True, ["item_id"]),
        ("idx_user_sku", "BTREE", True, False, ["user_id", "sku_id"]),
    ],
    "t_coupon": [
        ("PRIMARY", "PRIMARY", True, True, ["coupon_id"]),
        ("idx_valid_time", "BTREE", False, False, ["valid_start", "valid_end", "status"]),
    ],
    "t_user_coupon": [
        ("PRIMARY", "PRIMARY", True, True, ["user_coupon_id"]),
        ("idx_user_coupon", "BTREE", False, False, ["user_id", "coupon_id"]),
        ("idx_use_state", "BTREE", False, False, ["use_state", "valid_end"]),
    ],
    "t_notification": [
        ("PRIMARY", "PRIMARY", True, True, ["notification_id"]),
        ("idx_user_id", "BTREE", False, False, ["user_id", "read_state", "create_time"]),
    ],
    "t_notify_template": [
        ("PRIMARY", "PRIMARY", True, True, ["template_id"]),
        ("idx_template_code", "BTREE", True, False, ["template_code"]),
    ],
    "t_audit_log": [
        ("PRIMARY", "PRIMARY", True, True, ["log_id"]),
        ("idx_biz", "BTREE", False, False, ["biz_type", "biz_id"]),
        ("idx_operator", "BTREE", False, False, ["operator_id", "create_time"]),
    ],
    "t_finance_daily": [
        ("PRIMARY", "PRIMARY", True, True, ["daily_id"]),
        ("idx_biz_date", "BTREE", True, False, ["biz_date"]),
    ],
    "t_finance_reconcile": [
        ("PRIMARY", "PRIMARY", True, True, ["reconcile_id"]),
        ("idx_biz_date_channel", "BTREE", False, False, ["biz_date", "channel_code"]),
    ],
    "t_search_log": [
        ("PRIMARY", "PRIMARY", True, True, ["log_id"]),
        ("idx_user_keyword", "BTREE", False, False, ["user_id", "keyword"]),
        ("idx_create_time", "BTREE", False, False, ["create_time"]),
    ],
    "t_report_template": [
        ("PRIMARY", "PRIMARY", True, True, ["template_id"]),
        ("idx_template_code", "BTREE", True, False, ["template_code"]),
    ],
    "t_admin_user": [
        ("PRIMARY", "PRIMARY", True, True, ["admin_id"]),
        ("idx_username", "BTREE", True, False, ["username"]),
    ],
    "t_admin_role": [
        ("PRIMARY", "PRIMARY", True, True, ["role_id"]),
        ("idx_role_code", "BTREE", True, False, ["role_code"]),
    ],
}


# ============ 主执行逻辑 ============
def main():
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    print("=" * 60)
    print("Bloodline 电商业务 Mock 数据生成器")
    print("=" * 60)

    # Phase 0: 清空所有表
    print("\n[Phase 0] 清空所有表...")
    cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
    tables = [
        'lineage_index_column', 'lineage_index', 'datasource_schema', 'datasource',
        'project_app', 'analysis_task', 'lineage_edge', 'project', 'lineage_column_ref', 'application'
    ]
    for t in tables:
        cursor.execute(f"DELETE FROM {t}")
        print(f"  - Cleared {t}")
    cursor.execute("SET FOREIGN_KEY_CHECKS = 1")
    conn.commit()

    # Phase 1: 插入 Applications
    print("\n[Phase 1] 插入 15 个微服务...")
    for svc in SERVICES:
        cursor.execute(
            "INSERT INTO application (tenant_id, app_id, app_name, git_url, default_branch, language) VALUES (%s, %s, %s, %s, %s, %s)",
            ('dept_01', svc['app_id'], svc['app_name'],
             f"https://github.com/company/{svc['app_id']}.git", 'release_sit', 'java')
        )
    conn.commit()

    # Phase 2: 插入 Projects
    print("\n[Phase 2] 插入 3 个项目...")
    project_id_map = {}
    for proj in PROJECTS:
        cursor.execute(
            "INSERT INTO project (tenant_id, project_code, project_name, baseline_branch, dev_branch, status) VALUES (%s, %s, %s, %s, %s, %s)",
            ('dept_01', proj['code'], proj['name'], 'release_sit', proj.get('dev_branch'), proj['status'])
        )
        project_id_map[proj['code']] = cursor.lastrowid
    conn.commit()

    # Phase 3: 关联应用 -> 项目
    print("\n[Phase 3] 关联应用到项目...")
    for svc in SERVICES:
        proj_code = SERVICE_PROJECT[svc['app_id']]
        pid = project_id_map[proj_code]
        cursor.execute(
            "INSERT INTO project_app (tenant_id, project_id, app_id) VALUES (%s, %s, %s)",
            ('dept_01', pid, svc['app_id'])
        )
    conn.commit()

    # Phase 4: 生成分析任务
    print("\n[Phase 4] 生成分析任务...")
    for svc in SERVICES:
        proj_code = SERVICE_PROJECT[svc['app_id']]
        pid = project_id_map[proj_code]
        # 每个应用至少 1 个 completed 任务
        num_files = random.randint(20, 50)
        num_rels = random.randint(8, 25)
        cursor.execute(
            "INSERT INTO analysis_task (tenant_id, project_id, app_id, branch, commit_sha, trigger_type, status, result_summary, started_at, completed_at) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
            ('dept_01', pid, svc['app_id'], 'release_sit', random_sha(), 2, 2,
             f"Analyzed {num_files} files, found {num_rels} relations",
             random_time_ago(30), random_time_ago(10))
        )
        # 部分应用有 pending/running 任务
        if random.random() < 0.3:
            cursor.execute(
                "INSERT INTO analysis_task (tenant_id, project_id, app_id, branch, trigger_type, status) VALUES (%s, %s, %s, %s, %s, %s)",
                ('dept_01', pid, svc['app_id'], 'feature/dev', 2, random.choice([0, 1]))
            )
    conn.commit()

    # Phase 5: Datasource + Schema
    print("\n[Phase 5] 生成数据源和 Schema...")
    app_schema_map = {}
    for svc in SERVICES:
        if svc['app_id'] == 'gateway-service':
            continue  # 网关无数据库
        db_name = svc['app_id'].replace('-', '_')
        cursor.execute(
            "INSERT INTO datasource (tenant_id, app_id, datasource_code, datasource_name, db_type, db_version, jdbc_url) VALUES (%s, %s, %s, %s, %s, %s, %s)",
            ('dept_01', svc['app_id'], 'default', 'Main Database', 'mysql', '8.0',
             f"jdbc:mysql://mysql.company.internal:3306/{db_name}?useUnicode=true")
        )
        ds_id = cursor.lastrowid
        cursor.execute(
            "INSERT INTO datasource_schema (tenant_id, datasource_id, schema_name, schema_alias, description) VALUES (%s, %s, %s, %s, %s)",
            ('dept_01', ds_id, db_name, 'main', f"{svc['app_name']}主库")
        )
        schema_id = cursor.lastrowid
        app_schema_map[svc['app_id']] = schema_id
    conn.commit()

    # Phase 6: LineageColumnRef (核心：字段级血缘)
    print("\n[Phase 6] 生成字段级血缘引用...")
    ref_count = 0
    for app_id, table_name, column_name, sql_preview, op_type, op_detail in COLUMN_REFS:
        sig = sql_signature(sql_preview)
        # source_location 格式: com.company.{app_id}.mapper.{TableName}Mapper.{methodName}
        mapper_class = ''.join([w.capitalize() for w in table_name.split('_')]) + 'Mapper'
        method_name = {
            'SELECT': ['selectById', 'selectByCondition', 'selectList', 'findById'],
            'INSERT': ['insert', 'insertSelective', 'save'],
            'UPDATE': ['updateById', 'updateState', 'updateSelective'],
            'DELETE': ['deleteById'],
        }.get(op_type, ['query'])[0]
        source_loc = f"com.company.{app_id.replace('-', '')}.mapper.{mapper_class}.{method_name}"

        cursor.execute(
            "INSERT INTO lineage_column_ref (app_id, table_name, column_name, sql_signature, sql_preview, operation_type, operation_detail, source_location) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
            (app_id, table_name, column_name, sig, sql_preview, op_type, op_detail, source_loc)
        )
        ref_count += 1
    conn.commit()
    print(f"  - 插入 {ref_count} 条字段级血缘引用")

    # Phase 7: 回填 lineage_column_ref 的 schema_id
    print("\n[Phase 7] 回填 schema_id...")
    for app_id, schema_id in app_schema_map.items():
        cursor.execute(
            "UPDATE lineage_column_ref SET schema_id = %s WHERE app_id = %s",
            (schema_id, app_id)
        )
    conn.commit()

    # Phase 8: LineageEdge (应用间调用)
    print("\n[Phase 8] 生成应用间调用关系...")
    existing_edges = set()
    edge_count = 0
    for source, target, rel_type, detail, confidence in LINEAGE_EDGES:
        edge_key = (source, target, rel_type)
        if edge_key in existing_edges:
            continue
        existing_edges.add(edge_key)
        proj_code = SERVICE_PROJECT.get(source, 'COMMON')
        pid = project_id_map.get(proj_code)
        target_type = {'CALLS': 'SERVICE', 'HTTP_CALLS': 'API_ENDPOINT', 'QUERIES': 'TABLE'}.get(rel_type, 'SERVICE')
        cursor.execute(
            "INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, project_id, confidence, source_type) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
            ('dept_01', source, target, target_type, target, detail, rel_type, 'release_sit', pid, confidence, 'AST')
        )
        edge_count += 1
    conn.commit()
    print(f"  - 插入 {edge_count} 条调用边")

    # Phase 9: LineageIndex + LineageIndexColumn
    print("\n[Phase 9] 生成索引数据...")
    idx_count = 0
    col_idx_count = 0
    for app_id, schema_id in app_schema_map.items():
        tables = TABLES.get(app_id, {})
        for table_name, columns in tables.items():
            idx_defs = INDEX_DEFS.get(table_name, [])
            if not idx_defs:
                # 自动生成主键索引
                idx_defs = [("PRIMARY", "PRIMARY", True, True, [columns[0][0]])]
            for idx_name, idx_type, is_unique, is_primary, idx_cols in idx_defs:
                cols_str = ','.join(idx_cols)
                cursor.execute(
                    "INSERT INTO lineage_index (tenant_id, schema_id, table_name, index_name, index_type, is_unique, is_primary, index_columns) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
                    ('dept_01', schema_id, table_name, idx_name, idx_type, is_unique, is_primary, cols_str)
                )
                idx_id = cursor.lastrowid
                idx_count += 1
                for order, col in enumerate(idx_cols, 1):
                    cursor.execute(
                        "INSERT INTO lineage_index_column (index_id, column_name, column_order, is_descending) VALUES (%s, %s, %s, %s)",
                        (idx_id, col, order, False)
                    )
                    col_idx_count += 1
    conn.commit()
    print(f"  - 插入 {idx_count} 个索引, {col_idx_count} 个索引列")

    # Phase 10: 添加一些额外的循环引用，让图更丰富
    print("\n[Phase 10] 补充额外的数据库查询边...")
    extra_edges = [
        ("order-service", "user-service", "QUERIES", "查询用户等级和地址信息", 0.90),
        ("order-service", "product-service", "QUERIES", "查询商品和SKU信息", 0.90),
        ("payment-service", "user-service", "QUERIES", "查询用户支付限额", 0.85),
        ("report-service", "order-service", "QUERIES", "统计订单报表数据", 0.92),
        ("report-service", "payment-service", "QUERIES", "统计支付报表数据", 0.92),
        ("report-service", "inventory-service", "QUERIES", "统计库存报表数据", 0.88),
        ("report-service", "user-service", "QUERIES", "统计用户报表数据", 0.85),
        ("finance-service", "order-service", "QUERIES", "统计订单金额核对", 0.90),
        ("finance-service", "payment-service", "QUERIES", "核对支付金额", 0.95),
        ("finance-service", "payment-service", "QUERIES", "统计退款金额", 0.92),
        ("admin-service", "order-service", "QUERIES", "运营管理查询订单", 0.95),
        ("admin-service", "payment-service", "QUERIES", "运营管理查询支付", 0.95),
        ("admin-service", "product-service", "QUERIES", "运营管理查询商品", 0.92),
        ("admin-service", "user-service", "QUERIES", "运营管理查询用户", 0.90),
        ("search-service", "inventory-service", "QUERIES", "搜索过滤库存不足商品", 0.85),
        ("cart-service", "product-service", "QUERIES", "查询商品上架状态", 0.88),
    ]
    for source, target, rel_type, detail, confidence in extra_edges:
        edge_key = (source, target, rel_type)
        if edge_key in existing_edges:
            continue
        existing_edges.add(edge_key)
        proj_code = SERVICE_PROJECT.get(source, 'COMMON')
        pid = project_id_map.get(proj_code)
        cursor.execute(
            "INSERT INTO lineage_edge (tenant_id, app_id, target_app_id, target_type, target_name, target_detail, relation_type, branch, project_id, confidence, source_type) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
            ('dept_01', source, target, 'TABLE', target, detail, rel_type, 'release_sit', pid, confidence, 'AST')
        )
        edge_count += 1
    conn.commit()

    # Summary
    print("\n" + "=" * 60)
    print("数据生成完成！统计如下：")
    print("=" * 60)
    for table in ['application', 'project', 'project_app', 'analysis_task', 'datasource',
                  'datasource_schema', 'lineage_edge', 'lineage_column_ref', 'lineage_index', 'lineage_index_column']:
        cursor.execute(f"SELECT COUNT(*) FROM {table}")
        count = cursor.fetchone()[0]
        print(f"  {table}: {count}")

    cursor.execute("SELECT COUNT(*) FROM lineage_column_ref WHERE schema_id IS NOT NULL")
    with_schema = cursor.fetchone()[0]
    print(f"  column_refs with schema_id: {with_schema}")

    # 显示影响链核心数据
    print("\n  --- 核心影响链预览 ---")
    print("  t_order.order_state 被引用情况:")
    cursor.execute(
        "SELECT DISTINCT app_id, operation_detail FROM lineage_column_ref WHERE table_name = 't_order' AND column_name = 'order_state' ORDER BY app_id"
    )
    for row in cursor.fetchall():
        print(f"    - {row[0]}: {row[1]}")

    print("\n  t_order.total_amount 被引用情况:")
    cursor.execute(
        "SELECT DISTINCT app_id, operation_detail FROM lineage_column_ref WHERE table_name = 't_order' AND column_name = 'total_amount' ORDER BY app_id"
    )
    for row in cursor.fetchall():
        print(f"    - {row[0]}: {row[1]}")

    print("\n  t_payment_record.pay_amount 被引用情况:")
    cursor.execute(
        "SELECT DISTINCT app_id, operation_detail FROM lineage_column_ref WHERE table_name = 't_payment_record' AND column_name = 'pay_amount' ORDER BY app_id"
    )
    for row in cursor.fetchall():
        print(f"    - {row[0]}: {row[1]}")

    cursor.close()
    conn.close()
    print("\nDone!")

if __name__ == '__main__':
    main()
