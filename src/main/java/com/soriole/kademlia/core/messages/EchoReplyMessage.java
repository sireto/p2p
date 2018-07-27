package com.soriole.kademlia.core.messages;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.network.KademliaNetworkMessageProtocol;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@MessageType(type = 12)
public class EchoReplyMessage extends Message {

    public NodeInfo nodeInfo;

    @Override
    public byte[] writeToBytes() {
        KademliaNetworkMessageProtocol.NodeInfo.Builder builder = KademliaNetworkMessageProtocol.NodeInfo.newBuilder();
        builder.setAddress(ByteString.copyFrom(mSrcNodeInfo.getLanAddress().getAddress().getAddress()));
        builder.setPort(mSrcNodeInfo.getLanAddress().getPort());
        return builder.build().toByteArray();
    }

    @Override
    public void readFromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        KademliaNetworkMessageProtocol.NodeInfo nodeInfo = KademliaNetworkMessageProtocol.NodeInfo.parseFrom(bytes);
        try {
            this.nodeInfo = new NodeInfo(null, new InetSocketAddress(Inet4Address.getByAddress(nodeInfo.getAddress().toByteArray()), nodeInfo.getPort()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new InvalidProtocolBufferException("Given host is unreachable");
        }
    }
}
