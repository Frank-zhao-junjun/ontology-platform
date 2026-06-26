# =============================================
# 本体模型服务平台 Dockerfile
# 多阶段构建
# =============================================

# 第一阶段：构建
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# 复制Maven依赖文件
COPY pom.xml .
RUN cat pom.xml | grep -A1 '<artifactId>ontology-platform</artifactId>' | head -2 || true

# 复制源代码
COPY . .

# 构建项目
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -Pprod && \
    mv ontology-api/target/*.jar app.jar

# 第二阶段：运行
FROM eclipse-temurin:25-jre-alpine

# 安装必要工具
RUN apk add --no-cache \
    dumb-init \
    curl \
    tzdata

# 创建用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 设置工作目录
WORKDIR /app

# 复制构建产物
COPY --from=builder /app/ontology-api/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

# 设置非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# 启动命令
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.config.location=./,file:./config/"]
