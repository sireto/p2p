package com.soriole.kademlia.core.messages;

public class BlankMessage extends Message {
    @Override
    public byte[] writeToBytes() {
        return new byte[0];
    }

    @Override
    public void readFromBytes(byte[] bytes) {
        // blank message need not be read.
    }
}
