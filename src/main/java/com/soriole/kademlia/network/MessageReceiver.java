package com.soriole.kademlia.network;

import com.soriole.kademlia.core.messages.Message;

public interface MessageReceiver {
    void onReceive(Message message);
    void onTimeout();
}
