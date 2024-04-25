package org.example;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final List<Client> clients;

    public Server() {
        this.clients = new ArrayList<>();
    }

    public void addUser(Client client) {
        clients.add(client);
        for(Client c : clients){
            c.getWindow().updateClient(clients.toArray(new Client[0]));
        }
    }

    public void forwardMessage(Message message, Client clientSender, Client clientReceiver) throws Exception {
        clientReceiver.receiveMessage(message, clientSender);
    }

    public void distributeCertificate(Certificate certificate) {
        for (Client c : clients) {
            if (!c.getUsername().equals(certificate.getUsername())) {
                c.receiveCertificate(certificate);
            }
        }
    }

    public List<Client> getClients() {
        return clients;
    }
}