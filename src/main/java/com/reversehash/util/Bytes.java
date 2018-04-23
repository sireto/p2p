package com.reversehash.util;

import java.util.Random;

public class Bytes {
    static public byte[] getRandomBytes(int length){
        byte[] rbyte=new byte[length];

        new Random().nextBytes(rbyte);
        return rbyte;
    }

    static public String toBase64(byte[]data){
        return org.bouncycastle.util.encoders.Base64.toBase64String(data);

    }
    static public byte[] fromBase64(String data){
        return org.bouncycastle.util.encoders.Base64.decode(data);
    }
}
