package com.soriole.kademlia.core;

import com.soriole.kademlia.core.message.*;

/**
 * {@link MessageListener} which does nothing and does not respond.
 *
 * @author Grzegorz Milka
 */
public class IdleMessageListener implements MessageListener {
  @Override
  public FindNodeReplyMessage receiveFindNodeMessage(FindNodeMessage msg) {
    return null;
  }

  @Override
  public PongMessage receivePingMessage(PingMessage msg) {
    return null;
  }

  @Override
  public PongMessage receiveGetKeyMessage(GetKeyMessage msg) {
    return null;
  }

  @Override
  public StoreReplyMessage receiveStoreMessage(StoreMessage msg) {
    return null;
  }

  @Override
  public Message receiveFetchMessage(FetchMessage msg) {
    return null;
  }


}
