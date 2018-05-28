package com.soriole.kademlia.core.messages;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * The task of the MessageFactory Class is to craete message class instance based on integer type identifier.
 */
public abstract class MessageFactory {

    static Map<Integer, Class<? extends Message>> typeToClass = getMesageTypes();

    static Map<Integer, Class<? extends Message>> getMesageTypes() {
        Map<Integer, Class<? extends Message>> typeToClass = new Hashtable<>(100);
        org.reflections.Reflections reflections = new org.reflections.Reflections("com.soriole.kademlia.core.messages");

        // all the subclasses fo Message.class
        Set<Class<? extends Message>> allMessageClasses = reflections.getSubTypesOf(Message.class);
        Set<Class<?>> allMessageTypes = reflections.getTypesAnnotatedWith(MessageType.class);

        for (Class _class : allMessageTypes) {
            if (allMessageClasses.contains(_class) && _class.isAnnotationPresent(MessageType.class)) {
                int type = ((MessageType) _class.getAnnotation(MessageType.class)).type();
                typeToClass.put(type, _class);

            }
        }
        LoggerFactory.getLogger("MessageFactory").info("Message Types : "+typeToClass.toString());
        return typeToClass;
    }

    public static Message createMessage(int type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class messageClass = typeToClass.get(type);
        return (Message) messageClass.getConstructor().newInstance(new Object[]{});
    }

    static public void main(String[] args) {
        System.err.println(MessageFactory.getMesageTypes());

    }

    static public Collection<Class<? extends Message>> getMessageTypes() {
        return typeToClass.values();
    }
}
