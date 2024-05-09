package org.example;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a server object that is used to manage the clients and the communication between them.
 */
public class Server {
    private final List<Client> clients;

    /**
     * Instantiates a new Server.
     */
    public Server() {
        this.clients = new ArrayList<>();
    }

    /**
     * Adds a user to the server.
     *
     * @param client the client
     */

    public void addUser(Client client) {
        clients.add(client);
        for(Client c : clients){
            if(c.getWindow() != null) {
                c.getWindow().updateClient(clients.toArray(new Client[0]));
            }
        }
    }

    /**
     * Forwards a message from a client to another
     *
     * @param message        the message
     * @param clientSender   the client sender
     * @param clientReceiver the client receiver
     * @throws Exception the exception
     */
    public void forwardMessage(Message message, Client clientSender, Client clientReceiver) throws Exception {
        startCommunication(clientSender, clientReceiver);
        clientReceiver.receiveMessage(message, clientSender);
    }

    /**
     * Distributes a certificate to all clients.
     *
     * @param certificate the certificate
     */
    public void distributeCertificate(Certificate certificate) {
        for (Client c : clients) {
            if (!c.getUsername().equals(certificate.getUsername())) {
                c.receiveCertificate(certificate);
            }
        }
    }

    /**
     * Starts the communication between two clients.
     *
     * @param client1 the client 1
     * @param client2 the client 2
     * @throws Exception the exception
     */
    public void startCommunication(Client client1, Client client2) throws Exception {
        client1.agreeOnSharedSecret(client2);
        client2.agreeOnSharedSecret(client1);
    }

    /**
     * Gets clients.
     *
     * @return the clients
     */
    public List<Client> getClients() {
        return clients;
    }
}