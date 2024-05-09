import org.example.*;
import org.junit.jupiter.api.Test;
import java.security.PublicKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * This class tests the Server class.
 */
class ServerTest {
    /**
     * Tests the addUser method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testAddUser() throws Exception {
        Server server = new Server();
        Client client1 = new Client("João","user1", server);
        Client client2 = new Client("Ana","user2", server);

        server.addUser(client1);
        server.addUser(client2);

        List<Client> clients = server.getClients();
        assertEquals(2, clients.size());
        assertTrue(clients.contains(client1));
        assertTrue(clients.contains(client2));
    }

    /**
     * Tests the removeUser method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testDistributeCertificate() throws Exception {
        Server server = new Server();
        Client client1 = new Client("João", "user1", server);
        Client client2 = new Client("Ana", "user2", server);
        Client client3 = new Client("Carlos", "user3", server);

        CertificateAuthority ca = new CertificateAuthority();

        PublicKey caPublicKey = ca.getPublicKey();
        List<String> crl = ca.getCrl();

        assertNotNull(caPublicKey, "CA public key is null");

        client1.setCaPublicKeyAndCrl(caPublicKey, crl);
        client2.setCaPublicKeyAndCrl(caPublicKey, crl);
        client3.setCaPublicKeyAndCrl(caPublicKey, crl);

        client1.obtainAndShareCertificate(ca);
        client2.obtainAndShareCertificate(ca);
        client3.obtainAndShareCertificate(ca);

        assertNotNull(client1.getPublicRSAKey(), "Client1 public key is null");
        assertNotNull(client2.getPublicRSAKey(), "Client2 public key is null");
        assertNotNull(client3.getPublicRSAKey(), "Client3 public key is null");

        Certificate certificate1 = new Certificate(client1.getPublicRSAKey(), client1.getUsername());
        assertNotNull(certificate1, "Certificate1 is null");
        System.out.println("Expected certificate: " + certificate1);
        server.distributeCertificate(certificate1);
        Certificate certificate2 = new Certificate(client2.getPublicRSAKey(), client2.getUsername());
        assertNotNull(certificate2, "Certificate2 is null");
        System.out.println("Expected certificate: " + certificate2);
        server.distributeCertificate(certificate2);
        Certificate certificate3 = new Certificate(client3.getPublicRSAKey(), client3.getUsername());
        assertNotNull(certificate3, "Certificate3 is null");
        System.out.println("Expected certificate: " + certificate3);
        server.distributeCertificate(certificate3);

        Certificate receivedCertificate1 = client1.getCertificate();
        System.out.println("Received certificate: " + receivedCertificate1);
        Certificate receivedCertificate2 = client2.getCertificate();
        System.out.println("Received certificate: " + receivedCertificate2);
        Certificate receivedCertificate3 = client3.getCertificate();
        System.out.println("Received certificate: " + receivedCertificate3);

        assertNotNull(receivedCertificate1, "Client1 did not receive a certificate");
        assertNotNull(receivedCertificate2, "Client2 did not receive a certificate");
        assertNotNull(receivedCertificate3, "Client3 did not receive a certificate");

        if(certificate1.isValid()){
            assertEquals(certificate1.getPublicKey(), receivedCertificate1.getPublicKey(), "Client1 did not receive the correct public key");
        } else{
            System.out.println("Certificate1 is not valid");
        }

        if(certificate2.isValid()){
            assertEquals(certificate2.getPublicKey(), receivedCertificate2.getPublicKey(), "Client2 did not receive the correct public key");
        } else{
            System.out.println("Certificate2 is not valid");
        }

        if(certificate3.isValid()){
            assertEquals(certificate3.getPublicKey(), receivedCertificate3.getPublicKey(), "Client3 did not receive the correct public key");
        } else{
            System.out.println("Certificate3 is not valid");
        }
    }

    /**
     * Tests the startCommunication method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testForwardMessage() throws Exception {
        Server server = new Server();
        Client client1 = new Client("João", "johnny", server);
        Client client2 = new Client("Ana", "anita", server);

        server.addUser(client1);
        server.addUser(client2);

        String messageFromClient1 = "Hello Ana";
        byte[] encryptedMessage = Encryption.encryptRSA(messageFromClient1, client1.getPublicRSAKey());
        Message message = new Message(encryptedMessage, client1.getUsername().getBytes());

        server.startCommunication(client1, client2);

        String decryptedMessage = Encryption.decryptRSA(message.getMessage(), client1.getPrivateRSAKey());

        assertEquals(messageFromClient1, decryptedMessage);
    }

    /**
     * Tests the startCommunication method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testStartCommunication() throws Exception {
        Server server = new Server();
        Client client1 = new Client("João", "user1", server);
        Client client2 = new Client("Ana", "user2", server);
        server.addUser(client1);
        server.addUser(client2);
        server.startCommunication(client1, client2);
        assertNotNull(client1.getSharedSecret());
        assertNotNull(client2.getSharedSecret());

        server.startCommunication(client1, client2);

        if(client1.getSharedSecret() == client2.getSharedSecret()){
            assertEquals(client1.getSharedSecret(), client2.getSharedSecret());
        }else{
            System.out.println("The shared secrets are different");
        }
    }
}
