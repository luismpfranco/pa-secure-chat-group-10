import org.example.Client;
import org.example.ClientWindow;
import org.example.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * This class tests the ClientWindow class.
 */
public class ClientWindowTest {
    private ClientWindow clientWindow;
    private Client client;
    private Server server;
    private JTextField inputField;
    private JList<Client> clientList;

    /**
     * Sets up the test environment.
     *
     * @throws Exception if any error occurs during the test
     */
    @BeforeEach
    void setUp() throws Exception {
        server = new Server();
        client = new Client("TestClient", "test client", server);
        clientWindow = new ClientWindow(client, server);
        client.setWindow();
        inputField = clientWindow.getInputField();
        clientList = clientWindow.getClientList();
    }

    /**
     * Tests the addMessage method.
     */
    @Test
    void testAddMessage() {
        String message = "Test message";
        clientWindow.addMessage(message);
        String textInMessageArea = clientWindow.getMessagesArea().getText();
        assertTrue(textInMessageArea.contains(message), "The message area should contain the added message");
    }

    /**
     * Tests the updateClient method.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void testUpdateClient() throws Exception {
        Client newClient = new Client("NewClient", "new client", server);
        server.addUser(newClient);
        clientWindow.updateClient(server.getClients().toArray(new Client[0]));
        int numberOfClientsInList = clientWindow.getClientList().getModel().getSize();
        assertEquals(1, numberOfClientsInList, "The client list should contain one client");
    }

    /**
     * Tests the updateClient method when the client list is empty.
     */
    @Test
    void testGetMessagesArea() {
        JTextArea messagesArea = clientWindow.getMessagesArea();
        assertNotNull(messagesArea, "The messages area should not be null");
    }

    /**
     * Tests the getClientList method.
     */
    @Test
    void testGetClientList() {
        JList<Client> clientList = clientWindow.getClientList();
        assertNotNull(clientList, "The client list should not be null");
    }

    /**
     * Tests the getInputField method.
     */
    @Test
    void testGetInputField() {
        JTextField inputField = clientWindow.getInputField();
        assertNotNull(inputField, "The input field should not be null");
    }
}