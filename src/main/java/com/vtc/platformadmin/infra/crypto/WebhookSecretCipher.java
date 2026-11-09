package com.vtc.platformadmin.infra.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Webhook Secret symmetric encryption (AES-GCM).
 * <p>
 * The webhook secret is used for HMAC-SHA256 outbound signing, so it must be
 * recoverable at delivery time. One-way hashing is not viable; instead we
 * encrypt at rest and decrypt for signing.
 * <p>
 * Ciphertext format: Base64(IV(12B) || ciphertext+authTag).
 * The encryption key is read from {@code common.crypto.key}.
 */
@Component
public class WebhookSecretCipher {

    private static final Logger log = LoggerFactory.getLogger(WebhookSecretCipher.class);

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public WebhookSecretCipher(@Value("${common.crypto.key}") String configuredKey) {
        this.keySpec = new SecretKeySpec(decodeKey(configuredKey), "AES");
    }

    /** Encrypts plaintext; returns null for blank input. */
    public String encrypt(String plaintext) {
        if (!StringUtils.hasText(plaintext)) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception ex) {
            throw new IllegalStateException("Webhook secret encryption failed", ex);
        }
    }

    /** Decrypts ciphertext; returns null for blank input. */
    public String decrypt(String ciphertext) {
        if (!StringUtils.hasText(ciphertext)) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherBytes = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, cipherBytes, 0, cipherBytes.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Webhook secret decryption failed", ex);
        }
    }

    /**
     * Decodes the configured key. The value in application.yml is double-base64-encoded,
     * yielding a 16-byte AES-128 key. Falls back to SHA-256 derivation if the value
     * is not in that format.
     */
    private static byte[] decodeKey(String configuredKey) {
        try {
            byte[] firstDecode = Base64.getDecoder().decode(configuredKey);
            byte[] rawKey = Base64.getDecoder().decode(new String(firstDecode, StandardCharsets.UTF_8));
            if (rawKey.length == 16 || rawKey.length == 32) {
                return rawKey;
            }
        } catch (Exception ignored) {
            // fall through to SHA-256 derivation
        }
        log.warn("common.crypto.key is not double-base64-encoded; deriving key via SHA-256");
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(configuredKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to derive AES key from common.crypto.key", ex);
        }
    }
}
