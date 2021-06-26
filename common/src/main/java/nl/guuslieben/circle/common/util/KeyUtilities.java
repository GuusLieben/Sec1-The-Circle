package nl.guuslieben.circle.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
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

    private KeyUtilities() {
    }

    public static KeyPair generateKeyPair(UserData data) throws NoSuchAlgorithmException {
        var keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGenerator.initialize(CERTIFICATE_BITS, new SecureRandom(data.getEmail().getBytes(StandardCharsets.UTF_8)));
        return keyPairGenerator.generateKeyPair();
    }

    public static void storeKey(File out, Key key) throws IOException {
        try (var stream = new FileOutputStream(out)) {
            byte[] encoded = key.getEncoded();
            stream.write(encoded);
        }
    }

    public static Optional<PublicKey> getPublicKeyFromFile(File file) {
        var path = Paths.get(file.toURI());
        try {
            var bytes = Files.readAllBytes(path);
            KeySpec ks = new X509EncodedKeySpec(bytes);
            var kf = KeyFactory.getInstance(KEY_ALGORITHM);
            return Optional.ofNullable(kf.generatePublic(ks));
        }
        catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return Optional.empty();
        }
    }

    public static String encodeKeyToBase64(Key key) {
        var encodedKey = key.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    public static Optional<PublicKey> decodeBase64ToKey(String base64) {
        try {
            final var keyContent = Base64.getDecoder().decode(base64);
            final var kf = KeyFactory.getInstance(KEY_ALGORITHM);
            final KeySpec keySpecX509 = new X509EncodedKeySpec(keyContent);
            return Optional.ofNullable(kf.generatePublic(keySpecX509));
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static Optional<PrivateKey> decodeBase64ToPrivate(String base64) {
        try {
            final var encoded = Base64.getDecoder().decode(base64);
            final KeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            final var kf = KeyFactory.getInstance("RSA");

            return Optional.of(kf.generatePrivate(keySpec));
        } catch (NoSuchAlgorithmException | IllegalArgumentException | InvalidKeySpecException e) {
            return Optional.empty();
        }
    }

    public static File getServerPublic() {
        return new File("store/circle-server.pub");
    }

    public static Optional<Message> decryptMessage(byte[] body, Key key) {
        return decryptContent(body, key).map(MessageUtilities::fromJson);
    }

    public static Optional<String> decryptContent(byte[] body, Key key) {
        try {
            var cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            var decrypted = new String(cipher.doFinal(body));
            return Optional.ofNullable(decrypted);
        }
        catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            return Optional.empty();
        }

    }

    public static byte[] encryptMessage(Message content, Key key) {
        final var json = MessageUtilities.toJson(content);
        return encryptContent(json, key);
    }

    public static byte[] encryptContent(String content, Key key) {
        try {
            var cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(content.getBytes());
        }
        catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | NullPointerException | InvalidKeyException e) {
            return new byte[0];
        }
    }
}
