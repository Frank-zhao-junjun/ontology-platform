package com.ontology.platform.infrastructure.bridge;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 桥接执行层 — 通过 subprocess 调用 Python 桥接脚本（kimi/claude/codex）。
 */
@Slf4j
@Service
public class AgentBridgeService {

    private static final long DEFAULT_TIMEOUT_SECONDS = 300;
    private static final int MAX_OUTPUT_BYTES = 1_048_576; // 1MB 输出上限

    // 允许的 cwd 前缀白名单
    private static final List<String> ALLOWED_CWD_PREFIXES = List.of(
            "D:\\AI\\", "D:/AI/",
            "C:\\Users\\admin\\", "C:/Users/admin/"
    );

    @Value("${agent.bridge-dir:D:\\AI\\agent-bridge}")
    private String bridgeDir;

    @Value("${agent.allowed-cwd-prefixes:}")
    private String allowedCwdPrefixes;

    // ============ 公开 API ============

    public AgentResult executeKimi(String prompt, String cwd, Long timeoutSec) {
        String script = Paths.get(bridgeDir, "kimi_bridge.py").toString();
        return executePython(script, prompt, cwd, timeoutSec);
    }

    public AgentResult executeClaude(String prompt, String cwd, Long timeoutSec,
                                     Integer maxTurns, String model) {
        String script = Paths.get(bridgeDir, "claude_bridge.py").toString();
        return executePython(script, prompt, cwd, timeoutSec);
    }

    public AgentResult executeCodex(String prompt, String cwd, Long timeoutSec) {
        return executeDirect("codex", prompt, cwd, timeoutSec);
    }

    // ============ 内部实现 ============

    private AgentResult executePython(String scriptPath, String prompt,
                                      String cwd, Long timeoutSec) {
        if (!Files.exists(Path.of(scriptPath))) {
            return AgentResult.builder()
                    .status("FAILURE")
                    .errorMessage("桥接脚本不存在: " + scriptPath)
                    .build();
        }

        List<String> cmd = new ArrayList<>(List.of("python", scriptPath));
        if (prompt != null && !prompt.isBlank()) {
            cmd.add(prompt);
        }

        return runProcess(cmd, cwd, timeoutSec != null ? timeoutSec : DEFAULT_TIMEOUT_SECONDS);
    }

    private AgentResult executeDirect(String command, String prompt,
                                      String cwd, Long timeoutSec) {
        List<String> cmd = new ArrayList<>(List.of(command, "-p", prompt));
        return runProcess(cmd, cwd, timeoutSec != null ? timeoutSec : DEFAULT_TIMEOUT_SECONDS);
    }

    private AgentResult runProcess(List<String> command, String cwd, long timeoutSec) {
        long start = System.currentTimeMillis();

        // cwd 校验
        Path resolvedCwd = resolveCwd(cwd);
        if (resolvedCwd == null) {
            return AgentResult.builder()
                    .status("FAILURE")
                    .errorMessage("非法的工作目录: " + cwd)
                    .build();
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(resolvedCwd.toFile());
            pb.redirectErrorStream(true); // 合并 stdout/stderr，避免死锁

            String cmdLog = command.get(0) + " (" + (command.size() - 1) + " args)";
            log.info("Exec agent: {} | cwd={} | timeout={}s", cmdLog, resolvedCwd, timeoutSec);
            Process process = pb.start();

            // 读取输出（带上限）
            String output = readStreamWithLimit(process.getInputStream(), MAX_OUTPUT_BYTES);

            boolean finished = process.waitFor(timeoutSec, TimeUnit.SECONDS);
            long elapsed = System.currentTimeMillis() - start;

            if (!finished) {
                process.destroyForcibly();
                log.warn("Agent task timed out after {}s", timeoutSec);
                return AgentResult.builder()
                        .status("TIMEOUT")
                        .output(truncateOutput(output))
                        .errorMessage("执行超时(>" + timeoutSec + "s)")
                        .durationMs(elapsed)
                        .build();
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.warn("Agent task failed (exit={})", exitCode);
                return AgentResult.builder()
                        .status("FAILURE")
                        .output(truncateOutput(output))
                        .errorMessage("exit code: " + exitCode)
                        .durationMs(elapsed)
                        .build();
            }

            log.info("Agent task completed in {}ms", elapsed);
            return AgentResult.builder()
                    .status("SUCCESS")
                    .output(truncateOutput(output))
                    .durationMs(elapsed)
                    .build();

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("Agent task error: {}", e.getMessage(), e);
            return AgentResult.builder()
                    .status("FAILURE")
                    .errorMessage(e.getMessage())
                    .durationMs(elapsed)
                    .build();
        }
    }

    /**
     * cwd 路径校验：必须存在且在允许的白名单前缀中。
     */
    private Path resolveCwd(String cwd) {
        if (cwd == null || cwd.isBlank()) {
            return Paths.get(".").toAbsolutePath().normalize();
        }
        Path path = Paths.get(cwd).normalize();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return null;
        }
        String abs = path.toAbsolutePath().toString().replace('\\', '/');
        // 先检查配置的白名单
        if (allowedCwdPrefixes != null && !allowedCwdPrefixes.isBlank()) {
            for (String prefix : allowedCwdPrefixes.split(",")) {
                if (abs.startsWith(prefix.trim().replace('\\', '/'))) {
                    return path;
                }
            }
        }
        // 再检查默认白名单
        for (String prefix : ALLOWED_CWD_PREFIXES) {
            if (abs.startsWith(prefix.replace('\\', '/'))) {
                return path;
            }
        }
        return null;
    }

    /**
     * 读取 InputStream 并限制最大字节数。
     */
    private static String readStreamWithLimit(InputStream in, int maxBytes) throws Exception {
        byte[] buf = new byte[8192];
        int total = 0;
        StringBuilder sb = new StringBuilder();
        while (true) {
            int n = in.read(buf);
            if (n == -1) break;
            int available = Math.min(n, maxBytes - total);
            if (available <= 0) {
                sb.append("\n... [输出截断，超过 ").append(maxBytes / 1024).append("KB]");
                break;
            }
            sb.append(new String(buf, 0, available, StandardCharsets.UTF_8));
            total += available;
        }
        return sb.toString();
    }

    /**
     * 日志/响应中截断过长的输出。
     */
    private static String truncateOutput(String output) {
        if (output == null) return null;
        if (output.length() > 10_000) {
            return output.substring(0, 10_000) + "\n... [输出截断]";
        }
        return output;
    }

    @Getter
    @Builder
    public static class AgentResult {
        private final String status;       // SUCCESS / FAILURE / TIMEOUT
        private final String output;
        private final String errorMessage;
        private final Long durationMs;
    }
}
