package nl.guuslieben.circle.common.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtilities {

    private static final String PASSWORD_ALGORITHM = "RSA";


    public static void main(String[] args) {
        String originalPassword = "secret";
        System.out.println("Original password: " + originalPassword);
        String encryptedPassword = encrypt(originalPassword);
        System.out.println("Encrypted password: " + encryptedPassword);
        boolean verified = verify(encryptedPassword, originalPassword);
        System.out.println("Verified password: " + verified);
    }

    public static String encrypt(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public static boolean verify(String expectedHash, String password) {
        return BCrypt.verifyer().verify(password.toCharArray(), expectedHash).verified;
    }
}
