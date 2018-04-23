package com.soriole.kademlia.core;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NodeListMessage;
import com.soriole.kademlia.core.messages.NodeLookupMessage;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.network.KademliaMessageServer;
import com.soriole.kademlia.network.ServerShutdownException;
import com.soriole.kademlia.network.server.DataGramServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class KademliaDHT implements KadProtocol<String>{

    public final ContactBucket bucket;
    public final KademliaMessageServer server;

    public KademliaDHT(ContactBucket bucket, KademliaMessageServer server) {
        this.bucket=bucket;
        this.server=server;
    }

    /**
     * The FIND_NODE RPC includes a 160-bit key. The recipient of the RPC returns up to k triples (IP address, port, nodeID) for the contacts that it knows to be closest to the key.

     The recipient must return k triples if at all possible. It may only return fewer than k if it is returning all of the contacts that it has knowledge of.

     This is a primitive operation, not an iterative one
     */
    protected Collection<NodeInfo> findClosestNodes(Key key, NodeInfo info){return null;}


    @Override
    public Collection<NodeInfo> findClosestNodes(Key key) {
        return null;
    }

    @Override
    public boolean store(Key key, String value) {
        return false;
    }

    @Override
    public String findValue(Key key) throws KademliaException {
        return null;
    }

    @Override
    public boolean ping(NodeInfo node) {
        return false;
    }

    @Override
    public NodeInfo findNode(Key key) {

        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public boolean join(NodeInfo bootstrapNode){

        NodeListMessage nodeListMessage= null;
        try {
            nodeListMessage = (NodeListMessage) server.startQuery(bootstrapNode,new NodeLookupMessage(bucket.getLocalNode().getKey()));
            bucket.putAllNodes(nodeListMessage.nodes);
            return true;
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO: This is a naive way. Pinging each of received nodes is another way to make sure they are live.
        return false;

    }

    @Override
    public void join(InetSocketAddress address) throws KademliaException {

    }
}