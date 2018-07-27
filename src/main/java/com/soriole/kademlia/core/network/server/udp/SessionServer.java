package com.soriole.kademlia.core.network.server.udp;

import com.soriole.kademlia.core.KademliaConfig;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.network.ServerShutdownException;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.util.BlockingHashTable2;
import com.soriole.kademlia.core.util.BlockingHashTable4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.TimeoutException;

/**
 *<ol>
 *     <li>Uses the {@link com.soriole.kademlia.core.messages.Message#sessionId} data  and {@link BlockingHashTable2} to deliver message to correct waiting thread or to a default Listener.</li>
 *     <li>Note that the SessionServer itself doesn't start a session. So the Session data needs to be filled up from upper layer.</li>
 *     <li>Doesn't internally use any thread. Thus the The working Threads must be managed by the upper layer</li>
 * </ol>
 *
 * @author github.com/mesudip
 */
abstract class SessionServer extends MessageServer {
    private static Logger logger = LoggerFactory.getLogger(SessionServer.class);
    // for messages type operations
    BlockingHashTable4<Long, Message> incomingMessageTable = new BlockingHashTable4<>(3000);
    protected boolean listening = false;
    protected KademliaConfig config;
    public SessionServer(DatagramSocket socket, ContactBucket bucket, KademliaConfig config) {
        super(socket, bucket);
    }

    public Message sendingInstance(Message message) {
        message.mSrcNodeInfo = bucket.getLocalNode();
        return message;

    }


    protected Message query(Message message, NodeInfo receiver, long sessionId,long timeoutMs) throws TimeoutException, ServerShutdownException {
        if (receiver.equals(bucket.getLocalNode())) {
            logger.warn("Coding Error ! The server is trying to send message to itself!");
            return null;
        }
        // without this reserve line, the thread could get switched just after the sendMessage()
        // thus listening thread would receive the message and see no waiting threads.
        try {
            this.incomingMessageTable.reserverForGet(sessionId);
            super.sendMessage(message, receiver, sessionId);
            return incomingMessageTable.get(sessionId,timeoutMs);
        } catch (IOException e) {
            incomingMessageTable.getIfExists(sessionId);
            throw new ServerShutdownException(e.getClass().getName() + " --> " + e.getMessage());
        }
    }

    protected Message query(Message message,long timeoutMs) throws TimeoutException, ServerShutdownException {
        return query(message, message.mDestNodeInfo, message.sessionId,timeoutMs);
    }


    protected void listen() {
        listening = true;
        logger.info("Started listening on -> " + socket.getLocalSocketAddress().toString());
        while (true) {
            try {

                Message message = super.receiveMessage();
                // if somebody is waiting to receive message
                if (incomingMessageTable.putIfGetterWaiting(message.sessionId, message)) {

                } else {
                    // if nobody is waiting, it got to be handled by default Listeners.
                    this.OnNewMessage(message);
                }
            } catch (IOException e) {
                if (socket.isClosed()) {
                    listening = false;
                    logger.info("Server is shuttting down due to a request from another thread");
                    return;
                }

            } catch (Exception e) {
                logger.warn(String.format("%s : %s", e.getClass().getName(), e.getMessage()));
                StackTraceElement[] traces = e.getStackTrace();
                for (int i = 0; i < 10 && i < traces.length; i++) {
                    logger.warn(traces[i].toString());
                }
            }
        }
    }

    synchronized protected void stopListening() {
        if (!this.socket.isClosed()) {
            this.socket.close();
        }
        listening = false;
    }

    abstract protected void OnNewMessage(Message message);
}
