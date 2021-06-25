package nl.guuslieben.circle.common.message;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

public class CertificateUtilities {

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String CERTIFICATE_ALGORITHM = "RSA";
    private static final String CERTIFICATE_DN = "CN=cn, O=o, L=L, ST=il, C= c";
    private static final String CF_INSTANCE = "X509";
    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSAEncryption";
    private static final String KEYSTORE_TYPE = "JKS";
    private static final int CERTIFICATE_BITS = 1024;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        var keyPairGenerator = KeyPairGenerator.getInstance(CERTIFICATE_ALGORITHM);
        keyPairGenerator.initialize(CERTIFICATE_BITS, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    @SuppressWarnings("deprecation")
    public static X509Certificate createCertificate(KeyPair keyPair) throws Exception {
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

    public static String toPem(X509Certificate cert) throws Exception {
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
}
