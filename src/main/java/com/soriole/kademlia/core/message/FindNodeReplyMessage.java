package com.soriole.kademlia.core.message;

import com.soriole.kademlia.core.NodeInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class FindNodeReplyMessage extends MessageWithKnownRecipient {
  private static final long serialVersionUID = 1L;

  private final LinkedList<NodeInfo> mFoundNodes;

  public FindNodeReplyMessage(NodeInfo srcNodeInfo, NodeInfo destNodeInfo,
      Collection<NodeInfo> foundNodes) {
    super(srcNodeInfo, destNodeInfo);
    mFoundNodes = new LinkedList<NodeInfo>(foundNodes);
  }

  public Collection<NodeInfo> getFoundNodes() {
    return new ArrayList<>(mFoundNodes);
  }
}
