import org.example.Certificate;
import org.example.CertificateAuthority;
import org.example.Client;
import org.example.Server;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
/**
 * This class tests the CertificateAuthority class.
 */
public class CertificateAuthorityTest {
    /**
     * Tests the signCertificate method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testSignCertificate() throws Exception {
        CertificateAuthority ca = new CertificateAuthority();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        Client client = new Client("Alice", "Alice123",new Server());
        Certificate certificate = new Certificate(pair.getPublic(), client.getUsername());

        Certificate signedCertificate = ca.signCertificate(certificate);

        assertTrue(signedCertificate.isValid(), "Certificate should be valid");
    }

    /**
     * Tests the convertFromPemFormat method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testConvertFromPemFormat() throws Exception {
        CertificateAuthority ca = new CertificateAuthority();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        String username = "TestUser";

        String pemCertificate = "-----BEGIN CERTIFICATE-----\n" +
                Base64.getEncoder().encodeToString(username.getBytes()) + " " +
                Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()) +
                "\n-----END CERTIFICATE-----";

        Certificate convertedCertificate = ca.convertFromPemFormat(pemCertificate);

        assertEquals(username, convertedCertificate.getUsername(), "Username should match");
        assertArrayEquals(pair.getPublic().getEncoded(), convertedCertificate.getPublicKey().getEncoded(), "Public key should match");
    }

    /**
     * Tests the convertToPemFormat method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void TestGetPublicKey() throws Exception{
        CertificateAuthority ca = new CertificateAuthority();
        assertNotNull(ca.getPublicKey(), "Public key should not be null");
    }

    /**
     * Tests the convertToPemFormat method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testConvertFromPermFormatWithInvalidCertificate() throws NoSuchAlgorithmException {
        CertificateAuthority ca = new CertificateAuthority();
        String invalidPemCertificate = "-----BEGIN CERTIFICATE-----\n" +
                "InvalidCertificate" +
                "\n-----END CERTIFICATE-----";

        assertThrows(IllegalArgumentException.class, () -> ca.convertFromPemFormat(invalidPemCertificate));
    }
}