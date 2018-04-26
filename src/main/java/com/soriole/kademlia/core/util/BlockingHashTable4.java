package com.soriole.kademlia.core.util;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  Blocking Hash table implementation using  synchronized block, {@code ReetrantLock} and {@code notify()}
 * @param <Type1> type of key
 * @param <Type2> type of value
 *
 * @author github.com/mesudip
 */
public class BlockingHashTable4<Type1, Type2> {
    ReentrantLock ioLock = new ReentrantLock();

    private HashMap<Type1, Type2> hashStore = new HashMap<>();
    private HashMap<Type1, Object> waiters = new HashMap<>();
    private long waitTime;

    public BlockingHashTable4(long waitMilliTime) {
        this.waitTime = waitMilliTime;
    }

    public Type2 get(Type1 key) throws TimeoutException {
        Object o = new Object();

        //lock and check if we already have the key.
        ioLock.lock();
        Type2 returnValue = hashStore.remove(key);
        if (returnValue != null) {
            ioLock.unlock();
            return returnValue;
        }
        this.waiters.put(key, o);
        ioLock.unlock();
        // Oops, we don't have the key, so wait until somebody put it.
        synchronized (o) {
            long newTime = waitTime;
            long startTime = new Date().getTime();
            while (newTime > 0) {
                try {
                    o.wait(newTime);
                    ioLock.lock();
                    returnValue = hashStore.remove(key);
                    if (returnValue != null) {
                        ioLock.unlock();
                        return returnValue;
                    }
                    ioLock.unlock();
                    newTime = waitTime - (new Date().getTime() - startTime);

                } catch (InterruptedException e) {
                    // should this happen we need to pass the
                    // interrupt to be handled by above layers.
                    Thread.currentThread().interrupt();
                }
            }
        }
        ioLock.lock();
        if (hashStore.containsKey(key)) {
            return hashStore.remove(key);
        }
        ioLock.unlock();

        throw new TimeoutException();
    }

    public void reserverForGet(Type1 key) {
        ioLock.lock();
        Object o = new Object();
        this.waiters.put(key, o);
        ioLock.unlock();
    }

    public Type2 getIfExists(Type1 key) {
        ioLock.lock();
        Type2 returnValue = hashStore.remove(key);
        waiters.remove(key);
        ioLock.unlock();
        return returnValue;
    }


    public boolean putIfGetterWaiting(Type1 key, Type2 value) {
        boolean ret=true;
        ioLock.lock();
        Object o = waiters.remove(key);
        if (o == null) {
            ret=false;
        }
        else {
            hashStore.put(key,value);
            synchronized (o) {
                o.notify();
            }
        }
        ioLock.unlock();
        return ret;


    }
}
