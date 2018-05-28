package com.soriole.kademlia.model.remote;

import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;

import java.net.InetSocketAddress;

public final class NodeInfoBean {
  private String mKey;
  private String mInetAddress;
  private int mPort;

  public static NodeInfoBean fromNodeInfo(NodeInfo info) {
    NodeInfoBean bean = new NodeInfoBean();
    bean.setKey(info.getKey().toString());
    bean.setInetAddress(info.getLanAddress().getHostName());
    bean.setPort(info.getLanAddress().getPort());
    return bean;
  }

  public String getInetAddress() {
    return mInetAddress;
  }

  public String getKey() {
    return mKey;
  }

  public int getPort() {
    return mPort;
  }

  public void setInetAddress(String inetAddress) {
    mInetAddress = inetAddress;
  }

  public void setKey(String key) {
    mKey = key;
  }

  public void setPort(int port) {
    mPort = port;
  }

  public NodeInfo toNodeInfo() {
    return new NodeInfo(new Key(mKey), new InetSocketAddress(mInetAddress, mPort));
  }

}
