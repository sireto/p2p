package com.reversehash.communication.message;

@MessageType(type=2,version=0)
public class KeyExchangeMessage extends Message{

    @Override
    public void handle(){

    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @Override
    public void readBytes(int version,byte[] input) {

    }


}
