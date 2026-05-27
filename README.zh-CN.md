# Bloodline API

> 跨应用血缘分析系统的后端服务，面向企业级微服务架构。
> 通过静态分析 Java 源码提取 API 调用链与数据库依赖，构建可查询的血缘图谱。

## 概览

Bloodline API 是 Bloodline 平台的核心分析引擎与 REST 服务层。它解析 Java 源代码，识别以下依赖关系：

- **REST API 调用** — Spring `@RestController` 端点与 Feign 消费者
- **RPC 调用** — Dubbo `@Reference` 与 `@Service` 注解
- **数据库依赖** — MyBatis XML Mapper 与 SQL 查询

系统将提取出的关系存储为 `lineage_edge` 记录，支持在服务和表级别进行上下游影响分析。

## 特性

- **4 种解析器** — Dubbo、Feign、RestController、MyBatis
- **GitHub 集成** — Webhook Push 事件自动触发代码克隆与分析（基于 JGit）
- **异步分析** — 任务队列 + 定时调度器，支持 Webhook 触发与批量处理
- **血缘查询** — 上下游图谱遍历，支持递归展开
- **分支感知** — 边按分支与项目隔离，支持并行开发追踪
- **多租户** — 数据层租户隔离（可扩展）
- **27 个单元测试** — 覆盖解析器、服务层与 GitHub 集成

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
```

### 核心组件

| 组件 | 职责 |
|------|------|
| `JavaSourceParser` | 编排 4 个解析器，产出 `ParsedRelation` 对象 |
| `DubboParser` | 提取 Dubbo RPC 调用关系 |
| `FeignParser` | 提取 Feign HTTP 客户端调用关系 |
| `RestControllerParser` | 提取 REST 端点定义 |
| `MyBatisParser` | 从 XML Mapper 提取 SQL 表依赖 |
| `GitHubCodeFetchService` | 通过 JGit 克隆 GitHub 仓库，枚举 `.java` / `.xml` 文件 |
| `GitHubWebhookController` | 接收 Push 事件，映射仓库 URL 到应用，提交分析任务 |
| `AnalysisService` | 写入路径 — 删除已有边，批量插入新边 |
| `AnalysisTaskService` | 任务生命周期 — 提交、执行（含代码克隆）、状态追踪 |
| `LineageQueryService` | 读取路径 — 查询上下游，构建 `LineageGraph` |
| `AnalysisJobExecutor` | `@Scheduled` 调度器，每 30 秒轮询待处理任务 |

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
-- 运行 schema.sql 创建表
source bloodline-service/src/main/resources/db/schema.sql
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

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/v1/applications` | GET, POST | 列表 / 注册应用 |
| `/api/v1/applications/{appId}` | PUT, DELETE | 更新 / 删除应用 |
| `/api/v1/projects` | GET, POST | 列表 / 创建项目 |
| `/api/v1/lineage/graph` | GET | 查询应用血缘图谱 |
| `/api/v1/lineage/apps/{appId}/upstream` | GET | 上游依赖列表 |
| `/api/v1/lineage/apps/{appId}/downstream` | GET | 下游依赖列表 |
| `/api/v1/lineage/apps/{appId}/upstream/recursive` | GET | 递归上游（支持 maxDepth） |
| `/api/v1/lineage/apps/{appId}/downstream/recursive` | GET | 递归下游（支持 maxDepth） |
| `/api/v1/analysis/tasks` | GET, POST | 任务列表 / 提交异步分析任务 |
| `/api/v1/analysis/tasks/{id}` | GET | 获取任务状态 |
| `/api/v1/analysis/batch` | POST | 批量分析多个应用 |
| `/api/v1/github/webhook` | POST | GitHub Push Webhook |

## 数据模型

四张核心表：

- **`application`** — 已注册的微服务，含 `git_url`
- **`project`** — 开发项目，含分支追踪
- **`analysis_task`** — 异步分析任务状态
- **`lineage_edge`** — 核心血缘关系

完整 DDL 请见 `bloodline-service/src/main/resources/db/schema.sql`。

## 模块详情

### bloodline-analyzer

静态分析引擎。每个解析器操作 `CompilationUnit`（JavaParser AST）或 XML 文档（MyBatis Mapper），产出 `ParsedRelation` 列表：

```java
public class ParsedRelation {
    private String targetType;    // SERVICE, TABLE, API_ENDPOINT
    private String targetName;    // 服务名或表名
    private String relationType;  // CALLS, HTTP_CALLS, QUERIES
    private String targetDetail;  // URL、SQL 片段等
}
```

### bloodline-service

Spring Boot 应用，包含：

- **Controller** — `/api/v1/` 下的 REST 端点
- **GitHub Webhook** — Push 事件接收，仓库 URL 映射
- **Service** — 业务逻辑、事务性边替换、JGit 代码克隆
- **Job Executor** — `@Scheduled(fixedDelay = 30000)` 轮询待处理任务
- **MyBatis Mapper** — 注解式 SQL 映射

## 测试

```bash
# 全部测试
mvn test

# 单个模块
mvn test -pl bloodline-analyzer

# 单个测试类
mvn test -pl bloodline-analyzer -Dtest=DubboParserTest
```

测试覆盖：

| 模块 | 测试数 | 重点 |
|------|--------|------|
| bloodline-analyzer | 29 | 解析器正确性与边界情况 |
| bloodline-service | 27 | 服务层、任务生命周期、GitHub 集成 |

## 协议

MIT
