package com.soriole.kademlia.crypto;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;

public final class RSAEncryption {

    private PrivateKey privateKey;
    public PublicKey publicKey;

    public RSAEncryption(){
        KeyPair keyPair = giveMeKeyPair(256);
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    public byte[] encrypt(String message, PublicKey pub) throws Exception {
        byte[] cipherText = encryptToByte(message, pub);
        return cipherText;
    }

    public String encryptToBase64String(String message, PublicKey pub) throws Exception {
        byte[] cipherText = encryptToByte(message, pub);
        //NO_WRAP is important as was getting \n at the end
        String encoded = Base64.encodeBase64String(cipherText);
        return encoded;
    }

    public String decrypt(byte[] cipherText, PrivateKey prv) throws Exception {
        byte[] decryptedBytes = decryptToByte(cipherText, prv);
        String message = new String(decryptedBytes);
        return message;
    }

    public String decrypt(String base64EncodedCipherText, PrivateKey prv) throws Exception {
            byte[] decodedCipherText = Base64.decodeBase64(base64EncodedCipherText);
            byte[] decryptedBytes = decryptToByte(decodedCipherText, prv);
            String message = new String(decryptedBytes);
            return message;
    }


    public PublicKey getMyPublicKey() {
        return publicKey;
    }

    public PrivateKey getMyPrivateKey() {
        return privateKey;
    }

    public KeyPair giveMeKeyPair(int keySize){
        try {
            return generateKeyPair(keySize);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(RSAEncryption.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public PublicKey giveMePublicFromString(String pubInString){
        PublicKey p = null;
        try {
            p = getPublicKeyFromByte(pubInString);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return p;
    }


    public PrivateKey giveMePrivateString(String prvInString){
        PrivateKey p = null;
        try {
            p = getPrivateKeyFromByte(prvInString);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return p;
    }
    private KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException
    {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        return keyPair;
    }

    private byte[] encryptToByte(String message, PublicKey publicKey) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] enc = cipher.doFinal(message.getBytes());
        return enc;
    }

    private byte[] decryptToByte(byte[] cipherText, PrivateKey privateKey) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] dec = cipher.doFinal(cipherText);
        return dec;
    }

    private PublicKey getPublicKeyFromByte(String pubInString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] pubInByte = getByteArray(pubInString);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(pubInByte));
        return pub;
    }


    private PrivateKey getPrivateKeyFromByte(String prvInString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] prvInByte = getByteArray(prvInString);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey prv = kf.generatePrivate(new PKCS8EncodedKeySpec(prvInByte));
        return prv;
    }

    public String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public byte[] getByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


}