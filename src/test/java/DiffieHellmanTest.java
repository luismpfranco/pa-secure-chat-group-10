import org.example.DiffieHellman;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;
/**
 * This class tests the DiffieHellman class.
 */
public class DiffieHellmanTest {
    /**
     * Tests the key generation methods of the DiffieHellman class.
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testKeyGeneration() throws Exception {
        BigInteger privateKey = DiffieHellman.generatePrivateKey();
        assertNotNull(privateKey, "Private key should not be null");
        System.out.println("Private key: " + privateKey);

        BigInteger publicKey = DiffieHellman.generatePublicKey(privateKey);
        assertNotNull(publicKey, "Public key should not be null");
        System.out.println("Public key: " + publicKey);

        BigInteger secret = DiffieHellman.computeSecret(publicKey, privateKey);
        assertNotNull(secret, "Secret key should not be null");
        System.out.println("Secret key: " + secret);
    }
}