package com.soriole.kademlia.core.messages.listeners;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.KeyValueStore;
import com.soriole.kademlia.network.KademliaMessageServer;
import org.slf4j.LoggerFactory;

public abstract class MessageListener {
    /** these two values will be filled by the {@link ListenerFactory} before calling {@link #onReceive } so that the listener can use the features of bucket and server.
     **/

    protected KademliaMessageServer server;
    protected ContactBucket bucket;
    protected KeyValueStore<byte[]> keyStore;

    abstract public void onReceive(Message m) throws Exception;

    // package private
    void setKademliaParam(KademliaMessageServer server, ContactBucket bucket, KeyValueStore<byte[]> store){
        this.server=server;
        this.bucket=bucket;
        this.keyStore=store;
    }
}
