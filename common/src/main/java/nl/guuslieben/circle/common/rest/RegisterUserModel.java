package nl.guuslieben.circle.common.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.guuslieben.circle.common.UserData;

@JsonSerialize
@Getter
@NoArgsConstructor
public class RegisterUserModel {

    @JsonProperty
    private UserData data;

    // Should be encrypted by client
    @JsonProperty
    @JsonInclude(Include.NON_NULL)
    private String password;

    @JsonProperty
    @JsonInclude(Include.NON_NULL)
    private String publicKey;

    // Should only be sent by server, signed with server private, containing client public
    @JsonProperty
    @JsonInclude(Include.NON_NULL)
    private String certificate;

    public RegisterUserModel(UserData data, String certificate) {
        this.data = data;
        this.certificate = certificate;
    }

    public RegisterUserModel(UserData data, String password, String publicKey) {
        this.data = data;
        this.password = password;
        this.publicKey = publicKey;
    }
}
