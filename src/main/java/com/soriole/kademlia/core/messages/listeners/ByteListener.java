package com.soriole.kademlia.core.messages.listeners;

public interface ByteListener {
    public void onReceive(byte[] receivedBytes);
    public void onTimeout();
}
