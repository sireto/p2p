package com.soriole.kademlia.core.util;

import com.soriole.kademlia.core.store.Key;

import java.util.Comparator;

public class KeyComparatorByDistance implements Comparator<Key>{
    private static final long serialVersionUID = 1L;
    private final Key mReferenceKey;

    public KeyComparatorByDistance(Key key) {
        mReferenceKey = key;
    }

    @Override
    public int compare(Key arg0, Key arg1) {
        return mReferenceKey.calculateDistance(arg0).compareTo(mReferenceKey.calculateDistance(arg1));
    }
}
