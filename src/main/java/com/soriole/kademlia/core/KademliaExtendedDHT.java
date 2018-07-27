package com.soriole.kademlia.core;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NonKademliaMessage;
import com.soriole.kademlia.core.messages.listeners.ByteListener;
import com.soriole.kademlia.core.network.ExtendedMessageDispacher;
import com.soriole.kademlia.core.network.ServerShutdownException;
import com.soriole.kademlia.core.network.receivers.ByteReceiver;
import com.soriole.kademlia.core.network.receivers.MessageReceiver;
import com.soriole.kademlia.core.network.server.udp.ExtendedUdpServer;
import com.soriole.kademlia.core.store.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

// ExtendedKademliaDHT enables us to send messages beyond the kademlia protocol messages.
// These features will be used to build up application layer service by using
// kademlia to transmit other types of messages.
public class KademliaExtendedDHT extends KademliaDHT {
    static private Logger logger = LoggerFactory.getLogger(KademliaExtendedDHT.class);

    public static class NoSuchNodeFoundException extends Exception {
    }

    public KademliaExtendedDHT(Key localNodeKey, KademliaConfig config) throws SocketException {
        this(
                new ContactBucket(new NodeInfo(localNodeKey), config),
                new InMemoryByteStore(config.getKeyValueExpiryTime()),
                config
        );
    }

    public KademliaExtendedDHT(ContactBucket bucket, ExtendedMessageDispacher dispacher, TimestampedStore<byte[]> store, KademliaConfig config) {
        super(bucket, dispacher, store, config);
    }

    //constructor used to make above creation easy.
    public KademliaExtendedDHT(ContactBucket bucket, TimestampedStore<byte[]> store, KademliaConfig config) throws SocketException {
        super(bucket, new ExtendedUdpServer(config, bucket, store), store, config);
    }

    public KademliaExtendedDHT(Key localNodeKey, TimestampedStore<byte[]> store, KademliaConfig config) throws SocketException {
        this(new ContactBucket(new NodeInfo(localNodeKey), config), store, config);

    }

    public void sendMessageToNode(Key key, byte[] message) throws IOException, NoSuchNodeFoundException, ServerShutdownException, TimeoutException {
        NonKademliaMessage msg = new NonKademliaMessage();
        msg.rawBytes = message;
        server.sendMessage(toNode(key), msg);
    }

    public void sendMessageToNode(Key key, long sessionId, byte[] message) throws NoSuchNodeFoundException, ServerShutdownException, IOException, TimeoutException {
        NonKademliaMessage msg = new NonKademliaMessage();
        msg.rawBytes = message;
        msg.sessionId = sessionId;
        server.sendMessage(toNode(key), msg);
    }

    private byte[] queryWithNode(Key key, long sessionId, byte[] message, long timeoutMs) throws TimeoutException, NoSuchNodeFoundException, ServerShutdownException, IOException {
        NonKademliaMessage msg = new NonKademliaMessage();
        msg.sessionId = sessionId;
        msg.rawBytes = message;
        Message m = server.startQuery(toNode(key), msg, timeoutMs);
        if (m instanceof NonKademliaMessage) {
            return ((NonKademliaMessage) m).rawBytes;
        } else {
            logger.warn(String.format("Expected NonKademliaMessage but received msg of type %s", m.getClass().getSimpleName()));
            throw new TimeoutException();
        }
    }

    private byte[] queryWithNode(Key key, long sessionId, byte[] message) throws TimeoutException, NoSuchNodeFoundException, ServerShutdownException, IOException {
        NonKademliaMessage msg = new NonKademliaMessage();
        msg.sessionId = sessionId;
        msg.rawBytes = message;
        Message m = server.startQuery(toNode(key), msg);
        if (m instanceof NonKademliaMessage) {
            return ((NonKademliaMessage) m).rawBytes;
        } else {
            logger.warn(String.format("Expected NonKademliaMessage but received msg of type %s", m.getClass().getSimpleName()));
            throw new TimeoutException();
        }
    }


    public byte[] queryWithNode(Key key, byte[] message) throws NoSuchNodeFoundException, ServerShutdownException, TimeoutException, IOException {
        return queryWithNode(key, new Random().nextLong(), message);
    }

    public byte[] queryWithNode(Key key, byte[] message, long timeoutMs) throws NoSuchNodeFoundException, ServerShutdownException, TimeoutException, IOException {
        return queryWithNode(key, new Random().nextLong(), message, timeoutMs);
    }

    public void queryWithNodeAsync(Key key, byte[] message, ByteListener listener) throws NoSuchNodeFoundException, ServerShutdownException {
        server.startQueryAsync(toNode(key), new NonKademliaMessage(message), wrapReceiver(listener));
    }

    public void queryWithNodeAsync(Key key, byte[] message, ByteListener listener, long timeoutMs) throws NoSuchNodeFoundException, ServerShutdownException {
        server.startQueryAsync(toNode(key), new NonKademliaMessage(message), timeoutMs, wrapReceiver(listener));
    }

    public void setMessageReceiver(ByteReceiver receiver) {
        ((ExtendedMessageDispacher) (this.server)).setNonKademliaMessageReceiver(receiver);
    }

    private NodeInfo toNode(Key key) throws ServerShutdownException, NoSuchNodeFoundException {
        NodeInfo info = findNode(key);
        if (info == null) {
            throw new NoSuchNodeFoundException();
        }
        return info;
    }

    private static MessageReceiver wrapReceiver(final ByteListener listener) {
        return new MessageReceiver() {
            @Override
            public void onReceive(Message message) {
                if (message instanceof NonKademliaMessage) {
                    listener.onReceive(((NonKademliaMessage) message).rawBytes);

                } else {
                    logger.warn(String.format("Expected NonKademliaMessage reply but got response of :%s", message.getClass().getSimpleName()));
                    listener.onTimeout();
                }
            }

            @Override
            public void onTimeout() {
                listener.onTimeout();
            }
        };
    }
}
