package com.soriole.kademlia.core.message;

import com.soriole.kademlia.core.NodeInfo;

public class PongMessage extends MessageWithKnownRecipient {
  private static final long serialVersionUID = 1L;

  public PongMessage(NodeInfo srcNodeInfo, NodeInfo destNodeInfo) {
    super(srcNodeInfo, destNodeInfo);
  }
}
