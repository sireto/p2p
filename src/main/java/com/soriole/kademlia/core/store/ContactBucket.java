package com.soriole.kademlia.core.store;

import com.soriole.kademlia.core.util.BoundedSortedSet;
import com.soriole.kademlia.core.util.KeyByBucketPositionComparator;
import com.soriole.kademlia.core.util.NodeInfoComparatorByBucketPosition;
import java.util.*;

/**
 * handles the storage of NodeInfo as per the kademlia specification.
 */
public class ContactBucket {

    SortedSet<Contact>[] buckets;

    // may be required.
    int contactCount;

    public final int k;
    private NodeInfo localNode;

    public ContactBucket(NodeInfo localNode, int bitLength, int k) {
        this.localNode = localNode;
        this.k = k;
        buckets = new SortedSet[bitLength];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new TreeSet<Contact>();
        }
    }

    synchronized public NodeInfo getNode(Key key) {
        int distance = key.getBucketPosition(localNode.getKey());
        if(distance<0){
            return getLocalNode();
        }
        Contact contact = new Contact(new NodeInfo(key));
        for (Contact c : buckets[distance]) {
            if (c.equals(contact))
                return c.info;
        }
        return null;
    }

    private Contact getContact(Key key) {

        int distance = key.getBucketPosition(localNode.getKey());
        Contact contact = new Contact(new NodeInfo(key));
        for (Contact c : buckets[distance]) {
            if (c.equals(contact))
                return c;
        }
        return null;
    }

    synchronized public boolean putNode(NodeInfo info) {
        int distance = info.getKey().getBucketPosition(localNode.getKey());
        if(distance<0){
            return true;
        }
        if (buckets[distance].contains(new Contact(info))) {
            Contact contact = null;
            for (Contact c : buckets[distance]) {
                if (c.equals(info))
                    contact=c;
                    break;
            }
            assert(contact!=null);
            contact.lastActive = new Date();
            return true;
        } else if (buckets[distance].size() == k) {
            return false;
        } else {
            Contact c = new Contact(info);
            buckets[distance].add(c);
            return true;
        }
    }
    public void putNodeForced(NodeInfo info){
        int distance = info.getKey().getBucketPosition(localNode.getKey());
        if(distance<0){
            return;
        }
        if (buckets[distance].contains(new Contact(info))) {
            Contact contact = null;
            for (Contact c : buckets[distance]) {
                if (c.equals(info))
                    contact=c;
                break;
            }
            assert(contact!=null);
            contact.lastActive = new Date();
        } else if (buckets[distance].size() == k) {
            SortedSet set=new TreeSet<Contact>(ContactComparatorByLastActive.getDefaultInstance());
            set.addAll(buckets[distance]);
            buckets[distance].remove(set.first());
            buckets[distance].add(new Contact(info));

        } else {
            Contact c = new Contact(info);
            buckets[distance].add(c);
        }
    }
    public NodeInfo putOrGetChallenger(NodeInfo info){
        int distance = info.getKey().getBucketPosition(localNode.getKey());
        if(distance<0){
            return null;
        }
        if (buckets[distance].contains(new Contact(info))) {
            Contact contact = null;
            for (Contact c : buckets[distance]) {
                if (c.equals(info))
                    contact=c;
                break;
            }
            assert(contact!=null);
            contact.lastActive = new Date();
            return null;
        } else if (buckets[distance].size() == k) {
            SortedSet set=new TreeSet<Contact>(ContactComparatorByLastActive.getDefaultInstance());
            set.addAll(buckets[distance]);
            return ((Contact)set.first()).info;

        } else {
            Contact c = new Contact(info);
            buckets[distance].add(c);
            return null;
        }
    }

    synchronized public boolean removeNode(Key key){
        int distance=key.getBucketPosition(localNode.getKey());
        if(distance<0){
            return false;
        }
        return buckets[distance].remove(new Contact(new NodeInfo(key)));
    }

    synchronized public Collection<NodeInfo> getClosestNodes(Key key) {
        /// TODO: This can be optimized.
        Set<NodeInfo> closestNodes = new BoundedSortedSet<>(k, new NodeInfoComparatorByBucketPosition(key));
        for (int i = 0; i < buckets.length; i++) {
            for (Contact c : buckets[i]) {
                closestNodes.add(c.info);
            }
        }
        return closestNodes;
    }

    synchronized public Collection<NodeInfo> getAllNodes() {
        ArrayList<NodeInfo> allNode = new ArrayList<>(20);
        for (int i = 0; i < buckets.length; i++) {
            for (Contact c : buckets[i]) {
                allNode.add(c.info);
            }
        }
        return allNode;
    }

    public NodeInfo getLocalNode() {
        return localNode;
    }

    synchronized public void putAllNodes(Collection<NodeInfo> collection) {
        for (NodeInfo n : collection) {
            this.putNode(n);
        }
    }

}

class Contact implements Comparable{
    // node data
    NodeInfo info;

    // the last time we contacted with this node.
    Date lastActive;

    // the first time we saw this node.
    Date firstSeen;

    public Contact() {
    }

    public Contact(NodeInfo info) {
        this.info = info;
        this.firstSeen = new Date();
        this.lastActive = firstSeen;
    }

    @Override
    public boolean equals(Object o) {
        return compareTo(o)==0;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof Contact)
            return info.compareTo(((Contact) o).info);
        if(o instanceof NodeInfo)
            return info.compareTo(o);
        if (o instanceof Key)
            return info.getKey().compareTo(o);
        throw new IllegalArgumentException("Type Contact cannot be compared with "+o.getClass().getName());
    }
}

class ContactComparatorByBucketPosition implements Comparator<Contact> {

    KeyByBucketPositionComparator comparator;
    public ContactComparatorByBucketPosition(KeyByBucketPositionComparator comparator){
        this.comparator=comparator;
    }
    @Override
    public int compare(Contact o1, Contact o2) {
        return comparator.compare(o1.info.getKey(),o2.info.getKey());
    }
}

class ContactComparatorByFirstSeen implements Comparator<Contact>{
    private static ContactComparatorByFirstSeen comparator;
    @Override
    public int compare(Contact o1, Contact o2) {
        return o1.firstSeen.compareTo(o2.firstSeen);
    }
    public static ContactComparatorByFirstSeen getDefaultInstance(){
        return comparator;
    }

}

class ContactComparatorByLastActive implements Comparator<Contact>{
    private static ContactComparatorByLastActive comparator;
    @Override
    public int compare(Contact o1, Contact o2) {
        return o1.lastActive.compareTo(o2.lastActive);
    }
    public static ContactComparatorByLastActive getDefaultInstance(){
        return comparator;
    }

}