package org.example;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

public class Client {
    private String name;
    private String username;
    private KeyPair keyPair;
    public Certificate certificate;

    public Client(String name, String username,KeyPair keyPair) {
        this.name = name;
        this.username = username;
        this.keyPair = keyPair;
    }

    public void receiveMessage(String message, Client sender, Client receiver){
        System.out.println("Message from " + sender.getName() + ": " + message + " to " + receiver.getName());
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }
    public Certificate getCertificate() {
        return certificate;
    }
    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public void receiveCertificate(Certificate certificate) {
        this.certificate = certificate;
    }
}