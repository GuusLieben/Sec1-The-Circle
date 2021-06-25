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
import nl.guuslieben.circle.common.message.CertificateUtilities;
import nl.guuslieben.circle.common.message.Message;
import nl.guuslieben.circle.common.message.MessageUtilities;
import nl.guuslieben.circle.common.rest.RegisterUserModel;

@RestController
public class ServerController {

    @PostMapping("register")
    public Message register(@RequestBody Message message) {
        final Optional<RegisterUserModel> userModel = MessageUtilities.verifyContent(message, RegisterUserModel.class);

        if (userModel.isPresent()) {
            final RegisterUserModel user = userModel.get();
            final Optional<PublicKey> key = MessageUtilities.decodeBase64ToKey(user.getPublicKey());

            if (key.isPresent()) {
                try {
                    final KeyPair pair = new KeyPair(key.get(), TheCircleApplication.KEYS.getPrivate());
                    final X509Certificate certificate = CertificateUtilities.createCertificate(pair);
                    final String pem = CertificateUtilities.toPem(certificate);
                    // TODO: Store user data
                    final RegisterUserModel model = new RegisterUserModel(user.getData(), pem);
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
