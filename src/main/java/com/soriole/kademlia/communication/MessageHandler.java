package com.soriole.kademlia.communication;

import com.google.protobuf.InvalidProtocolBufferException;
import com.soriole.kademlia.core.Key;
import com.soriole.kademlia.crypto.AESEncryption;
import com.soriole.kademlia.crypto.MAC;
import com.soriole.kademlia.crypto.RSAEncryption;
import com.soriole.kademlia.protoc.ProtoMessage;
import com.soriole.kademlia.protoc.ProtoMessage.MessageWrapper;
import com.soriole.kademlia.protoc.ProtoMessagesHandler;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class MessageHandler {

    ProtoMessagesHandler protoMessagesHandler = new ProtoMessagesHandler();
    RSAEncryption rsaengine = new RSAEncryption();
    AESEncryption aesEngine = new AESEncryption();
    MAC macEngine = new MAC();
    String sessionPassword = "Session Password";
    String macPassword = "Mac Password";

    public void sendMessage(String m, Key key){
        Message message;
        MessageWrapper serializedMessage;
        try {
            byte[] sender = "me".getBytes();
            byte[] receiver = "you".getBytes();
            byte[] messageByte = aesEngine.encrypt(sessionPassword, m);
            byte[] sessionKey = rsaengine.encrypt(sessionPassword, rsaengine.getMyPublicKey());
            byte[] mac = macEngine.giveMeMAC(macPassword, messageByte);
            Message.MessageType type = Message.MessageType.P2P;
            message = new Message(type, sender, receiver, sessionKey, messageByte, mac);
            serializedMessage = serialize(message);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void handle(MessageWrapper messageWrapper){
        Message message = unSerialize(messageWrapper);
    }


    public Message createMessage(byte[] uidSender, byte[] uidReceiver, byte[] sessionKey, byte[] messageByte, byte[] mac, Message.MessageType type) {
        Message message = new Message(type, uidSender, uidReceiver, sessionKey, messageByte, mac);
        return message;
    }

    private MessageWrapper serialize(Message message){
        MessageWrapper messageWrapper = protoMessagesHandler.createMessage(message);
        return messageWrapper;

    }

    // Immediate function to call after a messageWrapper is recieved
    private Message unSerialize(MessageWrapper messageWrapper){
        try {
            Message message = protoMessagesHandler.handleMessage(messageWrapper);
        } catch (InvalidProtocolBufferException e) {
            // Handle messageWrapper errors here
            e.printStackTrace();
        }

        return null;
    }
}
