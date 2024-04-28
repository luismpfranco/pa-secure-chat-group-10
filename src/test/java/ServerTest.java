import org.example.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {
    @Test
    void testAddUser() throws Exception {
        Server server = new Server();
        Client client1 = new Client("João","user1", server);
        Client client2 = new Client("Ana","user2", server);

        client1.setWindow();
        client2.setWindow();

        server.addUser(client1);
        server.addUser(client2);

        List<Client> clients = server.getClients();
        assertEquals(2, clients.size());
        assertTrue(clients.contains(client1));
        assertTrue(clients.contains(client2));
    }

    @Test
    void testDistributeCertificate() throws Exception {
        Server server = new Server();
        Client client1 = new Client("João", "user1", server);
        Client client2 = new Client("Ana", "user2", server);
        Client client3 = new Client("Carlos", "user3", server);

        client1.setWindow();
        client2.setWindow();
        client3.setWindow();

        server.addUser(client1);
        server.addUser(client2);
        server.addUser(client3);

        Certificate certificate1 = new Certificate(client1.getPublicRSAKey(), "user1");
        server.distributeCertificate(certificate1);
        Certificate certificate2 = new Certificate(client2.getPublicRSAKey(), "user2");
        server.distributeCertificate(certificate2);
        Certificate certificate3 = new Certificate(client3.getPublicRSAKey(), "user3");
        server.distributeCertificate(certificate3);

        Certificate receivedCertificate1 = client1.getCertificate();
        Certificate receivedCertificate2 = client2.getCertificate();
        Certificate receivedCertificate3 = client3.getCertificate();

        assertNotNull(receivedCertificate1, "Client1 did not receive a certificate");
        assertNotNull(receivedCertificate2, "Client2 did not receive a certificate");
        assertNotNull(receivedCertificate3, "Client3 did not receive a certificate");

        assertEquals(certificate1, receivedCertificate1, "Client1 did not receive the correct certificate");
        assertEquals(certificate2, receivedCertificate2, "Client2 did not receive the correct certificate");
        assertEquals(certificate3, receivedCertificate3, "Client3 did not receive the correct certificate");
    }
}
