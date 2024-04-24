package org.example;

import java.security.PublicKey;

class Certificate {
    private PublicKey publicKey;
    private boolean valid;

    public Certificate(PublicKey publicKey) {
        this.publicKey = publicKey;
        this.valid = true; // Simulate certificate being initially valid
    }

    public boolean isValid() {
        return valid;
    }
}