package com.haem.ml.som;

import com.haem.ml.utils.Measure;

public class GaussianLearningCoefficients extends BaseLearningCoefficients {

  public GaussianLearningCoefficients(SelfOrganizingMap map, double eLife, double radius) {
    super(map, eLife, radius);
  }

  @Override
  public double theta(int v, int u, int iteration) {
    double[] vCoordinates = getMap().indexToCoordinates(v);
    double[] uCoordinates = getMap().indexToCoordinates(u);

    double distance = Measure.euclideanDistance(vCoordinates, uCoordinates) / getRadius();

    return Math.exp(-distance * distance);
  }
}
