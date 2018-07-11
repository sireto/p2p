package com.soriole.kademlia.core.messages;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.network.KademliaNetworkMessageProtocol;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
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
        LoggerFactory.getLogger("MessageFactory").info("Message Types : " + typeToClass.toString());
        return typeToClass;
    }

    public static Message createUninitialized(int type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class messageClass = typeToClass.get(type);
        return (Message) messageClass.getConstructor().newInstance(new Object[]{});
    }


    public static Message createInstance(byte[] serializedProto, InetSocketAddress senderAddress) throws InstantiationException, InvalidProtocolBufferException {
        return createInstance(KademliaNetworkMessageProtocol.Message.parseFrom(serializedProto), senderAddress);

    }

    public static Message createInstance(KademliaNetworkMessageProtocol.Message messageProto, InetSocketAddress senderAddress) throws InstantiationException, InvalidProtocolBufferException {
        Message message;
        try {
            message = MessageFactory.createUninitialized(messageProto.getType());

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new InstantiationException(e.getMessage());
        }
        Key senderKey = new Key(messageProto.getSender().toByteArray());

        // get the sender's socketAddress info from the datagramPacket
        message.mSrcNodeInfo = new NodeInfo(senderKey, senderAddress);

        // if the message has set session id, set it.
        if (messageProto.hasSessionId()) {
            message.sessionId = messageProto.getSessionId();
        }
        // if the receiver is set, fill the receiver value
        if (messageProto.hasReceiver()) {
            message.mDestNodeInfo = new NodeInfo(new Key(messageProto.getReceiver().toByteArray()));
        }
        // if the message has message data read it too.
        if (messageProto.hasMessageData()) {
            message.readFromBytes(messageProto.getMessageData().toByteArray());
        }
        return message;

    }

    public static KademliaNetworkMessageProtocol.Message toProtoInstance(Message message) throws IllegalArgumentException {

        if (message.mSrcNodeInfo == null) {
            throw new IllegalArgumentException("Message Instance is missing mSrcNodeInfo field");
        }

        KademliaNetworkMessageProtocol.Message.Builder builder = KademliaNetworkMessageProtocol.Message.newBuilder();
        builder.setSender(ByteString.copyFrom(message.mSrcNodeInfo.getKey().toBytes()))
                .setSessionId(message.sessionId)
                .setType(message.getClass().getAnnotation(MessageType.class).type());
        if (message.mDestNodeInfo != null) {
            if (message.mDestNodeInfo.getKey() != null) {
                builder.setReceiver(ByteString.copyFrom(message.mDestNodeInfo.getKey().toBytes()));
            }
        }

        byte[] data = message.writeToBytes();
        if (data != null) {
            builder.setMessageData(ByteString.copyFrom(data));
        }
        return builder.build();
    }

    static public void main(String[] args) {
        System.err.println(MessageFactory.getMesageTypes());

    }

    static public Collection<Class<? extends Message>> getMessageTypes() {
        return typeToClass.values();
    }
}
