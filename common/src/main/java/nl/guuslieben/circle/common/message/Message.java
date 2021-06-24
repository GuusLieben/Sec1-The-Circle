package nl.guuslieben.circle.common.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonSerialize
@NoArgsConstructor
@Getter
public class Message {

    @JsonProperty
    private String hash;
    @JsonProperty
    private String timestamp;
    @JsonProperty
    private String content;

    public Message(Object content) {
        this.content = MessageUtilities.toJson(content);
        this.hash = MessageUtilities.generateHash(this.content);
        this.timestamp = MessageUtilities.generateTimestamp();
    }
}
