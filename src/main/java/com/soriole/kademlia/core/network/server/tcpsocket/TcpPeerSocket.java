package com.soriole.kademlia.core.network.server.tcpsocket;

import com.soriole.kademlia.core.messages.AddressExchangeMessage;
import com.soriole.kademlia.core.messages.Message;
import com.soriole.kademlia.core.messages.MessageFactory;
import com.soriole.kademlia.core.messages.PongMessage;
import com.soriole.kademlia.core.store.ContactBucket;
import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.core.network.KademliaNetworkMessageProtocol;
import com.soriole.kademlia.core.network.receivers.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.NotYetConnectedException;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

import static com.soriole.kademlia.core.network.KademliaNetworkMessageProtocol.Message.parseDelimitedFrom;

public class TcpPeerSocket {
    private static Logger logger=LoggerFactory.getLogger(TcpPeerSocket.class);
    ExecutorService executorService;
    ServerSocket serverSocket;
    Hashtable<InetSocketAddress, Socket> socketMap=new Hashtable<>();
    MessageReceiver messageReceiver;
    Hashtable<Socket,SocketWatcher> socketWatchers= new Hashtable<>();
    ContactBucket bucket;
    boolean accepting=false;

    TcpPeerSocket(int port,ContactBucket bucket,ExecutorService service) throws IOException {
        this.bucket=bucket;
        serverSocket = new ServerSocket(port);
        executorService=service;
    }


    public Socket connect(InetSocketAddress address,long timeoutMs) throws IOException, TimeoutException {
        Socket client = socketMap.get(address);
        if (client == null) {
            return internalConnect(address,timeoutMs);
        }
        return client;
    }


    private Socket internalConnect(InetSocketAddress address,long timeoutMs) throws IOException, TimeoutException {
        Socket client = new Socket();
        client.connect(address);
        SocketWatcher watcher =new SocketWatcher(client,messageReceiver);
        executorService.submit(watcher);

        Message message = new AddressExchangeMessage(serverSocket.getLocalPort());
        message.mSrcNodeInfo=bucket.getLocalNode();
        message.sessionId=new Random().nextLong();
        message=watcher.query(message,timeoutMs);
        if(message instanceof PongMessage){
            newPeer(message.getSourceNodeInfo().getKey(),(InetSocketAddress) client.getRemoteSocketAddress(), (InetSocketAddress) client.getRemoteSocketAddress(),true);
            this.socketMap.put((InetSocketAddress) client.getRemoteSocketAddress(),client);
            this.socketWatchers.put(client,watcher);
            logger.info("Connection established with :"+message.getSourceNodeInfo().toDetailString());
            return client;
        }


        throw new IOException("Handshake with peer failed");

    }


    private void newPeer(Key key, InetSocketAddress connectAddress, InetSocketAddress listenAddress, boolean isPublic) {
        bucket.putNode(new NodeInfo(key,listenAddress));
    }

    public synchronized void accept() {
        if(!this.accepting) {
            executorService.submit(this::acceptLoop);
            this.accepting=true;
        }
    }

    private void acceptLoop() {
        Thread.currentThread().setName("Accept Looper of :"+bucket.getLocalNode().getKey());
        while (!serverSocket.isClosed()) {
            try {
                logger.info("Waiting for new Connection");

                Socket client = serverSocket.accept();
                executorService.submit(()-> {
                    try {
                        KademliaNetworkMessageProtocol.Message message = parseDelimitedFrom(client.getInputStream());
                        Message msg = MessageFactory.createInstance(message, (InetSocketAddress) client.getRemoteSocketAddress());
                        if (msg instanceof AddressExchangeMessage) {
                            AddressExchangeMessage exchangeMessage = (AddressExchangeMessage) msg;
                            InetSocketAddress mainAddress = new InetSocketAddress(((InetSocketAddress) client.getRemoteSocketAddress()).getAddress(), exchangeMessage.listeningPort);

                            this.socketMap.put(mainAddress, client);
                            this.socketMap.put((InetSocketAddress) client.getRemoteSocketAddress(), client);

                            PongMessage ack=new PongMessage();
                            ack.sessionId=exchangeMessage.sessionId;
                            ack.mSrcNodeInfo=bucket.getLocalNode();
                            MessageFactory.toProtoInstance(ack).writeDelimitedTo(client.getOutputStream());
                            newPeer(exchangeMessage.getSourceNodeInfo().getKey(),(InetSocketAddress) client.getRemoteSocketAddress(), mainAddress, exchangeMessage.isPublic);
                            SocketWatcher watcher = new SocketWatcher(client, messageReceiver);
                            this.socketWatchers.put(client, watcher);
                            logger.info("Connection accepted :" + new NodeInfo(exchangeMessage.mSrcNodeInfo.getKey(),mainAddress));
                            watcher.run();
                        }
                        client.close();
                        logger.warn("Invalid response type received from peer");
                    } catch (InstantiationException | IOException e) {
                        logger.warn("Error in connection handshake");

                    }
                });

            } catch (IOException e) {
                logger.info("IOException while accepting new Client connection");
                e.printStackTrace();
            }
        }
        logger.info("Stopping the connection Accept loop");
    }

    public Message query(NodeInfo receiver,Message message,long timeoutMs) throws IOException, TimeoutException {
        if(message.mSrcNodeInfo==null){
            message.mSrcNodeInfo=bucket.getLocalNode();
        }
        return this.socketWatchers.get(connect(receiver.getLanAddress(),timeoutMs)).query(message,timeoutMs);
    }
    public void sendMessage(InetSocketAddress address, Message message) throws IOException, IllegalArgumentException, TimeoutException {
        logger.debug("Sending message of type :` "+message.getClass().getSimpleName()+"` to  :"+address);
        if(message.mSrcNodeInfo==null){
            message.mSrcNodeInfo=bucket.getLocalNode();
        }
        MessageFactory.toProtoInstance(message).writeDelimitedTo(this.connect(address,10000).getOutputStream());
    }

    public Message receiveMessage(InetSocketAddress address, Message message) throws NotYetConnectedException, IOException, InstantiationException, TimeoutException {
        if (this.socketMap.contains(address)) {
            KademliaNetworkMessageProtocol.Message message1 = parseDelimitedFrom(connect(address,0).getInputStream());
            return MessageFactory.createInstance(message1, address);
        }
        throw new NotYetConnectedException();
    }
    public void setMessageReceiver(MessageReceiver receiver){
        messageReceiver=receiver;
    }
}
