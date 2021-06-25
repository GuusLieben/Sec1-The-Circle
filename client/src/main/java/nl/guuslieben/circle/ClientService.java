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

import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.util.CertificateUtilities;
import nl.guuslieben.circle.common.util.KeyUtilities;
import nl.guuslieben.circle.common.util.Message;
import nl.guuslieben.circle.common.util.MessageUtilities;
import nl.guuslieben.circle.common.rest.CertificateSigningRequest;

@Service
public class ClientService {

    private final RestTemplateBuilder templateBuilder;
    private final PublicKey serverPublic;

    public ClientService(RestTemplateBuilder templateBuilder) {
        this.templateBuilder = templateBuilder;
        final Optional<PublicKey> publicKey = KeyUtilities.getPublicKeyFromFile(KeyUtilities.getServerPublic());
        if (publicKey.isPresent()) {
            this.serverPublic = publicKey.get();
        } else {
            throw new IllegalStateException("No (valid) server public key found");
        }
    }

    public Optional<Message> request(String url) {
        RestTemplate template = this.templateBuilder.build();
        return Optional.ofNullable(template.getForObject(url, Message.class));
    }

    public Optional<Message> send(String url, Object body) {
        final Message content = new Message(body);
        RestTemplate template = this.templateBuilder.build();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        headers.set(HttpHeaders.CONNECTION, "keep-alive");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        final ResponseEntity<Message> response = template.postForEntity(url, content, Message.class);
        return Optional.ofNullable(response.getBody());
    }

    public boolean csr(UserData data, String password) {
        KeyPair pair = null;
        try {
            pair = KeyUtilities.generateKeyPair(data);
        }
        catch (NoSuchAlgorithmException e1) {
            Notification.show("Could not prepare keys: " + e1.getMessage());
        }

        final String publicKey = KeyUtilities.encodeKeyToBase64(pair.getPublic());

        final CertificateSigningRequest model = CertificateSigningRequest.create(publicKey);
        final Optional<Message> response = this.send("http://localhost:9090/csr", model);
        if (response.isPresent()) {
            final Message message = response.get();
            final Optional<CertificateSigningRequest> request = MessageUtilities.verifyContent(message, CertificateSigningRequest.class);

            if (request.isPresent()) {
                final String certificate = request.get().getCertificate();
                final Optional<X509Certificate> x509Certificate = CertificateUtilities.fromPem(certificate);

                if (x509Certificate.isPresent()) {
                    return CertificateUtilities.verify(x509Certificate.get(), this.serverPublic);
                }
            }
        }
        return false;
    }

}
