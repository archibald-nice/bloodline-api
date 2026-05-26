# Bloodline API

> Cross-application lineage analysis system for enterprise microservices.
> Statically analyzes Java source code to extract API call chains and database dependencies,
> storing them in a queryable lineage graph.

## Overview

Bloodline API provides the core analysis engine and REST services for tracing dependencies
across microservices. It parses Java source code to identify:

- **REST API calls** — Spring `@RestController` endpoints and Feign client consumers
- **RPC invocations** — Dubbo `@Reference` and `@Service` annotations
- **Database dependencies** — MyBatis XML mappers and SQL queries

The system stores extracted relationships as `lineage_edge` records, enabling upstream/downstream
impact analysis at the service and table level.

## Features

- **4 Parsers** — Dubbo, Feign, RestController, MyBatis
- **Async analysis** — Task queue with scheduled executor, webhook triggers, batch processing
- **Lineage queries** — Upstream/downstream graph traversal with confidence scoring
- **Branch awareness** — Edges scoped by branch and project, enabling parallel development tracking
- **Multi-tenant** — Tenant isolation at the data layer (ready for extension)
- **44 unit tests** — Full coverage of parsers and service layer

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 2.6.14 |
| Language | Java 8 |
| Build | Maven 3.x |
| Data access | MyBatis (annotation-based) |
| Database | MySQL 8+ |
| AST parsing | JavaParser 3.24.2 |
| SQL parsing | JSqlParser 4.5 |
| Testing | JUnit 5, AssertJ, Mockito |

## Architecture

```
bloodline-common   ── enums, constants (Jackson)
       │
       ▼
bloodline-domain   ── entities, MyBatis mappers (mybatis-spring-boot)
       │
       ▼
bloodline-analyzer ── static code parsers (JavaParser, JSqlParser)
       │
       ▼
bloodline-service  ── Spring Boot web app, REST controllers, async jobs
```

### Key Components

| Component | Responsibility |
|-----------|---------------|
| `JavaSourceParser` | Orchestrates 4 parsers, produces `ParsedRelation` objects |
| `DubboParser` | Extracts Dubbo RPC call relationships |
| `FeignParser` | Extracts Feign HTTP client call relationships |
| `RestControllerParser` | Extracts REST endpoint definitions |
| `MyBatisParser` | Extracts SQL table dependencies from XML mappers |
| `AnalysisService` | Write path — deletes existing edges, batch-inserts new ones |
| `LineageQueryService` | Read path — queries upstream/downstream, builds `LineageGraph` |
| `AnalysisTaskService` | Task lifecycle — submit, execute, track status |
| `AnalysisJobExecutor` | `@Scheduled` executor polling pending tasks every 30s |

## Quick Start

### Prerequisites

- JDK 8+
- Maven 3.6+
- MySQL 8.0+

### 1. Database Setup

```bash
mysql -u root -p
```

```sql
CREATE DATABASE bloodline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Run schema.sql to create tables
source bloodline-service/src/main/resources/db/schema.sql
```

### 2. Configure Database

Edit `bloodline-service/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bloodline
    username: bloodline
    password: bloodline
```

### 3. Build

```bash
mvn clean install
```

### 4. Run

```bash
mvn spring-boot:run -pl bloodline-service
```

The service starts on `http://localhost:8080`.

### 5. Run Tests

```bash
mvn test
```

## REST API

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/applications` | GET, POST | List / register applications |
| `/api/v1/projects` | GET, POST | List / create projects |
| `/api/v1/lineage/graph` | GET | Query lineage graph for an app |
| `/api/v1/lineage/upstream` | GET | List upstream dependencies |
| `/api/v1/lineage/downstream` | GET | List downstream dependents |
| `/api/v1/analysis` | POST | Trigger synchronous analysis |
| `/api/v1/analysis/tasks` | POST | Submit async analysis task |
| `/api/v1/analysis/tasks/{id}` | GET | Get task status |
| `/api/v1/analysis/webhook` | POST | Webhook-triggered analysis |
| `/api/v1/analysis/batch` | POST | Batch analysis for multiple apps |

## Data Model

Four core tables:

- **`application`** — Registered microservices
- **`project`** — Development projects with branch tracking
- **`analysis_task`** — Async analysis job status
- **`lineage_edge`** — Core lineage relationships

See [docs/database-schema.sql](bloodline-service/src/main/resources/db/schema.sql) for full DDL.

## Module Details

### bloodline-analyzer

The static analysis engine. Each parser operates on `CompilationUnit` (JavaParser AST) or
XML documents (MyBatis mappers) and produces a list of `ParsedRelation`:

```java
public class ParsedRelation {
    private String targetType;    // SERVICE, TABLE, API_ENDPOINT
    private String targetName;    // service name or table name
    private String relationType;  // CALLS, HTTP_CALLS, QUERIES
    private String targetDetail;  // URL, SQL snippet, etc.
}
```

### bloodline-service

Spring Boot application with:

- **Controllers** — REST endpoints under `/api/v1/`
- **Services** — Business logic, transactional edge replacement
- **Job Executor** — `@Scheduled(fixedDelay = 30000)` polling pending tasks
- **MyBatis Mappers** — Annotation-based SQL mapping

## Testing

```bash
# All tests
mvn test

# Single module
mvn test -pl bloodline-analyzer

# Single test class
mvn test -pl bloodline-analyzer -Dtest=DubboParserTest
```

Test coverage:

| Module | Tests | Focus |
|--------|-------|-------|
| bloodline-analyzer | 29 | Parser correctness & edge cases |
| bloodline-service | 15 | Service layer & task lifecycle |

## License

MIT
