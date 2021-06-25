package nl.guuslieben.circle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.guuslieben.circle.common.message.Message;
import nl.guuslieben.circle.models.UserData;

@RestController
public class ServerController {

    @GetMapping
    public Message get() {
        return new Message(new UserData("Sample", "john@example.com", 123L));
    }

}
