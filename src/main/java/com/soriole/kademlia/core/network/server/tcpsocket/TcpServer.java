package com.soriole.kademlia.core.network.server.tcpsocket;

import com.soriole.kademlia.core.KademliaConfig;
import com.soriole.kademlia.core.NodeInteractionListener;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.listeners.ListenerFactory;
import com.soriole.kademlia.core.network.MessageDispacher;
import com.soriole.kademlia.core.network.ServerShutdownException;
import com.soriole.kademlia.core.network.receivers.BulkMessageReceiver;
import com.soriole.kademlia.core.network.receivers.MessageReceiver;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.store.TimestampedStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class TcpServer implements MessageDispacher, MessageReceiver {
    ExecutorService service;
    TcpPeerSocket peerSocket;
    HashMap<InetSocketAddress, Socket> socketToUse;
    ContactBucket bucket;
    TimestampedStore store;
    Logger logger=LoggerFactory.getLogger(TcpServer.class);
    KademliaConfig config;

    public TcpServer(KademliaConfig config,ContactBucket bucket, TimestampedStore store) throws IOException {
        this.config=config;
        this.bucket = bucket;
        this.store = store;
        this.service = Executors.newFixedThreadPool(config.getnWorkers());
        peerSocket = new TcpPeerSocket(config.getKadeliaProtocolPort(), bucket, service);
        peerSocket.setMessageReceiver(this);
    }

    public TcpServer(KademliaConfig config,ContactBucket bucket, ExecutorService service, TimestampedStore store) throws IOException {
        this.config=config;
        this.bucket = bucket;
        this.store = store;
        this.service = service;
        peerSocket = new TcpPeerSocket(config.getKadeliaProtocolPort(), bucket, service);
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
    public Message queryFor(Message incoming, Message reply) throws TimeoutException, IOException, ServerShutdownException {
        return this.queryFor(incoming,reply,config.getConnectionTimeout());
    }

    @Override
    public Message queryFor(Message incoming, Message reply, long timeoutMs) throws TimeoutException, IOException, ServerShutdownException {
        reply.sessionId = incoming.sessionId;
        return peerSocket.query(incoming.mSrcNodeInfo,reply, timeoutMs);
    }

    @Override
    public Collection<Message> startAsyncQueryAll(Collection<NodeInfo> nodes, Message queryMessage) {
        return this.startAsyncQueryAll(nodes,queryMessage,config.getConnectionTimeout());
    }

    @Override
    public Collection<Message> startAsyncQueryAll(Collection<NodeInfo> nodes, Message queryMessage, long timeoutMs) {
        final BulkMessageReceiver receiver=new BulkMessageReceiver(nodes.size());
        queryMessage.sessionId=new Random().nextLong();
        // copy the list so that there's no inconsistency if calling thread modifies the passed collection.
        for (NodeInfo node: new ArrayList<>(nodes)){
            service.submit(() -> {
                try {
                    receiver.onReceive(peerSocket.query(node,queryMessage, timeoutMs));
                    return;
                } catch (TimeoutException e) {
                    logger.info("Timeout on startAsyncQueryAll to node : "+node.getKey());
                } catch ( IOException e) {
                    logger.info("Exception on sending/receiving message",e);
                }

                receiver.onTimeout();
            });
        }
        return receiver.getMessages();
    }

    @Override
    public void asynQueryFor(Message incoming, Message reply, MessageReceiver msgReceiver) throws ServerShutdownException {
        this.asynQueryFor(incoming,reply,config.getConnectionTimeout(),msgReceiver);
    }

    @Override
    public void asynQueryFor(Message incoming, Message reply, long timeoutMs, MessageReceiver msgReceiver) throws ServerShutdownException {
        reply.sessionId=incoming.sessionId;
        service.submit(() -> {
            try {
                msgReceiver.onReceive(this.queryFor(incoming, reply,timeoutMs));
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServerShutdownException e) {
                e.printStackTrace();
            }
            msgReceiver.onTimeout();
        });
    }

    @Override
    public Message startQuery(NodeInfo receiver, Message message) throws TimeoutException, ServerShutdownException, IOException {
       return this.startQuery(receiver,message,config.getConnectionTimeout());
    }

    @Override
    public Message startQuery(NodeInfo receiver, Message message, long timeoutMs) throws TimeoutException, ServerShutdownException, IOException {
        message.sessionId=new Random().nextLong();
        return peerSocket.query(receiver,message, timeoutMs);
    }

    @Override
    public void startQueryAsync(NodeInfo receiver, Message message, MessageReceiver msgReceiver) throws ServerShutdownException {
        this.startQueryAsync(receiver,message,config.getConnectionTimeout(),msgReceiver);

    }

    @Override
    public void startQueryAsync(NodeInfo receiver, Message message, long timeoutMs, MessageReceiver msgReceiver) throws ServerShutdownException {
        message.sessionId=new Random().nextLong();
        service.submit(() -> {
            try {
                msgReceiver.onReceive(peerSocket.query(receiver,message, timeoutMs));
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
        throw new UnsupportedOperationException();
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
            logger.warn("Exception while handling received message ",e);
        }
    }

    @Override
    public void onTimeout() {
        // the timeout is never called.

    }
}
