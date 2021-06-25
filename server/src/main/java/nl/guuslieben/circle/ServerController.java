package nl.guuslieben.circle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Optional;

import nl.guuslieben.circle.common.UserData;
import nl.guuslieben.circle.common.util.CertificateUtilities;
import nl.guuslieben.circle.common.util.KeyUtilities;
import nl.guuslieben.circle.common.util.Message;
import nl.guuslieben.circle.common.util.MessageUtilities;
import nl.guuslieben.circle.common.rest.CertificateSigningRequest;

@RestController
public class ServerController {

    @PostMapping("csr")
    public Message register(@RequestBody Message message) {
        final Optional<CertificateSigningRequest> userModel = MessageUtilities.verifyContent(message, CertificateSigningRequest.class);

        if (userModel.isPresent()) {
            final CertificateSigningRequest user = userModel.get();
            final Optional<PublicKey> key = KeyUtilities.decodeBase64ToKey(user.getPublicKey());

            if (key.isPresent()) {
                try {
                    final KeyPair pair = new KeyPair(key.get(), TheCircleApplication.KEYS.getPrivate());
                    final X509Certificate certificate = CertificateUtilities.createCertificate(pair);
                    final String pem = CertificateUtilities.toPem(certificate);
                    final CertificateSigningRequest model = CertificateSigningRequest.accept(pem);
                    return new Message(model);
                }
                catch (CertificateEncodingException | SignatureException | InvalidKeyException e) {
                    return MessageUtilities.reject("Could not create certificate");
                }

            } else {
                return MessageUtilities.reject("Invalid key");
            }

        } else {
            return MessageUtilities.reject("Invalid model");
        }
    }

    @GetMapping
    public Message get() {
        return new Message(new UserData("Sample", "john@example.com", 123L));
    }

}
