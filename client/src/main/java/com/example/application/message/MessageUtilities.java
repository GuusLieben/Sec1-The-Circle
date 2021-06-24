package com.example.application.message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class MessageUtilities {

    public static final String HASH_ALGORITHM = "SHA-512";

    public static final String KEY_ALGORITHM = "RSA";
    public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    public static final int INITIAL_BLOCK_SIZE = 4096;

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

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

}
