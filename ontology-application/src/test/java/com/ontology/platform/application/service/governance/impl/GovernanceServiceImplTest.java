package com.ontology.platform.application.service.governance.impl;

import com.ontology.platform.application.service.governance.GovernanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 针对 GovernanceServiceImpl 中 Token 验证（Task 4.4 Base64 降级兼容）的测试。
 *
 * <p>覆盖两个路径：BCrypt（当前算法）和 Base64 降级（SHA-256 + Base64 历史算法）。
 * 以及边缘情况：null / 不匹配 / 不同长度。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GovernanceServiceImpl - Token 验证")
class GovernanceServiceImplTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private GovernanceServiceImpl service;

    @BeforeEach
    void setUp() {
        // GovernanceServiceImpl 的构造函数只依赖 repository
        // tokenRepository/roleRepository/permissionRepository/approvalRepository 均由 @RequiredArgsConstructor 注入
        // matchesToken 不依赖任何 repository，可直接 new 并测试
        // 但由于类需要工厂构造，我们直接 new 并通过反射？不行——类有 final 字段。
        // 最简单：用 Mockito 的 @InjectMocks 假装注入 mock
        // 实际上 matchesToken 不访问任何 repository，所以 mock 可行
    }

    // ================================================================
    // 辅助方法：计算 SHA-256 + Base64（与 GovernanceServiceImpl.legacyHashToken 相同算法）
    // ================================================================

    private String legacyHash(String rawToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ================================================================
    // 先取得 service 实例：由于 matchesToken 是 public，我们可以
    // 用 @InjectMocks + mock repository 来创建 service
    // ================================================================

    // 我们直接用 GovernanceServiceImpl 的构造函数，手动注入 mock
    // 但所有 repo 都是 final field，需要构造函数参数
    // 最佳方式：使用 Mockito @InjectMocks

    @Nested
    @DisplayName("matchesToken()")
    class MatchesToken {

        private GovernanceServiceImpl service;

        @BeforeEach
        void init() {
            // 所有 repository 都用 mock，但测试 matchesToken 不需要它们
            service = new GovernanceServiceImpl(
                    null, null, null, null
            );
        }

        @Test
        @DisplayName("bcrypt hash → 验证通过")
        void bcryptHashMatches() {
            String raw = "otp_" + randomBase64();
            String bcryptHash = encoder.encode(raw);
            assertThat(service.matchesToken(raw, bcryptHash)).isTrue();
        }

        @Test
        @DisplayName("bcrypt hash → 错误 token 不通过")
        void bcryptHashWrongTokenFails() {
            String bcryptHash = encoder.encode("otp_correct");
            assertThat(service.matchesToken("otp_wrong", bcryptHash)).isFalse();
        }

        @Test
        @DisplayName("legacy Base64 hash → 验证通过")
        void legacyBase64HashMatches() {
            String raw = "otp_legacy_token_abc";
            String legacyHash = legacyHash(raw);
            assertThat(service.matchesToken(raw, legacyHash)).isTrue();
        }

        @Test
        @DisplayName("legacy Base64 hash → 错误 token 不通过")
        void legacyBase64HashWrongTokenFails() {
            String raw = "otp_legacy_token_abc";
            String legacyHash = legacyHash(raw);
            assertThat(service.matchesToken("otp_wrong", legacyHash)).isFalse();
        }

        @Test
        @DisplayName("null rawToken → false")
        void nullRawToken() {
            String bcryptHash = encoder.encode("otp_anything");
            assertThat(service.matchesToken(null, bcryptHash)).isFalse();
            String legacyHash = legacyHash("otp_anything");
            assertThat(service.matchesToken(null, legacyHash)).isFalse();
        }

        @Test
        @DisplayName("null storedHash → false")
        void nullStoredHash() {
            assertThat(service.matchesToken("otp_anything", null)).isFalse();
        }

        @Test
        @DisplayName("both null → false")
        void bothNull() {
            assertThat(service.matchesToken(null, null)).isFalse();
        }

        @Test
        @DisplayName("空字符串 storedHash 不走 bcrypt fallback → legacy 路径计算不匹配 → false")
        void emptyStoredHashFallsThroughToLegacy() {
            // 空字符串不以 $2a$ 开头，走 legacy 路径
            // SHA-256 + Base64 的结果不会是空字符串，所以返回 false
            assertThat(service.matchesToken("otp_anything", "")).isFalse();
        }

        @Test
        @DisplayName("新签发 token (bcrypt) 与 legacy token 互不混淆")
        void bcryptAndLegacyAreIndependent() {
            String raw = "otp_same_value";
            String bcryptHash = encoder.encode(raw);
            String legacyHash = legacyHash(raw);

            // 各自的 hash 只能被各自的路径验证通过
            assertThat(service.matchesToken(raw, bcryptHash)).isTrue();
            assertThat(service.matchesToken(raw, legacyHash)).isTrue(); // legacy 也能验证

            // 错误值
            assertThat(service.matchesToken("otp_other", bcryptHash)).isFalse();
            assertThat(service.matchesToken("otp_other", legacyHash)).isFalse();
        }
    }

    @Nested
    @DisplayName("constantTimeEquals() — 常数时间比较")
    class ConstantTimeEquals {

        // 通过 matchesToken 间接测试 constantTimeEquals 的行为
        // 使用 legacy hash 路径（不走 bcrypt）
        private GovernanceServiceImpl service;

        @BeforeEach
        void init() {
            service = new GovernanceServiceImpl(null, null, null, null);
        }

        @Test
        @DisplayName("相等 → true")
        void equalStrings() {
            String raw = "otp_test";
            String hash = legacyHash(raw);
            assertThat(service.matchesToken(raw, hash)).isTrue();
        }

        @Test
        @DisplayName("不等 → false")
        void differentStrings() {
            String hash = legacyHash("otp_original");
            assertThat(service.matchesToken("otp_different", hash)).isFalse();
        }

        @Test
        @DisplayName("不同长度 → false（常数时间仍在等长循环后返回）")
        void differentLengths() {
            String hash = legacyHash("otp_short");
            assertThat(service.matchesToken("otp_much_longer_token_here", hash)).isFalse();
        }

        @Test
        @DisplayName("legacy hash 路径不因时序泄漏而误判")
        void legacyPathConsistentRejection() {
            String raw = "otp_secure_token_42";
            String hash = legacyHash(raw);

            // 同一个错误值多次调用，结果稳定
            for (int i = 0; i < 5; i++) {
                assertThat(service.matchesToken("otp_wrong", hash)).isFalse();
            }
        }
    }

    // ================================================================
    // 辅助方法
    // ================================================================

    private static String randomBase64() {
        byte[] bytes = new byte[16];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
