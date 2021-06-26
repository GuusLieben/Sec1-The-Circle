package nl.guuslieben.circle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.util.CertificateUtilities;
import nl.guuslieben.circle.common.util.KeyUtilities;
import nl.guuslieben.circle.common.util.Message;
import nl.guuslieben.circle.common.util.MessageUtilities;
import nl.guuslieben.circle.common.rest.CertificateSigningRequest;
import nl.guuslieben.circle.common.User;
import nl.guuslieben.circle.users.PersistentUser;
import nl.guuslieben.circle.users.UserRepository;

@RestController
public class ServerController {

    private final UserRepository userRepository;
    private static final Map<String, X509Certificate> certificateCache = new HashMap<>();

    public ServerController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("csr")
    public Message csr(@RequestBody Message message) {
        final Optional<CertificateSigningRequest> request = MessageUtilities.verifyContent(message, CertificateSigningRequest.class);

        if (request.isPresent()) {
            final CertificateSigningRequest csr = request.get();
            final Optional<PublicKey> userKey = KeyUtilities.decodeBase64ToKey(csr.getPublicKey());

            if (userKey.isPresent()) {
                try {
                    final KeyPair pair = new KeyPair(userKey.get(), CircleServer.KEYS.getPrivate());
                    final X509Certificate certificate = CertificateUtilities.createCertificate(pair);
                    certificateCache.put(KeyUtilities.encodeKeyToBase64(userKey.get()), certificate);
                    final String pem = CertificateUtilities.toPem(certificate);
                    final CertificateSigningRequest model = CertificateSigningRequest.accept(pem);
                    return new Message(model);
                }
                catch (CertificateEncodingException | SignatureException | InvalidKeyException e) {
                    return MessageUtilities.reject("Could not create certificate", CircleServer.KEYS.getPrivate());
                }

            } else {
                return MessageUtilities.reject("Invalid invalid key", CircleServer.KEYS.getPrivate());
            }

        } else {
            return MessageUtilities.reject("Invalid model", CircleServer.KEYS.getPrivate());
        }
    }

    @PostMapping("register")
    public byte[] register(@RequestBody byte[] body, @RequestHeader(MessageUtilities.PUBLIC_KEY) String publicKey) throws CertificateEncodingException {
        final X509Certificate x509Certificate = certificateCache.get(publicKey);
        if (x509Certificate == null) return MessageUtilities.rejectEncrypted("Ensure CSR is run before registering a user", CircleServer.KEYS.getPrivate());

        final Optional<Message> message = this.parse(body, publicKey);
        if (message.isEmpty()) return MessageUtilities.rejectEncrypted("Could not decrypt content", CircleServer.KEYS.getPrivate());

        final Optional<User> userModel = MessageUtilities.verifyContent(message.get(), User.class);
        if (userModel.isEmpty()) return MessageUtilities.rejectEncrypted("Invalid model", CircleServer.KEYS.getPrivate());

        final User user = userModel.get();

        final String certFile = this.store(x509Certificate, user.getEmail());

        final PersistentUser persistentUser = PersistentUser.of(user, certFile);
        final Optional<PersistentUser> byId = this.userRepository.findById(user.getEmail());
        if (byId.isPresent()) return MessageUtilities.rejectEncrypted("User already exists", CircleServer.KEYS.getPrivate());

        this.userRepository.save(persistentUser);

        return KeyUtilities.encryptMessage(new Message(true), CircleServer.KEYS.getPrivate());
    }

    @GetMapping
    public Message get() {
        return new Message(new UserData("Sample", "john@example.com"));
    }

    private Optional<Message> parse(byte[] body, String publicKey) {
        final Optional<PublicKey> key = KeyUtilities.decodeBase64ToKey(publicKey);
        if (key.isEmpty()) return Optional.empty();

        return KeyUtilities.decryptMessage(body, key.get());
    }

    private String store(X509Certificate certificate, String email) {
        try {
            File certs = new File("certs");
            certs.mkdirs();
            final File file = new File(certs, email + "-" + System.currentTimeMillis() + ".cert");
            file.createNewFile();
            final String pem = CertificateUtilities.toPem(certificate);
            FileWriter writer = new FileWriter(file);
            writer.write(pem);
            writer.close();
            return file.getName();
        }
        catch (CertificateEncodingException | IOException e) {
            return null;
        }
    }

}
