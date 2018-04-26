package com.soriole.kademlia.core.messages.listeners;

import com.soriole.kademlia.core.messages.DataMessage;
import com.soriole.kademlia.core.messages.DataReplyMessage;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.NodeListMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@ListenerType(messageClass = DataMessage.class)
public class DataMessageListener extends MessageListener{
    static Logger logger= LoggerFactory.getLogger(DataMessageListener.class.getSimpleName());

    // on receiving a DataMessage, we store the data.
    @Override
    public void onReceive(Message m) throws Exception {
        DataMessage message=(DataMessage) m;

            if(message.updatedtime>0){
                logger.info("Dropped -- contains updatedTime");
                return;
            }
            // verify the expiration is not already over.
            if(message.expirationTime <= new Date().getTime()) {
                logger.info("Dropped -- already expired");
                return;
            }



            keyStore.put(message.key,message.value,message.expirationTime);

            // success parameter [Currently a node always saves when requested.]
            boolean success=true;

            // after you put a value, send a DataReplyMessage.
            DataReplyMessage reply= new DataReplyMessage();
            reply.success =success;
            reply.nearerNodes=bucket.getClosestNodes(message.key);
            server.replyFor(message,reply);

    }
}
