package com.soriole.kademlia.core.messages;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.TimeStampedData;
import com.soriole.kademlia.network.KademliaNetworkMessageProtocol;

import java.util.Comparator;

@MessageType(type=6)
public class DataMessage extends Message{
    public Key key;
    public byte[] value;
    public long expirationTime=-1;
    public long updatedtime=-1;

    @Override
    public byte[] writeToBytes() {
        KademliaNetworkMessageProtocol.DataMessage.Builder builder=KademliaNetworkMessageProtocol.DataMessage.newBuilder();

        builder.setKey(ByteString.copyFrom(key.toBytes()));
        builder.setValue(ByteString.copyFrom(value));
        builder.setExpirationTime(expirationTime);
        if(updatedtime>0){
            builder.setUpdateTime(updatedtime);
        }

        return builder.build().toByteArray();
    }

    @Override
    public void readFromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        KademliaNetworkMessageProtocol.DataMessage message=KademliaNetworkMessageProtocol.DataMessage.parseFrom(bytes);

        this.key=new Key(message.getKey().toByteArray());
        this.value=message.getValue().toByteArray();
        this.expirationTime=message.getExpirationTime();
        if(message.hasUpdateTime()){
            updatedtime=message.getUpdateTime();
        }

    }
    public static Comparator<DataMessage> getComparatorByUpdateTime(){
        return (o1, o2) -> Long.compare(o1.updatedtime,o2.updatedtime);
    }
    public TimeStampedData<byte[]> toTimeStampedData(){
        return new TimeStampedData<>(value,expirationTime,updatedtime);
    }
}
