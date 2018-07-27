package com.soriole.kademlia.core.store;

import com.soriole.kademlia.core.util.KeyComparatorByBucketPosition;
import java.util.Comparator;
import java.util.Date;

public class Contact implements Comparable {
    // node data
    NodeInfo info;

    // the last time we contacted with this node.
    public Date lastActive;

    // the first time we saw this node.
    public Date firstSeen;

    public Contact() {
    }

    public Contact(NodeInfo info) {
        this.info = info;
        this.firstSeen = new Date();
        this.lastActive = firstSeen;
    }
    public Date getLastActive(){
        return lastActive;
    }
    public Date getFirstSeen(){
        return firstSeen;
    }
    public NodeInfo getNodeInfo(){
        return info;
    }

    @Override
    public boolean equals(Object o) {
        return compareTo(o) == 0;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Contact)
            return info.compareTo(((Contact) o).info);
        if (o instanceof NodeInfo)
            return info.compareTo(o);
        if (o instanceof Key)
            return info.getKey().compareTo(o);
        throw new IllegalArgumentException("Type Contact cannot be compared with " + o.getClass().getName());
    }

    public static Comparator<Contact> getComparatorByBucketPosition(final Key key) {
        return new Comparator<Contact>() {
            KeyComparatorByBucketPosition comparator=new KeyComparatorByBucketPosition(key);

            @Override
            public int compare(Contact o1, Contact o2) {
                return comparator.compare(o1.info.getKey(), o2.info.getKey());
            }
        };
    }

    public static Comparator<Contact> getComparatorByFirstSeen() {
        return Comparator.comparing(o -> o.firstSeen);

    }

    public static Comparator<Contact> getComparatorByLastActive() {
        return Comparator.comparing(o -> o.lastActive);
    }


}
