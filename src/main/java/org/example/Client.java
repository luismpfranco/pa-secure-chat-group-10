package org.example;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<String, BigInteger> sharedSecrets = new HashMap<>();

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
            window.addMessage(sender.getName() + ": " + decryptedMessage);
        }
    }

    public Certificate createUnsignedCertificate() {
        return new Certificate(publicRSAKey, username);
    }

    public void obtainAndShareCertificate(CertificateAuthority ca) throws Exception {
        Certificate unsignedCertificate = createUnsignedCertificate();
        submitUnsignedCertificate(ca);
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

    public void agreeOnSharedSecret(Client otherClient) throws Exception {
        BigInteger privateKey = DiffieHellman.generatePrivateKey();

        BigInteger publicKey = DiffieHellman.generatePublicKey(privateKey);

        otherClient.receivePublicKeyForSharedSecret(this.username, publicKey, privateKey);
    }

    public void receivePublicKeyForSharedSecret(String otherUsername, BigInteger otherPublicKey, BigInteger myPrivateKey) throws Exception {
        if (myPrivateKey == null) {
            throw new Exception("Private key is null");
        }

        BigInteger sharedSecret = DiffieHellman.computeSecret(otherPublicKey, myPrivateKey);

        sharedSecrets.put(otherUsername, sharedSecret);
    }

    public void submitUnsignedCertificate(CertificateAuthority ca) throws Exception {
        Certificate unsignedCertificate = createUnsignedCertificate();

        String pemCertificate = convertToPemFormat(unsignedCertificate);
        //System.out.println(pemCertificate);

        if (!pemCertificate.contains(" ")) {
            throw new IllegalArgumentException("Invalid certificate data");
        }

        Path path = Paths.get("certificates/" + this.username + ".pem");
        Files.write(path, pemCertificate.getBytes());

        ca.receiveUnsignedCertificate(path);
    }

    private String convertToPemFormat(Certificate certificate) {
        String usernameData = Base64.getEncoder().encodeToString(certificate.getUsername().getBytes());
        String publicKeyData = Base64.getEncoder().encodeToString(certificate.getPublicKey().getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + usernameData + " " + publicKeyData + "\n-----END CERTIFICATE-----";
    }

    public void receiveCertificate(Certificate certificate)
    {
        try {
            validateReceivedCertificate(certificate, caPublicKey, crl);
        } catch (Exception e) {
            System.out.println("Failed to validate certificate for user " + certificate.getUsername());
        }
    }

    @Override
    public String toString() {
        return getName();
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

    public void setCaPublicKey(PublicKey caPublicKey)
    {
        this.caPublicKey = caPublicKey;
    }

    public void setCrl(List<String> crl)
    {
        this.crl = crl;
    }
}