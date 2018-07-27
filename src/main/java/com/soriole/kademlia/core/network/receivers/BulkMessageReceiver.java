package com.soriole.kademlia.core.network.receivers;

import com.soriole.kademlia.core.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Receives and put (count) no of message
 * And returns the list only after all the messages are either received or timeout has occcured.
 * xTODO: Implement with a blocking queue so that messages can be processed without waiting for others.
 *
 * @author github.com/mesudip
 */
public class BulkMessageReceiver implements MessageReceiver {
    static Logger logger = LoggerFactory.getLogger(BulkMessageReceiver.class.getSimpleName());
    ArrayList<Message> receivedMessages;
    int counter;

    public BulkMessageReceiver(int count) {
        counter = count;
        this.receivedMessages = new ArrayList<>(count);
    }

    @Override
    synchronized public void onReceive(Message message) {

        if (--counter == 0) {
            receivedMessages.add(message);
            notify();
        } else if (counter < 0) {
            logger.warn(Message.class.getSimpleName() + " Received after Timeout");
        }

    }

    @Override
    synchronized public void onTimeout() {
        if (--counter <= 0) {
            notify();
        }
    }

    synchronized public ArrayList<Message> getMessages() {
        while (counter > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                // put the interrupted flag back.
                Thread.currentThread().interrupt();
            }
        }

        ArrayList<Message> msgs = this.receivedMessages;
        this.receivedMessages = null;
        return msgs;

    }
}
