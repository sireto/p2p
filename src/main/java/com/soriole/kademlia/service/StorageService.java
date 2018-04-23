package com.soriole.kademlia.service;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentMap;

@Service("storageService")
public class StorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    @Value("${kademlia.store.db:kademlia.db}")
    public String storeFile;

    private ConcurrentMap<String, byte[]> keyMap;
    private ConcurrentMap<Long, String> expiryMap;
    private ConcurrentMap<String, Long> entryMap;

    @PostConstruct
    public void init() {
        LOGGER.debug("Storage service initialing with file: {}", storeFile);
        DB db = DBMaker.fileDB(storeFile)
                .fileMmapEnableIfSupported()
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();

        // preload file content into disk cache
        db.getStore().fileLoad();

        // map instances
        keyMap = db.hashMap("dht.key")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();
        entryMap = db.hashMap("dht.entry")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .createOrOpen();
        expiryMap = db.treeMap("dht.expiry")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.STRING)
                .createOrOpen();
    }

    public byte[] read(String key) {
        return keyMap.getOrDefault(key, new byte[]{});
    }

    public void write(String key, byte[] value) {
        keyMap.put(key, value);
        entryMap.put(key, System.nanoTime());
    }

    public void write(String key, byte[] value, Long nanoSecondExpiry) {
        String existingItem = expiryMap.getOrDefault(nanoSecondExpiry, "");

        while (!existingItem.isEmpty() || existingItem.equals(key.toString())) {
            existingItem = expiryMap.getOrDefault(++nanoSecondExpiry, "");
        }

        keyMap.put(key, value);
        entryMap.put(key, System.nanoTime());
        expiryMap.put(nanoSecondExpiry, key);
    }

    public void remove(String key){
        keyMap.remove(key);
        entryMap.remove(key);
        // expiryMap will be cleared eventually
    }

    @Scheduled(fixedRate = 5000l)
    public void clearExpired() {
        Long currentTs = System.nanoTime();
        expiryMap.keySet().stream().forEach(expiryTs -> {
            if (expiryTs < currentTs) {
                String key = expiryMap.get(expiryTs);
                if (key != null) {
                    keyMap.remove(key);
                    entryMap.remove(key);
                }
                expiryMap.remove(expiryTs);
            }
        });
    }

}
