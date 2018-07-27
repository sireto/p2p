package com.soriole.kademlia.core;

import com.soriole.kademlia.core.network.MessageDispacher;
import com.soriole.kademlia.core.network.ServerShutdownException;
import com.soriole.kademlia.core.network.server.tcpsocket.ExtendedTcpServer;
import com.soriole.kademlia.core.network.server.tcpsocket.TcpServer;
import com.soriole.kademlia.core.network.server.udp.ExtendedUdpServer;
import com.soriole.kademlia.core.network.server.udp.UdpServer;
import com.soriole.kademlia.core.store.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test diffreent kademlia rpcs by creating  a kademlia network containg nDHTS no. of nodes.
 * The test is repeated by using different
 */
@RunWith(Parameterized.class)
public class KademliaDHTTest {
    Logger logger = LoggerFactory.getLogger(KademliaDHTTest.class.getSimpleName());

    // 20 dht nodes should be sufficient to test it.
    // changing N_DHT might require changing the size of Executors.
    private static final int nDHTS = 20;

    ArrayList<KademliaDHT> dhts;

    /**
     * Creates this test class instance using all the available serverClasses.
     */
    @Parameterized.Parameters
    public static Collection dhtTypeToUse() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        return Arrays.asList(new Object[][]{
                {dhtCollection(nDHTS, 30, UdpServer.class)},
                {dhtCollection(nDHTS, 30, ExtendedUdpServer.class)},
                {dhtCollection(nDHTS, 700, TcpServer.class)},
                {dhtCollection(nDHTS, 700, ExtendedTcpServer.class)},

        });
    }


    public KademliaDHTTest(ArrayList<KademliaDHT> dhts) throws Exception {
        this.dhts = dhts;
        // now join each node with the first dht node.
        NodeInfo bootstrapNode = dhts.get(0).bucket.getLocalNode();
        for (int i = 1; i < dhts.size(); i++) {
            logger.debug("\nConnecting Node " + dhts.get(i).getLocalNode().getKey() + " with bootstrap node --");
            // join involves many async queries.
            assert dhts.get(i).join(bootstrapNode);

            logger.debug("\nSuccessful !!");

        }
    }

    /**
     * Each Node tries  finding ClosestNode to each other nodes in the DHT network
     * No of call to findClosestNodes() = (N_DHT-1)^2
     *
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

                    logger.debug("\nNode  " + ni + " is finding nodes closest to " + nj +
                            "\nBucket of " + ni + " : " + di.bucket.getAllNodes().toString()

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
     * Each node tries to ping all the nodes in it's kademlia bucket
     *
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

                    logger.debug("\nNode-" + di.getLocalNode().getKey() + " is finding  Node-" + dj.getLocalNode().getKey() +
                            "\nBucket of " + di.getLocalNode().getKey() + " : " + di.bucket.getAllNodes().toString()
                    );
                    //use findNode algorithm. since all nodes are present, it shouldn't return null.
                    assert di.findNode(dj.bucket.getLocalNode().getKey()) != null;

                    logger.debug("\nSuccessful !!!");
                }
            }
        }
    }


    // each node will put random (keys,value) in the network.
    // we test that that all the stored keys are available from all the nodes in dht.

    @Test
    public void store_Find_Value_1() throws ServerShutdownException, KademliaException, KadProtocol.ContentNotFoundException {

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

            logger.debug(" Node " + dhts.get(i).getLocalNode().getKey() + " storing  :" + rKey);

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
                logger.info(String.format("(%d/%d/%d)   Node %s serching with key :%s", i + 1, z, dhts.size(), dhts.get(i).bucket.getLocalNode(), k));
                found = dhts.get(i).get(k).getData();

                assertArrayEquals(found, target);

            }
            z++;
        }
        logKeyStores();


    }

    // we put the fixed keys such that the put of each node is always same.
    // we first verify that put and find are working
    // then verify that the each of node's key,value put is as expected.
    @Test
    public void store_Find_Value_2() throws ServerShutdownException, KademliaException, KadProtocol.ContentNotFoundException {
        Random random = new Random();
        logger.debug("\n############## Find Value Test ##############");

        // first, each dhtNode stores a random key and value to the dht
        for (int i = 0; i < dhts.size(); i++) {

            // if there are 20 nodes,
            // Node 20 stores Key(1) --  Node 19 stores Key(2) and so on.
            Key k = dhts.get(dhts.size() - i - 1).getLocalNode().getKey();

            // put the key and hex representation key, so that it's easy while debugging.
            dhts.get(i).put(k, k.toBytes());

            logger.debug(" Node " + dhts.get(i).getLocalNode().getKey() + " stored  :" + k);

        }

        int k = 1;
        for (KademliaDHT dht : dhts) {
            for (int i = 0; i < dhts.size(); i++) {
                Key key = dhts.get(i).getLocalNode().getKey();
                // since the key and value pairs are equal, the found value must be equal to the key
                assertArrayEquals(dht.get(key).getData(), key.toBytes());

                logger.info(String.format("(%d/%d/%d) Node :%s Verified key :%d", i + 1, k, dhts.size(), dht.bucket.getLocalNode().getKey(), i + 1));
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

    // creates a DHT instance at random port.
    private static KademliaDHT createDHTInstance(Key key, Constructor<? extends MessageDispacher> constructor, ExecutorService service) throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        KademliaConfig config = KademliaConfig.newBuilder()
                .setTimeoutMs(6000)
                // we don't want nodes to start pinging eachother during the test.
                .setNodeAutoPingTime(Integer.MAX_VALUE).build();
        // create node info
        NodeInfo localKey = new NodeInfo(key);
        //create contact bucket
        ContactBucket bucket = new ContactBucket(localKey, config);
        // create storage instance
        TimestampedStore<byte[]> timestampedStore = new InMemoryByteStore(config);

        // create messgeDispacher instance from the given constructor
        MessageDispacher server = constructor.newInstance(config, bucket, service, timestampedStore);

        bucket.getLocalNode().setLanAddress(server.getUsedSocketAddress());

        // return new KademliaDHT instance
        return new KademliaDHT(bucket, server, timestampedStore, config);
    }

    private static ArrayList<KademliaDHT> dhtCollection(int dhtCount, int threadCount, Class<? extends MessageDispacher> dispacherClass) throws NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends MessageDispacher> constructor = dispacherClass.getConstructor(KademliaConfig.class, ContactBucket.class, ExecutorService.class, TimestampedStore.class);
        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        ArrayList<KademliaDHT> dhts = new ArrayList<>(dhtCount);
        for (int i = 1; i <= dhtCount; i++) {

            // kademlia id of i'th dht = valueof(i)
            dhts.add(createDHTInstance(fromInt(i), constructor, service));
            dhts.get(i - 1).start();
        }
        return dhts;

    }

    private static Key fromInt(int i) {
        return new Key(new BigInteger(String.valueOf(i), 10).toByteArray());
    }


}