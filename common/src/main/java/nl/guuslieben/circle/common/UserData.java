package nl.guuslieben.circle.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonSerialize
@NoArgsConstructor
@Getter
public final class UserData {

    @JsonProperty
    private String name;
    @JsonProperty
    private String email;
    @JsonProperty
    private long id;

    public UserData(@JsonProperty String name, @JsonProperty String email, @JsonProperty long id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }
}
