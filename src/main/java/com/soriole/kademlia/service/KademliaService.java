package com.soriole.kademlia.service;

import com.soriole.kademlia.core.*;
import com.soriole.kademlia.core.store.*;
import com.soriole.kademlia.network.KademliaMessageServer;
import com.soriole.kademlia.network.ServerShutdownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

@Service("kademliaService")
public class KademliaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaService.class);

    @Value("${local.key:0}")
    public String localKeyValue;
    @Value("${local.address.ip}")
    public String localIp;
    @Value("${local.address.port:0}")
    public int localPort;

    @Value("${bootstrap.key}")
    public String bootstrapKeyValue;
    @Value("${bootstrap.address.ip}")
    public String bootstrapIp;
    @Value("${bootstrap.address.port}")
    public int bootstrapPort;

    @Value("${kademlia.bucket.size}")
    public int bucketSize;


    KademliaExtendedDHT kademliaDHT;
    ContactBucket contactBucket;
    KademliaMessageServer server;

    @PostConstruct
    public void init()  {
        // create local node info
        // if the key is zero create a random key.
        NodeInfo localNode = new NodeInfo(new Key(localKeyValue));
        if(localNode.getKey().equals(new Key("0"))){
            byte[] info=new byte[20];
            new Random().nextBytes(info);
            localNode=new NodeInfo(new Key(info));

        }

        if(localKeyValue.equals(bootstrapKeyValue)) {
            localPort=bootstrapPort;
        }
        KeyValueStore<byte[]> keyValueStore=new KeyValueStore<>(1000*60*60*24);

        // create contact bucket
        contactBucket = new ContactBucket(localNode, 160, bucketSize);

        // create a message server
        try {
            server = new KademliaMessageServer(localPort, contactBucket,keyValueStore);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException();
        }

        localNode.setLanAddress(server.getSocketAddress());

        try {
            server.start();
        } catch (SocketException e) {
            e.printStackTrace();
            throw new RuntimeException("Looks like port is not availab.e");
        }

        kademliaDHT = new KademliaExtendedDHT(contactBucket, server,keyValueStore);

        if (!localKeyValue.equals(bootstrapKeyValue)) {
            if(!kademliaDHT.join(new NodeInfo(new Key(bootstrapKeyValue), new InetSocketAddress(bootstrapIp, bootstrapPort)))){
                throw new RuntimeException("Cannot Connect with Bootstrap node");
            }
        }
    }

    // returns ID of the node subscribed by the client
    public String findValue(String key) {
        try {
            return new String(kademliaDHT.findValue(new Key(key)).getData());
        }
        catch (ServerShutdownException e) {
            e.printStackTrace();
        }
        catch (NoSuchElementException ex){
            //ignore
        }
        return "Null";

    }

    // sends messages to the  node given by the nodeID. waits for acknowledgement.
    public void sendMessage(byte[] message, String nodeID) {
        kademliaDHT.sendMessageToNode(new Key(nodeID), message);
    }

    // sends messages to the node given by nodeID and blocks until a messages is received
    public byte[] query(byte[] message, String nodeId) {
        try {
            return kademliaDHT.queryWithNode(new Key(nodeId), message);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean store(String key, String value) {
        try {
            kademliaDHT.store(new Key(key), value.getBytes());
            return true;
        } catch (ServerShutdownException e) {
            e.printStackTrace();
        }
        return false;
    }


    public KademliaExtendedDHT getKademliaProtocol() {
        return kademliaDHT;
    }

    @PreDestroy
    public void destroy(){
        try {
            server.shutDown(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String findLocal(String key){
        TimeStampedData<byte[]> data=kademliaDHT.keyValueStore.get(new Key(key));
        if(data==null){
            return "Null";
        }
        return new String(data.getData());
    }
}
