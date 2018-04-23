package com.soriole.kademlia.core.util;


import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingHashTable<Type1, Type2> {
    ReentrantLock ioLock = new ReentrantLock();
    private HashMap<Type1, Type2> hashStore=new HashMap<>();
    private HashMap<Type1, Thread> waitingThreads=new HashMap<>();
    private long waitTime;

    public BlockingHashTable(long waitMilliTime) {
        this.waitTime=waitMilliTime;
    }

    public Type2 get(Type1 key) throws TimeoutException {
        ioLock.lock();
        if (hashStore.containsKey(key)) {
            Type2 ans = hashStore.remove(key);
            ioLock.unlock();
            return ans;
        }

        waitingThreads.put(key, Thread.currentThread());
        try {
            /* TODO: These two statements should be executed one after another without
                     another thread being scheduled in between.
             */
            ioLock.unlock();
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            // we got interrupted but it's things are same  wether we were interrupted or timeout occured.
        }

        ioLock.lock();
        Type2 ans;
        if (hashStore.containsKey(key)) {
            ans = hashStore.remove(key);
        } else {
            ioLock.unlock();
            throw new TimeoutException();
        }
        ioLock.unlock();
        return ans;

    }

    public Type2 put(Type1 key, Type2 value) {
        ioLock.lock();
        hashStore.put(key, value);
        if (waitingThreads.containsKey(key)) {
            Thread waiting = waitingThreads.remove(key);
            ioLock.unlock();
            waiting.interrupt();
        } else {
            ioLock.unlock();
        }
        return value;

    }

    public boolean putIfGetterWaiting(Type1 key, Type2 value) {
        ioLock.lock();
        if (waitingThreads.containsKey(key)) {
            hashStore.put(key, value);
            Thread waiting = waitingThreads.remove(key);
            ioLock.unlock();
            waiting.interrupt();
            return true;
        } else {
            ioLock.unlock();
            return false;
        }
    }

    public Type2 getIfExists(Type1 key) {
        ioLock.lock();
        if (hashStore.containsKey(key)) {
            Type2 ans = hashStore.remove(key);
            ioLock.unlock();
            return ans;
        } else {
            ioLock.unlock();
            return null;
        }

    }

    /**
     * Get the value from DHT iff other threads are not waiting for the same key.
     * This is to make sure, to receive some  entries in the hash table if and only if
     * they are not required by any other threads.
     * @param key
     * @return
     */
    public Type2 getIfNotWaited(Type1 key) {
        ioLock.lock();
        if (waitingThreads.containsKey(key)) {
        } else if (hashStore.containsKey(key)) {
            Type2 ans = hashStore.remove(key);
            ioLock.unlock();
            return ans;
        }
        ioLock.unlock();
        return null;
    }
}
