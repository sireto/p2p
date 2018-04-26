package com.soriole.kademlia.core.store;

import java.util.HashMap;

/**
 * KeyValueStore stores (key,value).
 * the value is wrapped around by the TimeStampedData class to store the timestamp information.
 * @param <Type> The type of data to store
 */
public class KeyValueStore <Type>{

    HashMap<Key,TimeStampedData<Type>> store =new HashMap<>();
    long defaultExpirationTime;

    public KeyValueStore(long defaultExpirationTime){
        this.defaultExpirationTime=defaultExpirationTime;

    }
    public Type put(Key k,Type value){
        return put(k,value,defaultExpirationTime);
    }
    public Type put(Key k,Type value,long expirationTime){
        TimeStampedData previous =store.put(k,new TimeStampedData<>(value,expirationTime));
        if(previous==null){
            return null;
        }
        else{
            return (Type) previous.getData();
        }
    }
    public TimeStampedData<Type> get(Key k){
        return store.get(k);
    }
    public boolean refresh(Key k){
        return store.get(k).refresh();
    }

    public boolean remove(Key k){
        return store.remove(k)==null;
    }
    @Override
    public String toString(){
        return store.keySet().toString();
    }
}
