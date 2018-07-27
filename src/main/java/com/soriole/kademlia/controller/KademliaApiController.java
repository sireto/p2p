package com.soriole.kademlia.controller;

import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.model.remote.NodeInfoBean;
import com.soriole.kademlia.model.remote.NodeInfoCollectionBean;
import com.soriole.kademlia.core.network.ServerShutdownException;
import com.soriole.kademlia.service.KademliaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping(value = "/api/kademlia/v1")
public class KademliaApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaApiController.class);

    @Autowired
    KademliaService kademliaService;

    @GetMapping(value = "/start")
    public String start() {
        try {
            if (kademliaService.getDHT().start()) {
                return "STARTED";
            } else {
                LOGGER.info("Start command received but server is running");
                return "Server already running";
            }
        } catch (SocketException e) {
            LOGGER.error("Socket exception : " + e.getMessage());
            return "Server failed to startAsync : " + e.getMessage();
        }
    }

    @GetMapping(value = "/stop")
    public String stop() {
        if (kademliaService.getDHT().stop()) {
            return "STOPPED";
        } else {
            return "Server was already not running.";
        }
    }

    @GetMapping(value = "/routing_table")
    public NodeInfoCollectionBean getRoutingTable() {
        LOGGER.info("getRoutingTable()");
        Collection<NodeInfo> nodeInfos;
        nodeInfos = kademliaService.getDHT().getRoutingTable();
        NodeInfoBean[] parsedNodeInfos = new NodeInfoBean[nodeInfos.size()];
        int idx = 0;
        for (NodeInfo nodeInfo : nodeInfos) {
            parsedNodeInfos[idx] = NodeInfoBean.fromNodeInfo(nodeInfo);
            ++idx;
        }
        NodeInfoCollectionBean bean = new NodeInfoCollectionBean();
        bean.setNodeInfo(parsedNodeInfos);
        return bean;
    }

    @GetMapping(value = "/find_nodes/{key}")
    public NodeInfoCollectionBean findNodes(@PathVariable("key") String paramKey) throws ServerShutdownException {
        LOGGER.info("findNodes({})", paramKey);
        Key key = new Key(paramKey);

        Collection<NodeInfo> nodeInfos = null;
        nodeInfos = kademliaService.getDHT().findClosestNodes(key);

        NodeInfoBean[] parsedNodeInfos = new NodeInfoBean[nodeInfos.size()];
        int idx = 0;
        for (NodeInfo nodeInfo : nodeInfos) {
            parsedNodeInfos[idx] = NodeInfoBean.fromNodeInfo(nodeInfo);
            ++idx;
        }
        NodeInfoCollectionBean bean = new NodeInfoCollectionBean();
        bean.setNodeInfo(parsedNodeInfos);
        return bean;
    }

    @GetMapping(value = "/key")
    public String getKey() {
        return kademliaService.getDHT().getLocalNode().getKey().toString();
    }


    @GetMapping(value = "/store/{key}:{value}")
    public int store(@PathVariable("key") String paramKey, @PathVariable("value") String value,@RequestParam(value = "clone",required = false) Integer redundancy) {
        try {
            if(redundancy!=null) {
                return kademliaService.getDHT().put(new Key(paramKey), value.getBytes());
            }
            else{
                return kademliaService.getDHT().put(new Key(paramKey),value.getBytes(),redundancy);
            }
        } catch (ServerShutdownException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @GetMapping(value = "/fetch/{key}")
    public String fetch(@PathVariable("key") String paramKey) {
        try {
            return new String(kademliaService.getDHT().get(new Key(paramKey)).getData());
        } catch (ServerShutdownException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            LOGGER.info("/fetch/" + paramKey + " : not found!");
        } catch (com.soriole.kademlia.core.KadProtocol.ContentNotFoundException e) {
            e.printStackTrace();
        }
        return "Null";
    }

    @GetMapping(value = "/local/{key}")
    public String local(@PathVariable("key") String paramKey) {
        // may be null.
        return new String(kademliaService.getDHT().getLocal(new Key(paramKey)).getData());
    }

    @GetMapping(value = "/adjustAddress/{nodeid}:{address}:{port}")
    public String adjustAddress(@PathVariable("nodeid") String nodeId, @PathVariable("address") String newAddress, @PathVariable("port") int port) {

        NodeInfo nodeInfo = new NodeInfo(new Key(nodeId), new InetSocketAddress(newAddress, port));

        NodeInfo currentNode = kademliaService.getDHT().getLocalNode();
        if (kademliaService.getDHT().ping(nodeInfo) >= 0) {

            if (kademliaService.getDHT().updateNode(nodeInfo.getKey(), nodeInfo.getLanAddress())) {
                return "Success";
            }
        }
        return "Error connecting to node.";
    }
    @GetMapping(value="/join/{address}:{port}")
    public NodeInfoCollectionBean join(@PathVariable("address") String address,@PathVariable("port") int port){
        kademliaService.getDHT().join(new InetSocketAddress(address,port));
        return NodeInfoCollectionBean.fromNodeInfoCollection(kademliaService.getDHT().getRoutingTable());


    }

    @GetMapping("/refreshTable")
    public String refreshPeers() {
        kademliaService.getDHT().refreshRoutingTable();
        return "Refreshing Peers in background";
    }

    @GetMapping("/ping/{nodeid}")
    public String pingNode(@PathVariable("nodeid") String nodeId) throws ServerShutdownException {
        NodeInfo nodeInfo = kademliaService.getDHT().findNode(new Key(nodeId));
        if (nodeInfo == null) {
            return "Node not found in DHT Network";
        }
        long time = kademliaService.getDHT().ping(nodeInfo);
        if (time < 0) {
            return "Peer didn't reply";
        }
        return "Peer took " + String.valueOf(time) + "ms to reply";
    }

    @GetMapping("/udppuncture/enable")
    public String enableUdpPuncture(@RequestParam("period") long period) {
        if (kademliaService.getDHT().startUdpPuncture(period)) {
            return "Puncture started every " + String.valueOf(period) + " ms.";
        }
        return "Udp Puncture is already running";

    }

    @GetMapping("/udppuncture/disable")
    public String disableUdpPuncture() {
        if (kademliaService.getDHT().stopUdpPuncture()) {
            return "Udp Puncture stopped";
        }
        return "Udp Puncture was not running";
    }

    @GetMapping("/myinfo/{nodeid}")
    public ResponseEntity getMyip(@PathVariable("nodeid") String nodeid) {
        try {
            NodeInfo info = kademliaService.getDHT().findMyInfo(new Key(nodeid));
            if (info != null) {
                return ResponseEntity.ok(NodeInfoBean.fromNodeInfo(info));
            }
        } catch (TimeoutException e) {
            return ResponseEntity.ok("Peer didn't reply");
        }
        return ResponseEntity.ok("Unknown error");
    }
    @GetMapping("/systeminfo")
    public ResponseEntity getSystemInfo(){

        com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long physicalMemorySize = os.getTotalPhysicalMemorySize();
        long freePhysicalMemory = os.getFreePhysicalMemorySize();
        long freeSwapSize = os.getFreeSwapSpaceSize();
        long commitedVirtualMemorySize = os.getCommittedVirtualMemorySize();
        return ResponseEntity.ok("Not yet implemented");
    }
}
