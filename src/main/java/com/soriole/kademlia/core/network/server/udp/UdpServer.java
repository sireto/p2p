package com.soriole.kademlia.core.network.server.udp;

import com.soriole.kademlia.core.KademliaConfig;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.listeners.ListenerFactory;
import com.soriole.kademlia.core.messages.listeners.MessageListener;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.NodeInteractionListener;
import com.soriole.kademlia.core.store.TimestampedStore;
import com.soriole.kademlia.core.network.ServerShutdownException;
import com.soriole.kademlia.core.network.receivers.BulkMessageReceiver;
import com.soriole.kademlia.core.network.receivers.MessageReceiver;
import com.soriole.kademlia.core.network.MessageDispacher;
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

public class UdpServer extends SessionServer implements MessageDispacher {
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class.getSimpleName());
    NodeInteractionListener nodeInteractionListener;
    int port;
    ExecutorService workerPool = null;
    private TimestampedStore keyStore;

    public UdpServer(KademliaConfig config, ContactBucket bucket, ExecutorService service, TimestampedStore store) throws SocketException {
        super(new DatagramSocket(config.getKadeliaProtocolPort()), bucket, config);
        nodeInteractionListener = getDefaultInteractionListener(bucket);
        this.workerPool = service;
        this.port = socket.getLocalPort();
        this.keyStore = store;
        this.config = config;
    }

    public UdpServer(KademliaConfig config, ContactBucket bucket, TimestampedStore store) throws SocketException {
        this(config, bucket, Executors.newFixedThreadPool(config.getnWorkers()), store);

    }

    @Override
    public void sendMessage(NodeInfo receiver, Message message) throws IOException {
        message.mDestNodeInfo = receiver;
        super.sendMessage(message);
    }

    public void replyFor(Message incoming, Message reply) throws IOException {
        reply.sessionId = incoming.sessionId;
        reply.mDestNodeInfo = incoming.mSrcNodeInfo;
        super.sendMessage(reply);
    }

    public Message queryFor(Message incoming, Message reply) throws TimeoutException, ServerShutdownException, IOException {
        return this.queryFor(incoming, reply, config.getConnectionTimeout());

    }

    @Override
    public Message queryFor(Message incoming, Message reply, long timeoutMs) throws TimeoutException, IOException, ServerShutdownException {
        reply.sessionId = incoming.sessionId;
        reply.mDestNodeInfo = incoming.mSrcNodeInfo;
        return super.query(reply, timeoutMs);
    }

    @Override
    public Collection<Message> startAsyncQueryAll(Collection<NodeInfo> nodes, Message queryMessage) {
        return this.startAsyncQueryAll(nodes, queryMessage, config.getConnectionTimeout());
    }

    /**
     * Make asynchronous queries to all the nodes in the list and return the replies.
     *
     * <b>Note:</b> Even though the request handling is asynchronous, the  call will wait until all the nodes
     * have replied or timeout has occured.
     *
     * @param nodes        Collection of nodes to send message to.
     * @param queryMessage Message to send
     * @return reply from the nodes. //If a node fails to reply, message won't be available and the particular node cannot be determined
     */
    public Collection<Message> startAsyncQueryAll(Collection<NodeInfo> nodes, Message queryMessage, long timeoutMs) {
        final BulkMessageReceiver receiver = new BulkMessageReceiver(nodes.size());
        final Random random = new Random();
        // copy the list so that there's no inconsistency if calling thread modifies the passed collection.
        for (NodeInfo node : new ArrayList<>(nodes)) {
            workerPool.submit(() -> {
                try {
                    receiver.onReceive(super.query(queryMessage, node, random.nextLong(), timeoutMs));
                    return;
                } catch (TimeoutException e) {
                    // this is normal to happen
                } catch (ServerShutdownException e) {
                    LOGGER.warn("Unexpected exception", e);
                }
                LOGGER.info("Timeout on startAsyncQuery to : " + node.getKey());
                receiver.onTimeout();
            });
        }
        return receiver.getMessages();
    }

    public void asynQueryFor(Message incoming, Message reply, MessageReceiver msgReceiver) throws ServerShutdownException {
        this.asynQueryFor(incoming, reply, config.getConnectionTimeout(), msgReceiver);
    }

    @Override
    public void asynQueryFor(Message incoming, Message reply, long timeoutMs, MessageReceiver msgReceiver) throws ServerShutdownException {
        reply.sessionId = incoming.sessionId;
        reply.mDestNodeInfo = incoming.mSrcNodeInfo;
        submitQuerytoPool(reply, msgReceiver, timeoutMs);
    }

    public Message startQuery(NodeInfo receiver, Message message) throws TimeoutException, ServerShutdownException, IOException {
        return this.startQuery(receiver, message, config.getConnectionTimeout());

    }

    @Override
    public Message startQuery(NodeInfo receiver, Message message, long timeoutMs) throws TimeoutException, ServerShutdownException, IOException {
        message.mDestNodeInfo = receiver;
        message = super.query(message, timeoutMs);
        return message;
    }


    public void startQueryAsync(NodeInfo receiver, Message message, final MessageReceiver msgReceiver) throws ServerShutdownException {
        this.startQueryAsync(receiver, message, config.getConnectionTimeout(), msgReceiver);
    }

    @Override
    public void startQueryAsync(NodeInfo receiver, Message message, long timeoutMs, MessageReceiver msgReceiver) throws ServerShutdownException {
        message.mDestNodeInfo = receiver;
        message.sessionId = new Random().nextLong();
        submitQuerytoPool(message, msgReceiver, timeoutMs);
    }

    private void submitQuerytoPool(final Message message, final MessageReceiver msgReceiver, long timeoutMs) throws ServerShutdownException {
        if (workerPool == null || socket.isClosed()) {
            throw new ServerShutdownException();
        }
        workerPool.submit(() -> {
            try {
                msgReceiver.onReceive(super.query(message, timeoutMs));
                return;
            } catch (TimeoutException e) {
                // ignored exception.
            } catch (ServerShutdownException e) {
                LOGGER.warn("Unexpected Exception", e);
            }
            msgReceiver.onTimeout();
        });
    }


    // This method called by the super class when new message needs to be handled.
    @Override
    protected void OnNewMessage(final Message message) {
        final MessageListener listener;
        try {
            listener = ListenerFactory.getListener(message, bucket, this, keyStore);
            workerPool.submit(() -> {
                try {
                    listener.onReceive(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (ListenerFactory.NoListenerException e) {
            LOGGER.warn(message.getClass().getSimpleName() + " : sent by `" + message.mSrcNodeInfo.getKey() + "` received and dropped ");
        }
    }

    public boolean shutDown(int timeSeconds) throws InterruptedException {
        if (workerPool.isShutdown()) {
            return false;
        }
        super.stopListening();
        if (timeSeconds > 0) {
            this.workerPool.awaitTermination(timeSeconds, TimeUnit.SECONDS);
        }
        this.workerPool.shutdown();
        this.workerPool = null;
        return true;

    }

    public boolean stop() {
        if (this.socket.isClosed()) {
            return false;
        }
        super.stopListening();
        return true;
    }

    public void submitTask(Runnable task) {
        this.workerPool.submit(task);
    }

    /**
     * @return True if server begins starts , False if server was already running
     * @throws SocketException
     */
    public synchronized boolean start() throws SocketException {
        if (this.workerPool == null) {
            this.workerPool = Executors.newFixedThreadPool(config.getnWorkers());
        }

        if (this.socket.isClosed()) {
            this.socket = new DatagramSocket(port);

            workerPool.submit(this::listen);
            return true;
        }
        // socket is created but not started to read
        else if (!this.listening) {
            workerPool.submit(this::listen);
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
        if (destination != null) {
            try {
                sendMessage(destination, message);
                LOGGER.info("Received " + message.getClass().getSimpleName() + " to be forwarded to : " + destination.toString());
            } catch (IOException e) {
                LOGGER.info("Forwarding message to `" + destination.toString() + "` failed!");
            }
        }
        workerPool.submit(() -> nodeInteractionListener.onNewForwardMessage(message, destination));
    }

    public void setNodeInteractionListener(NodeInteractionListener listener) {
        this.nodeInteractionListener = listener;
    }

    @Override
    public InetSocketAddress getUsedSocketAddress() {
        return (InetSocketAddress) this.socket.getLocalSocketAddress();
    }

    // the default interaction listener.
    private static NodeInteractionListener getDefaultInteractionListener(ContactBucket b) {
        ContactBucket bucket = b;
        return new NodeInteractionListener() {
            @Override
            public void onNetworkAddressChange(Key senderKey, InetSocketAddress address) {
                NodeInfo senderInfo = bucket.getNode(senderKey);
                LOGGER.warn("Network address  of : " + senderKey.toString() + " changed from " + senderInfo.getLanAddress().toString() + " to " + address);
                senderInfo.setLanAddress(address);

            }

            @Override
            public void onNewNodeFound(NodeInfo info) {
                // ignored

            }

            @Override
            public void onNewForwardMessage(Message message, NodeInfo destination) {
                LOGGER.warn("Message to be forwarded to `" + destination.toString() + "` is dropped");
            }
        };
    }

}
