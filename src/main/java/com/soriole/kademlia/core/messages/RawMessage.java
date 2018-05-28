package com.soriole.kademlia.core.messages;

import com.google.protobuf.InvalidProtocolBufferException;

@MessageType(type = 3)
public class RawMessage extends Message {

    public byte[] rawBytes;

    @Override
    public byte[] writeToBytes() {
        return rawBytes;
    }

    // keeps the reference instead of copying for faster performance
    @Override
    public void readFromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        rawBytes =bytes;
    }
}
