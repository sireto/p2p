package com.soriole.kademlia.core;

import com.soriole.kademlia.core.message.*;

public class WaitingForMessageListener implements MessageListener {
  private Message mMsg;
  private boolean mIsWaiting = false;

  public synchronized void initializeCatchingTheNextMessage() {
    mIsWaiting = true;
  }

  @Override
  public FindNodeReplyMessage receiveFindNodeMessage(FindNodeMessage msg) {
    checkWaiting(msg);
    return null;
  }

  @Override
  public PongMessage receiveGetKeyMessage(GetKeyMessage msg) {
    checkWaiting(msg);
    return null;
  }

  @Override
  public StoreReplyMessage receiveStoreMessage(StoreMessage msg) {
    checkWaiting(msg);
    return null;
  }

  @Override
  public Message receiveFetchMessage(FetchMessage msg) {
    checkWaiting(msg);
    return null;
  }

  @Override
  public synchronized PongMessage receivePingMessage(PingMessage msg) {
    checkWaiting(msg);
    return null;
  }

  public synchronized Message waitForMessage() throws InterruptedException {
    if (!mIsWaiting && mMsg == null) {
      throw new IllegalStateException("Listener is not waiting for ping.");
    }
    while (mMsg == null) {
      this.wait();
    }
    Message msg = mMsg;
    mMsg = null;
    return msg;
  }

  private synchronized void checkWaiting(Message msg) {
    if (mIsWaiting) {
      mMsg = msg;
      mIsWaiting = false;
      this.notifyAll();
    }
  }
}
