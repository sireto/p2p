package com.soriole.kademlia.core.util;


import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Blocking hash table using ReetrantLock, thread.interrupt()
 *
 * @param <Type1> type of key
 * @param <Type2> type of value
 * @author github.com/mesudip
 */
public class BlockingHashTable<Type1, Type2> {
    // the lock object
    ReentrantLock ioLock = new ReentrantLock();

    private HashMap<Type1, Type2> hashStore = new HashMap<>();
    private HashMap<Type1, Thread> waitingThreads = new HashMap<>();
    private long waitTime;

    public BlockingHashTable(long waitMilliTime) {
        this.waitTime = waitMilliTime;
    }

    public Type2 get(Type1 key) throws TimeoutException {
        Type2 ans;
        ioLock.lock();
        ans = hashStore.remove(key);
        if (ans != null) {
            waitingThreads.remove(key);
            ioLock.unlock();
            return ans;
        }

        waitingThreads.put(key, Thread.currentThread());
        ioLock.unlock();
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // we got interrupted but it's things are same  wether we were interrupted or timeout occured.
        }
        ioLock.lock();
        ans = hashStore.remove(key);
        ioLock.unlock();

        if (ans != null) {
            return ans;
        }
        throw new TimeoutException();
    }

    public void reserverForGet(Type1 key) {
        this.ioLock.lock();
        this.waitingThreads.put(key, Thread.currentThread());
        ioLock.unlock();
    }


    public boolean putIfGetterWaiting(Type1 key, Type2 value) {
        ioLock.lock();
        Thread waitingThread = waitingThreads.remove(key);
        if (waitingThread == null) {
            ioLock.unlock();
            return false;
        } else {
            hashStore.put(key, value);
            waitingThread.interrupt();
            ioLock.unlock();
            return true;
        }
    }


}
