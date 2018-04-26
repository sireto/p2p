package com.soriole.kademlia.core;

import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.KeyValueStore;
import com.soriole.kademlia.network.KademliaMessageServer;

import java.util.concurrent.TimeoutException;

// ExtendedKademliaDHT enables us to send messages beyond the kademlia protocol messages.
// These features will be used to build up application layer service for application that uses
// kademlia to transmit other types of messages.
public class KademliaExtendedDHT extends KademliaDHT {
    public KademliaExtendedDHT(ContactBucket bucket, KademliaMessageServer server, KeyValueStore<byte[]> keyValueStore) {
        super(bucket,server,keyValueStore);
    }

    public void sendMessageToNode(Key key,byte[] message){

    }
    public byte[] queryWithNode(Key key,byte[] message) throws TimeoutException{
        return null;
    }
}
