package nl.guuslieben.circle.common.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class PasswordUtilities {

    public static final String KEY_ALGORITHM = "DESede";
    public static final String CIPHER_ALGORITHM = "DESede/CBC/PKCS5Padding";

    private PasswordUtilities() {
    }

    public static String encrypt(String unencryptedString, String password, Key publicKey) {
        try {
            final var cipher = cipher(password, publicKey, Cipher.ENCRYPT_MODE);
            if (cipher == null) return null;
            var plainText = unencryptedString.getBytes(StandardCharsets.UTF_8);
            var encryptedText = cipher.doFinal(plainText);
            return new String(Base64.getEncoder().encode(encryptedText));
        }
        catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
    }

    public static String decrypt(String encryptedString, String password, Key publicKey) {
        try {
            final var cipher = cipher(password, publicKey, Cipher.DECRYPT_MODE);
            if (cipher == null) return null;
            var encryptedText = Base64.getDecoder().decode(encryptedString);
            var plainText = cipher.doFinal(encryptedText);
            return new String(plainText);
        }
        catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
    }

    private static Cipher cipher(String password, Key publicKey, int cipherMode) {
        try {
            var secret = password + KeyUtilities.encodeKeyToBase64(publicKey).substring(0, 36);
            KeySpec ks = new DESedeKeySpec(secret.getBytes(StandardCharsets.UTF_8));
            var skf = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            var key = skf.generateSecret(ks);

            var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(cipherMode, key, new IvParameterSpec(new byte[8]));
            return cipher;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static boolean verify(String encrypted, String password, Key publicKey) {
        final var decrypted = decrypt(encrypted, password, publicKey);
        if (decrypted == null) return false;
        return decrypted.equals(password);
    }
}
