package com.soriole.kademlia.core.network.receivers;

import com.soriole.kademlia.core.store.NodeInfo;

public interface ByteReceiver {
    byte[] onNewMessage(NodeInfo key, byte[] message);
}