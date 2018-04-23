package com.soriole.kademlia.core.message;

import com.soriole.kademlia.core.NodeInfo;

import java.io.Serializable;

public abstract class Message implements Serializable {
  private static final long serialVersionUID = 1L;

  private final NodeInfo mSrcNodeInfo;

  Message(NodeInfo src) {
    mSrcNodeInfo = src;
  }

  public NodeInfo getSourceNodeInfo() {
    return mSrcNodeInfo;
  }
}
