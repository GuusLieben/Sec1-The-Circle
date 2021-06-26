package nl.guuslieben.circle.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@JsonSerialize
@Getter
public class User {

    @JsonProperty
    private String email;
    @JsonProperty
    private String name;
    @JsonProperty
    private String password;

    public UserData toData() {
        return new UserData(this.getName(), this.getEmail());
    }
}
