package com.haem.ml.utils;

public interface SamplingStrategy {
  /**
   * Returns the next sample to learn on.
   * 
   * @return
   */
  double[] next();

  /**
   * Returns the notional "sample size" of the strategy, i.e. how many samples should be iterated
   * before the whole dataset can be said to be iterated through.
   * 
   * @return
   */
  int cycle();
}
