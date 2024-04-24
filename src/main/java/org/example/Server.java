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
    }

    public void forwardMessage(String message, Client clientSender, Client clientReceiver) {

        clientReceiver.receiveMessage(message, clientSender, clientReceiver);
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
            System.out.println("["+ LocalDateTime.now() + "]" + user.getUsername() + " ligou-se ao Chat.");
        } else {
            System.out.println("[TIMESTAMP]: Falha ao validar certificado para o utilizador " + user.getUsername());
        }
    }
}