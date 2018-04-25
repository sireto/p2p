package com.soriole.kademlia.core.network;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NodeLookupMessage;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.network.KademliaMessageServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * Tests by transmitting a message in one server and receiving in another server. if the message is same
 */
public class KademliaMessageServerTest extends KademliaMessageServer{
    int port;
    Message receivedMessage;
    NodeInfo localNode;
    Logger logger= LoggerFactory.getLogger(KademliaMessageServerTest.class);
    // listen in a random available port.
    private static Key randomkey(){
        byte[] keyByte=new byte[20];
        new Random().nextBytes(keyByte);
        return new Key(keyByte);
    }
    public KademliaMessageServerTest() throws SocketException {
        super(0,
                new ContactBucket(new NodeInfo(randomkey()),160,3));
        this.bucket.getLocalNode().setLanAddress(this.getSocketAddress());
        port=this.getSocketAddress().getPort();

    }

    // we override this so that incoming messages are not directed to the default listeners but to this class itself as a loopback server.
    @Override
    protected void OnNewMessage(Message message){
        try {
            // return the same message.
            this.replyFor(message,message);
            logger.info("Message received on other side");

        } catch (IOException e) {
            assert(false);
        }
    }

    @Test
    public void messageReceiveTest() throws IOException, InterruptedException, TimeoutException {
        // startAsync listening on current server.
        this.start();

        // create another message server instance.
        KademliaMessageServer server=new KademliaMessageServerTest() ;
        server.start();

        // send a message to out server.
        Key k1=randomkey();

        // query the server for the message and get a loopback reply.
        // Note that this involves a complex process of maintaining a session to
        // map the received message to the specific query.
        Message mReply=server.startQuery(
                new NodeInfo(null,this.getSocketAddress()),
                new NodeLookupMessage(k1));

        // check that the replied message is same as the original message.
        NodeLookupMessage reply=(NodeLookupMessage) mReply;
        assert(reply.lookupKey.equals(k1));
        server.shutDown(1);

        this.shutDown(1);
        // nothing went wrong. thus the serialization, the session and message type mapping also is working fine.

    }
}
