package com.soriole.kademlia.core.util;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;

/**
 *  Blocking Hash table implementation using  synchronized methods  and {@code notifyAll()}
 * @param <Type1> type of key
 * @param <Type2> type of value
 *
 * @author github.com/mesudip
 */
public class BlockingHashTable3<Type1, Type2> {
    private HashMap<Type1, Type2> hashStore = new HashMap<>();
    private HashSet<Type1> waitedkeys=new HashSet<>();
    private long waitTime;

    public BlockingHashTable3(long waitMilliTime) {
        this.waitTime = waitMilliTime;
    }

    synchronized public Type2 get(Type1 key) throws TimeoutException {
        Type2 t = hashStore.remove(key);
        if (t != null) {
            return t;
        }
        long newTime = waitTime;
        long startTime = new Date().getTime();
        while (newTime > 0) {
            try {
                wait(newTime);
                if (hashStore.containsKey(key)) {
                    return hashStore.remove(key);
                }

                newTime = waitTime - (new Date().getTime() - startTime);
            } catch (InterruptedException e) {
                // should this happen we need to pass the
                // interrupt to be handled by above layers.
                Thread.currentThread().interrupt();
            }
        }
        throw new TimeoutException();
    }

    synchronized public void reserverForGet(Type1 key) {
        waitedkeys.add(key);
    }

    synchronized public boolean putIfGetterWaiting(Type1 key, Type2 value) {
        if(waitedkeys.contains(key)) {
            waitedkeys.remove(key);
            hashStore.put(key, value);
            notifyAll();
            return true;
        }
        return false;
    }
}
