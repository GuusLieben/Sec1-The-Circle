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

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Optional;

import lombok.Getter;
import nl.guuslieben.circle.common.User;
import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.rest.CertificateSigningRequest;
import nl.guuslieben.circle.common.util.CertificateUtilities;
import nl.guuslieben.circle.common.util.KeyUtilities;
import nl.guuslieben.circle.common.util.Message;
import nl.guuslieben.circle.common.util.MessageUtilities;

@Service
public class ClientService {

    @Getter
    private KeyPair pair;
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
        final Message content = new Message(body);
        RestTemplate template = this.templateBuilder.build();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        headers.set(HttpHeaders.CONNECTION, "keep-alive");

        if (encrypt) {
            final String publicKey = KeyUtilities.encodeKeyToBase64(this.pair.getPublic());
            headers.set(MessageUtilities.PUBLIC_KEY, publicKey);

            byte[] encrypted = KeyUtilities.encryptContent(content, this.pair.getPrivate());
            HttpEntity<?> entity = new HttpEntity<>(encrypted, headers);

            final ResponseEntity<byte[]> response = template.postForEntity(url, entity, byte[].class);
            return KeyUtilities.decryptContent(response.getBody(), this.serverPublic);
        } else {
            final ResponseEntity<Message> response = template.postForEntity(url, content, Message.class);
            return Optional.ofNullable(response.getBody());
        }
    }

    public Optional<X509Certificate> csr(UserData data, String password) {
        try {
            // TODO: null-check for development purposes only, remove once done
            if (this.pair == null)
                this.pair = KeyUtilities.generateKeyPair(data);
        }
        catch (NoSuchAlgorithmException e1) {
            Notification.show("Could not prepare keys: " + e1.getMessage());
        }

        final String publicKey = KeyUtilities.encodeKeyToBase64(this.pair.getPublic());

        final CertificateSigningRequest model = CertificateSigningRequest.create(publicKey);
        final Optional<Message> response = this.send("http://localhost:9090/csr", model, false);
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

    public boolean register(User user) {
        final Optional<Message> response = this.send("http://localhost:9090/register", user, true);
        if (response.isEmpty()) return false;

        final Optional<Boolean> result = MessageUtilities.verifyContent(response.get(), boolean.class);
        return result.orElse(false);
    }
}
