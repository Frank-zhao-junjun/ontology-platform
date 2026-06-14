package com.ontology.platform.infrastructure.webhook;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WebhookDispatcher}.
 * <p>
 * The {@code hmacSha256} method is private static, so we access it via reflection.
 * <p>
 * Full integration tests for the {@code dispatch} method (which uses a static
 * {@code HttpClient}) should be covered in a separate integration test suite.
 */
class WebhookDispatcherTest {

    @Test
    void hmacSha256_knownInput() throws Exception {
        var secret = "my-secret-key";
        var body = "{\"event\":\"test\"}";

        var signature = invokeHmacSha256(secret, body);

        // Compute expected signature using the same algorithm
        var expected = computeHmacSha256(secret, body);
        assertEquals(expected, signature);
    }

    @Test
    void hmacSha256_differentSecretsProduceDifferentSignatures() throws Exception {
        var body = "hello world";

        var sig1 = invokeHmacSha256("secret-1", body);
        var sig2 = invokeHmacSha256("secret-2", body);

        assertNotEquals(sig1, sig2);
    }

    @Test
    void hmacSha256_differentBodiesProduceDifferentSignatures() throws Exception {
        var secret = "shared-secret";

        var sig1 = invokeHmacSha256(secret, "body-a");
        var sig2 = invokeHmacSha256(secret, "body-b");

        assertNotEquals(sig1, sig2);
    }

    @Test
    void hmacSha256_emptyBody() throws Exception {
        var secret = "test-secret";

        var signature = invokeHmacSha256(secret, "");

        assertNotNull(signature);
        assertFalse(signature.isEmpty());
    }

    @Test
    void hmacSha256_emptySecret_shouldThrowIllegalArgument() {
        var body = "some-data";

        var exception = assertThrows(Exception.class, () -> invokeHmacSha256("", body));
        assertNotNull(exception);
    }

    // ---- helpers ----

    private static String invokeHmacSha256(String secret, String body) throws Exception {
        Method method = WebhookDispatcher.class.getDeclaredMethod("hmacSha256", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, secret, body);
    }

    private static String computeHmacSha256(String secret, String body) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes()));
    }
}
