package com.soriole.kademlia.core;

import com.soriole.kademlia.core.network.ExtendedMessageDispacher;
import com.soriole.kademlia.core.network.ServerShutdownException;
import com.soriole.kademlia.core.network.server.tcpsocket.ExtendedTCPServer;
import com.soriole.kademlia.core.store.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class ExtendedDHTTest {
    KademliaExtendedDHT node1, node2;

    private KademliaExtendedDHT createInstance() throws IOException {
        NodeInfo node1 = new NodeInfo(Key.gemerateNew());
        KademliaConfig config = KademliaConfig.newBuilder().build();
        ContactBucket bucket = new ContactBucket(node1, config);
        TimestampedStore store = new InMemoryByteStore(config);
        ExtendedMessageDispacher dispacher = new ExtendedTCPServer(config, bucket, store);
        return new KademliaExtendedDHT(bucket, dispacher, store, config);
    }

    public ExtendedDHTTest() throws IOException {
        node1 = createInstance();
        node2 = createInstance();
        node1.setMessageReceiver((key, message) -> message);
        node2.setMessageReceiver((key, message) -> message);
        node1.start();
        node2.start();
        node1.join(node2.getLocalNode());
    }

    // Test by sending a large message from one node to another node.
    @Test
    public void testMessageSend() throws KademliaExtendedDHT.NoSuchNodeFoundException, TimeoutException, ServerShutdownException, IOException {
        byte[] hundredMegaByte = new byte[1024 * 1024 * 100];
        new Random().nextBytes(hundredMegaByte);

        // send message from node1 to node 2
        byte[] returnByte = node1.queryWithNode(node2.getLocalNode().getKey(), hundredMegaByte, 30000);
        assert Arrays.equals(returnByte, hundredMegaByte);

        // send message from node2 to node 1
        byte[] returnByte2 = node2.queryWithNode(node1.getLocalNode().getKey(), hundredMegaByte, 30000);
        assert Arrays.equals(returnByte2, hundredMegaByte);
    }
}
