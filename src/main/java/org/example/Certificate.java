package org.example;

import java.security.PublicKey;

class Certificate {
    private final PublicKey publicKey;
    private boolean valid;
    private final String username;
    private String signature;

    public Certificate(PublicKey publicKey, String username) {
        this.publicKey = publicKey;
        this.username = username;
        this.signature = null;
        this.valid = false;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getUsername() {
        return username;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}