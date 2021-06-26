package nl.guuslieben.circle.common.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return this.getHash().equals(message.getHash()) && this.getContent().equals(message.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getHash(), this.getContent());
    }
}
