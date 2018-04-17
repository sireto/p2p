package com.soriole.kademlia.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class KeyTest {
  @Test
  public void shouldReturnCorrectDistance() {
    Key zero = new Key(0);
    Key one = new Key(1);
    assertEquals(0, zero.getDistanceBit(one));
  }

  @Test
  public void shouldReturnCorrectDistance2() {
    Key one = new Key(1);
    Key two = new Key(2);
    assertEquals(1, one.getDistanceBit(two));
  }
}
