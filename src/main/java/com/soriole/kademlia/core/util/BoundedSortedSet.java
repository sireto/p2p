package com.soriole.kademlia.core.util;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Bounded sorted set helps to get the first n items in the collection,
 * when sorted in ascending order.
 *
 * This is used for finding closest nodes in kademlia routhing table.
 * @param <Type> Type of data to store.
 */
public class BoundedSortedSet<Type> extends TreeSet<Type>{
    int mUpperCountBound;

    public BoundedSortedSet(int upperBound, Comparator<Type> comparator){
        super(comparator);
        this.mUpperCountBound=upperBound;
    }
    @Override
    public boolean add(Type element) {
        boolean hasBeenAdded = super.add(element);
        if (!hasBeenAdded) {
            return hasBeenAdded;
        }

        if (this.size() > mUpperCountBound) {
            assert this.size() == mUpperCountBound + 1;
            return this.comparator().compare(element,pollLast()) ==0;
        } else {
            return true;
        }
    }
    public boolean isFull(){
        return this.size()==this.mUpperCountBound;
    }
}
