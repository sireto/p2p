package com.soriole.kademlia.core;

import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.store.TimeStampedData;
import com.soriole.kademlia.network.ServerShutdownException;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 *
 * The original Kademlia paper, maymo02, says that the Kademlia protocol consists of four remote procedure calls ("RPCs") but then goes on to specify procedures that must be followed in executing these as well as certain other protocols. It seems best to add these procedures and other protocols to what we call here the Kademlia protocol.
 *
 * T1 : Type of the value that will be stored in the KademliaDHT.
 *x`
 */
public interface KadProtocol<T1> {

    /**
     * Iterative query that findsClosestNodes to a given key.
     * @return
     */
    Collection<NodeInfo> findClosestNodes(Key key) throws ServerShutdownException;

    /*
    The sender of the STORE RPC provides a key and a block of data and requires that the recipient put the data and make it available for later retrieval by that key.

This is a primitive operation, not an iterative one.
     */
    int put(Key key, T1 value) throws  ServerShutdownException;

    /**
     * A FIND_VALUE RPC includes a B=160-bit key. If a corresponding value is present on the recipient, the associated data is returned. Otherwise the RPC is equivalent to a FIND_NODE and a set of k triples is returned.

     This is a primitive operation, not an iterative one.
     */
    TimeStampedData<byte[]> get(Key key) throws NoSuchElementException, ServerShutdownException;

    /**
     * This RPC involves one node sending a PING messages to another, which presumably replies with a PONG.

     This has a two-fold effect: the recipient of the PING must update the bucket corresponding to the sender; and, if there is a reply, the sender must update the bucket appropriate to the recipient.

     All RPC packets are required to carry an RPC identifier assigned by the sender and echoed in the reply. This is a quasi-random number of length B (160 bits).
     */
    long ping(NodeInfo node);

    /**
     * This function uses the kademlia algorithm  for locating the k nodes nearest to a key. It must be understood that these are not necessarily closest in a strict sense. Also, the algorithm is iterative although the paper describes it as recursive.

     The search begins by selecting alpha contacts from the non-empty k-bucket closest to the bucket appropriate to the key being searched on. If there are fewer than alpha contacts in that bucket, contacts are selected from other buckets. The contact closest to the target key, closestNode, is noted.
     */
    NodeInfo findNode(Key key) throws ServerShutdownException;

    /**
     * If no node lookups have been performed in any given bucket's range for tRefresh (an hour in basic Kademlia), the node selects a random number in that range and does a refresh, an iterativeFindNode using that number as key.
     */
    void refresh();

    /**
     * A node joins the network as follows:

     -- if it does not already have a nodeID n, it generates one
     -- it inserts the value of some known node c into the appropriate bucket as its first contact
     -- it does an iterativeFindNode for n
     -- it refreshes all buckets further away than its closest neighbor, which will be in the occupied bucket with the lowest index.

     If the node saved a list of good contacts and used one of these as the "known node" it would be consistent with this protocol.
     */
    boolean join(NodeInfo node);

    // used to join to a node with known ip:port but not the KadID
    void join(InetSocketAddress address);

}
