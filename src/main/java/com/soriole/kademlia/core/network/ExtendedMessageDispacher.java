package com.soriole.kademlia.core.network;

import com.soriole.kademlia.core.network.receivers.ByteReceiver;

public interface ExtendedMessageDispacher extends MessageDispacher {
    void setNonKademliaMessageReceiver(ByteReceiver receiver);
}
