#!/bin/bash

# =============================================
# 本体模型服务平台 - 启动脚本
# =============================================

set -e

# 项目根目录
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 函数定义
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Java
check_java() {
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
    REQUIRED_VERSION="25"
    
    if [ "$(echo -e "$REQUIRED_VERSION\n$JAVA_VERSION" | sort -V | head -n1)" != "$REQUIRED_VERSION" ]; then
        log_error "Java 25 or higher is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    log_info "Java version: $JAVA_VERSION"
}

# 检查Maven
check_maven() {
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed or not in PATH"
        exit 1
    fi
    
    MVN_VERSION=$(mvn -version 2>&1 | head -n 1 | awk '{print $3}')
    log_info "Maven version: $MVN_VERSION"
}

# 编译项目
build_project() {
    log_info "Building project..."
    mvn clean compile -DskipTests -q
    if [ $? -eq 0 ]; then
        log_info "Build successful!"
    else
        log_error "Build failed!"
        exit 1
    fi
}

# 启动应用
start_app() {
    log_info "Starting Ontology Platform..."
    
    # 设置环境变量
    export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES:-dev}"
    export DB_HOST="${DB_HOST:-localhost}"
    export DB_PORT="${DB_PORT:-5432}"
    export REDIS_HOST="${REDIS_HOST:-localhost}"
    export REDIS_PORT="${REDIS_PORT:-6379}"
    
    mvn spring-boot:run -pl ontology-api -am -Pdev
}

# 显示帮助
show_help() {
    echo "Ontology Platform - Startup Script"
    echo ""
    echo "Usage: ./scripts/startup.sh [command]"
    echo ""
    echo "Commands:"
    echo "  start       Start the application"
    echo "  build       Build the project"
    echo "  check       Check environment"
    echo "  help        Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  SPRING_PROFILES     Spring profile (default: dev)"
    echo "  DB_HOST            PostgreSQL host (default: localhost)"
    echo "  DB_PORT            PostgreSQL port (default: 5432)"
    echo "  REDIS_HOST         Redis host (default: localhost)"
    echo "  REDIS_PORT         Redis port (default: 6379)"
}

# 主逻辑
case "${1:-help}" in
    start)
        check_java
        check_maven
        build_project
        start_app
        ;;
    build)
        check_java
        check_maven
        build_project
        ;;
    check)
        check_java
        check_maven
        log_info "Environment check passed!"
        ;;
    help)
        show_help
        ;;
    *)
        log_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
