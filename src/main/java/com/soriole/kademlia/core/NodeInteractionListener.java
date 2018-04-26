package com.soriole.kademlia.core;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;

import java.net.InetSocketAddress;

public interface NodeInteractionListener {
    // network address of a node has changed.
   void onNetworkAddressChange(Key senderKey, InetSocketAddress address);

   // new node has been found.
   void onNewNodeFound(NodeInfo info);

    void onNewForwardMessage(Message message, NodeInfo destination);
}
