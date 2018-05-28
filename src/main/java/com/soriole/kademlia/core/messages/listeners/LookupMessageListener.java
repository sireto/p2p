package com.soriole.kademlia.core.messages.listeners;

import com.soriole.kademlia.core.messages.DataMessage;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.LookupMessage;
import com.soriole.kademlia.core.messages.NodeListMessage;
import com.soriole.kademlia.core.store.TimeStampedData;

@ListenerType(messageClass = LookupMessage.class)
public class LookupMessageListener extends MessageListener{
    @Override
    public void onReceive(Message m) throws Exception {
        LookupMessage message=(LookupMessage)m;

        TimeStampedData<byte[]> timeStampedData=keyStore.get(message.lookupKey);

        // if we don't have the key, return nodeList message
        if(timeStampedData==null){
            // find the closest nodes.
            NodeListMessage reply=new NodeListMessage(bucket.getClosestNodes(message.lookupKey));
            // if the asking node is in the list, remove it.
            reply.nodes.remove(message.mSrcNodeInfo);
            // return the list
            server.replyFor(message,reply);
        }

        // if we have the key, return a DataMessage instance.
        else{
            DataMessage reply=new DataMessage();
            reply.key=message.lookupKey;
            reply.value=timeStampedData.getData();
            reply.updatedtime=timeStampedData.getInsertionTime();
            reply.expirationTime=timeStampedData.getExpirationTime();
            server.replyFor(message,reply);
        }
    }
}
