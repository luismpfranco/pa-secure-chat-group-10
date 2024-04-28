package org.example;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * This class provides methods for RSA encryption and decryption.
 */
public class Encryption {

    /**
     * Generates a RSA KeyPair.
     *
     * @return a RSA KeyPair
     *
     * @throws Exception if any error occurs during the KeyPair generation
     */
    public static KeyPair generateKeyPair ( ) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance ( "RSA" );
        keyPairGenerator.initialize ( 2048 );
        return keyPairGenerator.generateKeyPair ( );
    }

    /**
     * Encrypts a message using RSA encryption.
     *
     * @param message   the message to be encrypted
     * @param publicKey the public key to be used for encryption
     *
     * @return the encrypted message
     *
     * @throws Exception if any error occurs during the encryption process
     */
    public static byte[] encryptRSA ( String message , Key publicKey ) throws Exception {
        Cipher cipher = Cipher.getInstance ( "RSA" );
        cipher.init ( Cipher.ENCRYPT_MODE , publicKey );
        return cipher.doFinal ( message.getBytes() );
    }

    /**
     * Decrypts a message using RSA decryption.
     *
     * @param message    the message to be decrypted
     * @param privateKey the private key to be used for decryption
     *
     * @return the decrypted message
     *
     * @throws Exception if any error occurs during the decryption process
     */
    public static String decryptRSA ( byte[] message , Key privateKey ) throws Exception {
        Cipher cipher = Cipher.getInstance ( "RSA" );
        cipher.init ( Cipher.DECRYPT_MODE , privateKey );
        return new String(cipher.doFinal(message));
    }

    /**
     * Creates a digest of a message using SHA-256.
     *
     * @param message the message to be digested
     *
     * @return the digest of the message
     *
     * @throws Exception if any error occurs during the digest creation
     */
    public static byte[] createDigest(String message) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(message.getBytes());
    }

    /**
     * Verifies a digest of a message.
     *
     * @param message the message to be verified
     * @param digest  the digest to be verified
     *
     * @return true if the digest is verified, false otherwise
     *
     * @throws Exception if any error occurs during the digest verification
     */
    public static boolean verifyDigest(String message, byte[] digest) throws Exception {
        byte[] newDigest = createDigest(message);
        return Arrays.equals(newDigest, digest);
    }

}