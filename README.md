# Ontology Platform - 本体模型平台

企业级本体管理系统，基于领域驱动设计（DDD）和本体论架构，提供完整的本体模型、对象管理、MCP能力。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 3.2.x |
| **Java版本** | Java | 17 |
| **数据库** | PostgreSQL + Apache AGE | 15+ / 1.5.x |
| **缓存** | Redis | 7.x |
| **API文档** | SpringDoc OpenAPI | 2.5.x |
| **ORM** | MyBatis-Plus | 3.5.x |
| **数据库迁移** | Flyway | - |

## 项目结构

```
ontology-platform/
├── ontology-api/              # API层（Controller、DTO、配置）
├── ontology-application/      # 应用层（Service、Command/Query处理）
├── ontology-domain/           # 领域层（Entity、Value Object、Repository接口）
├── ontology-infrastructure/   # 基础设施层（Repository实现、数据库配置）
├── ontology-common/           # 公共模块（工具类、异常、常量、枚举）
├── db/
│   └── migrations/            # Flyway数据库迁移脚本
├── docker/                   # Docker配置文件
└── README.md
```

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose（用于本地开发环境）

### 本地开发环境搭建

#### 方式一：使用Docker Compose（推荐）

```bash
# 1. 进入docker目录
cd docker

# 2. 复制环境变量配置
cp .env.example .env

# 3. 启动服务
docker-compose up -d

# 4. 查看服务状态
docker-compose ps

# 5. 查看日志
docker-compose logs -f app
```

#### 方式二：本地安装

```bash
# 1. 安装PostgreSQL 15+并启用Apache AGE扩展
# 2. 安装Redis 7+
# 3. 创建数据库
createdb ontology

# 4. 配置环境变量或修改application.yml
```

### 构建项目

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package -DskipTests

# 构建Docker镜像
docker build -f docker/Dockerfile -t ontology-platform:latest ..
```

### 启动应用

```bash
# 使用Maven启动（开发环境）
mvn spring-boot:run -Pdev

# 或运行jar包
java -jar ontology-api/target/ontology-api-1.0.0-SNAPSHOT.jar
```

## API文档

启动应用后，访问以下地址：

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/api/v3/api-docs.yaml

## 主要API

### 本体管理

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/v1/ontologies` | 创建本体 |
| GET | `/v1/ontologies/{id}` | 获取本体详情 |
| GET | `/v1/ontologies` | 获取本体列表 |
| PUT | `/v1/ontologies/{id}` | 更新本体 |
| DELETE | `/v1/ontologies/{id}` | 删除本体 |
| POST | `/v1/ontologies/{id}/publish` | 发布本体 |
| POST | `/v1/ontologies/{id}/archive` | 归档本体 |
| POST | `/v1/ontologies/{id}/validate` | 验证本体 |

### 对象类型管理

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/v1/object-types` | 创建对象类型 |
| GET | `/v1/object-types/{id}` | 获取对象类型详情 |
| GET | `/v1/object-types` | 获取对象类型列表 |
| PUT | `/v1/object-types/{id}` | 更新对象类型 |
| DELETE | `/v1/object-types/{id}` | 删除对象类型 |

### 图遍历查询

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/v1/graphs/traverse` | 图遍历查询 |

## 配置说明

### 开发环境配置 (application.yml)

主要配置项：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ontology
    username: ontology
    password: ontology123
  data:
    redis:
      host: localhost
      port: 6379

ontology:
  graph:
    max-depth: 5        # 图遍历最大深度
    max-nodes: 1000    # 最大返回节点数
    timeout-ms: 5000   # 查询超时（毫秒）
```

### 环境变量

| 变量 | 默认值 | 描述 |
|------|--------|------|
| `DB_HOST` | localhost | PostgreSQL主机 |
| `DB_PORT` | 5432 | PostgreSQL端口 |
| `DB_NAME` | ontology | 数据库名 |
| `DB_USERNAME` | ontology | 数据库用户名 |
| `DB_PASSWORD` | ontology123 | 数据库密码 |
| `REDIS_HOST` | localhost | Redis主机 |
| `REDIS_PORT` | 6379 | Redis端口 |
| `REDIS_PASSWORD` | - | Redis密码 |
| `SPRING_PROFILES` | dev | Spring Profiles |

## 开发指南

### 代码规范

- **类命名**: PascalCase (如 `OntologyController`)
- **方法命名**: camelCase (如 `getOntologyById`)
- **变量命名**: camelCase (如 `ontologyId`)
- **常量命名**: UPPER_SNAKE_CASE (如 `MAX_PAGE_SIZE`)
- **数据库表名**: snake_case单数 (如 `ontology`)
- **数据库列名**: snake_case (如 `created_at`)

### API设计规范

- RESTful风格
- 使用JSON格式
- 分页使用cursor或offset
- 统一响应格式: `{code, message, data, meta}`

### 数据库设计规范

- 使用UUID作为主键
- 使用Flyway管理数据库迁移
- 每个表包含 `created_at` 和 `updated_at` 字段
- 使用JSONB存储扩展属性

## 部署

### Docker部署

```bash
# 构建镜像
docker build -f docker/Dockerfile -t ontology-platform:latest ..

# 运行容器
docker-compose -f docker/docker-compose.yml up -d
```

### Kubernetes部署

```bash
# 应用Kubernetes配置
kubectl apply -f k8s/
```

## 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify -Pintegration-test

# 生成测试覆盖率报告
mvn test jacoco:report
```

## 监控

- **健康检查**: `GET /api/actuator/health`
- **指标**: `GET /api/actuator/metrics`
- **Prometheus**: `GET /api/actuator/prometheus`

## 贡献

1. Fork本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

## 许可证

本项目基于 Apache License 2.0 许可证开源。

## 联系方式

- 邮箱: support@ontology-platform.com
- 项目地址: https://github.com/ontology-platform/ontology-platform
