package com.soriole.kademlia.core;

import com.soriole.kademlia.core.store.*;
import com.soriole.kademlia.network.KademliaMessageServer;
import com.soriole.kademlia.network.ServerShutdownException;
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

    Logger logger = LoggerFactory.getLogger(KademliaDHTTest.class.getSimpleName());
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
        TimestampedStore<byte[]> timestampedStore = new InMemoryByteStore(KademliaDHT.defaultExpirationTime);
        KademliaMessageServer server = new KademliaMessageServer(0, bucket, service, timestampedStore);
        bucket.getLocalNode().setLanAddress(server.getSocketAddress());
        return new KademliaDHT(bucket, server, timestampedStore);
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

                    logger.debug("\nNode  " + hex(i + 1) + " is finding  Node" + hex(j + 1) +
                            "\nBucket of " + hex(i + 1) + " : " + di.bucket.getAllNodes().toString()
                    );
                    //use findNode algorithm. since all nodes are present, it shouldn't return null.
                    assert dj.findNode(di.bucket.getLocalNode().getKey()) != null;

                    logger.debug("\nSuccessful !!!");
                }
            }
        }
    }


    // each node will put random (keys,value) in the network.
    // we test that that all the stored keys are available from all the nodes in dht.

    @Test
    public void store_Find_Value_1() throws ServerShutdownException, KademliaException {
        try {
            Random random = new Random();
            HashMap<Key, byte[]> verificationTable = new HashMap<>();
            logger.debug("\n############## Find Value Test ##############");

            HashSet<Key> randomkeys = new HashSet<>();
            // each puts 1  unique key in the dht network for test.
            while (randomkeys.size() != dhts.size()) {

                randomkeys.add(new Key(Long.toHexString(random.nextLong())));
            }
            Iterator<Key> keys = randomkeys.iterator();
            // first, each dhtNode stores a random key and value to the dht
            for (int i = 0; i < dhts.size(); i++) {
                Key rKey = keys.next();
                // create a random key and value
                byte[] bArray = new byte[8];
                random.nextBytes(bArray);

                logger.debug(" Node " + (i + 1) + " storing  :" + rKey);

                // put it them in the dht network and the verification Table.
                dhts.get(i).put(rKey, bArray);
                verificationTable.put(rKey, bArray);

                logger.debug(" Done !!");

            }

            // print the stores of each Nodes in the network.
            for (int i = 0; i < dhts.size(); i++) {
                logger.debug("Store of " + (i + 1) + " : " + dhts.get(i).timestampedStore);
            }

            int z = 1;
            for (Key k : verificationTable.keySet()) {

                byte[] target = verificationTable.get(k);
                //  find the value using dht's algorighm.
                // make sure that each stored key is visible to each nodes in the network.
                for (int i = 0; i < dhts.size(); i++) {
                    byte[] found = null;
                    logger.info("(" + (i + 1) + "/" + z + "/" + dhts.size() + ")   Node " + dhts.get(i).bucket.getLocalNode() + " serching with key :" + k);
                    found = dhts.get(i).get(k).getData();

                    assertArrayEquals(found, target);

                }
                z++;
            }
            logKeyStores();
        }
        catch (Exception e){
            logKeyStores();
            throw e;
        }
    }

    // we put the fixed keys such that the put of each node is always same.
    // we first verify that put and find are working
    // then verify that the each of node's key,value put is as expected.
    @Test
    public void store_Find_Value_2() throws ServerShutdownException, KademliaException {
        Random random = new Random();
        logger.debug("\n############## Find Value Test ##############");

        // first, each dhtNode stores a random key and value to the dht
        for (int i = 0; i < dhts.size(); i++) {

            // if there are 20 nodes,
            // Node 20 stores Key(1) --  Node 19 stores Key(2) and so on.
            Key k = new Key(hex(dhts.size() - i));

            // put the key and hex representation key, so that it's easy while debugging.
            dhts.get(i).put(k, k.toBytes());

            logger.debug(" Node " + (i + 1) + " stored  :" + k);

        }

        int k = 1;
        for (KademliaDHT dht : dhts) {
            for (int i = 0; i < dhts.size(); i++) {
                Key key = new Key(hex(i + 1));
                byte[] found = dht.get(key).getData();

                // the key and value should be equal
                assertEquals(new Key(found), key);

                logger.info("(" + (i + 1) + "/" + k + "/" + dhts.size() + ") Node :" + dht.bucket.getLocalNode().getKey() + " Verified key :" + (i + 1));
            }
            k++;
        }
        //TODO: verify that put of each contact are as aspected.
        logKeyStores();
    }

    private void logKeyStores() {
        // print the stores of each Nodes in the network.
        for (int i = 0; i < dhts.size(); i++) {
            logger.debug("Store of " + (i + 1) + " : " + dhts.get(i).timestampedStore);
        }

    }


}