package com.soriole.kademlia.core;

import com.google.common.util.concurrent.SettableFuture;
import com.soriole.kademlia.service.KademliaSetupService;
import com.soriole.kademlia.service.StorageService;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class KademliaStorageImpl implements KademliaStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaStorageImpl.class);

    private StorageService storageService;
    private KademliaRouting kademliaRouting;

    public KademliaStorageImpl(KademliaRouting kademliaRouting, StorageService storageService) {
        this.kademliaRouting = kademliaRouting;
        this.storageService = storageService;
    }

    @Override
    public void storeLocally(Key key, byte[] value) {
        storageService.write(key.toString(), value);
    }

    @Override
    public boolean store(Key key, byte[] value) {

        return false;
    }

    @Override
    public boolean store(Key key, byte[] value, int replica) {
        return false;
    }

    @Override
    public byte[] fetch(Key key) {
        return new byte[0];
    }

    @Override
    public void refresh() {

    }


    /**
     * {@link MessageResponseHandler} which puts responses into given queue.
     *
     */
    private class QueuedMessageResponseHandler implements MessageResponseHandler {
        private final BlockingQueue<Future<Message>> mOutputQueue;

        public QueuedMessageResponseHandler(BlockingQueue<Future<Message>> outputQueue) {
            mOutputQueue = outputQueue;
        }

        @Override
        public void onResponse(Message response) {
            SettableFuture<Message> futureResponse = SettableFuture.<Message>create();
            futureResponse.set(response);
//            processSenderNodeInfo(response.getSourceNodeInfo());
            mOutputQueue.add(futureResponse);
        }

        @Override
        public void onResponseError(IOException exception) {
            SettableFuture<Message> futureResponse = SettableFuture.<Message>create();
            futureResponse.setException(exception);
            mOutputQueue.add(futureResponse);
        }

        @Override
        public void onSendSuccessful() {
        }

        @Override
        public void onSendError(IOException exception) {
            SettableFuture<Message> futureResponse = SettableFuture.<Message>create();
            futureResponse.setException(exception);
            mOutputQueue.add(futureResponse);
        }
    }

}
