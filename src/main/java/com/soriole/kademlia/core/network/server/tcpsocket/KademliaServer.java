package com.soriole.kademlia.core.network.server.tcpsocket;

import com.soriole.kademlia.core.NodeInteractionListener;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NonKademliaMessage;
import com.soriole.kademlia.core.messages.listeners.ListenerFactory;
import com.soriole.kademlia.core.messages.listeners.MessageListener;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.store.TimestampedStore;
import com.soriole.kademlia.core.network.MessageDispacher;
import com.soriole.kademlia.core.network.ServerShutdownException;
import com.soriole.kademlia.core.network.receivers.BulkMessageReceiver;
import com.soriole.kademlia.core.network.receivers.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class KademliaServer implements MessageDispacher, MessageReceiver {
    ExecutorService service;
    TcpPeerSocket peerSocket;
    HashMap<InetSocketAddress, Socket> socketToUse;
    ContactBucket bucket;
    TimestampedStore store;
    Logger logger=LoggerFactory.getLogger(KademliaServer.class);

    public KademliaServer(int port, ContactBucket bucket, TimestampedStore store) throws IOException {
        this.bucket = bucket;
        this.store = store;
        this.service = Executors.newFixedThreadPool(10);
        peerSocket = new TcpPeerSocket(port, bucket, service);
        peerSocket.setMessageReceiver(this);
    }

    public KademliaServer(int port, ContactBucket bucket, ExecutorService service, TimestampedStore store) throws IOException {
        this.bucket = bucket;
        this.store = store;
        this.service = service;
        peerSocket = new TcpPeerSocket(port, bucket, service);
        peerSocket.setMessageReceiver(this);

    }

    @Override
    public void sendMessage(NodeInfo receiver, Message message) throws IOException, TimeoutException {
        message.sessionId=new Random().nextLong();
        peerSocket.sendMessage(receiver.getLanAddress(), message);
    }

    @Override
    public void replyFor(Message incoming, Message reply) throws IOException, TimeoutException {
        reply.sessionId = incoming.sessionId;
        peerSocket.sendMessage(incoming.mSrcNodeInfo.getLanAddress(), reply);
    }

    @Override
    public Message queryFor(Message incoming, Message reply) throws TimeoutException, IOException {
        reply.sessionId = incoming.sessionId;
        return peerSocket.query(incoming.mSrcNodeInfo,reply, 4000);
    }

    @Override
    public Collection<Message> startAsyncQueryAll(Collection<NodeInfo> nodes, Message queryMessage) {
        final BulkMessageReceiver receiver=new BulkMessageReceiver(nodes.size());
        final Random random = new Random();
        queryMessage.sessionId=new Random().nextLong();
        // copy the list so that there's no inconsistency if calling thread modifies the passed collection.
        for (NodeInfo node: new ArrayList<>(nodes)){
            service.submit(() -> {
                try {
                    receiver.onReceive(peerSocket.query(node,queryMessage, 4000));
                    return;
                } catch (TimeoutException e) {
                    // this is normal to happen
                } catch ( IOException e) {
                    e.printStackTrace();
                }
                logger.info("Timeout on startAsyncQuery to : "+node.getKey());
                receiver.onTimeout();
            });
        }
        return receiver.getMessages();
    }

    @Override
    public void asynQueryFor(Message incoming, Message reply, MessageReceiver msgReceiver) throws ServerShutdownException {
        reply.sessionId=incoming.sessionId;
        service.submit(() -> {
            try {
                msgReceiver.onReceive(this.queryFor(incoming, reply));
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            msgReceiver.onTimeout();
        });


    }

    @Override
    public Message startQuery(NodeInfo receiver, Message message) throws TimeoutException, ServerShutdownException, IOException {
        message.sessionId=new Random().nextLong();
        return peerSocket.query(receiver,message, 4000);
    }

    @Override
    public void startQueryAsync(NodeInfo receiver, Message message, MessageReceiver msgReceiver) throws ServerShutdownException {
        message.sessionId=new Random().nextLong();
        service.submit(() -> {
            try {
                msgReceiver.onReceive(peerSocket.query(receiver,message, 6000));
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
            msgReceiver.onTimeout();
        });

    }

    @Override
    public boolean shutDown(int timeSeconds) throws InterruptedException {
        return false;
    }

    @Override
    public boolean stop() {
        return false;
    }

    @Override
    public void submitTask(Runnable task) {
        service.submit(task);
    }

    @Override
    public boolean start() throws SocketException {
        peerSocket.accept();
        return true;
    }

    @Override
    public void setNodeInteractionListener(NodeInteractionListener listener) {

    }

    @Override
    public InetSocketAddress getUsedSocketAddress() {
        return (InetSocketAddress) this.peerSocket.serverSocket.getLocalSocketAddress();
    }

    @Override
    public void onReceive(Message message) {
        try {

            ListenerFactory.getListener(message, bucket, this, store).onReceive(message);


        } catch (ListenerFactory.NoListenerException e) {
            logger.warn("Message of type `"+message.getClass().getSimpleName()+" dropped after receive!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this function is never called.
    @Override
    public void onTimeout() {

    }
}
