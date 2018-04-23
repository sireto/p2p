package com.soriole.kademlia.core.messages.listeners;

import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.network.KademliaMessageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class ListenerFactory {
    private static Logger logger= LoggerFactory.getLogger(ListenerFactory.class);
    public static class NoListenerException extends Exception {
    }

    // map from message to listener.
    static Map<Class<? extends Message>, Class<? extends MessageListener>> messageToListener=getListenerTypes();

    static private Map<Class<? extends Message>, Class<? extends MessageListener>> getListenerTypes() {
        Map<Class<? extends Message>, Class<? extends MessageListener>> messageToListener=new HashMap<>();
        org.reflections.Reflections reflections = new org.reflections.Reflections("com.soriole.kademlia.core.messages.listeners");

        // all the subclasses fo Message.class

        Set<Class<?>> allListenerTypes = reflections.getTypesAnnotatedWith(ListenerType.class);
        Set<Class<? extends MessageListener>> allListeners = reflections.getSubTypesOf(MessageListener.class);

        for (Class listenerClass : allListeners) {
            if (allListenerTypes.contains(listenerClass) && listenerClass.isAnnotationPresent(ListenerType.class)) {
                Class<? extends Message> msgClass = ((ListenerType) listenerClass.getAnnotation(ListenerType.class)).messageClass();
                messageToListener.put(msgClass,listenerClass);
            }
        }
        LoggerFactory.getLogger("ListenerFactory").info("ListenerClasses: "+messageToListener.toString());
        return messageToListener;
    }


    public static MessageListener getListener(Message message, ContactBucket bucket, KademliaMessageServer server)
            throws NoListenerException {
        Class listenerClass = messageToListener.get(message.getClass());
        try {
             MessageListener listener= (MessageListener)listenerClass.getConstructor().newInstance(new Object[]{});
             listener.bucket=bucket;
             listener.server=server;
             return listener;
        } catch (Exception e) {
            logger.warn(e.getClass().getName()+" : "+e.getMessage());
            throw new NoListenerException();
        }
    }

    static public Collection<Class<?extends MessageListener>> getListeners(){
        return messageToListener.values();
    }
}
