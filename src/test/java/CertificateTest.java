import org.example.Certificate;
import org.example.Client;
import org.junit.jupiter.api.Test;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import static org.junit.jupiter.api.Assertions.*;
/**
 * This class tests the Certificate class.
 */
public class CertificateTest {
    /**
     * Tests the constructor and the getters of the Certificate class.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testConstructorAndGetters() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        Client client = new Client("Test", "test1", null);
        Certificate certificate = new Certificate(pair.getPublic(), client.getUsername());

        assertEquals(pair.getPublic(), certificate.getPublicKey(), "Public key should match");
        assertEquals(client.getUsername(), certificate.getUsername(), "Username should match");
        assertNull(certificate.getSignature(), "Signature should be null");
        assertFalse(certificate.isValid(), "Certificate should not be valid");
    }

    /**
     * Tests the setters of the Certificate class.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testSetters() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        Client client = new Client("Test2", "test2", null);
        Certificate certificate = new Certificate(pair.getPublic(), client.getUsername());

        String signature = "TestSignature";
        certificate.setSignature(signature);
        assertEquals(signature, certificate.getSignature(), "Signature should match");

        certificate.setValid(true);
        assertTrue(certificate.isValid(), "Certificate should be valid");
    }
}