package com.soriole.kademlia.controller;

import com.soriole.kademlia.core.KademliaException;
import com.soriole.kademlia.core.Key;
import com.soriole.kademlia.core.NodeInfo;
import com.soriole.kademlia.model.remote.NodeInfoBean;
import com.soriole.kademlia.model.remote.NodeInfoCollectionBean;
import com.soriole.kademlia.service.KademliaSetupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1")
public class ApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    KademliaSetupService kademliaSetupService;

    @GetMapping(value = "/hello")
    public String hello(){
        return "Hello DHT";
    }

    @GetMapping(value = "/start")
    public String start() {
        try {
            kademliaSetupService.kademlia.start();
            return "STARTED";
        } catch (Exception e) {
            LOGGER.error("Failed to start", e);
            return "FAILURE";
        }
    }

    @GetMapping(value = "/stop")
    public String stop() {
        try {
            kademliaSetupService.kademlia.stop();
            return "STOPPED";
        } catch (Exception e) {
            LOGGER.error("Failed to stop", e);
            return "FAILURE";
        }
    }

    @GetMapping(value = "/routing_table")
    public NodeInfoCollectionBean getRoutingTable() {
        LOGGER.info("getRoutingTable()");
        Collection<NodeInfo> nodeInfos;
        try {
            nodeInfos = kademliaSetupService.kademlia.getRoutingTable();
        } catch (Exception e) {
            LOGGER.error("getRoutingTable()", e);
            return null;
        }
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
    public NodeInfoCollectionBean findNodes(@PathVariable("key") String paramKey) {
        LOGGER.info("findNodes({})", paramKey);
        Key key = new Key(Integer.parseInt(paramKey));
        Collection<NodeInfo> nodeInfos = null;
        try {
            nodeInfos = kademliaSetupService.kademlia.findClosestNodes(key);
        } catch (InterruptedException | KademliaException e) {
            LOGGER.error(String.format("findNodes(%s)", key), e);
            return null;
        }
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
        return kademliaSetupService.kademlia.getLocalKey().toString();
    }

    @GetMapping(value = "/store/{key}")
    public int store(@PathVariable("key") String paramKey, @PathParam("value") String value){
        Key key = new Key(Integer.parseInt(paramKey));
        return kademliaSetupService.kademlia.store(key, value.getBytes());
    }

    @GetMapping(value = "/fetch/{key}")
    public Map<byte[], Integer> fetch(@PathVariable("key") String paramKey){
        Key key = new Key(Integer.parseInt(paramKey));
        try {
            return kademliaSetupService.kademlia.fetch(key, 1);
        } catch (InterruptedException e) {
            LOGGER.error("Could not fetch key {}", key, e);
        }
        return new HashMap<>();
    }

    @GetMapping(value = "/local/{key}")
    public String local(@PathVariable("key") String paramKey){
        Key key = new Key(Integer.parseInt(paramKey));
        return new String(kademliaSetupService.kademlia.fetchLocally(key));
    }
}
