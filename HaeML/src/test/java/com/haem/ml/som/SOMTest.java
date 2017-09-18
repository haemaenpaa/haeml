package com.haem.ml.som;

import java.util.Arrays;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SOMTest {

  private static final int TEST_ITERS = 1000;

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testIndexCoordinateBijection() {
    for (int iter = 0; iter < TEST_ITERS; iter++) {
      Random rnd = new Random();
      int w = 2 + rnd.nextInt(4);
      int h = 2 + rnd.nextInt(4);
      int d = 2 + rnd.nextInt(4);
      SelfOrganizingMap som = new SelfOrganizingMap(1, w, h, d);
      for (int i = 0; i < w * h * d; i++) {
        double[] coordinates = som.indexToCoordinates(i);
        int index = som.coordinatesToIndex(coordinates);
        Assert.assertTrue("Index-to-coordinates-to-index must be identity.", index == i);
      }
    }
  }

  @Test
  public void testCoordinateCoverage() {
    Random rnd = new Random();
    for (int iter = 0; iter < TEST_ITERS; iter++) {
      int[] dimensions = new int[2 + rnd.nextInt(4)];
      int length = 1;
      for (int i = 0; i < dimensions.length; i++) {
        int w = 2 + rnd.nextInt(4);
        length *= w;
        dimensions[i] = w;
      }
      SelfOrganizingMap som = new SelfOrganizingMap(1, dimensions);
      boolean[] found = new boolean[length];
      recurseCoordinateCoverage(som, found, dimensions);
      for (int i = 0; i < found.length; i++) {
        Assert.assertTrue("Index " + i + " does not match a coordinate", found[i]);
      }
    }
  }

  private void recurseCoordinateCoverage(SelfOrganizingMap som, boolean[] found, int[] dimensions,
      double... previous) {
    if (previous.length == dimensions.length) {
      int index = som.coordinatesToIndex(previous);
      Assert.assertTrue("Index for coordinates must be unique to coordinates", !found[index]);
      found[index] = true;
    } else {
      double[] toPass = Arrays.copyOf(previous, previous.length + 1);
      for (int i = 0; i < dimensions[previous.length]; i++) {
        toPass[previous.length] = i;
        recurseCoordinateCoverage(som, found, dimensions, toPass);
      }
    }
  }

}
