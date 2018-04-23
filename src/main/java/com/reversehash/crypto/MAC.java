package com.reversehash.crypto;

import com.reversehash.util.Hashes;
import org.bouncycastle.crypto.CryptoException;

import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

public class MAC {
    public static class MessageAuthenticationException extends CryptoException {
    }

    Mac mac;

    public MAC(byte[] key) {
        key=Hashes.sha3_256(key);
        try {
            SecretKeySpec secret = new SecretKeySpec(key, "HmacSHA256");
            mac = Mac.getInstance("HmacSHA256","BC");
            mac.init(secret);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw  new RuntimeException("Is this even possible?");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            throw  new RuntimeException("Is this even possible?");

        }

    }

    public byte[] appendMAC(byte[] data) {
        try {
            byte[] result = new byte[data.length + mac.getMacLength()];
            System.arraycopy(data, 0, result, 0, data.length);
            mac.doFinal(result, data.length);
            return result;
        } catch (ShortBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] verifyMAC(byte[] key, byte[] data) throws MessageAuthenticationException {


        if (data.length <= mac.getMacLength())
            throw new MessageAuthenticationException();
        byte[] messageData = Arrays.copyOf(data, data.length - mac.getMacLength());
        byte[] mac = this.mac.doFinal(messageData);
        if (!Arrays.equals(mac, Arrays.copyOfRange(data, data.length - this.mac.getMacLength(), data.length))) {
            throw new MessageAuthenticationException();
        }
        return messageData;
    }

    public byte[] getMac( byte[] data) {
        return mac.doFinal(data);
    }

    public boolean verifyMac( byte[] data, byte[] mac) {
        return Arrays.equals(getMac(data), mac);
    }
    public boolean verify(byte[] macByte){
        return Arrays.equals(this.mac.doFinal(), macByte);
    }
    public MAC addData(byte[] data){
        mac.update(data);
        return this;
    }
}
