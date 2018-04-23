package com.soriole.kademlia.network;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.listeners.ListenerFactory;
import com.soriole.kademlia.core.messages.listeners.MessageListener;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.network.server.SessionServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.*;

public class KademliaMessageServer extends SessionServer {
    int port;
    private static final int nThreadCount = 10;
    ExecutorService workerPool=null;

    public void setAlpha(int a) {
    }

    public KademliaMessageServer(int listeningPort, ContactBucket bucket) throws SocketException {
        super(new DatagramSocket(listeningPort), bucket);

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

    public Message queryFor(Message incoming, Message reply) throws TimeoutException, IOException {
        reply.sessionId = incoming.sessionId;
        reply.mDestNodeInfo = incoming.mSrcNodeInfo;
        return super.query(reply);

    }

    public void asynQueryFor(Message incoming, Message reply, MessageReceiver msgReceiver) throws ServerShutdownException {
        reply.sessionId = incoming.sessionId;
        reply.mDestNodeInfo = incoming.mSrcNodeInfo;
        submitQuerytoPool(reply, msgReceiver);
    }

    public Message startQuery(NodeInfo receiver, Message message) throws TimeoutException, IOException{
        message.mDestNodeInfo = receiver;
        message.sessionId = new Random().nextLong();

        message = super.query(message);
        return message;
    }


    public void startQueryAsync(NodeInfo receiver, Message message, final MessageReceiver msgReceiver) throws ServerShutdownException {

        message.mDestNodeInfo = receiver;
        message.sessionId = new Random().nextLong();
        submitQuerytoPool(message, msgReceiver);
    }

    private void submitQuerytoPool(final Message message, final MessageReceiver msgReceiver) throws ServerShutdownException {
        if (workerPool == null || socket.isClosed()) {
            throw new ServerShutdownException();
        }
        workerPool.submit(() -> {
            try {
                msgReceiver.onReceive(super.query(message));
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
            listener = ListenerFactory.getListener(message, bucket, this);
            workerPool.submit(() -> {
                try {
                    listener.onReceive(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (ListenerFactory.NoListenerException e) {
            return;
        }
        ;
    }

    public boolean stop(int timeSeconds) throws InterruptedException {
        if(workerPool.isShutdown()){
            return false;
        }
        super.stopListening();
        this.workerPool.awaitTermination(timeSeconds, TimeUnit.SECONDS);
        this.workerPool.shutdown();
        this.workerPool=null;
        return true;

    }

    /**
     *
     * @return True if server begins starts , False if server was already running
     * @throws SocketException
     */

    public boolean start() throws SocketException {
        if (this.workerPool==null) {
            this.workerPool = Executors.newFixedThreadPool(nThreadCount);
        }
        if (this.socket.isClosed() || !this.socket.isConnected()) {
            this.socket=new DatagramSocket(port);
            workerPool.submit(() -> listen());
            return true;
        }

        return false;
    }

    public void pause() {
        super.stopListening();
    }

    @Override
    protected void onNetworkAddressChange(Key key, InetSocketAddress address) {
        // TODO: LET'S IGNORE THIS NOW.
    }

    @Override
    protected void onNewNodeFound(NodeInfo info) {
        // TODO: Important. A node unknown to us somehow managed to contact us.
    }
}
