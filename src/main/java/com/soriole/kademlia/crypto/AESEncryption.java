package com.soriole.kademlia.crypto;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class AESEncryption {

    //AES using CBC and PKCS5Padding
    private static final String AES_MODE = "AES/CBC/PKCS5Padding";
    private static final String CHARSET = "UTF-8";

    //AES useing SHA-256 (and so a 256-bit key)
    private static final String HASH_ALGORITHM = "SHA-256";

    //AES useing blank IV (not the best security, but the aim here is compatibility)
    private static final byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private static SecretKeySpec generateKey(final String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }


    public byte[] encrypt(final String password, String message)
            throws GeneralSecurityException, UnsupportedEncodingException {
        final SecretKeySpec key = generateKey(password);
        byte[] cipherText = encrypt(key, ivBytes, message.getBytes(CHARSET));
        //NO_WRAP is important as was getting \n at the end
            return cipherText;
    }


    public String encryptTobase64String(final String password, String message)
            throws GeneralSecurityException, UnsupportedEncodingException {
            final SecretKeySpec key = generateKey(password);
            byte[] cipherText = encrypt(key, ivBytes, message.getBytes(CHARSET));
            //NO_WRAP is important as was getting \n at the end
            String encoded = Base64.encodeBase64String(cipherText);
            //Log.d("Base64.NO_WRAP", encoded);
            return encoded;
    }


    private byte[] encrypt(final SecretKeySpec key, final byte[] iv, final byte[] message)
            throws GeneralSecurityException{
        final Cipher cipher = Cipher.getInstance(AES_MODE);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] cipherText = cipher.doFinal(message);
        return cipherText;
    }

    public String decrypt(final String password, byte[] cipherText)
            throws GeneralSecurityException, UnsupportedEncodingException {
            final SecretKeySpec key = generateKey(password);
            byte[] decryptedBytes = decrypt(key, ivBytes, cipherText);
            String message = new String(decryptedBytes, CHARSET);
            //Log.d("message", message);
            return message;
    }

    public String decrypt(final String password, String base64EncodedCipherText)
            throws GeneralSecurityException, UnsupportedEncodingException {
            final SecretKeySpec key = generateKey(password);
            byte[] decodedCipherText = Base64.decodeBase64(base64EncodedCipherText);
            byte[] decryptedBytes = decrypt(key, ivBytes, decodedCipherText);
            String message = new String(decryptedBytes, CHARSET);
            //Log.d("message", message);
            return message;
    }


    private byte[] decrypt(final SecretKeySpec key, final byte[] iv, final byte[] decodedCipherText)
            throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(AES_MODE);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(decodedCipherText);
        return decryptedBytes;
    }



    public String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public  byte[] getByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    private static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
