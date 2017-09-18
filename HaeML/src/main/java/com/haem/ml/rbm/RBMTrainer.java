package com.haem.ml.rbm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.haem.ml.utils.Trainer;

public class RBMTrainer implements Trainer {
  private double error;
  private int iteration = 0;
  private double learningRate;

  private double minLearningRate = 1e-5;
  private double momentum = 0.5;
  private double[][] previousDeltas;
  private double previousError = Double.POSITIVE_INFINITY;
  private double pTarget = 0.1;
  private double qDecay = 0.9;
  private double[] qOld;
  private Random random;
  private double sparsityCost;

  private RestrictedBoltzmannMachine target;

  private List<double[]> testData;
  private List<double[]> validationData;

  private List<List<double[]>> trainingData;

  private double weightCost = 1e-4;
  private double overfit;
  private double[] previousDeltasH;
  private double[] previousDeltasV;
  private int cdCount = 1;

  public RBMTrainer(double learningRate, RestrictedBoltzmannMachine target,
      List<double[]>... dataSets) {
    super();
    this.learningRate = learningRate;
    this.target = target;
    this.trainingData = new ArrayList<>();
    for (int i = 0; i < dataSets.length; i++) {
      this.trainingData.add(dataSets[i]);
    }
    this.testData = new ArrayList<>();
    this.weightCost = Math.max(10 / (target.hiddenCount() * target.visibleCount()), 1e-3);
    this.sparsityCost = 0.01;
    this.error = target.hiddenCount() * target.visibleCount();
  }

  private synchronized void applyWeightAdjust(double[][] weightAdjust, double[] visibleBiasAdjust,
      double[] hiddenBiasAdjust) {
    if (previousDeltas == null) {
      previousDeltas = new double[weightAdjust.length][weightAdjust[0].length];
      previousDeltasV = new double[visibleBiasAdjust.length];
      previousDeltasH = new double[hiddenBiasAdjust.length];
    }
    double penalty = calculateWeightPenalty();
    updateQ();
    double[] sparsityAdjust = new double[target.hiddenCount()];
    for (int j = 0; j < target.hiddenCount(); j++) {
      sparsityAdjust[j] = sparsityCost * (qOld[j] - pTarget);
      double deltaB =
          learningRate * hiddenBiasAdjust[j] - sparsityAdjust[j] + momentum * previousDeltasH[j];
      previousDeltasH[j] = deltaB;
      hiddenBiasAdjust[j] = 0;
      target.setHiddenBias(j, target.getHiddenBias(j) + deltaB);
    }

    for (int i = 0; i < weightAdjust.length; i++) {

      double deltaB = learningRate * visibleBiasAdjust[i] + momentum * previousDeltasV[i];
      previousDeltasV[i] = 0;
      visibleBiasAdjust[i] = 0;
      target.setVisibleBias(i, target.getVisibleBias(i) + deltaB);

      for (int j = 0; j < weightAdjust[i].length; j++) {
        double delta = learningRate * (weightAdjust[i][j] - penalty)
            + momentum * previousDeltas[i][j] - sparsityAdjust[j];
        target.setWeight(target.getWeight(i, j) + delta, i, j);
        previousDeltas[i][j] = delta;
        weightAdjust[i][j] = 0;
      }
    }
  }


  private double calculateWeightPenalty() {
    BigDecimal l2 = new BigDecimal(0);
    double minWeight = Double.POSITIVE_INFINITY;
    double maxWeight = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < target.visibleCount(); i++) {
      for (int j = 0; j < target.hiddenCount(); j++) {
        double w = target.getWeight(i, j);
        l2 = l2.add(new BigDecimal(w * weightCost));
        if (w > maxWeight) {
          maxWeight = w;
        }
        if (w < minWeight) {
          w = minWeight;
        }
      }
    }
    double ret = l2.doubleValue();
    if (Double.isInfinite(ret)) {
      ret = Math.signum(ret);
    }

    return ret;
  }

  public double getError() {
    return error;
  }

  public int getIteration() {
    return iteration;
  }

  public double getLearningRate() {
    return learningRate;
  }

  public List<List<double[]>> getTrainingData() {
    return trainingData;
  }

  public void initialize(long randomSeed) {
    this.random = new Random(randomSeed);
    target.reset(this.random);

    double[] probability = new double[target.visibleCount()];
    int count = 0;
    for (List<double[]> list : trainingData) {
      count += list.size();
    }
    for (List<double[]> list : trainingData) {
      for (double[] input : list) {
        for (int i = 0; i < input.length; i++) {
          probability[i] += input[i] / count;
        }
      }
    }

    for (int i = 0; i < target.visibleCount(); i++) {
      double bias = Math.log(probability[i] / (1 - probability[i]));
      bias = Math.max(-3, bias);
      bias = Math.min(3, bias);
      target.setVisibleBias(i, bias);
    }
    int testSize = (int) (0.2 * trainingData.size() + 0.5);
    if (testData.isEmpty()) {
      for (int i = 0; i < testSize; i += trainingData.size()) {
        for (List<double[]> list : trainingData) {
          testData.add(list.remove(random.nextInt(list.size())));
        }
      }
    } else {
      testSize = testData.size();
    }
    validationData = new ArrayList<>();
    for (int i = 0; i < testSize; i += trainingData.size()) {
      for (List<double[]> list : trainingData) {
        testData.add(list.get(i));
      }
    }
  }

  public void iterate() {
    if (random == null) {
      throw new Error("You must initialize the trainer or set the random generator.");
    }

    Collections.shuffle(trainingData, this.random);
    double[][] weightAdjust = new double[target.visibleCount()][target.hiddenCount()];
    double[] visibleBiasAdjust = new double[target.visibleCount()];
    double[] hiddenBiasAdjust = new double[target.hiddenCount()];

    if (trainingData.size() == 1) {
      int interval = 10;
      for (int start = 0; start < trainingData.size(); start += interval) {
        iterateInternalLump(interval, start, weightAdjust, visibleBiasAdjust, hiddenBiasAdjust);
      }
    } else {
      int size = 0;
      for (List<double[]> l : trainingData) {
        if (l.size() > size) {
          size = l.size();
        }
      }
      for (int i = 0; i < size; i++) {
        iterateInternalMinibatch(i, weightAdjust, visibleBiasAdjust, hiddenBiasAdjust);
      }
    }
    updateError();

    setIteration(getIteration() + 1);

    if (getIteration() > 2) {
      this.cdCount = Math.min(10, getIteration() - 2);
    }
  }

  private void updateError() {
    double currentError = 0;
    for (double[] input : testData) {
      double[] visibleProbabilities = input;
      double[] hiddenProbabilities = null;
      double[] output = target.hiddenProbabilities(input);
      for (int i = 0; i < cdCount; i++) {
        hiddenProbabilities = target.hiddenProbabilities(visibleProbabilities);
        boolean[] hiddenState =
            RestrictedBoltzmannMachine.probabilitiesToBoolean(hiddenProbabilities);
        visibleProbabilities = target.visibleProbabilities(hiddenState);
      }

      double totalError = 0;
      for (int i = 0; i < target.visibleCount(); i++) {
        for (int j = 0; j < target.hiddenCount(); j++) {
          double dataExpected = input[i] * output[j];
          double reconExpected = visibleProbabilities[i] * hiddenProbabilities[j];
          double pointError = dataExpected - reconExpected;
          totalError += pointError * pointError;
        }
      }
      currentError += totalError / testData.size();
    }
    this.error = currentError;
    double previousOverfit = overfit;
    if (this.getIteration() % 3 == 0) {
      updateOverfit();
    }
    if (overfit < previousOverfit) {
      this.momentum = 0.9;
    }
    previousError = this.error;
  }

  private void updateOverfit() {
    double averageTestEnergy = averageEnergy(testData);
    double averageValidationEnergy = averageEnergy(validationData);
    this.overfit = Math.abs(averageValidationEnergy - averageTestEnergy);
  }

  public double getOverfit() {
    return overfit;
  }

  private double averageEnergy(List<double[]> data) {
    double ret = 0;

    for (double[] input : data) {
      boolean[] discreteInput = null;
      boolean[] output = null;
      for (int i = 0; i < cdCount; i++) {
        discreteInput = RestrictedBoltzmannMachine.probabilitiesToBoolean(input);
        output = target.hiddenState(discreteInput);
        input = target.visibleProbabilities(output);
      }
      for (int i = 0; i < discreteInput.length; i++) {
        if (discreteInput[i]) {
          ret -= target.getVisibleBias(i) / data.size();
        }
      }
      for (int j = 0; j < output.length; j++) {
        if (output[j]) {
          ret -= target.getHiddenBias(j) / data.size();
          for (int i = 0; i < discreteInput.length; i++) {
            if (discreteInput[i]) {
              ret -= target.getWeight(i, j) / data.size();
            }
          }
        }
      }
    }

    return ret;
  }

  private void calculateWeightAdjust(double[] trainingSample, double[][] weightAdjust,
      double[] visibleBiasAdjust, double[] hiddenBiasAdjust, double batchSize) {
    boolean[] discreteInput = RestrictedBoltzmannMachine.probabilitiesToBoolean(trainingSample);
    double[] hiddenProbabilities = target.hiddenProbabilities(discreteInput);
    boolean[] hiddenState = RestrictedBoltzmannMachine.probabilitiesToBoolean(hiddenProbabilities);

    double[] visibleProbabilities = target.visibleProbabilities(hiddenState);

    double[] rVisible = visibleProbabilities.clone();
    double[] rHidden = null;

    for (int i = 0; i < this.cdCount; i++) {
      rHidden = target.hiddenProbabilities(rVisible);
      rVisible =
          target.visibleProbabilities(RestrictedBoltzmannMachine.probabilitiesToBoolean(rHidden));
    }

    for (int i = 0; i < target.visibleCount(); i++) {
      visibleBiasAdjust[i] += (trainingSample[i] - rVisible[i]) / batchSize;
      for (int j = 0; j < target.hiddenCount(); j++) {
        double dataExpected = trainingSample[i] * hiddenProbabilities[j];
        double reconExpected = rVisible[i] * rHidden[j];
        double pointError = dataExpected - reconExpected;
        double dw = pointError;
        weightAdjust[i][j] += dw / batchSize;
      }
    }
    for (int j = 0; j < target.hiddenCount(); j++) {
      hiddenBiasAdjust[j] += (hiddenProbabilities[j] - rHidden[j]) / batchSize;
    }
  }

  private void iterateInternalLump(int interval, int start, double[][] weightAdjust,
      double[] visibleBiasAdjust, double[] hiddenBiasAdjust) {
    List<double[]> currentList =
        trainingData.get(0).subList(start, Math.min((start + interval), trainingData.size()));

    processMinibatch(weightAdjust, visibleBiasAdjust, hiddenBiasAdjust, currentList);
  }

  private void iterateInternalMinibatch(int index, double[][] weightAdjust,
      double[] visibleBiasAdjust, double[] hiddenBiasAdjust) {
    List<double[]> currentList = new ArrayList<>();
    for (List<double[]> list : trainingData) {
      if (list.size() > index) {
        currentList.add(list.get(index));
      }
    }
    processMinibatch(weightAdjust, visibleBiasAdjust, hiddenBiasAdjust, currentList);
  }

  private void processMinibatch(double[][] weightAdjust, double[] visibleBiasAdjust,
      double[] hiddenBiasAdjust, List<double[]> currentList) {
    int batchSize = currentList.size();
    for (double[] trainingSample : currentList) {
      calculateWeightAdjust(trainingSample, weightAdjust, visibleBiasAdjust, hiddenBiasAdjust,
          batchSize);
    }
    applyWeightAdjust(weightAdjust, visibleBiasAdjust, hiddenBiasAdjust);
  }

  public void setIteration(int iteration) {
    this.iteration = iteration;
  }

  public void setLearningRate(double learningRate) {
    this.learningRate = Math.max(learningRate, minLearningRate);
  }

  public void setTrainingData(List<List<double[]>> newData) {
    this.trainingData = newData;
  }

  private int lastQupdateIteration = 0;

  private void updateQ() {
    if (qOld != null && lastQupdateIteration == getIteration()) {
      return;
    }
    lastQupdateIteration = getIteration();
    double[] qCurrent = new double[target.hiddenCount()];
    for (double[] input : testData) {
      double[] output = target.hiddenProbabilities(input);
      for (int i = 0; i < output.length; i++) {
        qCurrent[i] += output[i] / testData.size();
      }
    }
    if (qOld == null) {
      qOld = qCurrent;
    } else {
      for (int i = 0; i < qCurrent.length; i++) {
        qOld[i] = qDecay * qOld[i] + (1 - qDecay) * qCurrent[i];
      }
    }
  }

}
