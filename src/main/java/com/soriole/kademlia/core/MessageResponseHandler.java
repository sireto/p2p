package com.soriole.kademlia.core;

import com.soriole.kademlia.core.message.Message;

import java.io.IOException;

/**
 * Observer of message send and response events.
 *
 * @author Grzegorz Milka
 */
interface MessageResponseHandler {
  void onResponse(Message response);

  void onResponseError(IOException exception);

  void onSendSuccessful();

  void onSendError(IOException exception);
}
