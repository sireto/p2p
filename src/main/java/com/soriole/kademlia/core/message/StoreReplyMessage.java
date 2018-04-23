package com.soriole.kademlia.core.message;

import com.soriole.kademlia.core.NodeInfo;

public class StoreReplyMessage extends MessageWithKnownRecipient {
  private static final long serialVersionUID = 1L;

  private final boolean success;

  public StoreReplyMessage(NodeInfo srcNodeInfo, NodeInfo destNodeInfo,
                           boolean stored) {
    super(srcNodeInfo, destNodeInfo);
    success = stored;
  }

  public boolean isSuccess(){
    return success;
  }
}
