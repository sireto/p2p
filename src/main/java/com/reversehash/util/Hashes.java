package com.reversehash.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 * Static class that exposes hash functions.
 */
public class Hashes {


    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Performs a SHA3-256 hash of the concatenated inputs.
     *
     * @param inputs The byte arrays to concatenate and hash.
     * @return The hash of the concatenated inputs.
     */
    public static byte[] sha3_256(final byte[]... inputs) {
        try {
            return hash("Keccak-256", inputs);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Performs a SHA3-512 hash of the concatenated inputs.
     *
     * @param inputs The byte arrays to concatenate and hash.
     * @return The hash of the concatenated inputs.
     */
    public static byte[] sha3_512(final byte[]... inputs) {
        try {
            return hash("Keccak-512", inputs);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Performs a RIPEMD160 hash of the concatenated inputs.
     *
     * @param inputs The byte arrays to concatenate and hash.
     * @return The hash of the concatenated inputs.
     */
    public static byte[] ripemd160(final byte[]... inputs) {
        try {
            return hash("RIPEMD160", inputs);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Performs a SHA256 hash of the concatenated inputs.
     *
     * @param inputs The byte arrays to concatenate and hash.
     * @return The hash of the concatenated inputs.
     */

    public static byte[] sha256(final byte[]... inputs) {
        try {
            return hash("SHA-256", inputs);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private static byte[] hash(final String algorithm, final byte[]... inputs) throws NoSuchProviderException, NoSuchAlgorithmException {

        final MessageDigest digest = MessageDigest.getInstance(algorithm, "BC");
        for (final byte[] input : inputs) {
            digest.update(input);
        }
        return digest.digest();
    }
}
