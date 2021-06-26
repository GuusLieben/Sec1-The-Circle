package nl.guuslieben.circle.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import nl.guuslieben.circle.common.UserData;

public class KeyUtilities {

    public static final String KEY_ALGORITHM = "RSA";
    private static final int CERTIFICATE_BITS = 4096;

    public static KeyPair generateKeyPair(UserData data) throws NoSuchAlgorithmException {
        var keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGenerator.initialize(CERTIFICATE_BITS, new SecureRandom(data.getEmail().getBytes(StandardCharsets.UTF_8)));
        return keyPairGenerator.generateKeyPair();
    }

    public static void storeKey(File out, Key key) {
        try {
            OutputStream stream;
            stream = new FileOutputStream(out);
            byte[] encoded = key.getEncoded();
            stream.write(encoded);
            stream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<PublicKey> getPublicKeyFromFile(File file) {
        Path path = Paths.get(file.toURI());
        try {
            byte[] bytes = Files.readAllBytes(path);
            KeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
            return Optional.ofNullable(kf.generatePublic(ks));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return Optional.empty();
        }
    }

    public static String encodeKeyToBase64(Key key) {
        byte[] encodedKey = key.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    public static Optional<PublicKey> decodeBase64ToKey(String base64) {
        try {
            byte[] keyContent = Base64.getDecoder().decode(base64);
            KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
            KeySpec keySpecX509 = new X509EncodedKeySpec(keyContent);
            return Optional.ofNullable(kf.generatePublic(keySpecX509));
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return Optional.empty();
        }
    }

    public static File getServerPublic() {
        return new File("store/circle-server.pub");
    }

    public static Optional<String> decryptContent(byte[] body, PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            String decrypted = new String(cipher.doFinal(body));
            return Optional.ofNullable(decrypted);
        }
        catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            return Optional.empty();
        }

    }

    public static Optional<Message> decryptMessage(byte[] body, PublicKey key) {
        return decryptContent(body, key).map(MessageUtilities::fromJson);
    }

    public static byte[] encryptMessage(Message content, Key key) {
        final String json = MessageUtilities.toJson(content);
        return encryptContent(json, key);
    }

    public static byte[] encryptContent(String content, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(content.getBytes());
        }
        catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            return null;
        }
    }
}
