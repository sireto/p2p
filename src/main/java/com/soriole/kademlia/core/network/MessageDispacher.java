package com.soriole.kademlia.core.network;

import com.soriole.kademlia.core.NodeInteractionListener;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.network.ServerShutdownException;
import com.soriole.kademlia.core.network.receivers.MessageReceiver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

public interface MessageDispacher {

    void sendMessage(NodeInfo receiver, Message message) throws IOException, TimeoutException;

    void replyFor(Message incoming, Message reply) throws IOException, TimeoutException;

    Message queryFor(Message incoming, Message reply) throws TimeoutException, IOException, ServerShutdownException;

    Collection<Message> startAsyncQueryAll(Collection<NodeInfo> nodes, Message queryMessage);

    void asynQueryFor(Message incoming, Message reply, MessageReceiver msgReceiver) throws ServerShutdownException;

    Message startQuery(NodeInfo receiver, Message message) throws TimeoutException, ServerShutdownException, IOException;

    void startQueryAsync(NodeInfo receiver, Message message, final MessageReceiver msgReceiver) throws ServerShutdownException;

    boolean shutDown(int timeSeconds) throws InterruptedException;

    boolean stop();

    void submitTask(Runnable task);

    boolean start() throws SocketException;

    void setNodeInteractionListener(NodeInteractionListener listener);

    InetSocketAddress getUsedSocketAddress();
}
