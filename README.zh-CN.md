# Bloodline API

> 跨应用血缘分析系统的后端服务，面向企业级微服务架构。
> 通过静态分析 Java 源码提取 API 调用链与数据库依赖，构建可查询的血缘图谱。

## 概览

Bloodline API 是 Bloodline 平台的核心分析引擎与 REST 服务层。它解析 Java 源代码，识别以下依赖关系：

- **REST API 调用** — Spring `@RestController` 端点与 Feign 消费者
- **RPC 调用** — Dubbo `@Reference` 与 `@Service` 注解
- **数据库依赖** — MyBatis XML Mapper 与 SQL 查询
- **字段级引用** — 通过 JSqlParser AST 遍历提取 SQL 中的列级引用（A1）

系统将提取出的关系存储为 `lineage_edge` 记录（服务/表级）和 `lineage_column_ref` 记录（字段级），
支持在多粒度下进行上下游影响分析。

**V2 架构**引入了通用节点级血缘图模型（`lineage_node` + `lineage_edge_v2`），
支持多租户、版本管理、快照管理与冲突分析。同时包含兼容 OpenLineage 的 ETL 适配器，
用于从数据管道中摄取血缘事件。

## 特性

- **5 种解析器** — Dubbo、Feign、RestController、MyBatis、JPA Entity
- **字段级血缘（A1）** — 通过 JSqlParser AST Visitor 从 SQL 中提取 `table.column` 引用
- **影响分析 API** — `POST /api/v1/impact-analysis` 生成跨字段影响报告
- **GitHub 集成** — Webhook Push 事件自动触发代码克隆与分析（基于 JGit）
- **异步分析** — 任务队列 + 定时调度器，支持 Webhook 触发与批量处理
- **血缘查询** — 上下游图谱遍历，支持递归展开
- **字段级图谱** — `GET /api/v1/lineage/apps/{appId}/fields` 获取列级血缘
- **分支感知** — 边按分支与项目隔离，支持并行开发追踪
- **V2 通用节点级血缘图** — TABLE/JOB/FIELD/API 节点，支持递归 CTE 上下游查询
- **多租户** — ThreadLocal 租户上下文 + MVC 拦截器，数据层隔离
- **快照管理** — 创建、列出、对比血缘快照，支持边数据序列化存储
- **冲突分析** — 检测快照间 ADDED/REMOVED 边差异，按高危/中危/低危分级
- **OpenLineage ETL 适配器** — 通过文件扫描摄取数据管道的 JSON RunEvent
- **软删除版本控制** — 边历史追踪与版本号管理
- **85+ 单元测试** — 覆盖解析器、V2 控制器、快照/冲突分析、服务层与 GitHub 集成

## 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 2.6.14 |
| 语言 | Java 8 |
| 构建 | Maven 3.x |
| 数据访问 | MyBatis（注解方式） |
| 数据库 | MySQL 8+ |
| AST 解析 | JavaParser 3.24.2 |
| SQL 解析 | JSqlParser 4.5 |
| Git 客户端 | JGit 5.13.1 |
| 测试 | JUnit 5, AssertJ, Mockito |

## 架构

```
bloodline-common   -- 枚举、常量（Jackson）
       |
       ▼
bloodline-domain   -- 实体、MyBatis Mapper（mybatis-spring-boot）
       |
       ▼
bloodline-analyzer -- 静态代码解析器（JavaParser、JSqlParser）
       |
       ▼
bloodline-service  -- Spring Boot Web 应用、REST Controller、异步任务
       |
       ▼
bloodline-etl-adapter -- OpenLineage 事件解析、文件扫描、摄取服务
```

### 核心组件

| 组件 | 职责 |
|------|------|
| `JavaSourceParser` | 编排 5 个解析器，产出 `ParsedRelation` 对象 |
| `DubboParser` | 提取 Dubbo RPC 调用关系 |
| `FeignParser` | 提取 Feign HTTP 客户端调用关系 |
| `RestControllerParser` | 提取 REST 端点定义 |
| `MyBatisParser` | 从 XML Mapper 提取 SQL 表与列依赖 |
| `ColumnRefExtractor` | JSqlParser AST Visitor，提取 `table.column` 引用 |
| `GitHubCodeFetchService` | 通过 JGit 克隆 GitHub 仓库，枚举 `.java` / `.xml` 文件 |
| `GitHubWebhookController` | 接收 Push 事件，映射仓库 URL 到应用，提交分析任务 |
| `AnalysisService` | 写入路径 — 事务性边与列引用替换 |
| `AnalysisTaskService` | 任务生命周期 — 提交、执行（含代码克隆）、状态追踪 |
| `LineageQueryService` | 读取路径 — 查询上下游，构建 `LineageGraph` |
| `LineageColumnRefService` | 列引用查询 — 按应用、列名或 SQL 签名检索 |
| `ImpactAnalysisService` | 核心影响分析 — 受影响应用与跨字段关联关系 |
| `AnalysisJobExecutor` | `@Scheduled` 调度器，每 30 秒轮询待处理任务 |
| `TenantInterceptor` | Spring MVC 拦截器，从 `X-Tenant-ID` Header 提取租户信息写入 ThreadLocal |
| `LineageV2Controller` | V2 REST 端点 — 基于 CTE 的递归上下游查询 |
| `SnapshotService` | 创建带序列化边数据的快照，按租户列出 |
| `ConflictAnalyzer` | 对比两个快照，报告 ADDED/REMOVED 边差异及严重级别 |
| `OpenLineageEventParser` | 将 OpenLineage RunEvent JSON 解析为内部 `LineageEvent` DTO |
| `OpenLineageFileScanner` | `@Scheduled` 文件系统扫描器，监听 `.json` 血缘事件 |
| `LineageIngestionService` | 编排 OpenLineage 摄取流程 — 解析、转换节点/边、持久化 |

## 快速开始

### 前置条件

- JDK 8+
- Maven 3.6+
- MySQL 8.0+

### 1. 数据库初始化

```bash
mysql -u root -p
```

```sql
CREATE DATABASE bloodline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 运行 schema.sql 创建 V1 表
source bloodline-service/src/main/resources/db/schema.sql
-- 运行 V2 表结构变更
source bloodline-service/src/main/resources/db/schema-v2-changes.sql
-- （可选）将现有 V1 数据迁移至 V2 模型
source bloodline-service/src/main/resources/db/migration-v1-to-v2.sql
```

### 2. 配置数据库

编辑 `bloodline-service/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bloodline
    username: bloodline
    password: bloodline
```

### 3. 构建

```bash
mvn clean install
```

### 4. 运行

```bash
mvn spring-boot:run -pl bloodline-service
```

服务启动于 `http://localhost:8080`。

### 5. 运行测试

```bash
mvn test
```

## REST API

### 应用管理

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/v1/applications` | GET, POST | 列表 / 注册应用 |
| `/api/v1/applications/{appId}` | PUT, DELETE | 更新 / 删除应用 |

### 血缘查询

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/v1/lineage/graph` | GET | 查询应用血缘图谱 |
| `/api/v1/lineage/apps/{appId}/upstream` | GET | 上游依赖列表 |
| `/api/v1/lineage/apps/{appId}/downstream` | GET | 下游依赖列表 |
| `/api/v1/lineage/apps/{appId}/upstream/recursive` | GET | 递归上游（支持 maxDepth） |
| `/api/v1/lineage/apps/{appId}/downstream/recursive` | GET | 递归下游（支持 maxDepth） |
| `/api/v1/lineage/apps/{appId}/fields` | GET | 字段级血缘查询 |
| `/api/v1/lineage/apps/{appId}/tables` | GET | 应用引用的数据表列表 |
| `/api/v1/lineage/tables/{tableName}/apps` | GET | 使用指定数据表的应用列表 |
| `/api/v1/lineage/tables/{tableName}/columns` | GET | 指定数据表的字段列表 |

### 影响分析

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/v1/impact-analysis` | POST | 字段变更影响报告生成 |

### 分析任务

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/v1/analysis/tasks` | GET, POST | 任务列表 / 提交异步分析任务 |
| `/api/v1/analysis/tasks/{id}` | GET | 获取任务状态 |
| `/api/v1/analysis/batch` | POST | 批量分析多个应用 |
| `/api/v1/github/webhook` | POST | GitHub Push Webhook |

### V2 血缘（节点中心）

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/v2/tenants` | GET, POST | 租户列表 / 创建租户 |
| `/api/v2/lineage/nodes/{nodeId}/upstream` | GET | 递归上游查询（支持 `maxDepth`） |
| `/api/v2/lineage/nodes/{nodeId}/downstream` | GET | 递归下游查询（支持 `maxDepth`） |
| `/api/v2/lineage/graph` | GET | 查询节点的完整上下游 |
| `/api/v2/lineage/snapshots` | GET, POST | 快照列表 / 创建快照 |
| `/api/v2/lineage/snapshots/{id}` | GET | 快照详情 |
| `/api/v2/lineage/conflict/analyze` | POST | 对比两个快照，报告冲突差异 |

## 数据模型

五张 V1 核心表 + 五张 V2 表：

**V1 表：**
- **`application`** — 已注册的微服务，含 `git_url`
- **`project`** — 开发项目，含分支追踪
- **`analysis_task`** — 异步分析任务状态
- **`lineage_edge`** — 服务/表级血缘关系
- **`lineage_column_ref`** — 字段级 SQL 列引用

**V2 表：**
- **`tenant`** — 多租户隔离
- **`lineage_node`** — 通用节点（TABLE/JOB/FIELD/API）
- **`lineage_edge_v2`** — 节点级血缘，支持软删除版本控制
- **`lineage_edge_history`** — 边版本历史
- **`lineage_snapshot`** — 快照元数据，含序列化边数据

完整 DDL 请见 `bloodline-service/src/main/resources/db/schema.sql`（V1）与 `schema-v2-changes.sql`（V2）。

### lineage_column_ref

存储从 SQL 中提取的列引用：

| 字段 | 说明 |
|------|------|
| `app_id` | 引用该列的应用 |
| `table_name` | 数据库表 |
| `column_name` | 列名 |
| `sql_signature` | SQL 文本的 MD5（用于将同 SQL 中共同出现的列分组） |
| `sql_preview` | SQL 前 200 字符 |
| `operation_type` | SELECT / INSERT / UPDATE / DELETE |
| `operation_detail` | READ / WRITE / WHERE / JOIN / GROUP_BY / ORDER_BY |
| `source_location` | 源码位置（如 `OrderMapper.java:15`） |

## 模块详情

### bloodline-analyzer

静态分析引擎。每个解析器操作 `CompilationUnit`（JavaParser AST）或 XML 文档（MyBatis Mapper），产出 `ParsedRelation` 列表：

```java
public class ParsedRelation {
    private String targetType;    // SERVICE, TABLE, COLUMN
    private String targetName;    // 服务名、表名或 "table.column"
    private String relationType;  // CALLS, HTTP_CALLS, QUERIES
    private String targetDetail;  // SQL 操作详情（READ, WRITE, WHERE 等）
    private String targetAppId;   // COLUMN 类型：存储操作类型（SELECT 等）
    private String sqlSignature;  // SQL MD5
    private String sqlPreview;    // SQL 预览
    private String sourceLocation; // 代码位置
}
```

`ColumnRefExtractor` 使用 JSqlParser AST Visitor 从 SELECT、INSERT、UPDATE、DELETE 语句中提取所有 `table.column` 引用，包括：
- SELECT 投影、WHERE、JOIN ON、GROUP BY、ORDER BY、HAVING
- INSERT 列列表与 VALUES 表达式
- UPDATE SET 左右两侧与 WHERE
- JOIN 查询的表别名解析

### bloodline-service

Spring Boot 应用，包含：

- **Controller** — `/api/v1/` 下的 REST 端点
- **GitHub Webhook** — Push 事件接收，仓库 URL 映射
- **Service** — 业务逻辑、事务性边替换、JGit 代码克隆、影响分析
- **Job Executor** — `@Scheduled(fixedDelay = 30000)` 轮询待处理任务
- **MyBatis Mapper** — 注解式 SQL 映射

## 测试

```bash
# 全部测试
mvn test

# 单个模块
mvn test -pl bloodline-analyzer

# 单个测试类
mvn test -pl bloodline-analyzer -Dtest=ColumnRefExtractorTest
```

测试覆盖：

| 模块 | 测试数 | 重点 |
|------|--------|------|
| bloodline-analyzer | 42 | 解析器正确性、列提取、别名解析 |
| bloodline-etl-adapter | 2 | OpenLineage JSON 解析 |
| bloodline-service | 41 | 服务层、V2 控制器、快照/冲突分析、任务生命周期、影响分析、GitHub 集成 |

## 协议

MIT
