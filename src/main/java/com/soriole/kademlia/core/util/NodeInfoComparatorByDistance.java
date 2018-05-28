package com.soriole.kademlia.core.util;

import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;

import java.util.Comparator;

public class NodeInfoComparatorByDistance implements Comparator<NodeInfo> {
    KeyComparatorByDistance comparator;

    public NodeInfoComparatorByDistance(NodeInfo node){
        comparator=new KeyComparatorByDistance(node.getKey());
    }
    public NodeInfoComparatorByDistance(Key key){
        comparator=new KeyComparatorByDistance(key);
    }
    @Override
    public int compare(NodeInfo o1, NodeInfo o2) {
        return comparator.compare(o1.getKey(),o2.getKey());
    }
}
