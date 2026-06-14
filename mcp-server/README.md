# MCP Server — Ontology Platform 协议适配层

JSON-RPC 2.0 协议适配器，将 ontology-platform 的 Spring Boot REST API 暴露为 MCP 工具，供任意 AI Agent 直接调用。

## 技术栈

- Node.js 22 + TypeScript
- Express 5 + @modelcontextprotocol/sdk
- Zod 4（参数校验）
- vitest（测试）
- jsonwebtoken（JWT 认证）

## 启动

```bash
# 安装依赖
npm ci

# 开发模式（热重载）
npm run dev

# 构建
npm run build

# 生产运行
npm start

# 测试
npm test
```

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MCP_PORT` | `3001` | MCP Server 端口 |
| `PLATFORM_BASE_URL` | `http://localhost:8080` | Spring Boot 后端地址 |
| `PLATFORM_API_KEY` | `dev-api-key` | 后端 API Key |
| `REQUEST_TIMEOUT_MS` | `15000` | 后端请求超时 |

## API

| 端点 | 协议 | 说明 |
|------|------|------|
| `POST /mcp` | JSON-RPC 2.0 | MCP 协议入口（tools/list, tools/call） |
| `GET /health` | HTTP | 健康检查 |

## Docker

已在根项目 docker-compose 中编排：

```bash
cd ../docker
cp .env.example .env
docker compose up -d
```

## MCP 工具列表

| 工具 | 说明 | 权限 |
|------|------|------|
| `resolve_intent` | 自然语言 → 意图分类 | READ |
| `query_ontology` | 查询行为/事件/EPC 定义 | READ |
| `traverse_graph` | 本体对象图遍历 | READ |
| `validate_instruction` | 指令校验 | READ |
| `execute_action` | 动态动作执行 | 取决于 riskLevel |

## 架构

```
AI Agent → POST /mcp (JSON-RPC 2.0) → MCP Server → platform-client.ts → Spring Boot API
```
