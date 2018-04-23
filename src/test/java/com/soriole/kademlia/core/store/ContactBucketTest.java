package com.soriole.kademlia.core.store;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import javax.xml.soap.Node;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class ContactBucketTest {

    NodeInfo localNode=new NodeInfo(new Key("FFFFF00000"));

    ContactBucket bucket=new ContactBucket(localNode,160,3);
    // helper function to generate another random key at given distance from known key.
    private Key randomKey(){
        byte[] bytes=new byte[Key.KEY_LENGTH/8];
        new Random().nextBytes(bytes);
        return new Key(bytes);
    }

    public ContactBucketTest(){

    }

    @Test
    public void testPutGetRemove() throws Exception {

        for(int i=0;i<500;i++) {
            NodeInfo n1 = new NodeInfo(randomKey());
            assert(bucket.putNode(n1));
            assertEquals(n1,bucket.getNode(n1.getKey()));
            assert(bucket.removeNode(n1.getKey()));
            assertEquals(null,bucket.getNode(n1.getKey()));

        }
    }

    @Test
    public void putInOrder() throws InterruptedException {

        NodeInfo n1=new NodeInfo(new Key("FFFFFF0000"));
        NodeInfo n2=new NodeInfo(new Key("FFFFFF1234"));
        NodeInfo n3=new NodeInfo(new Key("FFFFFFabcd"));
        NodeInfo n4=new NodeInfo(new Key("FFFFFF3453"));

        //put three nodes atleast with a millisecond gap.
        bucket.putNode(n1);
        Thread.sleep(1);
        bucket.putNode(n2);
        Thread.sleep(1);
        bucket.putNode(n3);


        // make sure that n1 n2 and n3 are all in the bucket.
        assertEquals(n1,bucket.getNode(n1.getKey()));
        assertEquals(n2,bucket.getNode(n2.getKey()));
        assertEquals(n3,bucket.getNode(n3.getKey()));

        // make sure the node 4 cannot be put into the bucket.
        assertEquals(false,bucket.putNode(n4));
        assertEquals(null,bucket.getNode(n4.getKey()));

        //make sure that node1 is the one that needs to be ping to check if it's active
        assertEquals(n1,bucket.putOrGetChallenger(n4));


        // make sure the node4 can be force put into the kademlia
        // node1 must be removed when node4 is forceput into it.
        bucket.putNodeForced(n4);
        assertEquals(n4,bucket.getNode(n4.getKey()));
        assertEquals(null,bucket.getNode(n1.getKey()));

        // remove all the nodes from the bucket.
        assertEquals(true,bucket.removeNode(n2.getKey()));
        assertEquals(true,bucket.removeNode(n3.getKey()));
        assertEquals(true,bucket.removeNode(n4.getKey()));
    }

    @Test
    public void testGetClosestNodes() throws Exception {
       ArrayList<NodeInfo> nodes=new ArrayList<>(3);
        NodeInfo n1=new NodeInfo(new Key("ffff"));
        NodeInfo n2=new NodeInfo(new Key("fffe"));
        NodeInfo n3=new NodeInfo(new Key("fffd"));
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        bucket.putAllNodes(nodes);

        // get closest nodes for some random key. result should always be above three.
        for(int i=0;i<20;i++){
            Collection<NodeInfo> gotNodes=bucket.getClosestNodes(randomKey());
            for(NodeInfo n:nodes){
                assert(gotNodes.contains(n));
            }
        }

        // remove all the entries
        for(NodeInfo n:nodes){
            bucket.removeNode(n.getKey());
        }

    }

}