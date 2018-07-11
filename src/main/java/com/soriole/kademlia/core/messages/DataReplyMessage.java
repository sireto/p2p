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

@MessageType(type = 7)
public class DataReplyMessage extends Message {
    public boolean success;
    public Collection<NodeInfo> nearerNodes;

    @Override
    public byte[] writeToBytes() {
        KademliaNetworkMessageProtocol.KeyValueStoreReply.Builder replyBuilder = KademliaNetworkMessageProtocol.KeyValueStoreReply.newBuilder();

        for (NodeInfo node : nearerNodes) {
            // build a new node and add to the list.
            replyBuilder.addNodes(KademliaNetworkMessageProtocol.NodeInfo.newBuilder()
                    .setKadid(ByteString.copyFrom(node.getKey().toBytes()))
                    .setAddress(ByteString.copyFrom(node.getLanAddress().getAddress().getAddress()))
                    .setPort(node.getLanAddress().getPort())
                    .build());
            replyBuilder.setStored(success);

        }

        return replyBuilder.build().toByteArray();
    }

    @Override
    public void readFromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        KademliaNetworkMessageProtocol.KeyValueStoreReply reply = KademliaNetworkMessageProtocol.KeyValueStoreReply.parseFrom(bytes);
        // iterate over each node info.
        nearerNodes = new ArrayList(reply.getNodesList().size());
        for (KademliaNetworkMessageProtocol.NodeInfo nodeInfo : reply.getNodesList()) {
            try {
                InetSocketAddress address = new InetSocketAddress(
                        InetAddress.getByAddress(nodeInfo.getAddress().toByteArray()),
                        nodeInfo.getPort()
                );
                nearerNodes.add(new NodeInfo(new Key(nodeInfo.getKadid().toByteArray()), address));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        this.success = reply.getStored();

    }
}
