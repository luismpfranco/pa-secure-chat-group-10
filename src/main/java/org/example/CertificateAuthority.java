package org.example;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
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

    public void receiveUnsignedCertificate(Path certificatePath) throws Exception {
        String pemCertificate = Files.readString(certificatePath);

        Certificate unsignedCertificate = convertFromPemFormat(pemCertificate);

        Certificate signedCertificate = signCertificate(unsignedCertificate);

        Files.write(certificatePath, convertToPemFormat(signedCertificate).getBytes());
    }

    private String convertToPemFormat(Certificate certificate) {
        String usernameData = Base64.getEncoder().encodeToString(certificate.getUsername().getBytes());
        String publicKeyData = Base64.getEncoder().encodeToString(certificate.getPublicKey().getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + usernameData + " " + publicKeyData + "\n-----END CERTIFICATE-----";
    }

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

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public List<String> getCrl() {
        return crl;
    }
}