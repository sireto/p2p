package com.soriole.kademlia.controller;

import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;
import com.soriole.kademlia.model.remote.NodeInfoBean;
import com.soriole.kademlia.model.remote.NodeInfoCollectionBean;
import com.soriole.kademlia.service.KademliaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.net.SocketException;
import java.util.Collection;

@RestController
@RequestMapping(value = "/api/kademlia/v1")
public class KademliaApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaApiController.class);

    @Autowired
    KademliaService kademliaService;

    @GetMapping(value = "/hello")
    public String hello(){
        return "Hello DHT";
    }

    @GetMapping(value = "/start")
    public String start() {
        try {
            if (kademliaService.getKademliaProtocol().server.start()) {
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
        try {
            if(kademliaService.getKademliaProtocol().server.stop(2)){
               return "STOPPED";
            }
            else{
                return "Server was already not running.";
            }
        } catch (InterruptedException e) {
            //  nobody interrupts this thread here.
            e.printStackTrace();
        }
        return "Unexpected condition";
    }

    @GetMapping(value = "/routing_table")
    public NodeInfoCollectionBean getRoutingTable() {
        LOGGER.info("getRoutingTable()");
        Collection<NodeInfo> nodeInfos;
        nodeInfos = kademliaService.getKademliaProtocol().bucket.getAllNodes();
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
    public NodeInfoCollectionBean findNodes(@PathParam("key") String paramKey) {
        LOGGER.info("findNodes({})", paramKey);
        Key key = new Key(paramKey);

        Collection<NodeInfo> nodeInfos = null;
        nodeInfos = kademliaService.getKademliaProtocol().findClosestNodes(key);

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
        return kademliaService.getKademliaProtocol().bucket.getLocalNode().getKey().toString();
    }
}
