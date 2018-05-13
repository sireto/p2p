package com.reversehash.crypto;

import java.util.Arrays;
import java.util.HashMap;

public class KeyFactory {
    private HashMap<byte[],byte[]> secrets=new HashMap<>();
    DHKeyPair myKey;
    public KeyFactory(DHKeyPair myKey){
        this.myKey=myKey;
    }
    public KeyFactory(){
        this.myKey=new DHKeyPair();
    }
    public boolean hasSecret(byte[] publicKey){
        return secrets.get(publicKey)==null;
    }
    public byte[] getSecret(byte[] publicKey){
        byte[] secret=secrets.get(publicKey);
        if (secret==null){
            secret=myKey.computeSharedKey(publicKey);
            secrets.put(publicKey,secret);
        }
        return secret;
    }
    public byte[] getSecret(byte[]receiverKey,int index){
        byte[] secret=this.getSecret(receiverKey);
        // we don't wan't to mess with the original secret
        secret= Arrays.copyOf(secret,secret.length);

        // change a byte deterministically
        index=index%secret.length;
        secret[index]^=(byte)index;
        return secret;
    }
    public DHKeyPair getMyKey(){
        return this.myKey;
    }
}
