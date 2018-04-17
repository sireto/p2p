package com.soriole.kademlia.service;

import com.soriole.kademlia.core.*;
import com.soriole.kademlia.network.UserGivenNetworkAddressDiscovery;
import com.soriole.kademlia.network.socket.SimpleSocketByteListeningService;
import com.soriole.kademlia.network.socket.SimpleSocketByteSender;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service("kademliaSetupService")
public class KademliaSetupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaSetupService.class);

    @Value("${local.key:10}")
    public String localKeyValue;
    @Value("${local.address.ip}")
    public String localIp;
    @Value("${local.address.port}")
    public int localPort;

    @Value("${bootstrap.key}")
    public String bootstrapKeyValue;
    @Value("${bootstrap.address.ip}")
    public String bootstrapIp;
    @Value("${bootstrap.address.port}")
    public int bootstrapPort;

    @Value("${kademlia.bucket.size}")
    public int bucketSize;

    public KademliaRouting kademlia;

    @PostConstruct
    public void init() throws UnknownHostException {

        final InetAddress localInetAddress = InetAddress.getByName(localIp);
        final InetAddress hostZeroInetAddress = InetAddress.getByName(bootstrapIp);
        final int hostZeroPort = bootstrapPort;
        final Key localKey = new Key(localKeyValue);
        final Key bootstrapKey = new Key(bootstrapKeyValue);

        final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
        final ExecutorService executor = Executors.newFixedThreadPool(1);

        KademliaRoutingBuilder builder = new KademliaRoutingBuilder(new Random());
        SimpleSocketByteListeningService ssbls = new SimpleSocketByteListeningService(localPort,
                executor);
        try {
            ssbls.start();
        } catch (IOException e) {
            LOGGER.error("main() -> Could not create listening service.", e);
            return;
        }
        builder.setByteListeningService(ssbls);
        builder.setByteSender(new SimpleSocketByteSender());
        builder.setExecutor(scheduledExecutor);
        Collection<NodeInfo> peersWithKnownAddresses = new LinkedList<>();
        if (!localKey.equals(bootstrapKey)) {
            peersWithKnownAddresses.add(new NodeInfo(bootstrapKey, new InetSocketAddress(
                    hostZeroInetAddress, hostZeroPort)));
        }
        builder.setInitialPeersWithKeys(peersWithKnownAddresses);
        builder.setKey(localKey);
        builder.setBucketSize(bucketSize);
        builder.setNetworkAddressDiscovery(new UserGivenNetworkAddressDiscovery(new InetSocketAddress(
                localInetAddress, localPort)));

        kademlia = builder.createPeer();

//        if (kademlia.isRunning()) {
//            try {
//                kademlia.stop();
//            } catch (KademliaException e) {
//                LOGGER.error("main(): kademlia.stop()", e);
//            }
//        }
//        ssbls.stop();
//        try {
//            LOGGER.debug("main(): executor.shutdown()");
//            executor.shutdown();
//            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
//            LOGGER.debug("main(): scheduledExecutor.shutdown()");
//            scheduledExecutor.shutdown();
//            scheduledExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            LOGGER.error("main() -> unexpected interrupt", e);
//        }
//        LOGGER.info("main() -> void");
    }
}
