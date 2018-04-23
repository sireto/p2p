package com.soriole.kademlia.core.messages.listeners;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.network.KademliaMessageServer;

public abstract class MessageListener {
    protected KademliaMessageServer server;
    protected ContactBucket bucket;

    abstract public void onReceive(Message m) throws Exception;

    // package private
    void setServerNBucket( KademliaMessageServer server,ContactBucket bucket){
        this.server=server;
        this.bucket=bucket;
    }
}
