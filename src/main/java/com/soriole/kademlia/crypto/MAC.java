package com.soriole.kademlia.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MAC {

    SecretKeySpec signKey;
    Mac mac;
    String algoMAC="HmacSha1";
    public void init(String password) throws NoSuchAlgorithmException, InvalidKeyException{
        //Generate Secret key from user key
        signKey = new SecretKeySpec(password.getBytes(), algoMAC);
        //Get mac instance
        mac = Mac.getInstance(algoMAC);
        //Init mac
        mac.init(signKey);

    }

    //generate MAC
    public byte[] giveMeMAC(String password, byte[] message){
        try {
            //Initialize the MAC with Key
            init(password);
            //Compute MAC
            return ( mac.doFinal(message));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MAC.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(MAC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}