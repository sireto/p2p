package com.soriole.kademlia.core.messages;

import com.soriole.kademlia.core.store.Key;

@MessageType(type=2)
public class NodeLookupMessage extends Message{
    public Key lookupKey;
    // there should be a no argument constructor for a MessageType
    public NodeLookupMessage(){}
    public NodeLookupMessage(Key lookupKey){
        this.lookupKey=lookupKey;
    }
    @Override
    public byte[] writeToBytes() {
        return lookupKey.toBytes();
    }

    @Override
    public void readFromBytes(byte[] bytes) {
        lookupKey=new Key(bytes);
    }
}
