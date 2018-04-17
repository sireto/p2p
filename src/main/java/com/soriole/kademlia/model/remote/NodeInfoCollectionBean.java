package com.soriole.kademlia.model.remote;

public final class NodeInfoCollectionBean {
  private NodeInfoBean[] mNodeInfos;

  public NodeInfoBean[] getNodeInfo() {
    return mNodeInfos;
  }

  public void setNodeInfo(NodeInfoBean[] nodeInfos) {
    mNodeInfos = nodeInfos;
  }
}
