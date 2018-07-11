package com.soriole.kademlia.core.messages.listeners;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.TimestampedStore;
import com.soriole.kademlia.core.network.MessageDispacher;

public abstract class MessageListener {
    /**
     * these two values will be filled by the {@link ListenerFactory} before calling {@link #onReceive } so that the listener can use the features of bucket and server.
     **/

    protected MessageDispacher server;
    protected ContactBucket bucket;
    protected TimestampedStore<byte[]> keyStore;

    abstract public void onReceive(Message m) throws Exception;

    // package private
    void setKademliaParam(MessageDispacher server, ContactBucket bucket, TimestampedStore<byte[]> store) {
        this.server = server;
        this.bucket = bucket;
        this.keyStore = store;
    }
}
