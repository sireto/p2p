package com.soriole.kademlia.core;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NonKademliaMessage;
import com.soriole.kademlia.core.messages.listeners.ByteListener;
import com.soriole.kademlia.core.store.*;
import com.soriole.kademlia.network.ExtendedKademliaMessageServer;
import com.soriole.kademlia.network.ServerShutdownException;
import com.soriole.kademlia.network.receivers.MessageReceiver;
import com.soriole.kademlia.network.receivers.ByteReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.concurrent.TimeoutException;

// ExtendedKademliaDHT enables us to send messages beyond the kademlia protocol messages.
// These features will be used to build up application layer service by using
// kademlia to transmit other types of messages.
public class KademliaExtendedDHT extends KademliaDHT {
    static private Logger logger= LoggerFactory.getLogger(KademliaExtendedDHT.class);

    public static class NoSuchNodeFoundException extends Exception{}

    public KademliaExtendedDHT(Key localNodeKey,KademliaConfig config) throws SocketException {
        this(
                new ContactBucket(new NodeInfo(localNodeKey),config.getKeyLength(),config.getK()),
                new InMemoryByteStore(config.getKeyValueExpiryTime()),
                config
        );
    }
    //constructor used to make above creation easy.
    public KademliaExtendedDHT(ContactBucket bucket, TimestampedStore<byte[]> store, KademliaConfig config) throws SocketException {
        super(bucket,new ExtendedKademliaMessageServer(config.getKadeliaProtocolPort(),bucket,store),store);
    }
    public KademliaExtendedDHT(Key localNodeKey,TimestampedStore<byte[]>store,KademliaConfig config) throws SocketException {
        this(new ContactBucket(new NodeInfo(localNodeKey),config.getKeyLength(),config.getK()),store,config);

    }

    public void sendMessageToNode(Key key,byte[] message) throws IOException, NoSuchNodeFoundException, ServerShutdownException {
        NonKademliaMessage msg=new NonKademliaMessage();
        msg.rawBytes=message;
        server.sendMessage(toNode(key),msg);


    }
    public byte[] queryWithNode(Key key,byte[] message) throws TimeoutException, NoSuchNodeFoundException,ServerShutdownException {
        NonKademliaMessage msg=new NonKademliaMessage();
        msg.rawBytes=message;
        NonKademliaMessage m= (NonKademliaMessage) server.startQuery(toNode(key),msg);
        if(m==null){
            logger.warn("Received message Type not matched.");
            throw new TimeoutException();
        }
        return m.rawBytes;
    }
    public void queryWithNodeAsync(Key key, byte[] message, ByteListener listener) throws NoSuchNodeFoundException, ServerShutdownException, TimeoutException {
        NonKademliaMessage msg=new NonKademliaMessage();
        msg.rawBytes=message;
        server.startQueryAsync(toNode(key), msg,wrapReceiver(listener));
    }
    public void setMessageReceiver(ByteReceiver receiver){
        ((ExtendedKademliaMessageServer)(this.server)).setNonKademliaMessageReceiver(receiver);
    }

    private NodeInfo toNode(Key key) throws ServerShutdownException, NoSuchNodeFoundException {
        NodeInfo info=bucket.getNode(key);
        if(info==null){
            SortedSet<NodeInfo> nodes=this.findClosestNodes(key);
            try {
                if (nodes.first().getKey().equals(key)) {
                    return nodes.first();
                }
            }
            catch (NoSuchElementException e){
                throw new NoSuchNodeFoundException();
            }
        }
        else{
            return info;
        }
        throw new NoSuchNodeFoundException();
    }
    private static MessageReceiver wrapReceiver(final ByteListener listener){
        return new MessageReceiver() {
            @Override
            public void onReceive(Message message) {
                if(message instanceof NonKademliaMessage){
                    listener.onReceive(((NonKademliaMessage) message).rawBytes);

                }
                else {
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
