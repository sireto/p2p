package com.soriole.kademlia.core.messages;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.network.KademliaNetworkMessageProtocol;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

@MessageType(type = 1)
public class NodeListMessage extends Message {
    public Collection<NodeInfo> nodes;

    // there should be a no argument constructor for a MessageType
    public NodeListMessage() {
    }

    public NodeListMessage(Collection<NodeInfo> nodes) {
        this.nodes = nodes;
    }

    @Override
    public byte[] writeToBytes() {
        KademliaNetworkMessageProtocol.NodeList.Builder listBuilder = KademliaNetworkMessageProtocol.NodeList.newBuilder();

        for (NodeInfo node : nodes) {
            // build a new node and add to the list.
            listBuilder.addNodes(KademliaNetworkMessageProtocol.NodeInfo.newBuilder()
                    .setKadid(ByteString.copyFrom(node.getKey().toBytes()))
                    .setAddress(ByteString.copyFrom(node.getLanAddress().getAddress().getAddress()))
                    .setPort(node.getLanAddress().getPort())
                    .build());
        }

        return listBuilder.build().toByteArray();
    }

    @Override
    public void readFromBytes(byte[] bytes) throws InvalidProtocolBufferException {

        KademliaNetworkMessageProtocol.NodeList nodeList = KademliaNetworkMessageProtocol.NodeList.parseFrom(bytes);
        // iterate over each node info.
        nodes = new ArrayList(nodeList.getNodesList().size());
        for (KademliaNetworkMessageProtocol.NodeInfo nodeInfo : nodeList.getNodesList()) {
            try {
                InetSocketAddress address = new InetSocketAddress(
                        InetAddress.getByAddress(nodeInfo.getAddress().toByteArray()),
                        nodeInfo.getPort()
                );
                nodes.add(new NodeInfo(new Key(nodeInfo.getKadid().toByteArray()), address));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

    }
}
