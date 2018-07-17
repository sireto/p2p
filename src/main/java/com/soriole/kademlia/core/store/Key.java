package com.soriole.kademlia.core.store;

import com.soriole.kademlia.core.util.Base58;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Random;

/**
 * Immutable 160 bit long Kademlia key.
 *
 * @author Grzegorz Milka
 */
public class Key implements ByteSerializable, Comparable {
    public static final int KEY_LENGTH = 160;
    private static BigInteger MAX_KEY = new BigInteger("ffffffffffffffffffffffffffffffffffffffff", 16);
    private BigInteger key;

    /**
     * Creates a key from integer in a little-endian bit fashion.
     *
     * @param key nonnegative number
     * @throws IllegalArgumentException
     */
    private Key(BigInteger key){
        this.construct(key);
    }

    private void construct(BigInteger key) {
        if (key.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Key should be a nonnegative number.");
        }
        if (key.compareTo(MAX_KEY) > 0) {
            throw new IllegalArgumentException("Key size very large");
        }
        // create new copy of the biginteger.
        this.key = new BigInteger(1, key.toByteArray());
    }

    public Key(byte[] keyByte) throws IllegalArgumentException {
        this(new BigInteger(1, keyByte));
    }

    public Key(String base58Key) throws IllegalArgumentException {

        try {
            this.construct(new BigInteger(1, Base58.fromBase58(base58Key)));
        } catch (Base58.ValidationException e) {
            throw new IllegalArgumentException("Invalid Base58 string");
        }
    }


    /**
     * @return distance {@link BitSet} between two keys in little-endian encoding.
     */
    public BigInteger calculateDistance(Key otherKey) {
        return key.xor(otherKey.key);
    }

    /**
     * Following assertion is true: assert (new Key(1)).getBucketPosition(new Key(2))
     * == 1
     *
     * @return most significant bit index of distance between this key and
     * argument.
     */
    public int getBucketPosition(Key otherKey) {
        BigInteger b1 = key.xor(otherKey.key);
        int k;
        if (b1.equals(BigInteger.ZERO)) {
            return -1;
        }
        return b1.bitLength() - 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Key)) {
            return false;
        }
        return this.key.equals(((Key) obj).key);
    }

    /**
     * @return little-endian encoding of this key
     */

    @Override
    public int hashCode() {
        return key.hashCode();
    }


    @Override
    public String toString() {
        return Base58.toBase58(key.toByteArray());
    }

    @Override
    public byte[] toBytes() {
        return this.key.toByteArray();
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return 1;
        }
        if (o instanceof Key)
            return key.compareTo(((Key) o).key);
        throw new IllegalArgumentException("Type Key cannot be compared with " + o.getClass().getName());
    }

    public static Key gemerateNew() {
        byte[] randomBytes = new byte[KEY_LENGTH / 8];
        new Random().nextBytes(randomBytes);
        return new Key(randomBytes);

    }
}
