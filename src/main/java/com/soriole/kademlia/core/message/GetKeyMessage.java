package com.soriole.kademlia.core.message;

import com.soriole.kademlia.core.NodeInfo;

public class GetKeyMessage extends Message {
  private static final long serialVersionUID = 1L;

  public GetKeyMessage(NodeInfo localNodeInfo) {
    super(localNodeInfo);
  }
}
