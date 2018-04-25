package com.soriole.kademlia.core.messages.listeners;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.network.KademliaMessageServer;

public abstract class MessageListener {

    /** these two values will be filled by the {@link ListenerFactory} before calling {@link #onReceive } so that the listener can use the features of bucket and server.
     **/

    protected KademliaMessageServer server;
    protected ContactBucket bucket;

    abstract public void onReceive(Message m) throws Exception;

    // package private
    void setServerNBucket( KademliaMessageServer server,ContactBucket bucket){
        this.server=server;
        this.bucket=bucket;
    }
}
