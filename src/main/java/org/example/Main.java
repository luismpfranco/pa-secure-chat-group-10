package org.example;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        KeyPair keyPair1 = keyPairGenerator.generateKeyPair();
        KeyPair keyPair2 = keyPairGenerator.generateKeyPair();

        Client client1 = new Client("Alice", "aliii",keyPair1);
        Client client2 = new Client("Bob", "boby",keyPair2);

        client1.setCertificate(new Certificate(keyPair1.getPublic()));
        client2.setCertificate(new Certificate(keyPair2.getPublic()));
        server.addUser(client1);
        server.addUser(client2);
        server.forwardMessage("Hello, Bob!", client1, client2); //Alice sends a message to Bob
        server.forwardMessage("Hello, Alice!", client2, client1); //Bob sends a message to Alice
        server.validateCertificate(client1, client2.getCertificate()); //Alice validates Bob's certificate
        server.validateCertificate(client2, client1.getCertificate()); //Bob validates Alice's certificate
    }
}