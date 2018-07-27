package com.soriole.kademlia.core.network.server.tcpsocket;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.MessageFactory;
import com.soriole.kademlia.core.network.KademliaNetworkMessageProtocol;
import com.soriole.kademlia.core.network.receivers.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.TimeoutException;

class SocketWatcher implements Runnable {
    static final Logger logger = LoggerFactory.getLogger("");
    Socket socket;
    MessageReceiver receiver;

    SocketWatcher(Socket socket, MessageReceiver receiver) {
        this.socket = socket;
        this.receiver = receiver;
    }

    Set<Long> waiting = new HashSet<>();
    Hashtable<Long, Message> replies = new Hashtable<>();

    @Override
    public void run() {
        String listeningAddress=String.valueOf(socket.getLocalPort())+" <- "+((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress()+":"+((InetSocketAddress)socket.getRemoteSocketAddress()).getPort();
        Thread.currentThread().setName("SocketWatcer : "+listeningAddress);
        while (socket.isConnected()) {
            try {
                KademliaNetworkMessageProtocol.Message message = KademliaNetworkMessageProtocol.Message.parseDelimitedFrom(socket.getInputStream());
                Message recMessage = MessageFactory.createInstance(message, (InetSocketAddress) socket.getRemoteSocketAddress());
                logger.debug("New message of type :" + recMessage.getClass().getSimpleName() + " received from : " + recMessage.getSourceNodeInfo().toDetailString());
                synchronized (this) {
                    if (waiting.contains(message.getSessionId())) {
                        replies.put(message.getSessionId(),recMessage);
                        notify();
                        continue;

                    }
                }

                receiver.onReceive(recMessage);
            } catch (IOException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        logger.info("watch loop ended!");

    }

    public synchronized Message query(Message message, long timeoutMs) throws IOException, TimeoutException {
        waiting.add(message.sessionId);

        logger.debug("Sent query message of type " + message.getClass().getSimpleName() + " to : " +socket.getInetAddress().toString());
        MessageFactory.toProtoInstance(message).writeDelimitedTo(socket.getOutputStream());

        long newTime = timeoutMs;
        long startTime = System.currentTimeMillis();
        while (newTime > 0) {
            try {
                wait(newTime);
                if (replies.containsKey(message.sessionId)) {
                    waiting.remove(message.sessionId);
                    return replies.remove(message.sessionId);
                }
                newTime = timeoutMs - (System.currentTimeMillis() - startTime);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Unexpected Interrupt on SocketWatcher thread");
                throw new TimeoutException("Thread was interrupted before timeout");
            }
        }
        waiting.remove(message.sessionId);
        throw new TimeoutException("Timeout waiting for message");
    }


}
