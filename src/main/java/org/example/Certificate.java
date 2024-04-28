package org.example;

import java.security.PublicKey;
//* This class represents a certificate object that is used to authenticate clients.
public class Certificate {
    //* The public key of the client.
    private final PublicKey publicKey;
    //* A boolean value that indicates whether the certificate is valid.
    private boolean valid;
    //* The username of the client.
    private final String username;
    //* The signature of the certificate.
    private String signature;

    /**
     * Instantiates a new Certificate.
     *
     * @param publicKey the public key
     * @param username  the username
     */

    public Certificate(PublicKey publicKey, String username) {
        this.publicKey = publicKey;
        this.username = username;
        this.signature = null;
        this.valid = false;
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
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets signature.
     *
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets signature.
     *
     * @param signature the signature
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Is valid boolean.
     *
     * @return the boolean
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets valid.
     *
     * @param valid the valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }
}