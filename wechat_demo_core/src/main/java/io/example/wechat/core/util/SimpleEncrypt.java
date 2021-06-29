package io.example.wechat.core.util;

import io.example.wechat.core.error.PlatformException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * Created by bruce.wan on 2021/6/23.
 */
public class SimpleEncrypt {
    public static byte[] aesEncrypt(String content, String key) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(key.getBytes());
            kgen.init(128, random);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));

            return cipher.doFinal(content.getBytes());
        } catch (Exception e) {
            throw new PlatformException(ErrorCode.COMMON_ERROR, "aes encrypt fail", e);
        }
    }

    public static String aesEncryptToBase64(String content, String key) {
        return CodecSupport.encodeBase64(aesEncrypt(content, key));
    }

    public static String aesEncryptToHex(String content, String key) {
        return CodecSupport.encodeHex(aesEncrypt(content, key));
    }

    public static String aesDecrypt(byte[] data, String key) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(key.getBytes());
            kgen.init(128, random);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));

            return new String(cipher.doFinal(data));
        } catch (Exception e) {
            throw new PlatformException(ErrorCode.ENCRYPT_ERROR, "aes decrypt fail", e);
        }
    }

    public static String aesDecryptFromBase64(String content, String key) {
        return aesDecrypt(CodecSupport.decodeBase64(content), key);
    }

    public static String aesDecryptFromHex(String content, String key) {
        return aesDecrypt(CodecSupport.decodeHex(content), key);
    }

}
