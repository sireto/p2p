package com.soriole.kademlia.core.store;

import com.soriole.kademlia.core.KademliaConfig;
import com.soriole.kademlia.core.util.BoundedSortedSet;
import com.soriole.kademlia.core.util.NodeInfoComparatorByDistance;

import java.util.*;

/**
 * handles the storage of NodeInfo as per the kademlia specification.
 */
public class ContactBucket {

    SortedSet<Contact>[] buckets;
    SortedSet<Contact> contactsByLastSeen=new TreeSet<>(Contact.getComparatorByLastActive());

    // may be required.
    int contactCount;

    public final int k;
    private NodeInfo localNode;

    private ContactBucket(NodeInfo localNode, int bitLength, int k) {
        this.localNode = localNode;
        this.k = k;
        buckets = new SortedSet[bitLength];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new TreeSet<Contact>();
        }
    }

    public ContactBucket(NodeInfo localNode,KademliaConfig config){
        this(localNode,config.getKeyLength(),config.getK());

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
                if (c.equals(info)) {
                    contact = c;
                    break;
                }

            }
            assert(contact!=null);
            contactsByLastSeen.remove(contact);
            contact.lastActive = new Date();
            contactsByLastSeen.add(contact);
            return true;
        } else if (buckets[distance].size() == k) {
            return false;
        } else {
            Contact c = new Contact(info);
            buckets[distance].add(c);
            contactsByLastSeen.add(c);
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
                if (c.equals(info)) {
                    contact = c;
                    break;
                }
            }
            assert(contact!=null);
            contact.lastActive = new Date();
        } else if (buckets[distance].size() == k) {
            //Todo: find some other roundAboutWay
            SortedSet set=new TreeSet<Contact>(Contact.getComparatorByLastActive());
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
            SortedSet set=new TreeSet<Contact>(Contact.getComparatorByLastActive());
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
        contactsByLastSeen.remove(getContact(key));
        return buckets[distance].remove(new Contact(new NodeInfo(key)));
    }

    /**
     *  -- Find the buket position for the node.
     *  -- Add all contacts in that bucket.
     *  -- Traverse both left and right simultaneously until BoundeSortedSet is not full.
     *
     */
    synchronized public BoundedSortedSet<NodeInfo> getClosestNodes(Key key, int count) {

        BoundedNodeInfoSet closestNodes = new BoundedNodeInfoSet(count+1, key);

        int pos = getLocalNode().getKey().getBucketPosition(key);
        if(pos<0){
            pos++;
        }
        // put all nodes in that bucket to the list
        closestNodes.addAllContacts(buckets[pos]);

        int i = 1;

        while (!closestNodes.isFull()) {
            if ((pos - i) < 0) {
                while (!closestNodes.isFull() && i < buckets.length) {
                    closestNodes.addAllContacts(buckets[i]);
                    i++;
                }
                break;

            } else if (pos + i >= buckets.length) {
                i=pos-i;
                while (!closestNodes.isFull() && i > -1) {
                    closestNodes.addAllContacts(buckets[i]);
                    i--;
                }
                break;

            }
            closestNodes.addAllContacts(buckets[pos-i]);
            closestNodes.addAllContacts(buckets[pos+i]);
            i++;
        }
        return closestNodes;
    }

    public BoundedSortedSet<NodeInfo> getClosestNodes(Key key) {
        return getClosestNodes(key, k);
    }
    synchronized public void clearAll(){
        for(int i=0;i<this.buckets.length;i++){
            this.buckets[i].clear();
        }

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

    synchronized public boolean  putAllNodes(Collection<NodeInfo> collection) {
        boolean ret=true;
        for (NodeInfo n : collection) {
            ret &= this.putNode(n);
        }
        return ret;
    }
    synchronized public Contact getMostInactiveContact(){
        return this.contactsByLastSeen.first();
    }
    @Override
    public String toString(){
        return getAllNodes().toString();
    }
    private static final class BoundedNodeInfoSet extends BoundedSortedSet<NodeInfo> {

        public BoundedNodeInfoSet(int upperBound, Key k) {
            super(upperBound, new NodeInfoComparatorByDistance(new NodeInfo(k)));
        }

        void addAllContacts(Collection<Contact> cc) {
            for (Contact c : cc) {
                add(c.info);
            }
        }
    }
}



