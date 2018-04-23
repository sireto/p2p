package com.reversehash.communication;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.reversehash.communication.message.KeyExchangeMessage;
import com.reversehash.communication.message.Message;
import com.reversehash.communication.message.MessageType;
import com.reversehash.communication.message.TextMessage;
import com.reversehash.crypto.*;

import javax.xml.bind.ValidationException;

import java.util.Random;

public class MessageFactory {
    KeyFactory factory;

    public MessageFactory(KeyFactory factory) {
        this.factory = factory;

//        Reflections reflections=new Reflections("com.com.reversehash.communication.messages");
//        Set<Class<?>> messageTypes=reflections.getTypesAnnotatedWith(MessageType.class);

    }


    public Message  handleMessage(MessageSerializationProtocol.Message message) throws ValidationException, InvalidProtocolBufferException {
        byte[] checksum = message.getChecksum().toByteArray();
        byte[] messageData = message.getMessageByte().toByteArray();
        byte[] sender = message.getSender().toByteArray();
        byte[] secret = factory.getSecret(sender, message.getIndex());

        MAC mac = new MAC(secret);
        if (!mac.addData(messageData).addData(sender).verify(checksum)) {
            return null;
        }
        messageData = AESOutputStream.directDecrypt(secret, messageData);
        if (messageData == null) {
            return null;
        }

        MessageSerializationProtocol.MessageData data= MessageSerializationProtocol.MessageData.parseFrom(messageData);


        MessageType textType=TextMessage.class.getAnnotation(MessageType.class);
        MessageType keyExchangeType=KeyExchangeMessage.class.getAnnotation(MessageType.class);

        int type=data.getType();

        Message _message=null;
        if (type==textType.type()) {
            _message = new TextMessage(null);
        }
        else if (type==keyExchangeType.type()){
            _message = new TextMessage(null);

        }

        _message.readBytes(data.getVersion(),data.getMessage().toByteArray());
        _message.sender = message.getSender().toByteArray();
        _message.receiver = message.getReceiver().toByteArray();
        _message.handle();
        return _message;
    }


    public MessageSerializationProtocol.Message createMessage(Message message) {
        int index=new Random().nextInt();
        if (index<0)
            index=-index;

        byte[] secret = factory.getSecret(message.receiver,index);
        MessageType messageType=message.getClass().getAnnotation(MessageType.class);

        MessageSerializationProtocol.MessageData data = MessageSerializationProtocol.MessageData.newBuilder()
                .setType(messageType.type())
                .setVersion(messageType.version())
                .setMessage(ByteString.copyFrom(message.toBytes())).build();

        byte[] _message = AESInputStream.directEncrypt  (secret, data.toByteArray());
        byte[] mac = new MAC(secret).addData(_message).getMac(message.sender);

        MessageSerializationProtocol.Message.Builder builder = MessageSerializationProtocol.Message.getDefaultInstance().toBuilder();
        builder.setSender(ByteString.copyFrom(message.sender))
                .setReceiver(ByteString.copyFrom(message.receiver))
                .setChecksum(ByteString.copyFrom(mac))
                .setMessageByte(ByteString.copyFrom(_message))
                .setIndex(index);


        return builder.build();
    }

}
