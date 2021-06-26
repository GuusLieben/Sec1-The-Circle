package nl.guuslieben.circle.common.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class PasswordUtilities {

    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private static final String UNICODE_FORMAT = "UTF8";

    public static String encrypt(String unencryptedString, String password, Key publicKey) {
        try {
            final Cipher cipher = cipher(password, publicKey, Cipher.ENCRYPT_MODE);
            if (cipher == null) return null;
            byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
            byte[] encryptedText = cipher.doFinal(plainText);
            return new String(Base64.getEncoder().encode(encryptedText));
        }
        catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
    }

    public static String decrypt(String encryptedString, String password, Key publicKey) {
        try {
            final Cipher cipher = cipher(password, publicKey, Cipher.DECRYPT_MODE);
            if (cipher == null) return null;
            byte[] encryptedText = Base64.getDecoder().decode(encryptedString);
            byte[] plainText = cipher.doFinal(encryptedText);
            return new String(plainText);
        }
        catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
    }

    private static Cipher cipher(String password, Key publicKey, int cipherMode) {
        try {
            String secret = password + KeyUtilities.encodeKeyToBase64(publicKey).substring(0, 36);
            KeySpec ks = new DESedeKeySpec(secret.getBytes(StandardCharsets.UTF_8));
            SecretKeyFactory skf = SecretKeyFactory.getInstance(DESEDE_ENCRYPTION_SCHEME);
            SecretKey key = skf.generateSecret(ks);

            Cipher cipher = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
            cipher.init(cipherMode, key);
            return cipher;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static boolean verify(String encrypted, String password, Key publicKey) {
        final String decrypted = decrypt(encrypted, password, publicKey);
        if (decrypted == null) return false;
        return decrypted.equals(password);
    }
}
