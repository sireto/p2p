package com.soriole.kademlia.core;

import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.network.KademliaMessageServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Test diffreent kademlia rpcs by creating  a kademlia network containg N_DHTS no. of nodes.
 */
public class KademliaDHTTest {

    Logger logger = LoggerFactory.getLogger(KademliaDHTTest.class);
    // all the dht instances will share the same executor instance.
    // the no of threads should me atleast (noOfDHTInstance + 5)
    ExecutorService service = Executors.newFixedThreadPool(30);


    // 20 dht nodes should be sufficient to test it.
    final int N_DHTS = 20;

    ArrayList<KademliaDHT> dhts = new ArrayList<>(N_DHTS);

    private String hex(int i) {
        return Integer.toHexString(i);
    }

    // creates a DHT instance at random port.
    private KademliaDHT createDHTinstance(Key key) throws SocketException {
        NodeInfo localKey = new NodeInfo(key);
        ContactBucket bucket = new ContactBucket(localKey, 160, 3);
        KademliaMessageServer server = new KademliaMessageServer(0, bucket, service);
        bucket.getLocalNode().setLanAddress(server.getSocketAddress());
        return new KademliaDHT(bucket, server);
    }

    public KademliaDHTTest() throws Exception {
        for (int i = 0; i < N_DHTS; i++) {

            // kademlia id of i'th dht = valueof(i)
            dhts.add(createDHTinstance(new Key(hex(i+1))));
            dhts.get(i).server.start();
        }

        join();

    }


    /**
     * Join each nodes with the bootstrap node.
     * 1st node in the list is the bootstrap node.
     * @throws Exception
     */
    public void join() throws Exception {
        // 1st dht is the bootstrap all others will join it.
        NodeInfo bootstrapNode = dhts.get(0).bucket.getLocalNode();
        for (int i = 1; i < dhts.size(); i++) {
            logger.debug("\nConnecting Node " + hex(i + 1) + " with bootstrap node --");
            // join involves many async queries.
            assert dhts.get(i).join(bootstrapNode);

            logger.debug("\nSuccessful !!");

        }
    }

    /**
     * Each Node tries  finding ClosestNode to each other nodes in the DHT network
     * No of call to findClosestNodes() = (N_DHT-1)^2
     * @throws Exception
     */

    @Test
    public void findClosestNodes() throws Exception {
        SortedSet<NodeInfo> returnedNodes;
        // all of the above Nodes try to find eachother.
        //
        logger.debug("\n ############ Find Closest Nodes Test ##########");
        for (int i = 0; i < dhts.size(); i++) {
            for (int j = 0; j < dhts.size(); j++) {
                if (i != j) {
                    KademliaDHT di = dhts.get(i);
                    NodeInfo ni = di.bucket.getLocalNode();

                    KademliaDHT dj = dhts.get(j);
                    NodeInfo nj = dj.bucket.getLocalNode();

                    logger.debug("\nNode  " + hex(i + 1) + " is finding nodes closest to " + hex(j + 1) +
                            "\nBucket of " + hex(i + 1) + " : " + di.bucket.getAllNodes().toString()

                    );

                    // get closest nodes and compare the first node in the list with the searched node.
                    returnedNodes = di.findClosestNodes(nj.getKey());
                    assertEquals(returnedNodes.first(), nj);

                    logger.debug("\nClosest nodes are : " + returnedNodes.toString() +
                            "\nSuccessful !!! ");
                }

            }

        }
    }

    /**
     *  Each node tries to ping all the nodes in it's kademlia bucket
     * @throws Exception
     */

    @Test
    public void ping() throws Exception {
        SortedSet<NodeInfo> returnedNodes;

        logger.debug("\n ############ Ping  Test ##########");
        for (KademliaDHT dht : dhts) {
            logger.debug("\nBucket of " + dht.bucket.getLocalNode() + " : " + dht.bucket.getAllNodes().toString());

            for (NodeInfo node : dht.bucket.getAllNodes()) {

                //ping the node
                long p = dht.ping(node);
                // the ping time should be greater or equal 0. -1 means failure.
                assert (p >= 0);
                logger.debug(" Node " + dht.bucket.getLocalNode() + " pinged " + node + ": Reply in - " + p + " ms");
            }
        }


    }

    /**
     * Each of the node issues findNode() to find every other nodes in the DHT network.
     */

    @Test
    public void findNode() throws Exception {
        // all of the above Nodes try to find eachother.

        logger.debug("\n ############ Find Node Test ##########");
        for (int i = 0; i < dhts.size(); i++) {
            for (int j = 0; j < dhts.size(); j++) {
                if (i != j) {
                    KademliaDHT di = dhts.get(i);
                    KademliaDHT dj = dhts.get(j);

                    logger.debug("\nNode  " + hex(i + 1) + " is finding " + hex(j + 1) +
                            "\nBucket of " + hex(i + 1) + " : " + di.bucket.getAllNodes().toString()
                    );
                    //use findNode algorithm. since all nodes are present, it shouldn't return null.
                    assert dj.findNode(di.bucket.getLocalNode().getKey()) != null;

                    logger.debug("\nSuccessful !!!");
                }
            }
        }
    }

}