package nl.guuslieben.circle.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonSerialize
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Response {

    @JsonProperty
    private long topicId;
    @JsonProperty
    private String content;
    @JsonProperty
    private UserData author;

}
