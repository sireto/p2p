package com.soriole.kademlia.core.messages.listeners;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NodeListMessage;
import com.soriole.kademlia.core.messages.NodeLookupMessage;
import com.soriole.kademlia.core.store.NodeInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@ListenerType(messageClass = NodeLookupMessage.class)
public class NodeLookupMessageListener extends MessageListener {

    @Override
    public void onReceive(Message m) throws Exception {
        NodeLookupMessage message = (NodeLookupMessage) m;
        Collection<NodeInfo> nodes = bucket.getClosestNodes(message.lookupKey);
        // the list will surely contain the sender's nodeInfo. so remove this.
        nodes.remove(m.mSrcNodeInfo);
        server.replyFor(m, new NodeListMessage(nodes));
    }
}
