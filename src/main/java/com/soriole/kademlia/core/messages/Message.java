package com.soriole.kademlia.core.messages;

import com.google.protobuf.InvalidProtocolBufferException;
import com.soriole.kademlia.core.store.NodeInfo;

import java.io.Serializable;

@MessageType(type=0)
public abstract class Message implements Serializable {
  private static final long serialVersionUID = 1L;

  public NodeInfo mSrcNodeInfo;
  transient public NodeInfo mDestNodeInfo;
  public transient long sessionId;
  public NodeInfo getSourceNodeInfo() {
    return mSrcNodeInfo;
  }

  abstract public byte[] writeToBytes();
  abstract public void readFromBytes(byte[] bytes) throws InvalidProtocolBufferException;
}
