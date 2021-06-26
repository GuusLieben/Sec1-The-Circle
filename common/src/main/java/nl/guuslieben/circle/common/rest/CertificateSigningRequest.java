package nl.guuslieben.circle.common.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonSerialize
@Getter
@NoArgsConstructor
public class CertificateSigningRequest {

    @JsonProperty
    @JsonInclude(Include.NON_NULL)
    private String publicKey;

    // Should only be sent by server, signed with server private, containing client public
    @JsonProperty
    @JsonInclude(Include.NON_NULL)
    private String certificate;

    private CertificateSigningRequest(String publicKey, String certificate) {
        this.publicKey = publicKey;
        this.certificate = certificate;
    }

    public static CertificateSigningRequest create(String key) {
        return new CertificateSigningRequest(key, null);
    }

    public static CertificateSigningRequest accept(String certificate) {
        return new CertificateSigningRequest(null, certificate);
    }
}
