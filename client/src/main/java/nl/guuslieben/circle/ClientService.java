package nl.guuslieben.circle;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
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
        return Optional.ofNullable(template.getForObject(url, Message.class));
    }

}
