package com.soriole.kademlia.core;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Pair of {@link Key} and associated socket address of a node.
 *
 * @author Grzegorz Milka
 */
public class NodeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Key mKey;
    private final InetSocketAddress mLanAddress;
    private final InetSocketAddress mWanAddress;

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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NodeInfo)) {
            return false;
        }
        NodeInfo other = (NodeInfo) obj;
        if (mKey == null) {
            if (other.mKey != null) {
                return false;
            }
        } else if (!mKey.equals(other.mKey)) {
            return false;
        }
        if (mLanAddress == null) {
            if (other.mLanAddress != null) {
                return false;
            }
        } else if (!mLanAddress.equals(other.mLanAddress)) {
            return false;
        }
        if (mWanAddress == null) {
            if (other.mWanAddress != null) {
                return false;
            }
        } else if (!mWanAddress.equals(other.mWanAddress)) {
            return false;
        }
        return true;
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
        return String.format("NodeInfo[key: %s, lan address: %s, wan address: %s]", mKey, mLanAddress, mWanAddress);
    }
}
