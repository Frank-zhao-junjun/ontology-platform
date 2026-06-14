# =============================================
# MCP Server Dockerfile
# 基于 Node.js 的多阶段构建
# =============================================

# 第一阶段：构建
FROM node:22-alpine AS builder

WORKDIR /app

# 复制依赖文件
COPY package.json package-lock.json ./

# 安装依赖（含 devDependencies 用于 tsc）
RUN npm ci

# 复制源代码
COPY tsconfig.json vitest.config.ts ./
COPY src/ ./src/

# TypeScript 编译
RUN npx tsc

# 第二阶段：运行
FROM node:22-alpine AS runner

WORKDIR /app

# 复制运行时依赖
COPY package.json package-lock.json ./
RUN npm ci --omit=dev && npm cache clean --force

# 复制构建产物
COPY --from=builder /app/dist/ ./dist/

# 暴露端口
EXPOSE 3001

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=10s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:3001/health || exit 1

# 启动
CMD ["node", "dist/index.js"]
