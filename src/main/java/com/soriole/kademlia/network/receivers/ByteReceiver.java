package com.soriole.kademlia.network.receivers;

import com.soriole.kademlia.core.store.NodeInfo;

public interface ByteReceiver {
    byte[] onNewMessage(NodeInfo key, byte[] message);
}