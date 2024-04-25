package org.example;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server();

        Client client1 = new Client("Catarina", "MC_fanatic", server);
        Client client2 = new Client("Luís", "luf", server);
        Client client3 = new Client("Rosa", "quinhaz", server);
        Client client4 = new Client("Josefa", "josefa4609", server);

        client1.setCertificate(new Certificate(client1.getPublicRSAKey()));
        client2.setCertificate(new Certificate(client2.getPublicRSAKey()));
        client3.setCertificate(new Certificate(client3.getPublicRSAKey()));
        client4.setCertificate(new Certificate(client4.getPublicRSAKey()));
        server.addUser(client1);
        server.addUser(client2);
        server.addUser(client3);
        server.addUser(client4);
        server.validateCertificate(client1, client2.getCertificate()); //Alice validates Bob's certificate
        server.validateCertificate(client2, client1.getCertificate()); //Bob validates Alice's certificate
        server.validateCertificate(client3, client1.getCertificate()); //Alice validates Charlie's certificate
        server.validateCertificate(client4, client1.getCertificate()); //Alice validates David's certificate
    }
}