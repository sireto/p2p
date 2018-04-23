package com.soriole.kademlia.core;

import com.soriole.kademlia.core.message.*;
import com.soriole.kademlia.network.ByteListener;
import com.soriole.kademlia.network.ByteListeningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter from {@link ByteListeningService} to
 * {@link ListeningService}.
 *
 * @author Grzegorz Milka
 */
class MessageListeningServiceAdapter implements ListeningService {
  private static final Logger LOGGER = LoggerFactory.getLogger(
      MessageListeningServiceAdapter.class);
  private final ByteListeningService mByteListeningService;
  private final ByteToMessageTranslatingListener mByteToMsgListener;
  private MessageListener mListener;

  public MessageListeningServiceAdapter(ByteListeningService byteListeningService) {
    mByteListeningService = byteListeningService;
    mByteToMsgListener = new ByteToMessageTranslatingListener();
  }

  @Override
  public void registerListener(MessageListener listener) {
    assert mListener == null;
    mListener = listener;
    mByteListeningService.registerListener(mByteToMsgListener);

  }

  @Override
  public void unregisterListener(MessageListener listener) {
    assert mListener != null && mListener.equals(listener);
    mByteListeningService.unregisterListener(mByteToMsgListener);
    mListener = null;
  }

  private class ByteToMessageTranslatingListener implements ByteListener {
    @Override
    public byte[] receiveByteArrayWithResponse(byte[] byteMsg) {
      Message msg = MessageSerializer.translateFromByteToMessage(byteMsg);
      Message response = null;
      if (msg instanceof FindNodeMessage) {
        response = mListener.receiveFindNodeMessage((FindNodeMessage) msg);
      } else if (msg instanceof GetKeyMessage) {
        response = mListener.receiveGetKeyMessage((GetKeyMessage) msg);
      } else if (msg instanceof PingMessage) {
        response = mListener.receivePingMessage((PingMessage) msg);
      } else if (msg instanceof StoreMessage) {
        response = mListener.receiveStoreMessage((StoreMessage) msg);
      } else if (msg instanceof FetchMessage) {
        response = mListener.receiveFetchMessage((FetchMessage) msg);
      }else {
        LOGGER.error("receiveByteArrayWithResponse() -> received unexpected type");
      }
      if (response == null) {
        return null;
      } else {
        return MessageSerializer.translateFromMessageToByte(response);
      }
    }
  }
}