package com.reversehash.crypto;

import com.reversehash.util.Hashes;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.util.Arrays;

public class AESOutputStream extends CipherInputStream {

    public AESOutputStream(InputStream inputStream, byte[] key) {
        super(inputStream, getAESInstance(key));
    }

    static public Cipher getAESInstance(byte[] key) {
        try {
            Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            key = Hashes.sha3_256(key);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOf(key,16));


            aes.init(Cipher.DECRYPT_MODE, keySpec, iv);
            return aes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("AES algorithm instance not found");
        }
    }
    static public byte[] directDecrypt(byte[] key,byte[] data){
        byte[] ans=null;
        try{
            ans=getAESInstance(key).doFinal(data);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return ans;
    }

}
