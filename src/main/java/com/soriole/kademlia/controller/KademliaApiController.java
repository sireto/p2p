package com.soriole.kademlia.controller;

import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.model.remote.NodeInfoBean;
import com.soriole.kademlia.model.remote.NodeInfoCollectionBean;
import com.soriole.kademlia.network.ServerShutdownException;
import com.soriole.kademlia.service.KademliaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.SocketException;
import java.util.Collection;
import java.util.NoSuchElementException;

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
        }
        catch(SocketException e){
            LOGGER.error("Socket exception : "+e.getMessage());
            return "Server failed to startAsync : "+e.getMessage();
        }
    }

    @GetMapping(value = "/stop")
    public String stop() {
        if(kademliaService.getDHT().stop()){
           return "STOPPED";
        }
        else{
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
    public int store(@PathVariable("key") String paramKey, @PathVariable("value") String value){
        try {
            this.kademliaService.getDHT().put(new Key(paramKey),value.getBytes());
            return kademliaService.getDHT().put(new Key(paramKey),value.getBytes())+1;

        }catch (ServerShutdownException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @GetMapping(value = "/fetch/{key}")
    public String fetch(@PathVariable("key") String paramKey){
        try {
            return new String(kademliaService.getDHT().get(new Key(paramKey)).getData());
        } catch (ServerShutdownException e) {
            e.printStackTrace();
        }
        catch (NoSuchElementException e){
            LOGGER.info("/fetch/"+paramKey+" : not found!");
        }
        return "Null";
    }

    @GetMapping(value = "/local/{key}")
    public String local(@PathVariable("key") String paramKey){
        // may be null.
        return new String(kademliaService.getDHT().getLocal(new Key(paramKey)).getData());
    }
}
