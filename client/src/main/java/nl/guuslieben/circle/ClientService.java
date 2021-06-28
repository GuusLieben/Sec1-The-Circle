package nl.guuslieben.circle;

import com.vaadin.flow.component.notification.Notification;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;
import nl.guuslieben.circle.common.Response;
import nl.guuslieben.circle.common.Topic;
import nl.guuslieben.circle.common.TopicCollection;
import nl.guuslieben.circle.common.User;
import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.rest.CertificateSigningRequest;
import nl.guuslieben.circle.common.rest.CreateTopic;
import nl.guuslieben.circle.common.rest.LoginRequest;
import nl.guuslieben.circle.common.util.CertificateUtilities;
import nl.guuslieben.circle.common.util.KeyUtilities;
import nl.guuslieben.circle.common.util.Message;
import nl.guuslieben.circle.common.util.MessageUtilities;

@Service
public class ClientService {

    private static final String BASE_URL = "http://localhost:9090/";

    @Setter
    private KeyPair pair;
    private String email;
    @Getter
    private final PublicKey serverPublic;
    private final RestTemplateBuilder templateBuilder;

    public ClientService(RestTemplateBuilder templateBuilder) {
        this.templateBuilder = templateBuilder;
        final Optional<PublicKey> publicKey = KeyUtilities.getPublicKeyFromFile(KeyUtilities.getServerPublic());
        if (publicKey.isPresent()) {
            this.serverPublic = publicKey.get();
        } else {
            throw new IllegalStateException("No (valid) server public key found");
        }
    }

    public Optional<Message> send(String url, Object body, boolean encrypt) {
        url = BASE_URL + url;
        final Message content = new Message(body);
        RestTemplate template = this.templateBuilder.build();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        headers.set(HttpHeaders.CONNECTION, "keep-alive");

        if (encrypt) {
            final String publicKey = KeyUtilities.encodeKeyToBase64(this.getPair(this.email).getPublic());
            headers.set(MessageUtilities.PUBLIC_KEY, publicKey);

            byte[] encrypted = KeyUtilities.encryptMessage(content, this.getPair(this.email).getPrivate());
            HttpEntity<?> entity = new HttpEntity<>(encrypted, headers);

            final ResponseEntity<byte[]> response = template.postForEntity(url, entity, byte[].class);
            return KeyUtilities.decryptMessage(response.getBody(), this.serverPublic);
        } else {
            final ResponseEntity<Message> response = template.postForEntity(url, content, Message.class);
            return Optional.ofNullable(response.getBody());
        }
    }

    public <T> Optional<T> get(String url, Class<T> type) {
        url = BASE_URL + url;
        RestTemplate template = this.templateBuilder.build();
        final ResponseEntity<T> response = template.getForEntity(url, type);
        return Optional.of(response.getBody());
    }

    public Optional<X509Certificate> csr(UserData data, String password) {
        final String publicKey = KeyUtilities.encodeKeyToBase64(this.getPair(data.getEmail()).getPublic());

        final CertificateSigningRequest model = CertificateSigningRequest.create(publicKey);
        final Optional<Message> response = this.send("csr", model, false);
        if (response.isPresent()) {
            final Message message = response.get();
            final Optional<CertificateSigningRequest> request = MessageUtilities.verifyContent(message, CertificateSigningRequest.class);

            if (request.isPresent()) {
                final String certificate = request.get().getCertificate();
                return CertificateUtilities.fromPem(certificate);
            }
        }
        return Optional.empty();
    }

    private <T> ServerResponse<T> response(Optional<Message> response, Class<T> type, Supplier<T> defaultValue) {
        if (response.isEmpty()) return new ServerResponse<>("Could not verify response");

        final Message message = response.get();
        final Optional<String> rejection = MessageUtilities.getRejection(message);
        if (rejection.isPresent()) return new ServerResponse<>(rejection.get());

        final Optional<T> result = MessageUtilities.verifyContent(message, type);
        return new ServerResponse<>(result.orElseGet(defaultValue));
    }

    public ServerResponse<Boolean> register(User user) {
        this.email = user.getEmail();
        final Optional<Message> response = this.send("register", user, true);
        return this.response(response, boolean.class, () -> false);
    }

    public ServerResponse<UserData> login(LoginRequest loginRequest) {
        this.email = loginRequest.getUsername();
        final Optional<Message> response = this.send("login", loginRequest, true);
        return this.response(response, UserData.class, () -> null);
    }

    public ServerResponse<Topic> createTopic(String name) {
        final CreateTopic topic = new CreateTopic(-1, name, new UserData(null, this.email));
        final Optional<Message> response = this.send("topic", topic, true);
        return this.response(response, Topic.class, () -> null);
    }

    public ServerResponse<Response> createResponse(String content, long topic) {
        final Response topicResponse = new Response(topic, content, new UserData(null, this.email));
        final Optional<Message> response = this.send("topic/" + topic, topicResponse, true);
        return this.response(response, Response.class, () -> null);
    }

    public List<Topic> getTopics() {
        final Optional<TopicCollection> topics = this.get("topics", TopicCollection.class);
        return topics.map(TopicCollection::getTopics).orElseGet(ArrayList::new);
    }

    public String store(String key, String email) {
        try {
            File keys = new File("store/keys");
            keys.mkdirs();
            final File file = new File(keys, email + ".pfx");
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(key);
            writer.close();
            return file.getName();
        }
        catch (IOException e) {
            return null;
        }
    }

    public KeyPair getPair(String email) {
        try {
            if (this.pair == null) {
                Notification.show("Generating keys..");
                this.pair = KeyUtilities.generateKeyPair(new UserData(null, email));
            }
        }
        catch (NoSuchAlgorithmException e1) {
            Notification.show("Could not prepare keys: " + e1.getMessage());
        }
        return this.pair;
    }
}
