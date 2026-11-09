package com.vtc.platformadmin.infra.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WebhookSecretCipher}.
 */
class WebhookSecretCipherTest {

    private final WebhookSecretCipher cipher =
            new WebhookSecretCipher("eWxtZkRseFhLcm04Skc0OWdGYlUwUT09");

    @Test
    void encryptDecryptRoundTrip() {
        String plaintext = "whsec_abc123secret";
        String encrypted = cipher.encrypt(plaintext);

        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);
        assertEquals(plaintext, cipher.decrypt(encrypted));
    }

    @Test
    void differentEncryptionsProduceDifferentCiphertexts() {
        String plaintext = "whsec_same_secret";
        String enc1 = cipher.encrypt(plaintext);
        String enc2 = cipher.encrypt(plaintext);

        assertNotEquals(enc1, enc2);
        assertEquals(plaintext, cipher.decrypt(enc1));
        assertEquals(plaintext, cipher.decrypt(enc2));
    }

    @Test
    void encryptNullReturnsNull() {
        assertNull(cipher.encrypt(null));
        assertNull(cipher.encrypt(""));
    }

    @Test
    void decryptNullReturnsNull() {
        assertNull(cipher.decrypt(null));
        assertNull(cipher.decrypt(""));
    }

    @Test
    void decryptTamperedCiphertextThrows() {
        String encrypted = cipher.encrypt("whsec_secret");
        String tampered = encrypted.substring(0, encrypted.length() - 2) + "AA";

        assertThrows(IllegalStateException.class, () -> cipher.decrypt(tampered));
    }
}
