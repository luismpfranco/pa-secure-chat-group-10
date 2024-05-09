package org.example;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * This class represents a client object that is used to send and receive messages between clients.
 */
public class Client
{
    private final String name;
    private final String username;
    private PrivateKey privateRSAKey;
    private PublicKey publicRSAKey;
    public Certificate certificate;
    private final Server server;
    private ClientWindow window;
    private PublicKey caPublicKey;
    private List<String> crl;
    private final Map<String, BigInteger> sharedSecrets = new HashMap<>();

    private final List<Message> receivedMessages = new ArrayList<>();
    /**
     * Instantiates a new Client.
     *
     * @param name     the name
     * @param username the username
     * @param server   the server
     * @throws Exception the exception
     */
    public Client(String name, String username, Server server) throws Exception {
        this.name = name;
        this.username = username;
        this.server = server;
        KeyPair keyPair = Encryption.generateKeyPair();
        this.publicRSAKey = keyPair.getPublic();
        this.privateRSAKey = keyPair.getPrivate();
    }

    /**
     * Send message to a specific client(s) or to all clients.
     *
     * @param message   the message
     * @param receivers the receivers
     * @throws Exception the exception
     */
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

    /**
     * Receive message from a client.
     *
     * @param message the message
     * @param sender  the sender
     * @throws Exception the exception
     */
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

    /**
     * Create unsigned certificate certificate for the client.
     *
     * @return the certificate
     */
    public Certificate createUnsignedCertificate() {
        return new Certificate(publicRSAKey, username);
    }

    /**
     * Obtain and share certificate with the certificate authority.
     *
     * @param ca the ca
     * @throws Exception the exception
     */
    public void obtainAndShareCertificate(CertificateAuthority ca) throws Exception {
        Certificate unsignedCertificate = createUnsignedCertificate();
        submitUnsignedCertificate(ca);
        Certificate signedCertificate = ca.signCertificate(unsignedCertificate);
        this.certificate = signedCertificate;
        server.distributeCertificate(signedCertificate);
    }

    /**
     * Validate received certificate from the certificate authority.
     *
     * @param certificate the certificate
     * @param caPublicKey the ca public key
     * @param crl         the crl
     * @throws Exception the exception
     */
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

    /**
     * Agree on shared secret with another client.
     *
     * @param otherClient the other client
     * @throws Exception the exception
     */
    public void agreeOnSharedSecret(Client otherClient) throws Exception {
        BigInteger privateKey = DiffieHellman.generatePrivateKey();

        BigInteger publicKey = DiffieHellman.generatePublicKey(privateKey);

        otherClient.receivePublicKeyForSharedSecret(this.username, publicKey, privateKey);
    }

    /**
     * Receive public key for shared secret from another client.
     *
     * @param otherUsername  the other username
     * @param otherPublicKey the other public key
     * @param myPrivateKey   my private key
     * @throws Exception the exception
     */
    public void receivePublicKeyForSharedSecret(String otherUsername, BigInteger otherPublicKey, BigInteger myPrivateKey) throws Exception {
        if (myPrivateKey == null) {
            throw new Exception("Private key is null");
        }
        BigInteger sharedSecret = DiffieHellman.computeSecret(otherPublicKey, myPrivateKey);

        sharedSecrets.put(otherUsername, sharedSecret);
    }

    /**
     * Submit unsigned certificate to the certificate authority.
     *
     * @param ca the ca
     * @throws Exception the exception
     */
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

    /**
     * Receive certificate from the certificate authority.
     *
     * @param certificate the certificate
     */
    private String convertToPemFormat(Certificate certificate) {
        String usernameData = Base64.getEncoder().encodeToString(certificate.getUsername().getBytes());
        String publicKeyData = Base64.getEncoder().encodeToString(certificate.getPublicKey().getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + usernameData + " " + publicKeyData + "\n-----END CERTIFICATE-----";
    }

    /**
     * Receive certificate from the certificate authority.
     *
     * @param certificate the certificate
     */
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

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
            * Gets username.
        *
        * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Gets certificate.
     *
     * @return the certificate
     */
    public Certificate getCertificate()
    {
        return certificate;
    }

    /**
     * Gets public rsa key.
     *
     * @return the public rsa key
     */
    public PublicKey getPublicRSAKey()
    {
        return publicRSAKey;
    }

    /**
     * Gets private rsa key.
     *
     * @return the private rsa key
     */
    public PrivateKey getPrivateRSAKey()
    {
        return privateRSAKey;
    }

    /**
     * Sets window.
     */
    public void setWindow()
    {
        this.window = new ClientWindow(this, server);
    }

    /**
     * Gets window.
     *
     * @return the window
     */
    public ClientWindow getWindow()
    {
        return window;
    }

    /**
     * Sets ca public key and crl.
     *
     * @param caPublicKey the ca public key
     * @param crl         the crl
     */
    public void setCaPublicKeyAndCrl(PublicKey caPublicKey, List<String> crl) {
        this.caPublicKey = caPublicKey;
        this.crl = crl;
    }

    /**
     * Gets shared secret.
     *
     * @return the shared secret
     */
    public Map<String, BigInteger> getSharedSecret() {
        return sharedSecrets;
    }

    /**
     * Sets certificate.
     *
     * @param invalidCertificate the invalid certificate
     */
    public void setCertificate(Certificate invalidCertificate) {
        this.certificate = invalidCertificate;
    }
}