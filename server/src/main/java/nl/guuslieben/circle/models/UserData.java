package nl.guuslieben.circle.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record UserData(@JsonProperty String name, @JsonProperty String email, @JsonProperty long id) {
}
