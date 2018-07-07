package com.soriole.kademlia.network;

import com.soriole.kademlia.core.KademliaConfig;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.listeners.ListenerFactory;
import com.soriole.kademlia.core.messages.listeners.MessageListener;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.NodeInteractionListener;
import com.soriole.kademlia.core.store.TimestampedStore;
import com.soriole.kademlia.network.receivers.BulkMessageReceiver;
import com.soriole.kademlia.network.receivers.MessageReceiver;
import com.soriole.kademlia.network.server.SessionServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.*;

public class KademliaMessageServer extends SessionServer {
    private static Logger LOGGER = LoggerFactory.getLogger(KademliaMessageServer.class.getSimpleName());
    NodeInteractionListener nodeInteractionListener ;
    int port;
    private static final int nThreadCount = 10;
    ExecutorService workerPool = null;
    private TimestampedStore keyStore;

    public void setAlpha(int a) {
    }

    public KademliaMessageServer(int listeningPort, ContactBucket bucket,ExecutorService service,TimestampedStore store) throws SocketException {
        super(new DatagramSocket(listeningPort), bucket);
        nodeInteractionListener=getDefaultInteractionListener(bucket);
        this.workerPool=service;
        this.port = listeningPort;
        this.keyStore=store;

    }
    public KademliaMessageServer(ContactBucket bucket, ExecutorService service, TimestampedStore store, KademliaConfig config) throws SocketException {
        this(config.getKadeliaProtocolPort(),bucket,service,store);

    }


    // this method was used when the kademlia DHT didn't had implementation of storage.
    // this will be removed in future refactor.
    @Deprecated
    public KademliaMessageServer(int listeningPort, ContactBucket bucket) throws SocketException {
        this(listeningPort, bucket,null);
    }

    public KademliaMessageServer(int listeningPort, ContactBucket bucket,TimestampedStore store) throws SocketException {
        this(listeningPort, bucket,null,store);
    }


    public void sendMessage(NodeInfo receiver, Message message) throws IOException {
        message.mDestNodeInfo = receiver;
        super.sendMessage(message);
    }

    public void replyFor(Message incoming, Message reply) throws IOException {
        reply.sessionId = incoming.sessionId;
        reply.mDestNodeInfo = incoming.mSrcNodeInfo;
        super.sendMessage(reply);
    }

    public Message queryFor(Message incoming, Message reply) throws TimeoutException, ServerShutdownException{
        reply.sessionId = incoming.sessionId;
        reply.mDestNodeInfo = incoming.mSrcNodeInfo;
        return super.query(reply);

    }

    /**
     * Make asynchronous queries to all the nodes in the list and return the replies.
     *
     * <b>Note:</b> Even though the request handling is asynchronous, the  call will wait until all the nodes
     * have replied or timeout has occured.
     *
     * @param nodes
     * @param queryMessage
     * @return
     * @throws IOException
     */
    public Collection<Message> startAsyncQueryAll(Collection<NodeInfo> nodes, Message queryMessage) {
        final BulkMessageReceiver receiver=new BulkMessageReceiver(nodes.size());
        final Random random = new Random();
        // copy the list so that there's no inconsistency if calling thread modifies the passed collection.
        for (NodeInfo node: new ArrayList<>(nodes)){
            workerPool.submit(() -> {
                try {
                    receiver.onReceive(super.query(queryMessage,node,random.nextLong()));
                    return;
                } catch (TimeoutException e) {
                    // this is normal to happen
                } catch (ServerShutdownException e) {
                    e.printStackTrace();
                }
                LOGGER.info("Timeout on startAsyncQuery to : "+node.getKey());
                receiver.onTimeout();
            });
        }
        return receiver.getMessages();
    }

    public void asynQueryFor(Message incoming, Message reply, MessageReceiver msgReceiver) throws ServerShutdownException {
        reply.sessionId = incoming.sessionId;
        reply.mDestNodeInfo = incoming.mSrcNodeInfo;
        submitQuerytoPool(reply, msgReceiver);
    }

    public Message startQuery(NodeInfo receiver, Message message) throws TimeoutException, ServerShutdownException {
        message.mDestNodeInfo = receiver;
            message = super.query(message);
            return message;

    }


    public void startQueryAsync(NodeInfo receiver, Message message, final MessageReceiver msgReceiver) throws ServerShutdownException {

        message.mDestNodeInfo = receiver;
        message.sessionId=new Random().nextLong();
        submitQuerytoPool(message, msgReceiver);
    }

    private void submitQuerytoPool(final Message message, final MessageReceiver msgReceiver) throws ServerShutdownException {
        if (workerPool == null || socket.isClosed()) {
            throw new ServerShutdownException();
        }
        workerPool.submit(() -> {
            try {
                msgReceiver.onReceive(super.query(message));
                return;
            } catch (TimeoutException e) {
            } catch (ServerShutdownException e) {
                e.printStackTrace();
            }
            msgReceiver.onTimeout();
        });
    }


    // This method called by the super class when new message needs to be handled.
    @Override
    protected void OnNewMessage(final Message message) {
        final MessageListener listener;
        try {
            listener = ListenerFactory.getListener(message, bucket, this,keyStore);
            workerPool.submit(() -> {
                try {
                    listener.onReceive(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (ListenerFactory.NoListenerException e) {
            LOGGER.warn(message.getClass().getSimpleName() + " : sent by `" + message.mSrcNodeInfo.getKey() + "` received and dropped ");
            return;
        }
        ;
    }

    public boolean shutDown(int timeSeconds) throws InterruptedException {
        if (workerPool.isShutdown()) {
            return false;
        }
        super.stopListening();
        this.workerPool.awaitTermination(timeSeconds, TimeUnit.SECONDS);
        this.workerPool.shutdown();
        this.workerPool = null;
        return true;

    }
    public boolean stop(){
        if(this.socket.isClosed()){
            return false;
        }
        super.stopListening();
        return true;
    }

    /**
     * @return True if server begins starts , False if server was already running
     * @throws SocketException
     */
    public void submitTask(Runnable task){
        this.workerPool.submit(task);
    }
    public boolean start() throws SocketException {
        if (this.workerPool == null) {
            this.workerPool = Executors.newFixedThreadPool(nThreadCount);
        }

        if (this.socket.isClosed()) {
            this.socket = new DatagramSocket(port);
            workerPool.submit(() -> listen());
            return true;
        }
        // socket is created but not started to read
        else if (!this.listening) {
            workerPool.submit(() -> listen());
            return true;
        }

        return false;
    }

    @Override
    protected void onNetworkAddressChange(Key senderKey, InetSocketAddress newSocketAddress) {
        workerPool.submit(() -> nodeInteractionListener.onNetworkAddressChange(senderKey, newSocketAddress));
    }

    @Override
    protected void onNewNodeFound(NodeInfo info) {
        workerPool.submit(() -> nodeInteractionListener.onNewNodeFound(info));
    }

    @Override
    protected void onNewForwardMessage(Message message, NodeInfo destination) {
        if(destination!=null){
            try {
                sendMessage(destination,message);
                LOGGER.info("Received "+message.getClass().getSimpleName()+" to be forwarded to : "+destination.toString());
            } catch (IOException e) {
                LOGGER.info("Forwarding message to `"+destination.toString()+"` failed!");
            }
        }
        workerPool.submit(() -> nodeInteractionListener.onNewForwardMessage(message,destination));
    }

    public void setNodeInteractionListener(NodeInteractionListener listener) {
        this.nodeInteractionListener = listener;
    }

    // the default interaction listener.
    private static NodeInteractionListener getDefaultInteractionListener(ContactBucket b) {
        ContactBucket bucket=b;
        return new NodeInteractionListener() {
            @Override
            public void onNetworkAddressChange(Key senderKey, InetSocketAddress address) {
                NodeInfo senderInfo=bucket.getNode(senderKey);
                LOGGER.warn("Network address  of : " + senderKey.toString()+" changed from "+ senderInfo.getLanAddress().toString() +" to "+address);
                senderInfo.setLanAddress(address);

            }

            @Override
            public void onNewNodeFound(NodeInfo info) {
                //LOGGER.warn("Bucket overflow occured and the contact is ignored : " + info.getKey().toString());

            }
            @Override
            public void onNewForwardMessage(Message message, NodeInfo destination) {
                LOGGER.warn("Message to be forwarded to `"+destination.toString()+"` is dropped");
            }
        };
    }

}
