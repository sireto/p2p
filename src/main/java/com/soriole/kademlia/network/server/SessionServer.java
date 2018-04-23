package com.soriole.kademlia.network.server;


import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.util.BlockingHashTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 *
 * Uses the sessionID data to deliver message to correct waiting thread or to a default Listener.
 * Note that the SessionServer itself doesn't startAsync a session. So that needs to be done from upper layer.
 *
 * Doesn't internally use any thread. Thus the The concurrency must be managed by the class that implements it.
 *
 */
public  abstract class SessionServer extends MessageServer {
    private static Logger logger= LoggerFactory.getLogger(SessionServer.class);
    // for messages type operations
    BlockingHashTable<Long, Message> incomingMessageTable=new BlockingHashTable<>(3000);

    Timer timer = new Timer(true);

    public SessionServer(DatagramSocket socket, ContactBucket bucket){
        super(socket,bucket);
    }
    public Message sendingInstance(Message message) {
        message.mSrcNodeInfo=bucket.getLocalNode();
        return message;

    }


    protected Message query(Message message) throws TimeoutException, IOException{
        super.sendMessage(message);
        return incomingMessageTable.get(message.sessionId);
    }

    protected void listen(){
        while (true) {
            try {

                Message message = super.receiveMessage();
                // if somebody is waiting to receive message
                if (incomingMessageTable.putIfGetterWaiting(message.sessionId, message)) {

                } else {
                    // if nobody is waiting, it got to be handled by default Listeners.
                    OnNewMessage(message);
                }
            }
            catch (IOException e){
                if(socket.isClosed()) {
                    logger.info("Server is shuttting down due to a request from another thread");
                    return;
                }

            } catch (Exception e) {
                logger.warn(e.getClass().getName()+" : "+e.getMessage());
                StackTraceElement[] traces=e.getStackTrace();
                for(int i=0;i<5 && i<traces.length;i++) {
                    logger.warn(traces[i].toString());
                }

            }

        }
    }
    protected void stopListening(){
       this.socket.close();
    }
    abstract protected void OnNewMessage(Message message);
}
