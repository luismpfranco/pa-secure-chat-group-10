import org.example.*;
import org.example.Certificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * This class tests the Client class.
 */
public class ClientTest {
    private Server server;
    private Client client1;
    private Client client2;
    @BeforeEach
    void setUp() throws Exception {
        server = new Server();

        client1 = new Client("Sender", "sender", server);
        client2 = new Client("Receiver", "receiver", server);

        server.addUser(client1);
        server.addUser(client2);
    }

    /**
     * Tests the sendMessage method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testSendMessage() throws Exception {

        client1.setWindow();
        client2.setWindow();

        String message = "Hello, Receiver!";

        client1.sendMessage(message, client2);

        Thread.sleep(2000);

        assertTrue(client2.getReceivedMessages().isEmpty(), "Receiver did not receive the message");
        if (!client2.getReceivedMessages().isEmpty()) {
            assertEquals(message, new String(client2.getReceivedMessages().get(0).getMessage()), "Received message does not match the sent message");
        }

        assertTrue(client1.getReceivedMessages().isEmpty(), "Sender did not receive the message");
        if (!client1.getReceivedMessages().isEmpty()) {
            assertEquals(message, new String(client1.getReceivedMessages().get(0).getMessage()), "Received message does not match the sent message");
        }
    }

    /**
     * Tests the sendMessage method when the receiver is not connected to the server.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testSendMessageToSelf() throws Exception {
        client1.setWindow();
        String message = "Hello, Sender!";

        client1.sendMessage(message, client1);

        assertEquals(0, client1.getReceivedMessages().size(), "No messages should be received");
    }

    /**
     * Tests the sendMessage method when the receiver is not connected to the server.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testReceiveCertificate() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        Certificate certificate = new Certificate(pair.getPublic(), client1.getUsername());

        server.distributeCertificate(certificate);

        client1.setCaPublicKeyAndCrl(pair.getPublic(), new ArrayList<>());

        if(certificate.isValid()){
            client1.receiveCertificate(certificate);
            assertEquals(certificate, client1.getCertificate(), "Certificate should match");
        } else {
            assertNull(client1.getCertificate(), "Certificate should be null");
        }
    }

    /**
     * Tests the submitUnsignedCertificate method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testSubmitUnsignedCertificate() throws Exception {
        CertificateAuthority ca = new CertificateAuthority();
        client1.submitUnsignedCertificate(ca);

        Path path = Paths.get("certificates/" + client1.getUsername() + ".pem");
        assertTrue(Files.exists(path), "The certificate file does not exist");
    }

    /**
     * Tests the receivePublicKeyForSharedSecret method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testReceivePublicKeyForSharedSecret() throws Exception {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair1 = keyPairGenerator.generateKeyPair();
        KeyPair keyPair2 = keyPairGenerator.generateKeyPair();

        BigInteger privateKey1 = new BigInteger(1, keyPair1.getPrivate().getEncoded());

        BigInteger publicKey2 = new BigInteger(1, keyPair2.getPublic().getEncoded());

        client1.receivePublicKeyForSharedSecret(client2.getUsername(), publicKey2, privateKey1);

        BigInteger expectedSharedSecret = DiffieHellman.computeSecret(publicKey2, privateKey1);
        assertEquals(expectedSharedSecret, client1.getSharedSecret().get(client2.getUsername()), "The shared secret does not match the expected value");
    }

    /**
     * Tests the receivePublicKeyForSharedSecret method with a null private key.
     */
    @Test
    void testValidateReceivedCertificate(){
        Certificate validCertificate = new Certificate(client1.getPublicRSAKey(), client1.getUsername());
        validCertificate.setSignature(generateValidSignature());

        List<String> crl = new ArrayList<>();

        crl.add(client1.getUsername());

        assertThrows(CertificateException.class, () -> client1.validateReceivedCertificate(validCertificate, client1.getPublicRSAKey(), crl));

        Certificate invalidCertificate = new Certificate(client1.getPublicRSAKey(), client1.getUsername() + "invalid");
        invalidCertificate.setSignature("invalidSignature");

        assertThrows(SignatureException.class, () -> client1.validateReceivedCertificate(invalidCertificate, client1.getPublicRSAKey(), crl));
    }

    /**
     * Tests the receivePublicKeyForSharedSecret method with a null private key.
     */
    private String generateValidSignature(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] signature = new byte[256];
        secureRandom.nextBytes(signature);
        return Base64.getEncoder().encodeToString(signature);
    }

    /**
     * Tests the receivePublicKeyForSharedSecret method with a null private key.
     */
    @Test
    void testReceivePublicKeyForSharedSecretWithNullPrivateKey() {

        BigInteger otherPublicKey = BigInteger.valueOf(12345);
        BigInteger myPrivateKey = null;

        Exception exception = assertThrows(Exception.class, () -> {
            client1.receivePublicKeyForSharedSecret("otherUsername", otherPublicKey, myPrivateKey);
        });

        String expectedMessage = "Private key is null";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Tests the receivePublicKeyForSharedSecret method with a null public key.
     */
    @Test
    void testValidateReceivedCertificateWithRevokedCertificate() {

        Certificate validCertificate = new Certificate(client1.getPublicRSAKey(), client1.getUsername());
        validCertificate.setSignature(generateValidSignature());

        List<String> crl = new ArrayList<>();
        crl.add(client1.getUsername());

        Exception exception = assertThrows(CertificateException.class, () -> {
            client1.validateReceivedCertificate(validCertificate, client1.getPublicRSAKey(), crl);
        });

        String expectedMessage = "The certificate for the user " + client1.getUsername() + " has been revoked.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Tests the receivePublicKeyForSharedSecret method with a null public key.
     */
    @Test
    void testValidateReceivedCertificateWithInvalidSignature(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[256];
        secureRandom.nextBytes(randomBytes);
        String invalidSignature = Base64.getEncoder().encodeToString(randomBytes);

        Certificate invalidCertificate = new Certificate(client1.getPublicRSAKey(), client1.getUsername());
        invalidCertificate.setSignature(invalidSignature);

        List<String> crl = new ArrayList<>();

        assertThrows(SignatureException.class, () -> {
            client1.validateReceivedCertificate(invalidCertificate, client1.getPublicRSAKey(), crl);
        });
    }
}