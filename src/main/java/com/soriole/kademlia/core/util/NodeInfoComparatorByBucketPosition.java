package com.soriole.kademlia.core.util;

import com.soriole.kademlia.core.store.Key;
import com.soriole.kademlia.core.store.NodeInfo;

import java.util.Comparator;

public class NodeInfoComparatorByBucketPosition implements Comparator<NodeInfo> {
    KeyComparatorByBucketPosition comparator;
    public NodeInfoComparatorByBucketPosition(NodeInfo node){
        comparator=new KeyComparatorByBucketPosition(node.getKey());
    }
    public NodeInfoComparatorByBucketPosition(Key key){
        comparator=new KeyComparatorByBucketPosition(key);
    }
    @Override
    public int compare(NodeInfo o1, NodeInfo o2) {
        return comparator.compare(o1.getKey(),o2.getKey());
    }
}
