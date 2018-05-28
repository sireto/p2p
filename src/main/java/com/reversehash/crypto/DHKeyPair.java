package com.reversehash.crypto;

import com.reversehash.util.Bytes;
import com.reversehash.util.Hashes;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.KeyFactory;
import java.security.spec.InvalidKeySpecException;

public class DHKeyPair {

    private KeyPair keyPair;
    private KeyAgreement keyAgree;

    public DHKeyPair() {
        this(Bytes.getRandomBytes(64));
    }

    public DHKeyPair(byte[] seed) {
        DHParameterSpec dhParamSpec;
        try {
            seed = Hashes.sha3_512(seed);
            BigInteger intSeed = new BigInteger(1, seed);
            dhParamSpec = new DHParameterSpec(P, G);
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DiffieHellman", "BC");
            keyPairGen.initialize(dhParamSpec);
            keyPair = keyPairGen.generateKeyPair();
            keyAgree = KeyAgreement.getInstance("DH");
            keyAgree.init(keyPair.getPrivate());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected outcome");
        }
    }

    public byte[] computeSharedKey(byte[] peerPublicByte) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("DH", "BC");

            BigInteger peerPublicInt = new BigInteger(1, peerPublicByte);
            DHPublicKeySpec spec= new DHPublicKeySpec(peerPublicInt, P, G);
            PublicKey pubKey = keyFactory.generatePublic(spec);

            keyAgree.doPhase(pubKey, true);
            byte[] sharedKeyBytes = keyAgree.generateSecret();

            return sharedKeyBytes;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getPublic() {
        BigInteger pubKeyBI = ((DHPublicKey) keyPair.getPublic()).getY();
        byte[] pubKeyBytes = pubKeyBI.toByteArray();
        return pubKeyBytes;
    }

    public byte[] getPrivate() {
        return keyPair.getPrivate().getEncoded();
    }

    private static final byte P_BYTES[] = {
            (byte) 0xF4, (byte) 0x88, (byte) 0xFD, (byte) 0x58,
            (byte) 0xE9, (byte) 0x2F, (byte) 0x78, (byte) 0xC7,
            (byte) 0xF4, (byte) 0x88, (byte) 0xFD, (byte) 0x58,
            (byte) 0xE9, (byte) 0x2F, (byte) 0x78, (byte) 0xC7,
            (byte) 0xF4, (byte) 0x88, (byte) 0xFD, (byte) 0x58,
            (byte) 0xE9, (byte) 0x2F, (byte) 0x78, (byte) 0xC7,
            (byte) 0xF4, (byte) 0x88, (byte) 0xFD, (byte) 0x58,
            (byte) 0xE9, (byte) 0x2F, (byte) 0x78, (byte) 0xC7
    };
    private static final BigInteger P = new BigInteger(1, P_BYTES);
    private static final BigInteger G = BigInteger.valueOf(2);
}