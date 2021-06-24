package nl.guuslieben.circle.common.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class MessageUtilities {

    public static final Logger log = LoggerFactory.getLogger(MessageUtilities.class);
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static final String HASH_ALGORITHM = "SHA-512";

    public static final String KEY_ALGORITHM = "RSA";
    public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    public static final int INITIAL_BLOCK_SIZE = 4096;

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);
    public static final String INVALID = "InvalidatedContent";

    public static <T> Message fromJson(String message) {
        try {
            return MAPPER.readValue(message, Message.class);
        }
        catch (JsonProcessingException e) {
            return new Message(INVALID);
        }
    }

    public static <T> Optional<T> getContent(Message message, Class<T> type) {
        try {
            return Optional.ofNullable(MAPPER.readValue(message.getContent(), type));
        }
        catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public static boolean verify(Message message) {
        final String actual = message.getHash();
        final String expected = generateHash(message.getContent());

        return expected.equals(actual);
    }

    public static String generateTimestamp() {
        return DATE_FORMAT.format(new Date());
    }

    public static LocalDateTime parseTimestamp(String timestamp) {
        try {
            final Instant instant = DATE_FORMAT.parse(timestamp).toInstant();
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (ParseException e) {
            return null;
        }
    }

    public static String generateHash(String content) {
        try {
            log.info("Generating hash for '" + content.replaceAll("\n", "^newLine|") + "', generating with '" + HASH_ALGORITHM + "'");
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] messageDigest = md.digest(content.getBytes(StandardCharsets.UTF_8));

            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            return INVALID;
        }
    }

    public static Optional<KeyPair> generateKeyPair() {
        log.info("New key pair requested, generating with '" + KEY_ALGORITHM + "'");
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            kpg.initialize(INITIAL_BLOCK_SIZE);
            return Optional.ofNullable(kpg.generateKeyPair());
        } catch (NoSuchAlgorithmException e) {
            return Optional.empty();
        }
    }

    public static <T> String toJson(T content) {
        try {
            return MAPPER.writeValueAsString(content);
        }
        catch (JsonProcessingException e) {
            return INVALID;
        }
    }
}
