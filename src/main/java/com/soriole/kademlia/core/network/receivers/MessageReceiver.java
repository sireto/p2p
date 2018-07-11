package com.soriole.kademlia.core.network.receivers;

import com.soriole.kademlia.core.messages.Message;

public interface MessageReceiver {
    void onReceive(Message message);

    void onTimeout();
}
