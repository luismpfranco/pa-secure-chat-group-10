package org.example;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<Client> clients;

    public Server() {
        this.clients = new ArrayList<>();
    }

    public void addUser(Client client) {
        clients.add(client);
        distributeCertificate(client);
        for(Client c : clients){
            c.getWindow().updateClient(clients.toArray(new Client[0]));
        }
    }

    public void forwardMessage(Message message, Client clientSender, Client clientReceiver) throws Exception {
        if (clientReceiver == null)
        {
            broadcastMessage(message, clientSender);
        }
        else
        {
            clientReceiver.receiveMessage(message, clientSender, clientReceiver);
        }
    }

    public void broadcastMessage(Message message, Client clientSender) throws Exception {
        String decryptedMessage = Encryption.decryptRSA(message.getMessage(), clientSender.getPrivateRSAKey());
        if (!Encryption.verifyDigest(decryptedMessage, message.getDigest())) {
            throw new Exception("Message integrity check failed");
        }
        for (Client c : clients) {
            if (!c.getUsername().equals(clientSender.getUsername())) {
                c.receiveMessage(message, clientSender, c);
            }
        }
    }

    private void distributeCertificate(Client user) {
        for (Client c : clients) {
            if (!c.getUsername().equals(user.getUsername())) {
                c.receiveCertificate(user.getCertificate());
            }
        }
    }

    public void validateCertificate(Client user, Certificate certificate) {

        if(certificate == null) {
            System.out.println("[TIMESTAMP]: O utilizador " + user.getUsername() + " don't have a certificate.");
        }
        else if (certificate.isValid()) {
            System.out.println("["+ LocalDateTime.now() + "] " + user.getUsername() + " ligou-se ao Chat.");
        } else {
            System.out.println("[TIMESTAMP]: Falha ao validar certificado para o utilizador " + user.getUsername());
        }
    }

    public List<Client> getClients() {
        return clients;
    }
}