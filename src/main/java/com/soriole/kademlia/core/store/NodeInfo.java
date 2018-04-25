package com.soriole.kademlia.core.store;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Pair of {@link Key} and associated socket address of a node.
 *
 * @author Grzegorz Milka
 */
public class NodeInfo implements Serializable,Comparable{
    private static final long serialVersionUID = 1L;

    private final Key mKey;
    private InetSocketAddress mLanAddress;
    private InetSocketAddress mWanAddress;

    public NodeInfo(Key key) {
        mKey = key;
        mLanAddress = null;
        mWanAddress = null;
    }

    public NodeInfo(Key key, InetSocketAddress lanAddress) {
        mKey = key;
        mLanAddress = lanAddress;
        mWanAddress = null;
    }

    public NodeInfo(Key key, InetSocketAddress lanAddress, InetSocketAddress wanAddress) {
        mKey = key;
        mLanAddress = lanAddress;
        mWanAddress = wanAddress;
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo(obj)==0;
    }

    public Key getKey() {
        return mKey;
    }

    public InetSocketAddress getLanAddress() {
        return mLanAddress;
    }

    public InetSocketAddress getWanAddress() {
        return mWanAddress;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mKey == null) ? 0 : mKey.hashCode());
        result = prime * result + ((mLanAddress == null) ? 0 : mLanAddress.hashCode());
        result = prime * result + ((mWanAddress == null) ? 0 : mWanAddress.hashCode());
        return result;
    }

    @Override
    public String toString() {
        if(getKey()==null){
            return "nullNodeInfo";
        }
        return getKey().toString();

    }
    public String toDetailString(){
        return String.format("NodeInfo[key: %s, lan address: %s, wan address: %s]", mKey, mLanAddress, mWanAddress);
    }

    public void setLanAddress(InetSocketAddress mLanAddress) {
        // the method need not be synchronized, because assignment of reference is atomic operation.
        this.mLanAddress = mLanAddress;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof NodeInfo)
            return getKey().compareTo(((NodeInfo) o).getKey());
        if(o instanceof Key)
            return getKey().compareTo(o);
        throw new IllegalArgumentException("Type NodeInfo cannot be compared with "+o.getClass().getName());
    }
    public NodeInfo clone(){
        return new NodeInfo( getKey(),getLanAddress(),getWanAddress());
    }
}
