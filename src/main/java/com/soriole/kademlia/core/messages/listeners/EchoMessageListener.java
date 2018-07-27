package com.soriole.kademlia.core.messages.listeners;

import com.soriole.kademlia.core.messages.EchoMessage;
import com.soriole.kademlia.core.messages.EchoReplyMessage;
import com.soriole.kademlia.core.messages.Message;

public class EchoMessageListener extends MessageListener{

    @Override
    public void onReceive(Message m) throws Exception {
        if(m instanceof EchoMessage){
            EchoMessage message=(EchoMessage)m;
            EchoReplyMessage message1=new EchoReplyMessage();
            message1.nodeInfo=m.mSrcNodeInfo;
            server.replyFor(m,message1);
        }
    }
}
