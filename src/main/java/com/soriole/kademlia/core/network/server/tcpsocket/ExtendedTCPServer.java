package com.soriole.kademlia.core.network.server.tcpsocket;

import com.soriole.kademlia.core.KademliaConfig;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NonKademliaMessage;
import com.soriole.kademlia.core.network.ExtendedMessageDispacher;
import com.soriole.kademlia.core.network.receivers.ByteReceiver;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.TimestampedStore;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

public class ExtendedTCPServer extends TcpServer implements ExtendedMessageDispacher {
    ByteReceiver messageReceiver= (key, message) -> null;

    public ExtendedTCPServer(KademliaConfig config, ContactBucket bucket, TimestampedStore store) throws IOException {
        super(config, bucket, store);
    }

    public ExtendedTCPServer(KademliaConfig config, ContactBucket bucket, ExecutorService service, TimestampedStore store) throws IOException {
        super(config,bucket,service,store);
    }

    @Override
    public void onReceive(Message message) {
        if(message instanceof NonKademliaMessage){
            NonKademliaMessage msg= (NonKademliaMessage) message;
            if(msg!=null){
                if(msg.rawBytes.length>0){
                    byte[] reply=this.messageReceiver.onNewMessage(msg.getSourceNodeInfo(),msg.rawBytes);
                    if(reply!=null){
                        if(reply.length>0){
                            NonKademliaMessage replyMsg=new NonKademliaMessage();
                            replyMsg.rawBytes=reply;
                            try {
                                this.replyFor(message,replyMsg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (TimeoutException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                    return;
                }
            }
            else {
                logger.warn("Received blank instance of NonKademliaMessage from " + msg.getSourceNodeInfo());
            }
        }
        super.onReceive(message);
    }

    @Override
    public void setNonKademliaMessageReceiver(ByteReceiver receiver) {
        this.messageReceiver=receiver;
    }
}
