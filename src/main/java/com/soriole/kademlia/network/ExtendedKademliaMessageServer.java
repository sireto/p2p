package com.soriole.kademlia.network;

import com.soriole.kademlia.core.KademliaConfig;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NonKademliaMessage;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.store.TimestampedStore;
import com.soriole.kademlia.network.receivers.ByteReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;

/**
 * Extended kademlia Message server enables us to Send and receive
 * Messages that are beyond kademlia protocol, so that upper layer
 * services can be implemented.
 *
 * @author github.com/mesudip
 */
public class ExtendedKademliaMessageServer extends KademliaMessageServer {
    static Logger logger=LoggerFactory.getLogger(ExtendedKademliaMessageServer.class);

    public ByteReceiver receiver=getDefaultReceiver();
    public ExtendedKademliaMessageServer(int listeningPort, ContactBucket bucket, TimestampedStore store) throws SocketException {
        super(listeningPort, bucket, store);
    }
    public ExtendedKademliaMessageServer(ContactBucket bucket, TimestampedStore store, KademliaConfig config) throws SocketException {
        this(config.getKadeliaProtocolPort(),bucket,store);
    }

    @Override
    protected void OnNewMessage(final Message message) {
        if(message instanceof NonKademliaMessage){
            byte[] msg=receiver.onNewMessage(message.mSrcNodeInfo, ((NonKademliaMessage) message).rawBytes);
            if(msg!=null){
                if(msg.length>0) {
                    Message reply = new NonKademliaMessage();
                    ((NonKademliaMessage) reply).rawBytes = msg;
                    try {
                        this.replyFor(message, reply);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return;
        }
        super.OnNewMessage(message);
    }
    public void setNonKademliaMessageReceiver(ByteReceiver receiver){
        this.receiver=receiver;
    }

    public static ByteReceiver getDefaultReceiver(){
        return new ByteReceiver() {
            @Override
            public byte[] onNewMessage(NodeInfo key,  byte[] message) {
                logger.info("NonKademlia Message from "+key+" dropped by default!");
                return null;
            }
        };
    }

}
