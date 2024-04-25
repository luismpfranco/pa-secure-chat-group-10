package org.example;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

public class Client
{
    private String name;
    private String username;
    private PrivateKey privateRSAKey;
    private final PublicKey publicRSAKey;
    public Certificate certificate;

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket client;
    private Server server;

    private ClientWindow window;

    public Client(String name, String username, Server server) throws Exception {
        this.name = name;
        this.username = username;
        KeyPair keyPair = Encryption.generateKeyPair ( );
        this.privateRSAKey = keyPair.getPrivate();
        this.publicRSAKey = keyPair.getPublic();
        this.server = server;
        window = new ClientWindow(this, server);
    }

    public void sendMessage(String message, Client... receivers) throws Exception {
        byte[] encryptedMessage;
        byte[] digest = Encryption.createDigest(message);
        Message messageObj;

        StringBuilder receiverNames = new StringBuilder();
        for (Client r : receivers) {
            if(r == this)
            {
                continue;
            }
            receiverNames.append("@").append(r.getName()).append(", ");
            encryptedMessage = Encryption.encryptRSA(message, r.getPublicRSAKey());
            messageObj = new Message(encryptedMessage, digest);
            server.forwardMessage(messageObj, this, r);

        }

        if(!receiverNames.isEmpty()){
            receiverNames.setLength(receiverNames.length() - 2);
        }
        window.addMessage(receiverNames + " " + message);
    }

    public void receiveMessage(Message message, Client sender, Client receiver) throws Exception {
        String decryptedMessage = Encryption.decryptRSA(message.getMessage(), this.getPrivateRSAKey());
        if (!Encryption.verifyDigest(decryptedMessage, message.getDigest())) {
            throw new Exception("Message integrity check failed");
        }
        if(sender != this)
        {
            window.addMessage(sender.getUsername() + ": " + decryptedMessage);
        }
    }

    public String getName()
    {
        return name;
    }

    public String getUsername()
    {
        return username;
    }

    public Certificate getCertificate()
    {
        return certificate;
    }

    public void setCertificate(Certificate certificate)
    {
        this.certificate = certificate;
    }

    public void receiveCertificate(Certificate certificate)
    {
        this.certificate = certificate;
    }

    public PublicKey getPublicRSAKey()
    {
        return publicRSAKey;
    }

    public PrivateKey getPrivateRSAKey()
    {
        return privateRSAKey;
    }

    public Server getServer()
    {
        return server;
    }

    public ClientWindow getWindow()
    {
        return window;
    }

    @Override
    public String toString() {
        return getUsername();
    }
}