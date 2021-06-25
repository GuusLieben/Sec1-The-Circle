package nl.guuslieben.circle.common.message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Optional;

class UtilitiesTests {

    @Test
    void testToJson() {
        final String json = MessageUtilities.toJson(new TestObject("Circle"));
        Assertions.assertNotNull(json);
        Assertions.assertEquals("{\"name\":\"Circle\"}", json);
    }

    @Test
    void testFromJson() {
        final Message message = new Message(new TestObject("Circle"));
        final String json = MessageUtilities.toJson(message);

        final Message output = MessageUtilities.fromJson(json);
        Assertions.assertEquals(message, output);
    }

    @Test
    void testCreateCertificate() throws Exception {
        final KeyPair keyPair = CertificateUtilities.generateKeyPair();
        final X509Certificate certificate = CertificateUtilities.createCertificate(keyPair);
        Assertions.assertNotNull(certificate);
        Assertions.assertEquals(keyPair.getPublic(), certificate.getPublicKey());
    }

    @Test
    void testCertificateToPem() throws Exception {
        final KeyPair keyPair = CertificateUtilities.generateKeyPair();
        final X509Certificate certificate = CertificateUtilities.createCertificate(keyPair);
        final String pem = CertificateUtilities.toPem(certificate);
        Assertions.assertNotNull(pem);
        Assertions.assertTrue(pem.startsWith(CertificateUtilities.BEGIN_CERT));
        Assertions.assertTrue(pem.endsWith(CertificateUtilities.END_CERT));
    }

    @Test
    void testCertificateFromPem() throws Exception {
        final KeyPair keyPair = CertificateUtilities.generateKeyPair();
        final X509Certificate certificate = CertificateUtilities.createCertificate(keyPair);
        final String pem = CertificateUtilities.toPem(certificate);

        final Optional<X509Certificate> x509CertificateCandidate = CertificateUtilities.fromPem(pem);
        Assertions.assertTrue(x509CertificateCandidate.isPresent());
        final X509Certificate x509Certificate = x509CertificateCandidate.get();
        Assertions.assertEquals(keyPair.getPublic(), x509Certificate.getPublicKey());
    }
}
