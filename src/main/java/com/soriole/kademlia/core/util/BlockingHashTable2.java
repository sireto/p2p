package com.soriole.kademlia.core.util;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  Blocking Hash table implementation using  nested synchronized blocks and notify()
 * @param <Type1> type of key
 * @param <Type2> type of value
 *
 * @author github.com/mesudip
 */
public class BlockingHashTable2<Type1,Type2> {
    ReentrantLock ioLock = new ReentrantLock();

    private HashMap<Type1, Type2> hashStore=new HashMap<>();
    private HashMap<Type1, Object> waiters=new HashMap<>();
    private long waitTime;

    public BlockingHashTable2(long waitMilliTime) {
        this.waitTime=waitMilliTime;
    }

    // making this method synchronized will block everything, instead block different parts of it.
    public Type2 get(Type1 key) throws TimeoutException {
        Object o=new Object();
        synchronized (this) {
            Type2 t=hashStore.remove(key);
            if(t!=null){
                return t;
            }
            this.waiters.put(key,o);
        }

        synchronized (o) {
            long newTime=waitTime;
            long startTime=new Date().getTime();
            while(newTime>0){
                try {
                    o.wait(newTime);
                    synchronized (this) {
                        if (hashStore.containsKey(key)){
                            return hashStore.remove(key);
                        }
                    }
                    newTime=waitTime-(new Date().getTime()-startTime);

                } catch (InterruptedException e) {
                    // should this happen we need to pass the
                    // interrupt to be handled by above layers.
                    Thread.currentThread().interrupt();
                }
            }
        }
        synchronized (this) {
            if (hashStore.containsKey(key)){
                return hashStore.remove(key);
            }
        }
        throw new TimeoutException();
    }
    synchronized public void reserverForGet(Type1 key){
            Object o = new Object();
            this.waiters.put(key,o);
    }

    synchronized public boolean putIfGetterWaiting(Type1 key, Type2 value) {
            Object o=waiters.remove(key);
            if(o==null){
                return false;
            }
            hashStore.put(key,value);
            synchronized (o) {
                o.notify();
            }
            return true;
    }
}
