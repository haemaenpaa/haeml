package com.haem.ml.som;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.haem.ml.utils.SamplingStrategy;
import com.haem.ml.utils.Trainer;

public class SOMTrainer implements Trainer {
  public interface LearningCoefficients {
    double alpha(int iteration);

    double theta(int v, int u, int iteration);

  }

  private class WorkerThread extends Thread {
    private AtomicInteger counter;
    private double[][] weightAdjustments;
    private int iterationCount = 0;
    boolean finished = false;

    public WorkerThread(AtomicInteger counter) {
      super();
      this.counter = counter;
    }

    @Override
    public synchronized void run() {
      double[][] oldWeights = target.getWeights();
      weightAdjustments = new double[oldWeights.length][oldWeights[0].length];
      double[] input;
      while (counter.getAndIncrement() < samplingStrategy.cycle()) {
        synchronized (lock) {
          input = samplingStrategy.next();
        }

        int u = target.bestMatchingIndex(input);
        for (int v = 0; v < oldWeights.length; v++) {
          double theta = coefficients.theta(v, u, iteration);
          if (theta <= 0 && (!allowNegativeTheta || theta >= 0)) {
            continue;
          }
          double alpha = coefficients.alpha(iteration);
          for (int j = 0; j < oldWeights[v].length; j++) {
            double diff = input[j] - oldWeights[v][j];
            weightAdjustments[v][j] += alpha * theta * diff;
          }
        }
        iterationCount++;
      }
      finished = true;
    }

    public synchronized boolean waitForFinish() {
      return finished;
    }

    public double[][] getWeightAdjustments() {
      return weightAdjustments;
    }

    public int getIterationCount() {
      return iterationCount;
    }
  }

  private Object lock = new Object();

  private SelfOrganizingMap target;


  private SamplingStrategy samplingStrategy;
  private LearningCoefficients coefficients;

  private int iteration;
  private int threadCount = 4;
  private boolean allowNegativeTheta = false;

  public SOMTrainer(SelfOrganizingMap target, SamplingStrategy samplingStrategy,
      LearningCoefficients coefficients) {
    super();
    this.target = target;
    this.samplingStrategy = samplingStrategy;
    this.coefficients = coefficients;
    this.iteration = 0;
  }

  @Override
  public void iterate() {

    AtomicInteger counter = new AtomicInteger(0);
    List<WorkerThread> threads = new ArrayList<>(threadCount);
    for (int i = 0; i < threadCount; i++) {
      threads.add(new WorkerThread(counter));
    }

    for (WorkerThread w : threads) {
      w.start();
    }
    boolean allFinished = false;
    while (!allFinished) {
      allFinished = true;
      for (WorkerThread w : threads) {
        allFinished = allFinished && w.waitForFinish();
      }
    }

    double[][] weights = target.getWeights();
    for (WorkerThread w : threads) {
      double[][] weightAdjustments = w.getWeightAdjustments();
      double iterationCount = w.getIterationCount();
      for (int i = 0; i < weightAdjustments.length; i++) {
        for (int j = 0; j < weightAdjustments[i].length; j++) {
          weights[i][j] += weightAdjustments[i][j] / iterationCount;
        }
      }
    }
    iteration++;

  }


  @Override
  public void initialize(long randomSeed) {
    double density = 0.25;
    double fill = 1.0;
    Random rnd = new Random(randomSeed);
    initialize(density, fill, rnd);
  }

  public void initialize(double density, double fill, Random rnd) {
    double[][] weights = target.getWeights();
    for (int i = 0; i < weights.length; i++) {
      for (int j = 0; j < weights[i].length; j++) {
        if (rnd.nextDouble() < density) {
          weights[i][j] = rnd.nextDouble() * 2 - 1;
        } else {
          weights[i][j] = fill;
        }
      }
    }
  }

}
