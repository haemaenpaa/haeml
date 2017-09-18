package com.haem.ml.utils;

/**
 * An interface for trainers of Machine Learning algorithms
 * 
 * @author heikki
 *
 */
public interface Trainer {
  /**
   * Advances the training by one iteration.
   */
  public void iterate();

  /**
   * Initializes the target algorithm with the given random seed.
   * 
   * @param randomSeed
   */
  public void initialize(long randomSeed);
}
