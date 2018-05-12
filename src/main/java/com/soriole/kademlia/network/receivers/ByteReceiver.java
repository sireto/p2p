package com.soriole.kademlia.network.receivers;

import com.soriole.kademlia.core.store.Key;

public interface ByteReceiver {
    public void onNewMessage(Key key, byte[] message);
}