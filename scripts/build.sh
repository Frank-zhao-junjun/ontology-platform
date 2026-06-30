#!/bin/bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_DIR"

echo "[INFO] Building ontology-platform project..."
echo "[INFO] Working directory: $(pwd)"
echo "[INFO] Java version: $(java -version 2>&1 | head -n 1)"
echo "[INFO] Maven version: $(mvn -version 2>&1 | head -n 1)"

# 如果预构建 JAR 已存在，跳过 Maven 构建
PREBUILT_JAR="deploy/ontology-api-1.0.0-SNAPSHOT.jar"
if [ -f "$PREBUILT_JAR" ]; then
  echo "[INFO] Pre-built JAR found: $PREBUILT_JAR"
  echo "[INFO] Skipping Maven build, using pre-built JAR"
  mkdir -p ontology-api/target
  cp "$PREBUILT_JAR" ontology-api/target/ontology-api-1.0.0-SNAPSHOT.jar
  echo "[INFO] Build successful (pre-built)!"
  exit 0
fi

# 配置 Maven JVM 参数
export MAVEN_OPTS="-Xmx2g -XX:+UseG1GC -XX:+ParallelRefProcEnabled"

# 创建临时 settings.xml 使用阿里云镜像
SETTINGS_FILE=$(mktemp)
cat > "$SETTINGS_FILE" << 'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
EOF

echo "[INFO] Using Maven settings: $SETTINGS_FILE"

# 构建参数说明：
# -T 1C          = 并行构建（每 CPU 核心一个线程）
# -B             = 批处理模式（减少输出）
# --no-transfer-progress = 不显示下载进度条（减少 I/O）
# -nsu           = 不检查 SNAPSHOT 更新
# -DskipTests    = 跳过测试
# -Dmaven.javadoc.skip = 跳过 Javadoc 生成
# -Dmaven.source.skip    = 跳过 source JAR

mvn package -T 1C -B --no-transfer-progress -nsu \
  -DskipTests \
  -Dmaven.javadoc.skip=true \
  -Dmaven.source.skip=true \
  -s "$SETTINGS_FILE"

BUILD_RESULT=$?

# 清理临时文件
rm -f "$SETTINGS_FILE"

if [ $BUILD_RESULT -eq 0 ]; then
  echo "[INFO] Build successful!"
  echo "[INFO] JAR file: $(ls -lh ontology-api/target/*.jar 2>/dev/null | head -n 1)"
else
  echo "[ERROR] Build failed!"
  exit 1
fi
