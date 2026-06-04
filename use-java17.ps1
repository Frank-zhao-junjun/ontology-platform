# use-java17.ps1 — 为本项目设置 Java 17 + Maven 环境
$javaHome = "e:\00 - AI\本体建模\.jdk\jdk-17.0.18(1)"
$mavenHome = "e:\00 - AI\本体建模\.maven\maven-3.9.16"
$env:JAVA_HOME = $javaHome
$env:MAVEN_HOME = $mavenHome
$env:PATH = "$javaHome\bin;$mavenHome\bin;$env:PATH"
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
Write-Host "MAVEN_HOME: $env:MAVEN_HOME" -ForegroundColor Green
java -version 2>&1 | Select-Object -First 1
mvn --version 2>&1 | Select-Object -First 1
