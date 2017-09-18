package com.haem.ml.rbm;

import java.util.Arrays;
import java.util.Random;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;

public class RestrictedBoltzmannMachine implements Cloneable {
  private double[][] weights;

  private double[] visibleBias;
  private double[] hiddenBias;


  private ActivationFunction function;

  public ActivationFunction getFunction() {
    return function;
  }

  public void setFunction(ActivationFunction function) {
    this.function = function;
  }

  public RestrictedBoltzmannMachine(int visible, int hidden) {
    weights = new double[visible][hidden];
    visibleBias = new double[visible];
    hiddenBias = new double[hidden];
    this.function = new ActivationSigmoid();
  }

  public double[] visibleProbabilities(boolean[] hidden) {
    double[] ret = new double[visibleBias.length];
    for (int i = 0; i < ret.length; i++) {
      double wh = visibleBias[i];
      for (int j = 0; j < hiddenBias.length; j++) {
        wh += (hidden[j] ? weights[i][j] : 0);
      }
      ret[i] = wh;
    }
    function.activationFunction(ret, 0, ret.length);
    return ret;
  }

  public double[] hiddenProbabilities(boolean[] visible) {
    double[] ret = new double[hiddenBias.length];
    for (int i = 0; i < ret.length; i++) {
      double wh = hiddenBias[i];
      for (int j = 0; j < hiddenBias.length; j++) {
        wh += (visible[j] ? weights[j][i] : 0);
      }
      ret[i] = wh;
    }
    function.activationFunction(ret, 0, ret.length);
    return ret;
  }

  public boolean[] hiddenState(boolean[] visibleState) {

    double[] probabilities = hiddenProbabilities(visibleState);
    boolean[] ret = probabilitiesToBoolean(probabilities);
    return ret;
  }

  public boolean[] visibleState(boolean[] visibleState) {
    double[] probabilities = visibleProbabilities(visibleState);
    return probabilitiesToBoolean(probabilities);
  }

  public double[] hiddenProbabilities(double[] visible) {
    double[] ret = new double[hiddenBias.length];
    for (int i = 0; i < ret.length; i++) {
      double wh = hiddenBias[i];
      for (int j = 0; j < hiddenBias.length; j++) {
        wh += visible[j] * weights[j][i];
      }
      ret[i] = wh;
    }
    function.activationFunction(ret, 0, ret.length);
    return ret;
  }

  public void reset() {
    this.reset(new Random());
  }

  public void reset(Random random) {
    for (int i = 0; i < weights.length; i++) {
      for (int j = 0; j < weights[i].length; j++) {
        weights[i][j] = random.nextGaussian() * 0.01;
      }
    }
  }

  public double getWeight(int i, int j) {
    return weights[i][j];
  }

  public void setWeight(double w, int i, int j) {
    if (!Double.isFinite(w)) {
      throw new IllegalArgumentException("Attempt to set non-finite weight " + w);
    }
    weights[i][j] = w;
  }

  public double getVisibleBias(int i) {
    return visibleBias[i];
  }

  public void setVisibleBias(int i, double b) {
    visibleBias[i] = b;
  }

  public double getHiddenBias(int j) {
    return hiddenBias[j];
  }

  public void setHiddenBias(int j, double b) {
    hiddenBias[j] = b;
  }

  public int visibleCount() {
    return visibleBias.length;
  }

  public int hiddenCount() {
    return hiddenBias.length;
  }

  public static boolean[] probabilitiesToBoolean(double[] probabilities) {
    boolean[] ret = new boolean[probabilities.length];
    for (int i = 0; i < probabilities.length; i++) {
      ret[i] = Math.random() < probabilities[i];
    }
    return ret;
  }

  public RestrictedBoltzmannMachine clone() {
    RestrictedBoltzmannMachine ret = new RestrictedBoltzmannMachine(visibleCount(), hiddenCount());
    ret.visibleBias = visibleBias.clone();
    ret.hiddenBias = hiddenBias.clone();

    for (int i = 0; i < weights.length; i++) {
      ret.weights[i] = weights[i].clone();
    }

    ret.function = this.function;

    return ret;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((function == null) ? 0 : function.hashCode());
    result = prime * result + Arrays.hashCode(hiddenBias);
    result = prime * result + Arrays.hashCode(visibleBias);
    result = prime * result + Arrays.deepHashCode(weights);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RestrictedBoltzmannMachine other = (RestrictedBoltzmannMachine) obj;
    if (function == null) {
      if (other.function != null)
        return false;
    } else if (!function.equals(other.function))
      return false;
    if (!Arrays.equals(hiddenBias, other.hiddenBias))
      return false;
    if (!Arrays.equals(visibleBias, other.visibleBias))
      return false;
    if (!Arrays.deepEquals(weights, other.weights))
      return false;
    return true;
  }
}
