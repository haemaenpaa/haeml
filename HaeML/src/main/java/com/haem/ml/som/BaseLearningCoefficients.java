package com.haem.ml.som;

import com.haem.ml.utils.Measure;

/**
 * Learning coefficients for which the learning rate is exponentially decreasing, and the
 * neighborhood function returns one for vectors within a radius of the best matching and zero
 * otherwise.
 * 
 * @author heikki
 *
 */
public class BaseLearningCoefficients implements SOMTrainer.LearningCoefficients {

  private double eLife;
  private double radius;
  private SelfOrganizingMap map;

  /**
   * Builds the coefficients
   * 
   * @param map the map that is being trained
   * @param eLife The amount of iterations for the learning coefficient to get divided by e
   * @param radius The radius at which neighborhood function returns one
   */
  public BaseLearningCoefficients(SelfOrganizingMap map, double eLife, double radius) {
    super();
    this.eLife = eLife;
    this.map = map;
    this.radius = radius;
  }

  @Override
  public double alpha(int iteration) {
    return Math.exp(-iteration / eLife);
  }

  @Override
  public double theta(int v, int u, int iteration) {
    double[] vCoordinates = map.indexToCoordinates(v);
    double[] uCoordinates = map.indexToCoordinates(u);

    double distance = Measure.euclideanDistance(vCoordinates, uCoordinates);

    return distance <= radius ? 1 : 0;
  }

  public double geteLife() {
    return eLife;
  }

  public void seteLife(double eLife) {
    this.eLife = eLife;
  }

  public double getRadius() {
    return radius;
  }

  public void setRadius(double radius) {
    this.radius = radius;
  }

  public SelfOrganizingMap getMap() {
    return map;
  }

  public void setMap(SelfOrganizingMap map) {
    this.map = map;
  }

}
