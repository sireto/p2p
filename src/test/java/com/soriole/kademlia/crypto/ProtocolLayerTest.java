package com.soriole.kademlia.crypto;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author anish
 */
public class ProtocolLayerTest {

    @Test
    public void shouldDecipherCorrectMessage(){

        String KSA_AB = "KSA_AB";
        String MacKey_AB = "MAC_AB";
        String KSA_AP1 = "KSA_AP1";
        String KSA_P1P2 = "KSA_P1P2";
        String KSA_P2P3 = "KSA_P2P3";
        String KSA_BP3 = "KSA_BP3";
        String algoMAC="HmacSha1";

        AESEncryption aesengine = new AESEncryption();
        MAC mac = new MAC();

        String m = "A quick brown fox jumps over the lazy dog";
        String cipher = null, decrypted = null;



        System.out.println(String.format("Plain Text:\t %s", m));
        String sendingMAC = Base64.encodeBase64String(mac.giveMeMAC(MacKey_AB, m, algoMAC));

        try {
            cipher = aesengine.encrypt( KSA_AB, m);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            decrypted = aesengine.decrypt(KSA_AB, cipher );
            System.out.println(String.format("Decrypted:\t %s", decrypted));
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}