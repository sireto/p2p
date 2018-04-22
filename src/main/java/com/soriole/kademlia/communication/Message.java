package com.soriole.kademlia.communication;

public class Message {

    private byte[] uidSender, uidReceiver,  sessionKey, messageByte, mac;
    // Message type
    public enum MessageType {
        Client2P,
        P2P,
        P2Client
    };

    private MessageType messageType;

    Message(MessageType type, byte[] uidSender, byte[] uidReceiver, byte[] sessionKey, byte[] messageByte, byte[] mac) {
        this.uidSender = uidSender;
        this.uidReceiver = uidReceiver;
        this.sessionKey = sessionKey;
        this.messageByte = messageByte;
        this.mac = mac;
        this.messageType = type;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public byte[] getUidSender() {

        return uidSender;
    }

    public void setUidSender(byte[] uidSender) {
        this.uidSender = uidSender;
    }

    public byte[] getUidReceiver() {
        return uidReceiver;
    }

    public void setUidReceiver(byte[] uidReceiver) {
        this.uidReceiver = uidReceiver;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public byte[] getMessageByte() {
        return messageByte;
    }

    public void setMessageByte(byte[] messageByte) {
        this.messageByte = messageByte;
    }

    public byte[] getMac() {
        return mac;
    }

    public void setMac(byte[] mac) {
        this.mac = mac;
    }
}
