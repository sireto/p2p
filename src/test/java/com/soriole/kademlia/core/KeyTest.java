package com.soriole.kademlia.core;

import com.soriole.kademlia.core.store.Key;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

import static org.junit.Assert.assertEquals;

public final class KeyTest {
    Key zero = new Key("0");
    Key one = new Key("1");
    Key two = new Key("2");
    Key ffff=new Key ("ffff");

  @Test
  public void shouldReturnCorrectDistance() {

    assertEquals(zero.calculateDistance(one),
            BigInteger.valueOf(0^1));

    assertEquals(one.calculateDistance(ffff),
            BigInteger.valueOf(0xffff^1));

    assertEquals(two.calculateDistance(ffff),
            BigInteger.valueOf(0xffff^2));
  }

  @Test
  public void shouldSerializeProperly(){

    String startKeyString="bd3423afc9";

    // craete a key from the string
    Key k=new Key(startKeyString);

    //check equality of strings when using Key.toString()
    assert(startKeyString.equals(k.toString()));


    // check equality of bytes when using Key.toBytes()
    assert(Arrays.equals(k.toBytes(),
            new Key(k.toBytes()).toBytes()));

    // check equality of Key when Key(byte[]) is used
    assertEquals(k,new Key(k.toBytes()));

  }
  @Test
  public void testBucketPosition(){

      assertEquals(1,0,one.getBucketPosition(two));

      assertEquals(0,new Key("fffe").getBucketPosition(ffff));

      assertEquals(159,new Key("ffffffffffffffffffffffffffffffffffffffff").getBucketPosition(zero));

      assertEquals(160/2-1,new Key("0fffffffffffffffffff").getBucketPosition(new Key("8abcde12343ffff41adf")));
  }
}
