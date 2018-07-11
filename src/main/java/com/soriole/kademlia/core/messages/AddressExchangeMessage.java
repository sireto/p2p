package com.soriole.kademlia.core.messages;

import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

@MessageType(type = 9)
public class AddressExchangeMessage extends Message{
    public int listeningPort;
    public boolean isPublic=true;
    public AddressExchangeMessage(){}
    public AddressExchangeMessage(int port){
        this.listeningPort=port;
    }

    @Override
    public byte[] writeToBytes() {
        ByteBuffer buffer=ByteBuffer.allocate(5);
        if(isPublic){
            buffer.put((byte)1);
        }
        else{
            buffer.put((byte)0);
        }

        buffer.putInt(listeningPort);
        return buffer.array();

    }

    @Override
    public void readFromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        try {
            ByteBuffer wrap = ByteBuffer.wrap(bytes);
            if(wrap.get()==(byte)0){
                isPublic=false;
            }
            listeningPort=wrap.getInt();
        }
        catch (BufferOverflowException e){
            throw new InvalidProtocolBufferException("Data incompelete");
        }

    }
}
