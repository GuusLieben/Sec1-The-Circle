package nl.guuslieben.circle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import nl.guuslieben.circle.common.User;
import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.rest.CertificateSigningRequest;
import nl.guuslieben.circle.common.rest.LoginRequest;
import nl.guuslieben.circle.common.util.CertificateUtilities;
import nl.guuslieben.circle.common.util.KeyUtilities;
import nl.guuslieben.circle.common.util.Message;
import nl.guuslieben.circle.common.util.MessageUtilities;
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
                    return MessageUtilities.rejectMessage("Could not create certificate");
                }

            } else {
                return MessageUtilities.rejectMessage("Invalid invalid key");
            }

        } else {
            return MessageUtilities.rejectMessage("Invalid model");
        }
    }

    @PostMapping("register")
    public byte[] register(@RequestBody byte[] body, @RequestHeader(MessageUtilities.PUBLIC_KEY) String publicKey) {
        final X509Certificate x509Certificate = certificateCache.get(publicKey);
        if (x509Certificate == null) return MessageUtilities.rejectEncrypted("Ensure CSR is run before registering a user", CircleServer.KEYS.getPrivate());

        return this.process(body, publicKey, User.class, user -> {
            final String certFile = CertificateUtilities.store(x509Certificate, user.getEmail());

            final PersistentUser persistentUser = PersistentUser.of(user, certFile);
            final Optional<PersistentUser> byId = this.userRepository.findById(user.getEmail());
            if (byId.isPresent()) return MessageUtilities.rejectMessage("User already exists");

            this.userRepository.save(persistentUser);

            return new Message(true);
        });
    }

    @PostMapping("login")
    public byte[] login(@RequestBody byte[] body, @RequestHeader(MessageUtilities.PUBLIC_KEY) String publicKey) {
        return this.process(body, publicKey, LoginRequest.class, request -> {
            final String username = request.getUsername();
            final Optional<PersistentUser> user = this.userRepository.findById(username);
            if (user.isEmpty()) return MessageUtilities.rejectMessage("No user with that name exists");

            final PersistentUser persistentUser = user.get();
            final boolean passwordValid = persistentUser.getPassword().equals(request.getPassword());

            if (!passwordValid) return MessageUtilities.rejectMessage("Incorrect password");

            final UserData userData = new UserData(persistentUser.getName(), persistentUser.getEmail());
            return new Message(userData);
        });
    }

    @GetMapping
    public Message get() {
        return new Message(new UserData("Sample", "john@example.com"));
    }

    private <T> byte[] process(byte[] body, String publicKey, Class<T> type, Function<T, Message> function) {
        final Optional<Message> message = this.parse(body, publicKey);
        if (message.isEmpty()) return MessageUtilities.rejectEncrypted("Could not decrypt content", CircleServer.KEYS.getPrivate());

        final Optional<T> model = MessageUtilities.verifyContent(message.get(), type);
        if (model.isEmpty()) return MessageUtilities.rejectEncrypted("Invalid model", CircleServer.KEYS.getPrivate());

        return KeyUtilities.encryptMessage(function.apply(model.get()), CircleServer.KEYS.getPrivate());
    }

    private Optional<Message> parse(byte[] body, String publicKey) {
        final Optional<PublicKey> key = KeyUtilities.decodeBase64ToKey(publicKey);
        if (key.isEmpty()) return Optional.empty();

        return KeyUtilities.decryptMessage(body, key.get());
    }

}
