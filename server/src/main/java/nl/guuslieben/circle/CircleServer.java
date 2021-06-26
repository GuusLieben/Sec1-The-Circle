package nl.guuslieben.circle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.util.KeyUtilities;

@SpringBootApplication
public class CircleServer {

    public static KeyPair KEYS;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        KEYS = KeyUtilities.generateKeyPair(new UserData("CIRCLE", "admin@circle.org"));
        final File serverPublic = KeyUtilities.getServerPublic();
//        if (!serverPublic.exists())
        // TODO: For development purposes we'll generate the key every time the server starts
        KeyUtilities.storeKey(serverPublic, KEYS.getPublic());

        SpringApplication.run(CircleServer.class, args);
    }

}