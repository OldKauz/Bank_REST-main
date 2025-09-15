package com.example.bankcards;

import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil("1234567890123456");
    }

    @Test
    void encryptAndDecrypt_success() {
        String original = "1234567812345678";

        String encrypted = encryptionUtil.encrypt(original);
        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);

        String decrypted = encryptionUtil.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_null_throws() {
        assertThrows(RuntimeException.class, () -> encryptionUtil.encrypt(null));
    }

    @Test
    void decrypt_invalidData_throws() {
        assertThrows(RuntimeException.class, () -> encryptionUtil.decrypt("invalid!"));
    }
}
