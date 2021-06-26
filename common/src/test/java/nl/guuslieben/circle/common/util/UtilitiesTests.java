package nl.guuslieben.circle.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Optional;

import nl.guuslieben.circle.common.UserData;

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
        final KeyPair keyPair = KeyUtilities.generateKeyPair(new UserData("John Doe", "john@example.org"));
        final X509Certificate certificate = CertificateUtilities.createCertificate(keyPair);
        Assertions.assertNotNull(certificate);
        Assertions.assertEquals(keyPair.getPublic(), certificate.getPublicKey());
    }

    @Test
    void testCertificateToPem() throws Exception {
        final KeyPair keyPair = KeyUtilities.generateKeyPair(new UserData("John Doe", "john@example.org"));
        final X509Certificate certificate = CertificateUtilities.createCertificate(keyPair);
        final String pem = CertificateUtilities.toPem(certificate);
        Assertions.assertNotNull(pem);
        Assertions.assertTrue(pem.startsWith(CertificateUtilities.BEGIN_CERT));
        Assertions.assertTrue(pem.endsWith(CertificateUtilities.END_CERT));
    }

    @Test
    void testCertificateFromPem() throws Exception {
        final KeyPair keyPair = KeyUtilities.generateKeyPair(new UserData("John Doe", "john@example.org"));
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

    @Test
    void testVerifyCertificate() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        final KeyPair keyPair = KeyUtilities.generateKeyPair(new UserData("John Doe", "john@example.org"));
        final X509Certificate certificate = CertificateUtilities.createCertificate(keyPair);
        Assertions.assertTrue(CertificateUtilities.verify(certificate, keyPair.getPublic()));
    }

    @Test
    void testInvalidVerifyCertificate() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        final KeyPair keyPair = KeyUtilities.generateKeyPair(new UserData("John Doe", "john@example.org"));
        final KeyPair otherKeyPair = KeyUtilities.generateKeyPair(new UserData("Jane Doe", "jane@example.org"));
        final X509Certificate certificate = CertificateUtilities.createCertificate(keyPair);
        Assertions.assertFalse(CertificateUtilities.verify(certificate, otherKeyPair.getPublic()));
    }

    @Test
    void testPasswordEncryption() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PublicKey publicKey = pair.getPublic();
        final String password = "password";
        final String encrypted = PasswordUtilities.encrypt(password, password, publicKey);
        Assertions.assertNotNull(encrypted);
    }

    @Test
    void testPasswordDecryption() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PublicKey publicKey = pair.getPublic();
        final String password = "password";
        final String encrypted = PasswordUtilities.encrypt(password, password, publicKey);
        Assertions.assertNotNull(encrypted);

        final String decrypted = PasswordUtilities.decrypt(encrypted, password, publicKey);
        Assertions.assertNotNull(decrypted);
        Assertions.assertEquals(password, decrypted);
    }

    @Test
    void testIncorrectPasswordDecryption() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PublicKey publicKey = pair.getPublic();
        final String password = "password";
        final String encrypted = PasswordUtilities.encrypt(password, password, publicKey);
        Assertions.assertNotNull(encrypted);

        final String decrypted = PasswordUtilities.decrypt(encrypted, "wrongPassword", publicKey);
        Assertions.assertNull(decrypted);
    }

    @Test
    void testVerifyPassword() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PublicKey publicKey = pair.getPublic();
        final String password = "password";
        final String encrypted = PasswordUtilities.encrypt(password, password, publicKey);
        Assertions.assertNotNull(encrypted);

        final boolean verified = PasswordUtilities.verify(encrypted, password, publicKey);
        Assertions.assertTrue(verified);
    }

    @Test
    void testVerifyIncorrectPassword() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PublicKey publicKey = pair.getPublic();
        final String password = "password";
        final String encrypted = PasswordUtilities.encrypt(password, password, publicKey);
        Assertions.assertNotNull(encrypted);

        final boolean verified = PasswordUtilities.verify(encrypted, "wrongPassword", publicKey);
        Assertions.assertFalse(verified);
    }

    @Test
    void testEncodeKey() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PublicKey publicKey = pair.getPublic();
        final String encoded = KeyUtilities.encodeKeyToBase64(publicKey);
        Assertions.assertNotNull(encoded);
        Assertions.assertEquals(736, encoded.length());
    }

    @Test
    void testDecodeKey() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PublicKey publicKey = pair.getPublic();
        final String encoded = KeyUtilities.encodeKeyToBase64(publicKey);
        final Optional<PublicKey> key = KeyUtilities.decodeBase64ToKey(encoded);
        Assertions.assertTrue(key.isPresent());
        Assertions.assertEquals(publicKey, key.get());
    }

    @Test
    void testDecodeIncorrectKey() {
        final Optional<PublicKey> key = KeyUtilities.decodeBase64ToKey("invalidBase64");
        Assertions.assertFalse(key.isPresent());
    }

    @Test
    void testEncryptMessage() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PrivateKey privateKey = pair.getPrivate();
        final Message message = new Message("content");
        final byte[] bytes = KeyUtilities.encryptMessage(message, privateKey);
        Assertions.assertNotNull(bytes);
        Assertions.assertNotEquals(0, bytes.length);
    }

    @Test
    void testEncryptContent() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PrivateKey privateKey = pair.getPrivate();
        final String content = "content";
        final byte[] bytes = KeyUtilities.encryptContent(content, privateKey);
        Assertions.assertNotNull(bytes);
        Assertions.assertNotEquals(0, bytes.length);
    }

    @Test
    void testEncryptInvalidContent() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PrivateKey privateKey = pair.getPrivate();
        final byte[] bytes = KeyUtilities.encryptContent(null, privateKey);
        Assertions.assertNotNull(bytes);
        Assertions.assertEquals(0, bytes.length);
    }

    @Test
    void testDecryptMessage() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PrivateKey privateKey = pair.getPrivate();
        final Message message = new Message("content");
        final byte[] bytes = KeyUtilities.encryptMessage(message, privateKey);

        final PublicKey publicKey = pair.getPublic();
        final Optional<Message> decrypted = KeyUtilities.decryptMessage(bytes, publicKey);
        Assertions.assertTrue(decrypted.isPresent());
        Assertions.assertEquals(message, decrypted.get());
    }

    @Test
    void testDecryptContent() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PrivateKey privateKey = pair.getPrivate();
        final String content = "content";
        final byte[] bytes = KeyUtilities.encryptContent(content, privateKey);

        final PublicKey publicKey = pair.getPublic();
        final Optional<String> decrypted = KeyUtilities.decryptContent(bytes, publicKey);
        Assertions.assertTrue(decrypted.isPresent());
        Assertions.assertEquals(content, decrypted.get());
    }

    @Test
    void testDecryptInvalidContent() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PublicKey publicKey = pair.getPublic();
        final Optional<String> decrypted = KeyUtilities.decryptContent(new byte[] {1,2,3}, publicKey);
        Assertions.assertFalse(decrypted.isPresent());
    }

    @Test
    void testStoreAndGetKey() throws IOException, NoSuchAlgorithmException {
        final Path tempFile = Files.createTempFile("circle", "tmp");
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PublicKey publicKey = pair.getPublic();
        Assertions.assertDoesNotThrow(() -> KeyUtilities.storeKey(tempFile.toFile(), publicKey));

        final Optional<PublicKey> fileKey = KeyUtilities.getPublicKeyFromFile(tempFile.toFile());
        Assertions.assertTrue(fileKey.isPresent());
        Assertions.assertEquals(publicKey, fileKey.get());
    }

    @Test
    void testVerifyContent() {
        final Message message = new Message(new TestObject("Test"));
        final Optional<TestObject> testObject = MessageUtilities.verifyContent(message, TestObject.class);
        Assertions.assertTrue(testObject.isPresent());
        Assertions.assertEquals("Test", testObject.get().getName());
    }

    @Test
    void testEncryptedRejectMessage() throws NoSuchAlgorithmException {
        final KeyPair pair = KeyUtilities.generateKeyPair(new UserData("Bob", "bob@circle.com"));
        final PrivateKey privateKey = pair.getPrivate();
        final byte[] bytes = MessageUtilities.rejectEncrypted("reason", privateKey);
        Assertions.assertNotNull(bytes);
        Assertions.assertNotEquals(0, bytes.length);
    }

    @Test
    void testRejection() {
        final Message message = MessageUtilities.rejectMessage("reason");
        final Optional<String> rejection = MessageUtilities.getRejection(message);
        Assertions.assertTrue(rejection.isPresent());
        Assertions.assertEquals("reason", rejection.get());
    }
}
