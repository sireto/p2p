package com.soriole.kademlia.core.network.server.udp;

import com.soriole.kademlia.core.KademliaConfig;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NodeLookupMessage;
import com.soriole.kademlia.core.network.ServerShutdownException;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.InMemoryByteStore;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * Tests by transmitting a message in one server and receiving in another server. if the message is same
 * //TODO: rewrite the test properly
 */
public class UdpServerTest extends UdpServer {

    Logger logger = LoggerFactory.getLogger(UdpServerTest.class);

    // listen in a random available port.
    private static Key randomkey() {
        byte[] keyByte = new byte[20];
        new Random().nextBytes(keyByte);
        return new Key(keyByte);
    }

    public UdpServerTest() throws SocketException {
        super(KademliaConfig.newBuilder().setnWorkers(5).build(),
                new ContactBucket(new NodeInfo(Key.gemerateNew()), KademliaConfig.newBuilder().build()),
                new InMemoryByteStore(KademliaConfig.newBuilder().build()));


    }

    // we override this so that incoming messages are not directed to the default listeners but to this class itself as a loopback server.
    @Override
    protected void OnNewMessage(Message message) {
        try {
            // return the same message.
            this.replyFor(message, message);
            logger.info("Message received on other side");

        } catch (IOException e) {
            assert (false);
        }
    }

    @Test
    public void messageReceiveTest() throws ServerShutdownException, InterruptedException, TimeoutException, IOException {
        // startAsync listening on current server.
        assert(this.start());

        // create another message server instance.
        UdpServer server = new UdpServerTest();
        assert(server.start());


        Key k1=randomkey();
        // query the server for the message and get a loopback reply.
        // Note that this involves a complex process of maintaining a session to
        // map the received message to the specific query.
        Message mReply = startQuery(
                new NodeInfo(null,new InetSocketAddress("localhost",server.port)),
                new NodeLookupMessage(k1));

        // check that the replied message is same as the original message.
        NodeLookupMessage reply = (NodeLookupMessage) mReply;
        assert (reply.lookupKey.equals(k1));
        Thread.currentThread().setName("Test Thread");
        // nothing went wrong. thus the serialization, the session and message type mapping also is working fine.

    }
}
