package com.soriole.kademlia.core;

import com.soriole.kademlia.core.message.Message;

import java.net.InetSocketAddress;

interface MessageSender {
  void sendMessageWithReply(InetSocketAddress dest, Message msg, MessageResponseHandler handler);
}
