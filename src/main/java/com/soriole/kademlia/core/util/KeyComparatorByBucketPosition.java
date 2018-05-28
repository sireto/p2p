package com.soriole.kademlia.core.util;// the classes below might be required for the contactBucket operations

import com.soriole.kademlia.core.store.Key;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.Comparator;

/**
 * Comparator of Kademlia {@link Key} with specified key based on closeness.
 *
 * @author Grzegorz Milka
 */
public class KeyComparatorByBucketPosition implements Comparator<Key>, Serializable {
    private static final long serialVersionUID = 1L;
    private final Key mReferenceKey;

    public KeyComparatorByBucketPosition(Key key) {
        mReferenceKey = key;
    }

    @Override
    public int compare(Key arg0, Key arg1) {
        return Integer.compare(
                mReferenceKey.getBucketPosition(arg0),
                mReferenceKey.getBucketPosition(arg1));

    }
}
