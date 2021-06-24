package nl.guuslieben.circle;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import nl.guuslieben.circle.common.message.Message;

@Service
public class ClientService {

    private final RestTemplateBuilder templateBuilder;

    public ClientService(RestTemplateBuilder templateBuilder) {
        this.templateBuilder = templateBuilder;
    }

    public Optional<Message> request(String url) {
        RestTemplate template = this.templateBuilder.build();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        headers.set(HttpHeaders.CONNECTION, "keep-alive");

        HttpEntity<?> entity = new HttpEntity<>(headers);
        return Optional.ofNullable(template.getForObject(url, Message.class));
    }

}
