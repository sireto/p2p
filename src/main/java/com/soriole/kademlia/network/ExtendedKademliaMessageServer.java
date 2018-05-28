package com.soriole.kademlia.network;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NonKademliaMessage;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.TimestampedStore;
import com.soriole.kademlia.network.receivers.ByteReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Override
    protected void OnNewMessage(final Message message) {
        if(message instanceof NonKademliaMessage){
            receiver.onNewMessage(message.mSrcNodeInfo.getKey(),((NonKademliaMessage) message).rawBytes);
        }
        super.OnNewMessage(message);
    }
    public void setNonKademliaMessageReceiver(ByteReceiver receiver){
        this.receiver=receiver;
    }

    public static ByteReceiver getDefaultReceiver(){
        return new ByteReceiver() {
            @Override
            public void onNewMessage(Key key, byte[] message) {
                logger.info("NonKademlia Message from "+key+" dropped by default!");
            }
        };
    }

}
