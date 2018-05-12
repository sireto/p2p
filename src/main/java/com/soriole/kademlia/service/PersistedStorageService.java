package com.soriole.kademlia.service;

import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.TimeStampedData;
import com.soriole.kademlia.core.store.TimestampedStore;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Service("PersistedStorageService")
public class PersistedStorageService implements TimestampedStore<byte[]> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistedStorageService.class);

    @Value("${local.key}")
    public String localKeyValue;

    @Value("${kademlia.put.db:kademlia}")
    public String storeFile;

    private int defaultExpiryTime = 1000*60*60*24;

    private ConcurrentMap<byte[], byte[]> keyMap;
    private ConcurrentMap<byte[], Long> expiryMap;
    private ConcurrentMap<byte[], Long> entryMap;

    // this will help to quickly find the key that has expired in O(1) complexity.
    // then
    private BTreeMap<Long,byte[]> expiryReverseMap;

    @PostConstruct
    public void init() {
        if(storeFile.endsWith(".db")){
            storeFile=storeFile.substring(0,storeFile.length()-3);
        }
        storeFile+="-"+localKeyValue+".db";

        LOGGER.debug("Storage service initialing with file: {}", storeFile);
        DB db = DBMaker.fileDB(storeFile)
                .fileMmapEnableIfSupported()
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();

        // preload file content into disk cache
        db.getStore().fileLoad();

        // map instances
        keyMap = db.hashMap("dht.key-"+localKeyValue)
                .keySerializer(Serializer.BYTE_ARRAY)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();
        entryMap = db.hashMap("dht.entry-"+localKeyValue)
                .keySerializer(Serializer.BYTE_ARRAY)
                .valueSerializer(Serializer.LONG)
                .createOrOpen();
        expiryMap = db.hashMap("dht.expiry-"+localKeyValue)
                .keySerializer(Serializer.BYTE_ARRAY)
                .valueSerializer(Serializer.LONG)
                .createOrOpen();

        expiryReverseMap = db.treeMap("dht.reverse-expiry-"+localKeyValue)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();
    }
    public void remove(String key) {
        keyMap.remove(key);
        entryMap.remove(key);
        // expiryMap will be cleared eventually
    }



    @Override
    public boolean put(Key k, byte[] value) {
        return put(k, value, defaultExpiryTime);
    }

    @Override
    synchronized public boolean put(Key k, byte[] value, long expirationTime) {
        long expiry=System.currentTimeMillis() + expirationTime;
        byte[] previousEntry = keyMap.put(k.toBytes(), value);
        entryMap.put(k.toBytes(), System.currentTimeMillis());
        expiryMap.put(k.toBytes(), expiry);
        expiryReverseMap.put(expiry,k.toBytes());
        if (previousEntry != null) {
            return false;
        }
        return true;
    }

    @Override
    synchronized public TimeStampedData<byte[]> get(Key k) {
        byte[] key = k.toBytes();
        byte[] value = keyMap.get(key);
        if (value != null) {
            return new TimeStampedData<>(
                    value,
                    expiryMap.get(key).longValue(),
                    entryMap.get(key).longValue());
        }
        return null;

    }

    @Override
    synchronized public boolean refreshCreatedTime(Key k) {
        byte[] key=k.toBytes();
        if(entryMap.get(key)!=null) {
            entryMap.replace(key, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Override
    synchronized public boolean remove(Key k) {
        byte[] key=k.toBytes();
        expiryMap.remove(key);
        entryMap.remove(key);
        if(keyMap.remove(key)==null){
            return false;
        }
        return true;
    }

    @Override
    synchronized public TimeStampedData<byte[]> getFirstExpiring() {
        Map.Entry<Long, byte[]> firstEntry = expiryReverseMap.firstEntry();
        if(firstEntry==null){
            return null;
        }
        byte[] key=firstEntry.getValue();
        long expiryTime= firstEntry.getKey();
        return new TimeStampedData<>(keyMap.get(key),expiryTime,entryMap.get(key));
    }

    @Override
    public TimeStampedData<byte[]> getFirstInserted() {
       //todo;
        return null;

    }

//    @Scheduled(fixedRate = 5_000L)
    synchronized public void clearExpired() {
        // let's not repeat the native time each time.
        long startTime=System.currentTimeMillis();

        while(true){
            TimeStampedData<byte[]> data=getFirstExpiring();
            if(data==null){
                return;
            }

            if(data.getExpirationTime()<startTime) {
                // remove the first entry from reverse map.
                expiryReverseMap.pollFirstEntry();
                this.remove(new Key(data.getData()));

            }
            else{

                //TODO: we now know when the next value will expire. We might now schedule the clearExpired() to that time instead of repeatedly scheduling it.
                // clearExpired.scheduleAfter(data.getExpirationTime())
                return;

            }
        }
    }
}
