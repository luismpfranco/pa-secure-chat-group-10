package org.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        CertificateAuthority ca = new CertificateAuthority();

        List<Client> clients = new ArrayList<>();
        clients.add(new Client("Catarina", "MC_fanatic", server));
        clients.add(new Client("Luís", "luf", server));
        clients.add(new Client("Rosa", "quinhaz", server));
        clients.add(new Client("Josefa", "josefa4609", server));

        for ( Client c : clients ) {
            c.setCaPublicKeyAndCrl(ca.getPublicKey(), ca.getCrl());
            c.obtainAndShareCertificate(ca);

            if(c.getCertificate() == null) {
                System.out.println("["+ LocalDateTime.now() + "] The user " + c.getUsername() + " don't have a certificate.");
            }
            else if (c.getCertificate().isValid()) {
                System.out.println("["+ LocalDateTime.now() + "] " + c.getUsername() + " has connected to Chat.");
                c.setWindow();
                server.addUser(c);
            } else {
                System.out.println("["+ LocalDateTime.now() + "] " + " Failed to validate certificate for user " + c.getUsername());
            }
        }
    }
}