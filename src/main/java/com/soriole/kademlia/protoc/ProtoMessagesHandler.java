package com.soriole.kademlia.protoc;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.soriole.kademlia.communication.Message;
import com.soriole.kademlia.crypto.MAC;

import java.io.IOException;
import java.io.OutputStream;

public class ProtoMessagesHandler {



    public ProtoMessage.MessageWrapper createMessage(Message message) {

        // Getting the protocol buffer builder class
        ProtoMessage.MessageWrapper.Builder builder = ProtoMessage.MessageWrapper.getDefaultInstance().toBuilder();

        // Setting the values from message to their corresponding messagewrapper class from protocol buffer
        switch (message.getMessageType()){
            case Client2P:
                builder.setMessageType(ProtoMessage.MessageWrapper.MessageType.Client2P);
                break;
            case P2P:
                builder.setMessageType(ProtoMessage.MessageWrapper.MessageType.P2P);
                break;
            case P2Client:
                builder.setMessageType(ProtoMessage.MessageWrapper.MessageType.P2Client);
                break;
        }
        builder.setUidSender(ByteString.copyFrom(message.getUidSender()))
                .setUidReceiver(ByteString.copyFrom(message.getUidReceiver()))
                .setSessionKey(ByteString.copyFrom(message.getSessionKey()))
                .setMessageByte(ByteString.copyFrom(message.getMessageByte()))
                .setMac(ByteString.copyFrom(message.getMac()));
        if (builder.isInitialized()) {
            // Building the immutable messagewrapper and returning
            return builder.build();
        }
        else
            return null;
    }

    public Message handleMessage(ProtoMessage.MessageWrapper messageWrapper) throws InvalidProtocolBufferException {

        Message message = null;
        switch(messageWrapper.getMessageType()){
            case Client2P:
                message.setMessageType(Message.MessageType.Client2P);
                break;
            case P2P:
                message.setMessageType(Message.MessageType.P2P);
                break;
            case P2Client:
                message.setMessageType(Message.MessageType.P2Client);
                break;
        }
        message.setUidSender(messageWrapper.getUidSender().toByteArray());
        message.setUidReceiver(messageWrapper.getUidReceiver().toByteArray());
        message.setSessionKey(messageWrapper.getSessionKey().toByteArray());
        message.setMessageByte(messageWrapper.getMessageByte().toByteArray());
        message.setMac(messageWrapper.getMac().toByteArray());
        return message;
    }

    public void sendMessage(OutputStream outputStream, ProtoMessage.MessageWrapper messageWrapper) throws IOException {
        ProtoMessage.MessageWrapper.Builder builder = ProtoMessage.MessageWrapper.getDefaultInstance().toBuilder();
        messageWrapper.writeTo(outputStream);
    }
}
