package org.example;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.List;

public class Client
{
    private final String name;
    private final String username;
    private final PrivateKey privateRSAKey;
    private final PublicKey publicRSAKey;
    public Certificate certificate;
    private final Server server;
    private ClientWindow window;
    private PublicKey caPublicKey;
    private List<String> crl;

    public Client(String name, String username, Server server) throws Exception {
        this.name = name;
        this.username = username;
        this.server = server;
        KeyPair keyPair = Encryption.generateKeyPair();
        this.publicRSAKey = keyPair.getPublic();
        this.privateRSAKey = keyPair.getPrivate();
    }

    public void sendMessage(String message, Client... receivers) throws Exception {
        byte[] digest = Encryption.createDigest(message);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(this.privateRSAKey);
        signature.update(digest);

        StringBuilder receiverNames = new StringBuilder();
        for (Client r : receivers) {
            if(r == this)
            {
                continue;
            }
            receiverNames.append("@").append(r.getName()).append(", ");
            byte[] encryptedMessage = Encryption.encryptRSA(message, r.getPublicRSAKey());
            Message messageObj = new Message(encryptedMessage, digest);
            server.forwardMessage(messageObj, this, r);
        }

        if(!receiverNames.isEmpty()){
            receiverNames.setLength(receiverNames.length() - 2);
        }
        window.addMessage(receiverNames + " " + message);
    }

    public void receiveMessage(Message message, Client sender) throws Exception {
        String decryptedMessage = Encryption.decryptRSA(message.getMessage(), this.getPrivateRSAKey());
        if (!Encryption.verifyDigest(decryptedMessage, message.getDigest())) {
            throw new Exception("Message integrity check failed");
        }
        if(sender != this)
        {
            window.addMessage(sender.getUsername() + ": " + decryptedMessage);
        }
    }

    public Certificate createUnsignedCertificate() {
        return new Certificate(publicRSAKey, username);
    }

    public void obtainAndShareCertificate(CertificateAuthority ca) throws Exception {
        Certificate unsignedCertificate = createUnsignedCertificate();
        Certificate signedCertificate = ca.signCertificate(unsignedCertificate);
        this.certificate = signedCertificate;
        server.distributeCertificate(signedCertificate);
    }

    public void validateReceivedCertificate(Certificate certificate, PublicKey caPublicKey, List<String> crl) throws Exception {
        if (crl.contains(certificate.getUsername())) {
            throw new CertificateException("The certificate for the user " + certificate.getUsername() + " has been revoked.");
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String data = certificate.getPublicKey() + certificate.getUsername();
        byte[] hashCode = digest.digest(data.getBytes(StandardCharsets.UTF_8));

        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(caPublicKey);
        publicSignature.update(hashCode);
        byte[] signatureBytes = Base64.getDecoder().decode(certificate.getSignature());

        if (!publicSignature.verify(signatureBytes)) {
            throw new SignatureException("Failed to validate certificate for user " + certificate.getUsername() + ". Certificate: " + certificate);
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

    public void receiveCertificate(Certificate certificate)
    {
        try {
            validateReceivedCertificate(certificate, caPublicKey, crl);
        } catch (Exception e) {
            System.out.println("Failed to validate certificate for user " + certificate.getUsername());
        }
    }

    public PublicKey getPublicRSAKey()
    {
        return publicRSAKey;
    }

    public PrivateKey getPrivateRSAKey()
    {
        return privateRSAKey;
    }

    public void setWindow()
    {
        this.window = new ClientWindow(this, server);
    }

    public ClientWindow getWindow()
    {
        return window;
    }

    @Override
    public String toString() {
        return getUsername();
    }

    public void setCaPublicKey(PublicKey caPublicKey)
    {
        this.caPublicKey = caPublicKey;
    }

    public void setCrl(List<String> crl)
    {
        this.crl = crl;
    }
}