package nl.guuslieben.circle.common.message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MessageUtilitiesTests {

    @Test
    void testToJson() {
        final String json = MessageUtilities.toJson(new TestObject("Circle"));
        Assertions.assertNotNull(json);
        Assertions.assertEquals("{\"name\":\"Circle\"}", json);
    }

    @Test
    void testFromJson() {
        final Message message = new Message(new TestObject("Circle"));
        final String json = MessageUtilities.toJson(message);

        final Message output = MessageUtilities.fromJson(json);
        Assertions.assertEquals(message, output);
    }
}
