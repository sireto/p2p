package com.soriole.kademlia.core;

import com.soriole.kademlia.core.message.Message;

import java.io.IOException;

/**
 * {@link MessageResponseHandler} which does nothing.
 *
 * @author Grzegorz Milka
 */
class IdleMessageResponseHandler implements MessageResponseHandler {

  @Override
  public void onResponse(Message response) {
  }

  @Override
  public void onResponseError(IOException exception) {
  }

  @Override
  public void onSendSuccessful() {
  }

  @Override
  public void onSendError(IOException exception) {
  }
}
