package com.soriole.kademlia.core.network.server.udp;

import com.google.protobuf.ByteString;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.MessageFactory;
import com.soriole.kademlia.core.messages.MessageType;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.network.KademliaNetworkMessageProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * KademliaServer is able to read the datagram received in a socket and convert them into
 * appropriate message format so that the upper layer can handle it.
 * The tasks performed by this server are:
 *  <ol>
 *      <li>read datagram and from DatagramServer and convert them to subclasses of Message. </li>
 *      <li>check the sender if it's in kademliaBucket and try to insert it if it's not.</li>
 *      <li>check for the change of network address of the sender and inform it to upper layer.</li>
 *  </ol>
 */

abstract class MessageServer extends DataGramServer {

    static private Logger logger = LoggerFactory.getLogger(MessageServer.class);

    protected ContactBucket bucket;

    protected MessageServer(DatagramSocket socket, ContactBucket bucket) {
        super(socket);
        this.bucket = bucket;
    }

    protected void sendMessage(Message message, NodeInfo receiver, long sessionId) throws IOException {
        if (bucket.getLocalNode().equals(receiver)) {
            logger.warn("Coding Error ! The server is trying to send message to itself!");
            return;
        }
        if (message.mSrcNodeInfo == null) {
            message.mSrcNodeInfo = bucket.getLocalNode();
        }

        KademliaNetworkMessageProtocol.Message.Builder builder = KademliaNetworkMessageProtocol.Message.newBuilder();
        builder.setSender(ByteString.copyFrom(message.mSrcNodeInfo.getKey().toBytes()))
                .setSessionId(sessionId)
                .setType(message.getClass().getAnnotation(MessageType.class).type());
        if (receiver.getKey() != null) {
            builder.setReceiver(ByteString.copyFrom(receiver.getKey().toBytes()));
        }

        byte[] data = message.writeToBytes();
        if (data != null) {
            builder.setMessageData(ByteString.copyFrom(data));
        }
        DatagramPacket packet = super.createPacket(builder.build().toByteArray(), receiver.getLanAddress());
        super.sendPacket(packet);
    }

    protected void sendMessage(Message message) throws IOException {
        sendMessage(message, message.mDestNodeInfo, message.sessionId);

    }

    protected Message receiveMessage() throws IOException {
        DatagramPacket packet = receivePacket();
        byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
        KademliaNetworkMessageProtocol.Message messageProto = KademliaNetworkMessageProtocol.Message.parseFrom(data);
        Message message = null;
        try {
            message = MessageFactory.createUninitialized(messageProto.getType());
        } catch (Exception e) {
            logger.warn("MessageFactory.createMessageInstance failed for type " + String.valueOf(messageProto.getType()));
        }

        // get The sender's identifier key
        Key senderKey = new Key(messageProto.getSender().toByteArray());

        // get the sender's socketAddress info from the datagramPacket
        message.mSrcNodeInfo = new NodeInfo(senderKey, (InetSocketAddress) packet.getSocketAddress());

        // if the message has set session id, set it.
        if (messageProto.hasSessionId()) {
            message.sessionId = messageProto.getSessionId();
        }
        // if the receiver is set, fill the receiver value
        if (messageProto.hasReceiver()) {
            message.mDestNodeInfo = bucket.getNode(new Key(messageProto.getReceiver().toByteArray()));

            // message not for us and we don't know receiver's address.
            if (message.mDestNodeInfo != bucket.getLocalNode()) {
                onNewForwardMessage(message, message.mDestNodeInfo);
            }

        }
        // message doesn't have the receiver's information.
        // so we safely assume that the message is for us.
        else {
            message.mDestNodeInfo = bucket.getLocalNode();
        }
        // if the message has message data read it too.
        if (messageProto.hasMessageData()) {
            message.readFromBytes(messageProto.getMessageData().toByteArray());
        }

        // sessionId=1 is the session value used by the clients to access kademlia features.
        if (message.sessionId != 1) {
            // Check if the sender is already is in out kademlia bucket.
            NodeInfo bucketNode = bucket.getNode(senderKey);
            // if this node is not present in the bucket
            if (bucketNode == null) {
                // try to put it into the bucket
                if (!bucket.putNode(message.mSrcNodeInfo)) {
                    // if we cannot put it into the bucket we need to notify for furthur action
                    onNewNodeFound(message.mSrcNodeInfo.clone());
                }
                return message;
            }
            // if the contact is already in the kademlia bucket, check if the address is same.
            // if the address is changed, we might or might need to do some extra tasks.
            // TODO: may not be appropriate if it's a forwarded message without onion layer.
            else if (!message.mSrcNodeInfo.getLanAddress().equals(bucketNode.getLanAddress())) {
                onNetworkAddressChange(senderKey, (InetSocketAddress) packet.getSocketAddress());
            }
            bucket.putNode(message.mSrcNodeInfo);
        }
        return message;

    }

    protected abstract void onNetworkAddressChange(Key senderKey, InetSocketAddress newSocketAddress);

    protected abstract void onNewNodeFound(NodeInfo info);

    protected abstract void onNewForwardMessage(Message message, NodeInfo destination);

}