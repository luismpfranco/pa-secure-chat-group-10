import org.example.Encryption;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/**
 * This class tests the Encryption class.
 */
public class EncryptionTest {
    /**
     * Tests the createDigest method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testCreateDigest() throws Exception {
        String message = "Test message";
        byte[] digest = Encryption.createDigest(message);

        assertNotNull(digest, "Digest should not be null");
        assertEquals(32, digest.length, "Digest length should be 32 bytes for SHA-256");
    }

    /**
     * Tests the verifyDigest method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testVerifyDigest() throws Exception {
        String message = "Test message";
        byte[] digest = Encryption.createDigest(message);

        boolean isVerified = Encryption.verifyDigest(message, digest);

        assertTrue(isVerified, "Digest should be verified");
    }
}