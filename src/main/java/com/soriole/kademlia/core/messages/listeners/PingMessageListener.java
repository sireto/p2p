package com.soriole.kademlia.core.messages.listeners;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.PingMessage;
import com.soriole.kademlia.core.messages.PongMessage;

@ListenerType(messageClass = PingMessage.class)
public class PingMessageListener extends MessageListener {
    @Override
    public void onReceive(Message m) throws Exception {
        // putting the sending node into the DHT is done by the upper layer.

        // convert to PingMessage instance
        PingMessage msg = (PingMessage) m;

        // create a PongMessage with reference to the bytes in Ping message and reply it.
        PongMessage m2 = new PongMessage();
        server.replyFor(msg, m2);
    }
}
