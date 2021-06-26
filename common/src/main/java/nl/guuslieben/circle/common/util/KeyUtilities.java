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
    public static final String CIPHER_ALGORITHM = "RSA/None/OAEPWithSHA-1AndMGF1Padding";
    private static final int CERTIFICATE_BITS = 4096;

    private KeyUtilities() {
    }

    public static KeyPair generateKeyPair(UserData data) throws NoSuchAlgorithmException {
        var keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGenerator.initialize(CERTIFICATE_BITS, new SecureRandom(data.getEmail().getBytes(StandardCharsets.UTF_8)));
        return keyPairGenerator.generateKeyPair();
    }

    public static void storeKey(File out, Key key) throws IOException {
        var stream = new FileOutputStream(out);
        byte[] encoded = key.getEncoded();
        stream.write(encoded);
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
            var keyContent = Base64.getDecoder().decode(base64);
            var kf = KeyFactory.getInstance(KEY_ALGORITHM);
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

    public static Optional<Message> decryptMessage(byte[] body, PublicKey key) {
        return decryptContent(body, key).map(MessageUtilities::fromJson);
    }

    public static Optional<String> decryptContent(byte[] body, PublicKey key) {
        try {
            var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
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
            var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(content.getBytes());
        }
        catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            return new byte[0];
        }
    }
}
