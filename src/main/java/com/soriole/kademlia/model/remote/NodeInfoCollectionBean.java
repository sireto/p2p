package com.soriole.kademlia.model.remote;

import com.soriole.kademlia.core.store.NodeInfo;

import java.util.ArrayList;
import java.util.Collection;

public final class NodeInfoCollectionBean {
  private NodeInfoBean[] mNodeInfos;

  public NodeInfoBean[] getNodeInfo() {
    return mNodeInfos;
  }

  public void setNodeInfo(NodeInfoBean[] nodeInfos) {
    mNodeInfos = nodeInfos;
  }


  public static NodeInfoCollectionBean fromNodeInfoCollection(Collection<NodeInfo> e){
    NodeInfoCollectionBean bean=new NodeInfoCollectionBean();
    bean.mNodeInfos=new NodeInfoBean[e.size()];
    int i=0;
    for(NodeInfo n:e){
      bean.mNodeInfos[i++]=NodeInfoBean.fromNodeInfo(n);
    }
    return bean;
  }
}
