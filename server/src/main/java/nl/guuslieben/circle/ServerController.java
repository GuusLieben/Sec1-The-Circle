package nl.guuslieben.circle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import nl.guuslieben.circle.common.Response;
import nl.guuslieben.circle.common.Topic;
import nl.guuslieben.circle.common.TopicCollection;
import nl.guuslieben.circle.common.rest.CreateTopic;
import nl.guuslieben.circle.common.User;
import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.rest.CertificateSigningRequest;
import nl.guuslieben.circle.common.rest.LoginRequest;
import nl.guuslieben.circle.common.util.CertificateUtilities;
import nl.guuslieben.circle.common.util.KeyUtilities;
import nl.guuslieben.circle.common.util.Message;
import nl.guuslieben.circle.common.util.MessageUtilities;
import nl.guuslieben.circle.persistence.EventRepository;
import nl.guuslieben.circle.persistence.PersistentEvent;
import nl.guuslieben.circle.persistence.PersistentResponse;
import nl.guuslieben.circle.persistence.PersistentTopic;
import nl.guuslieben.circle.persistence.PersistentUser;
import nl.guuslieben.circle.persistence.ResponseRepository;
import nl.guuslieben.circle.persistence.TopicRepository;
import nl.guuslieben.circle.persistence.UserRepository;

@RestController
public class ServerController {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final ResponseRepository responseRepository;
    private final EventRepository eventRepository;
    private static final Map<String, X509Certificate> certificateCache = new HashMap<>();

    public ServerController(UserRepository userRepository, TopicRepository topicRepository, ResponseRepository responseRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.responseRepository = responseRepository;
        this.eventRepository = eventRepository;
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

            this.log("UserRegistered", null, persistentUser.getEmail());

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

            this.log("UserLoggedIn", null, persistentUser.getEmail());

            final UserData userData = new UserData(persistentUser.getName(), persistentUser.getEmail());
            return new Message(userData);
        });
    }

    @PostMapping("topic")
    public byte[] createTopic(@RequestBody byte[] body, @RequestHeader(MessageUtilities.PUBLIC_KEY) String publicKey) {
        return this.process(body, publicKey, CreateTopic.class, topic -> {
            final String email = topic.getAuthor().getEmail();
            final Optional<PersistentUser> user = this.userRepository.findById(email);

            final Optional<Message> message = this.verifyUser(user, publicKey);
            if (message.isPresent()) return message.get();

            final PersistentUser persistentUser = user.get();
            final PersistentTopic persistentTopic = new PersistentTopic(persistentUser, topic.getName());
            final PersistentTopic savedTopic = this.topicRepository.save(persistentTopic);

            this.log("TopicCreated", topic, persistentUser.getEmail());

            return new Message(new Topic(savedTopic.getId(), savedTopic.getName(), new UserData(persistentUser.getName(), persistentUser.getEmail()), new ArrayList<>()));
        });
    }

    private Optional<Message> verifyUser(Optional<PersistentUser> user, String publicKey) {
        if (user.isEmpty()) return Optional.of(MessageUtilities.rejectMessage("User does not exist"));

        final Optional<X509Certificate> x509Certificate = CertificateUtilities.get(user.get().getEmail());
        if (x509Certificate.isEmpty()) return Optional.of(MessageUtilities.rejectMessage("No certificate for user"));

        // We already know this can be decoded as we are in a callback
        final Optional<PublicKey> pubKey = KeyUtilities.decodeBase64ToKey(publicKey);
        final boolean verified = CertificateUtilities.compare(x509Certificate.get(), pubKey.get());

        if (!verified) return Optional.of(MessageUtilities.rejectMessage("Keys do not match!"));

        return Optional.empty();
    }

    @GetMapping("topics")
    public TopicCollection topics() {
        final Iterable<PersistentTopic> persistentTopics = this.topicRepository.findAll();
        final List<Topic> topics = new ArrayList<>();
        for (PersistentTopic persistentTopic : persistentTopics) {
            final PersistentUser author = persistentTopic.getAuthor();
            topics.add(new Topic(persistentTopic.getId(), persistentTopic.getName(), new UserData(author.getName(), author.getEmail()), null));
        }
        Collections.reverse(topics);
        return new TopicCollection(topics);
    }

    @GetMapping("topic/{id}")
    public Topic topic(@PathVariable("id") long id) {
        final Optional<PersistentTopic> topicOptional = this.topicRepository.findById(id);
        if (topicOptional.isPresent()) {
            final PersistentTopic persistentTopic = topicOptional.get();
            final PersistentUser author = persistentTopic.getAuthor();
            final List<Response> responses = new ArrayList<>();

            for (PersistentResponse response : persistentTopic.getResponses()) {
                final PersistentUser responseAuthor = response.getAuthor();
                responses.add(new Response(persistentTopic.getId(), response.getContent(), new UserData(responseAuthor.getName(), responseAuthor.getEmail())));
            }

            return new Topic(persistentTopic.getId(), persistentTopic.getName(), new UserData(author.getName(), author.getEmail()), responses);
        }
        return null;
    }

    @PostMapping("topic/{id}")
    public byte[] respond(@PathVariable("id") long id, @RequestBody byte[] body, @RequestHeader(MessageUtilities.PUBLIC_KEY) String publicKey) {
        return this.process(body, publicKey, Response.class, response -> {
            final Optional<PersistentTopic> topicOptional = this.topicRepository.findById(id);
            if (topicOptional.isEmpty()) return MessageUtilities.rejectMessage("Topic with id '" + id + "' does not exist");
            final PersistentTopic persistentTopic = topicOptional.get();

            final Optional<PersistentUser> user = this.userRepository.findById(response.getAuthor().getEmail());

            final Optional<Message> message = this.verifyUser(user, publicKey);
            if (message.isPresent()) return message.get();

            final PersistentUser persistentUser = user.get();
            final PersistentResponse persistentResponse = new PersistentResponse(persistentUser, persistentTopic, response.getContent());

            this.log("ReponseCreated", response, persistentUser.getEmail());

            final PersistentResponse saved = this.responseRepository.save(persistentResponse);
            return new Message(new Response(saved.getTopic().getId(), saved.getContent(), new UserData(persistentUser.getName(), persistentUser.getEmail())));
        });
    }

    @GetMapping
    public List<PersistentEvent> get() {
        List<PersistentEvent> events = new ArrayList<>();
        for (PersistentEvent event : this.eventRepository.findAll()) events.add(event);
        return events;
    }

    private void log(String action, Object data, String actor) {
        final PersistentEvent event = new PersistentEvent(MessageUtilities.generateTimestamp(), action, data == null ? null : MessageUtilities.toJson(data), actor);
        this.eventRepository.save(event);
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
