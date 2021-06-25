package nl.guuslieben.circle.common.message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Optional;

class UtilitiesTests {

    @Test
    void testToJson() {
        final String json = MessageUtilities.toJson(new TestObject("Circle"));
        Assertions.assertNotNull(json);
        Assertions.assertEquals("{\"name\":\"Circle\"}", json);
    }

    @Test
    void testInvalidToJson() {
        final String json = MessageUtilities.toJson(null);
        Assertions.assertNotNull(json);
        Assertions.assertEquals(MessageUtilities.INVALID, json);
    }

    @Test
    void testFromJson() {
        final Message message = new Message(new TestObject("Circle"));
        final String json = MessageUtilities.toJson(message);

        final Message output = MessageUtilities.fromJson(json);
        Assertions.assertEquals(message, output);
    }

    @Test
    void testParseTimestamp() {
        final String timestamp = "2021-01-01 17:30:00";
        final LocalDateTime localDateTime = MessageUtilities.parseTimestamp(timestamp);

        Assertions.assertNotNull(localDateTime);
        Assertions.assertEquals(2021, localDateTime.getYear());
        Assertions.assertEquals(1, localDateTime.getMonthValue());
        Assertions.assertEquals(1, localDateTime.getDayOfMonth());
        Assertions.assertEquals(17, localDateTime.getHour());
        Assertions.assertEquals(30, localDateTime.getMinute());
        Assertions.assertEquals(0, localDateTime.getSecond());
    }

    @Test
    void testParseInvalidTimestamp() {
        final String timestamp = "01-01-2021T17:30";
        final LocalDateTime localDateTime = MessageUtilities.parseTimestamp(timestamp);
        Assertions.assertNull(localDateTime);
    }

    @Test
    void testGetContent() {
        final Message message = new Message("{\"name\":\"Circle\"}");
        final Optional<TestObject> content = MessageUtilities.getContent(message, TestObject.class);
        Assertions.assertTrue(content.isPresent());
        Assertions.assertEquals("Circle", content.get().getName());
    }

    @Test
    void testInvalidGetContent() {
        final Message message = new Message("{name\":1}");
        final Optional<TestObject> content = MessageUtilities.getContent(message, TestObject.class);
        Assertions.assertFalse(content.isPresent());
    }

    @Test
    void testValidMessageVerifies() {
        final Message message = new Message("{\"name\":\"Circle\"}");
        Assertions.assertTrue(MessageUtilities.verify(message));
    }

    @Test
    void testInvalidMessageFails() throws NoSuchFieldException, IllegalAccessException {
        final Message message = new Message("{\"name\":\"Circle\"}");
        final Field hash = Message.class.getDeclaredField("hash");
        hash.setAccessible(true);
        hash.set(message, "notARealHash");
        Assertions.assertFalse(MessageUtilities.verify(message));
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

    @Test
    void testInvalidCertificateFromPem() {
        final Optional<X509Certificate> certificate = CertificateUtilities.fromPem("EOL");
        Assertions.assertFalse(certificate.isPresent());
    }

    @Test
    void testNullCertificateFromPem() {
        final Optional<X509Certificate> certificate = CertificateUtilities.fromPem(null);
        Assertions.assertFalse(certificate.isPresent());
    }
}
