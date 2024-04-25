// CertificateAuthority.java
package org.example;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

public class CertificateAuthority {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final List<String> crl;

    public CertificateAuthority() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        this.crl = new ArrayList<>();
    }

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

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public List<String> getCrl() {
        return crl;
    }
}