package com.haem.ml.utils;

import java.util.Arrays;

public class Measure {
  public static double euclideanDistance(double[] a, double[] b) {
    double ret = 0;
    for (int i = 0; i < a.length; i++) {
      double diff = a[i] - b[i];
      ret += diff * diff;
    }
    return Math.sqrt(ret);
  }

  public static double dtw(double[] a, double[] b) {
    double[][] preCalc = new double[a.length][b.length];
    for (double[] d : preCalc) {
      Arrays.fill(d, -1);
    }
    return dtw(a, b, 0, 0, preCalc);
  }

  private static double dtw(double[] a, double[] b, int i, int j, double[][] preCalc) {
    if (i >= a.length || j >= b.length) {
      return (i == a.length && j == b.length) ? 0 : Double.POSITIVE_INFINITY;
    }
    if (preCalc[i][j] >= 0) {
      return preCalc[i][j];
    }

    double distance = Math.abs(a[i] - b[j]);

    double matchCost = distance + dtw(a, b, i + 1, j + 1, preCalc);
    double skipCost = Math.min(dtw(a, b, i + 1, j, preCalc), dtw(a, b, i, j + 1, preCalc));

    preCalc[i][j] = Math.min(matchCost, skipCost);

    return preCalc[i][j];
  }
}
