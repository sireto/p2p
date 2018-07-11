package com.soriole.kademlia.core.store;

import com.soriole.kademlia.core.KademliaConfig;

import java.util.HashMap;

/**
 * Stores timestamped data in-memory
 * The data is lost on restart.
 *
 * @author github.com/mesudip
 */
public class InMemoryByteStore implements TimestampedStore<byte[]> {
    HashMap<Key, TimeStampedData<byte[]>> store = new HashMap<>();
    long defaultExpirationTime;

    public InMemoryByteStore(KademliaConfig config) {
        this(config.getKeyValueExpiryTime());
    }

    public InMemoryByteStore(long defaultExpirationTime) {
        this.defaultExpirationTime = defaultExpirationTime;

    }

    @Override
    public boolean put(Key k, byte[] value) {
        return put(k, value, defaultExpirationTime);

    }

    @Override
    public boolean put(Key k, byte[] value, long expirationTime) {
        TimeStampedData previous = store.put(k, new TimeStampedData<>(value, expirationTime));
        if (previous != null) {
            return false;
        }
        return true;
    }

    @Override
    public TimeStampedData<byte[]> get(Key k) {
        return store.get(k);
    }

    @Override
    public boolean refreshCreatedTime(Key k) {
        TimeStampedData data = store.get(k);
        if (data != null) {
            data.refresh();
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Key k) {
        TimeStampedData data = store.remove(k);
        return data == null;
    }

    @Override
    public TimeStampedData<byte[]> getFirstExpiring() {
        throw new RuntimeException("Not implemented yet");
        //TODO: Not implemented yet
    }

    @Override
    public TimeStampedData<byte[]> getFirstInserted() {
        throw new RuntimeException("Not implemented yet");
        //TODO: Not implemented yet
    }


    @Override
    public String toString() {
        return store.keySet().toString();
    }
}
