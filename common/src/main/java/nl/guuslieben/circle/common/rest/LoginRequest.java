package nl.guuslieben.circle.common.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonSerialize
@NoArgsConstructor
@Getter
public class LoginRequest {

    @JsonProperty
    private String username;
    @JsonProperty
    private String password;

    public LoginRequest(String username, String password) {
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }
}
