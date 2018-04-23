package com.soriole.kademlia.network.server;

import com.google.protobuf.ByteString;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.MessageFactory;
import com.soriole.kademlia.core.messages.MessageType;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.network.KademliaNetworkMessageProtocol;
import org.apache.tomcat.util.collections.SynchronizedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * KademliaMessageServer is able to read the datagram received in a socket and convert them into
 * appropriate message format so that the upper layer can handle it.
 */
abstract class MessageServer extends DataGramServer {
    static private Logger logger = LoggerFactory.getLogger(MessageServer.class);


    SynchronizedQueue<Message> messages;

    protected ContactBucket bucket;

    protected MessageServer(DatagramSocket socket, ContactBucket bucket) {
        super(socket);
        this.bucket = bucket;
    }

    protected void sendMessage(Message message) throws IOException {
        //KademliaNetworkMessageProtocol.Message  messageProto=new KademliaNetworkMessageProtocol.Message.;
        if (message.mSrcNodeInfo == null) {
            message.mSrcNodeInfo = bucket.getLocalNode();
        }

        KademliaNetworkMessageProtocol.Message.Builder builder = KademliaNetworkMessageProtocol.Message.newBuilder();
        builder.setSender(ByteString.copyFrom(message.mSrcNodeInfo.getKey().toBytes()))
                .setSessionId(message.sessionId)
                .setType(((MessageType) message.getClass().getAnnotation(MessageType.class)).type());
        if (message.mDestNodeInfo.getKey() != null) {
            builder.setReceiver(ByteString.copyFrom(message.mDestNodeInfo.getKey().toBytes()));
        }

        byte[] data = message.writeToBytes();
        if (data != null) {
            builder.setMessageData(ByteString.copyFrom(data));
        }
        DatagramPacket packet = super.createPacket(builder.build().toByteArray(), message.mDestNodeInfo.getLanAddress());
        super.sendPacket(packet);
    }

    protected Message receiveMessage() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        DatagramPacket packet = receivePacket();
        byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
        KademliaNetworkMessageProtocol.Message messageProto = KademliaNetworkMessageProtocol.Message.parseFrom(data);
        Message message = MessageFactory.createMessage(messageProto.getType());

        // get The sender's identifier key
        Key senderKey = new Key(messageProto.getSender().toByteArray());

        // get the sender's socketAddress info from the datagramPacket
        message.mSrcNodeInfo = new NodeInfo(senderKey, (InetSocketAddress) packet.getSocketAddress());

        // if the message has set session id, set it.
        if (messageProto.hasSessionId()) {
            message.sessionId = messageProto.getSessionId();
        } else {
            message.sessionId = 0;
        }

        // if the receiver is set, fill the receiver value
        if (messageProto.hasReceiver()) {
            message.mDestNodeInfo = bucket.getNode(new Key(messageProto.getReceiver().toByteArray()));

            // message not for us and we don't know receiver's address.
            if (message.mDestNodeInfo == null) {
                //TODO: Message received for forwarding, but we don't know receiver's address.
            }

        } else {
            message.mDestNodeInfo = bucket.getLocalNode();
        }
        // if the message has message data read it too.
        if (messageProto.hasMessageData()) {
            message.readFromBytes(messageProto.getMessageData().toByteArray());
        }

        // Check if the sender is already is in out kademlia bucket.
        NodeInfo bucketNode = bucket.getNode(senderKey);
        if (bucketNode == null) {
            onNewNodeFound(message.mSrcNodeInfo.clone());
        } else if (!message.mSrcNodeInfo.getLanAddress().equals(bucketNode.getLanAddress())) {
            onNetworkAddressChange(senderKey, (InetSocketAddress) packet.getSocketAddress());
        }
        return message;
    }

    abstract protected void onNetworkAddressChange(Key key, InetSocketAddress address);

    abstract protected void onNewNodeFound(NodeInfo info);


}