package com.vtc.platformadmin.infra.redis;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PartnerTokenRedisStoreTest {

    @Test
    public void sha256Hex_returnsCorrectHash() {
        String testValue = "test-token-123";
        String hash = PartnerTokenRedisStore.sha256Hex(testValue);
        assertNotNull(hash);
        assertEquals(64, hash.length());
        // Verify deterministic
        String hash2 = PartnerTokenRedisStore.sha256Hex(testValue);
        assertEquals(hash, hash2);
    }

    @Test
    public void tokenKey_usesPrefixAndHash() {
        String token = "my-test-token";
        String key = PartnerTokenRedisStore.tokenKey(token);
        assertNotNull(key);
        assertEquals("partner:token:", key.substring(0, 14));
        assertEquals(64 + 14, key.length());
    }
}
