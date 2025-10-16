package com.bankcards.util;

import com.bankcards.config.EncryptionProperties;
import com.bankcards.exception.EncryptionException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {
    private static TextEncryptor textEncryptor;

    @Autowired
    private EncryptionProperties encryptionProperties;

    @PostConstruct
    public void init() {
        textEncryptor = Encryptors.text(encryptionProperties.getKey(), "deadbeef");
    }

    public static String encrypt(String data) {
        if (data == null) return null;
        try {
            return textEncryptor.encrypt(data);
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            return textEncryptor.decrypt(encryptedData);
        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }
}
