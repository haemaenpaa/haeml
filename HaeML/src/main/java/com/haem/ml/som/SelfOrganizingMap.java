package com.haem.ml.som;

import java.io.Serializable;

import com.haem.ml.utils.Measure;

public class SelfOrganizingMap implements Serializable {
  private static final long serialVersionUID = 1L;

  private double[][] weights;

  private int[] gridSizes;
  private int[] gridFactors;

  public SelfOrganizingMap(int dimension, int... gridSizes) {
    this.gridSizes = gridSizes;
    this.gridFactors = new int[gridSizes.length];
    gridFactors[gridSizes.length - 1] = 1;
    for (int i = gridSizes.length - 2; i >= 0; i--) {
      gridFactors[i] = gridSizes[i + 1] * gridFactors[i + 1];
    }
    int size = gridFactors[0] * gridSizes[0];
    this.weights = new double[size][dimension];
  }

  public double[] indexToCoordinates(int index) {
    double[] ret = new double[gridSizes.length];
    for (int i = 0; i < gridSizes.length; i++) {
      ret[i] = index / gridFactors[i];
      index -= ret[i] * gridFactors[i];
    }
    return ret;
  }

  public int coordinatesToIndex(double... coordinates) {
    int ret = 0;
    for (int i = 0; i < gridSizes.length; i++) {
      ret += gridFactors[i] * coordinates[i];
    }
    return ret;
  }

  /**
   * Returns the best matching weight vector.
   * 
   * @param input
   * @return
   */
  public double[] bestMatching(double[] input) {
    int closestIndex = bestMatchingIndex(input);
    return weights[closestIndex].clone();
  }

  /**
   * Returns the index of the best matching weight vector.
   * 
   * @param input
   * @return
   */
  public int bestMatchingIndex(double[] input) {
    int closestIndex = 0;
    double closest = Double.POSITIVE_INFINITY;
    for (int i = 0; i < weights.length; i++) {
      double euclideanDistance = Measure.euclideanDistance(input, weights[i]);
      if (euclideanDistance < closest) {
        closest = euclideanDistance;
        closestIndex = i;
      }
    }
    return closestIndex;
  }

  public double[][] getWeights() {
    return weights;
  }

  public void setWeights(double[][] weights) {
    this.weights = weights;
  }
}
