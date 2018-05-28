package com.soriole.kademlia.core.messages;

import com.google.protobuf.InvalidProtocolBufferException;

public class BlankMessage extends Message {
    @Override
    public byte[] writeToBytes() {
        return new byte[0];
    }

    @Override
    public void readFromBytes(byte[] bytes){

    }
}
