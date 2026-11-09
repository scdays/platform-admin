package com.vtc.platformadmin.domain.partner.model.support;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Webhook HMAC-SHA256 signing-secret generator.
 * <p>
 * The generated plaintext is returned to the admin once (for sharing with the
 * partner) and then encrypted by the repository before persistence.
 *
 * @author asset-security
 */
@Component
public final class WebhookSecretGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private WebhookSecretGenerator() {
    }

    public static String generate() {
        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        return "whsec_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
