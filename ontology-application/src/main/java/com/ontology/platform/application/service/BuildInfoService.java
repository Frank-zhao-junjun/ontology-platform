package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.BuildInfoResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * 构建信息服务
 * Build Information Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuildInfoService {

    private static final String BUILD_INFO_PATH = "META-INF/build-info.properties";
    private static final String DEFAULT_VERSION = "unknown";
    private static final String DEFAULT_BUILD_TIME = "unknown";
    private static final String DEFAULT_JAVA_VERSION = "unknown";

    private final Environment environment;

    /**
     * Surefire 报告搜索根目录，默认使用运行时工作目录
     */
    @Value("${build.info.surefire.base-dir:${user.dir}}")
    private String surefireBaseDir;

    private final Properties buildProperties = new Properties();

    @PostConstruct
    public void init() {
        loadBuildInfo();
    }

    private void loadBuildInfo() {
        ClassPathResource resource = new ClassPathResource(BUILD_INFO_PATH);
        if (!resource.exists()) {
            log.warn("Build info properties not found at classpath:{}, using default values", BUILD_INFO_PATH);
            return;
        }
        try (InputStream is = resource.getInputStream()) {
            buildProperties.load(is);
            log.info("Loaded build info: version={}, time={}, javaVersion={}",
                    buildProperties.getProperty("build.version"),
                    buildProperties.getProperty("build.time"),
                    buildProperties.getProperty("build.java.version"));
        } catch (IOException e) {
            log.warn("Failed to load build info properties: {}", e.getMessage());
        }
    }

    /**
     * 获取完整构建信息
     */
    public BuildInfoResponse getBuildInfo() {
        return BuildInfoResponse.builder()
                .version(getVersion())
                .buildTime(getBuildTime())
                .javaVersion(getJavaVersion())
                .activeProfiles(getActiveProfiles())
                .testCount(getTestCount())
                .uptime(getUptime())
                .build();
    }

    /**
     * 获取构建版本
     */
    public String getVersion() {
        return buildProperties.getProperty("build.version", DEFAULT_VERSION);
    }

    /**
     * 获取构建时间
     */
    public String getBuildTime() {
        return buildProperties.getProperty("build.time", DEFAULT_BUILD_TIME);
    }

    /**
     * 获取 JDK 版本（优先使用构建时版本，否则取运行时版本）
     */
    public String getJavaVersion() {
        String javaVersion = buildProperties.getProperty("build.java.version");
        if (javaVersion != null && !javaVersion.isBlank()) {
            return javaVersion;
        }
        String runtimeVersion = System.getProperty("java.version");
        return runtimeVersion != null && !runtimeVersion.isBlank() ? runtimeVersion : DEFAULT_JAVA_VERSION;
    }

    /**
     * 获取当前激活的 profile
     */
    public List<String> getActiveProfiles() {
        return Arrays.asList(environment.getActiveProfiles());
    }

    /**
     * 获取应用启动时长（毫秒）
     */
    public long getUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    /**
     * 从 surefire-reports 目录递归统计测试总数
     */
    public int getTestCount() {
        Path baseDir = Paths.get(surefireBaseDir);
        if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
            log.warn("Surefire base directory does not exist: {}", baseDir);
            return 0;
        }

        try (Stream<Path> dirs = Files.walk(baseDir)) {
            return dirs.filter(Files::isDirectory)
                    .filter(path -> "surefire-reports".equals(path.getFileName().toString()))
                    .flatMap(this::countTestsInReportDir)
                    .mapToInt(Integer::intValue)
                    .sum();
        } catch (IOException e) {
            log.warn("Failed to scan surefire reports: {}", e.getMessage());
            return 0;
        }
    }

    private Stream<Integer> countTestsInReportDir(Path reportDir) {
        try (Stream<Path> files = Files.walk(reportDir)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .map(this::parseTestCount)
                    .toList()
                    .stream();
        } catch (IOException e) {
            log.warn("Failed to read surefire report directory {}: {}", reportDir, e.getMessage());
            return Stream.empty();
        }
    }

    private int parseTestCount(Path xmlFile) {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(is);
            String tests = doc.getDocumentElement().getAttribute("tests");
            return tests.isBlank() ? 0 : Integer.parseInt(tests);
        } catch (Exception e) {
            log.warn("Failed to parse surefire report {}: {}", xmlFile, e.getMessage());
            return 0;
        }
    }
}
