package org.example;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * This class represents a certificate authority that is responsible for signing certificates.
 */
public class CertificateAuthority {
    /**
     * The private key of the certificate authority
     */
    private final PrivateKey privateKey;
    /**
     * The public key of the certificate authority
     */
    private final PublicKey publicKey;
    /**
     * The certificate revocation list
     */
    private final List<String> crl;

    /**
     * Instantiates a new Certificate authority.
     *
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public CertificateAuthority() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        this.crl = new ArrayList<>();
    }

    /**
     * Sign certificate with the CA's private key.
     *
     * @param certificate the certificate
     * @return the certificate
     * @throws Exception the exception
     */
    public Certificate signCertificate(Certificate certificate) throws Exception {
        String data = certificate.getPublicKey() + certificate.getUsername();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashCode = digest.digest(data.getBytes(StandardCharsets.UTF_8));

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(this.privateKey);
        privateSignature.update(hashCode);
        byte[] signatureBytes = privateSignature.sign();

        certificate.setSignature(Base64.getEncoder().encodeToString(signatureBytes));
        certificate.setValid(true);

        return certificate;
    }

    /**
     * Receive unsigned certificate and sign it.
     *
     * @param certificatePath the certificate path
     * @throws Exception the exception
     */
    public void receiveUnsignedCertificate(Path certificatePath) throws Exception {
        String pemCertificate = Files.readString(certificatePath);

        Certificate unsignedCertificate = convertFromPemFormat(pemCertificate);

        Certificate signedCertificate = signCertificate(unsignedCertificate);

        Files.write(certificatePath, convertToPemFormat(signedCertificate).getBytes());
    }

    /**
     * Convert to pem format certificate string.
     *
     * @param certificate the certificate
     * @return the certificate
     */
    private String convertToPemFormat(Certificate certificate) {
        String usernameData = Base64.getEncoder().encodeToString(certificate.getUsername().getBytes());
        String publicKeyData = Base64.getEncoder().encodeToString(certificate.getPublicKey().getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + usernameData + " " + publicKeyData + "\n-----END CERTIFICATE-----";
    }

    /**
     * Convert from pem format certificate.
     *
     * @param pemCertificate the pem certificate
     * @return the certificate
     * @throws Exception the exception
     */
    private Certificate convertFromPemFormat(String pemCertificate) throws Exception {
        String certificateData = pemCertificate.replace("-----BEGIN CERTIFICATE-----\n", "").replace("\n-----END CERTIFICATE-----", "");
        String[] parts = certificateData.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid certificate data");
        }
        String username = new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        byte[] publicKeyData = Base64.getDecoder().decode(parts[1]);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyData);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(spec);

        return new Certificate(publicKey, username);
    }


    /**
     * Gets public key.
     *
     * @return the public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Gets crl.
     *
     * @return the crl
     */
    public List<String> getCrl() {
        return crl;
    }
}