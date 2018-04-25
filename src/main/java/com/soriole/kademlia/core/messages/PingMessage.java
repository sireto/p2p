package com.soriole.kademlia.core.messages;

import java.util.Random;


/**
 * The default message listener for Ping Message is at:
   {@link com.soriole.kademlia.core.messages.listeners.PingMessageListener}
 */
@MessageType(type=4)
public class PingMessage extends RawMessage{
    private static byte[] randomByte(){
        byte[] bytes=new byte[8];
        new Random().nextBytes(bytes);
        return bytes;
    }
    public PingMessage(){
        this.rawBytes=randomByte();
    }
}
