package com.soriole.kademlia.core;

/**
 * Storage in Kademlia's network.
 */
public interface KademliaStorage {

    void storeLocally(Key key, byte[] value);

    boolean store(Key key, byte[] value);

    boolean store(Key key, byte[] value, int replica);

    byte[] fetch(Key key);

    void refresh();

}
