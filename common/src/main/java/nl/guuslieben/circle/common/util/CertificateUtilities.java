package nl.guuslieben.circle.common.util;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CertificateUtilities {

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String CERTIFICATE_DN = "CN=cn, O=o, L=L, ST=il, C= c";
    private static final String CF_INSTANCE = "X509";
    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSAEncryption";
    private static final File CERTS = new File("store/certs");


    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private CertificateUtilities() {
    }

    @SuppressWarnings("deprecation")
    public static X509Certificate createCertificate(KeyPair keyPair) throws SignatureException, InvalidKeyException {
        X509Certificate cert;

        var v3CertGen = new X509V3CertificateGenerator();
        v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        v3CertGen.setIssuerDN(new X509Principal(CERTIFICATE_DN));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * 10)));
        v3CertGen.setSubjectDN(new X509Principal(CERTIFICATE_DN));
        v3CertGen.setPublicKey(keyPair.getPublic());
        v3CertGen.setSignatureAlgorithm(SIGNATURE_ALGORITHM);
        cert = v3CertGen.generateX509Certificate(keyPair.getPrivate());
        return cert;
    }

    public static String toPem(X509Certificate cert) throws CertificateEncodingException {
        final var encoder = Base64.getEncoder();

        final var rawCrtText = cert.getEncoded();
        final var encodedCertText = new String(encoder.encode(rawCrtText));
        return BEGIN_CERT + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + END_CERT;
    }

    public static Optional<X509Certificate> fromPem(String certificate) {
        try {
            if (certificate != null && !certificate.trim().isEmpty()) {
                certificate = certificate.replace(BEGIN_CERT + LINE_SEPARATOR, "")
                        .replace(LINE_SEPARATOR + END_CERT, ""); // NEED FOR PEM FORMAT CERT STRING
                var certificateData = Base64.getDecoder().decode(certificate);
                var factory = CertificateFactory.getInstance(CF_INSTANCE);
                return Optional.of((X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certificateData)));
            }
            else {
                return Optional.empty();
            }
        }
        catch (CertificateException e) {
            return Optional.empty();
        }
    }

    public static boolean compare(Certificate certificate, PublicKey key) {
        return certificate.getPublicKey().equals(key);
    }

    public static boolean verify(Certificate certificate, PublicKey key) {
        try {
            certificate.verify(key);
            return true;
        }
        catch (CertificateException | NoSuchAlgorithmException | SignatureException | InvalidKeyException | NoSuchProviderException e) {
            return false;
        }
    }

    public static String store(X509Certificate certificate, String email) {
        try {
            final var file = new File(CERTS, email + ".cert");
            if (!CERTS.exists() && !CERTS.mkdirs()) return null;
            if (!file.exists() && !file.createNewFile()) return null;
            
            final var pem = CertificateUtilities.toPem(certificate);
            try (var stream = new FileOutputStream(file)) {
                stream.write(pem.getBytes(StandardCharsets.UTF_8));
            }

            return file.getName();
        }
        catch (CertificateEncodingException | IOException e) {
            return null;
        }
    }

    public static Optional<X509Certificate> get(String email) {
        final var file = new File(CERTS, email + ".cert");
        if (!file.exists()) return Optional.empty();

        try {
            final List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            final var content = String.join(LINE_SEPARATOR, lines);

            return fromPem(content);
        }
        catch (IOException e) {
            return Optional.empty();
        }
    }
}
