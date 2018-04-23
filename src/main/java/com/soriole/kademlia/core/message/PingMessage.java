package com.soriole.kademlia.core.message;

import com.soriole.kademlia.core.NodeInfo;

public class PingMessage extends MessageWithKnownRecipient {
  private static final long serialVersionUID = 1L;

  public PingMessage(NodeInfo srcNodeInfo, NodeInfo destNodeInfo) {
    super(srcNodeInfo, destNodeInfo);
  }
}
