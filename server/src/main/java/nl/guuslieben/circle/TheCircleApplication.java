package nl.guuslieben.circle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.message.CertificateUtilities;

@SpringBootApplication
public class TheCircleApplication {

    public static KeyPair KEYS;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        // TODO: Store to file for permanent record
        KEYS = CertificateUtilities.generateKeyPair(new UserData("CIRCLE", "admin@circle.org", 48132L));
        SpringApplication.run(TheCircleApplication.class, args);
    }

}
