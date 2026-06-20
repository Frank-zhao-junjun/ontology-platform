package com.ontology.platform.infrastructure.bridge;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    private static final String BRIDGE_DIR = "D:\\AI\\agent-bridge";
    private static final long DEFAULT_TIMEOUT_SECONDS = 300;

    @Value("${agent.bridge-dir:D:\\AI\\agent-bridge}")
    private String bridgeDir;

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
        // Codex CLI 通常通过 HTTP bridge 调用（:38440）
        // MVP: 直接尝试 shell 调用 codex
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
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(cwd != null ? Paths.get(cwd).toFile() : Paths.get(".").toAbsolutePath().toFile());
            pb.redirectErrorStream(false);

            log.info("Exec: {} | cwd={} | timeout={}s", String.join(" ", command), pb.directory(), timeoutSec);
            Process process = pb.start();

            // 读取 stdout
            StringBuilder stdout = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append("\n");
                }
            }

            // 读取 stderr
            StringBuilder stderr = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(timeoutSec, TimeUnit.SECONDS);
            long elapsed = System.currentTimeMillis() - start;

            if (!finished) {
                process.destroyForcibly();
                log.warn("Agent task timed out after {}s", timeoutSec);
                return AgentResult.builder()
                        .status("TIMEOUT")
                        .output(stdout.toString())
                        .errorMessage("执行超时(>" + timeoutSec + "s)")
                        .durationMs(elapsed)
                        .build();
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.warn("Agent task failed (exit={}): {}", exitCode, stderr);
                return AgentResult.builder()
                        .status("FAILURE")
                        .output(stdout.toString())
                        .errorMessage(stderr.length() > 0 ? stderr.toString() : "exit code: " + exitCode)
                        .durationMs(elapsed)
                        .build();
            }

            log.info("Agent task completed in {}ms", elapsed);
            return AgentResult.builder()
                    .status("SUCCESS")
                    .output(stdout.toString())
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

    @Getter
    @Builder
    public static class AgentResult {
        private final String status;       // SUCCESS / FAILURE / TIMEOUT
        private final String output;
        private final String errorMessage;
        private final Long durationMs;
    }
}
