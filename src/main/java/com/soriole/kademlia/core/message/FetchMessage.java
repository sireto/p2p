package com.soriole.kademlia.core.message;

import com.soriole.kademlia.core.Key;
import com.soriole.kademlia.core.NodeInfo;

public class FetchMessage extends MessageWithKnownRecipient {
  private static final long serialVersionUID = 1L;

  private final Key mKey;

  public FetchMessage(NodeInfo srcNodeInfo, NodeInfo destNodeInfo, Key key) {
    super(srcNodeInfo, destNodeInfo);
    mKey = key;
  }

  public Key getKey() {
    return mKey;
  }
}
