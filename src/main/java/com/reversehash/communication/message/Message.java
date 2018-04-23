package com.reversehash.communication.message;

import java.io.Serializable;

@MessageType(type=0,version=0)
public abstract class Message implements Serializable {
    // for the spring test only
    public byte[] sender;
    public byte[] receiver;

    // well each messages neds to be handled when received.
    abstract public void handle();

    // other information rather than sender/receiver needs to be converted to byte.
    abstract public byte[] toBytes();

    abstract public void readBytes(int version,byte[] input);

}
