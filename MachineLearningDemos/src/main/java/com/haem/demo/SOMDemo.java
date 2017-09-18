package com.haem.demo;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.haem.ml.som.GaussianLearningCoefficients;
import com.haem.ml.som.SOMTrainer;
import com.haem.ml.som.SelfOrganizingMap;
import com.haem.ml.utils.SamplingStrategy;
import com.haem.ubyte.UByteImageStream;

public class SOMDemo {

  private static ImageGridView view;

  public static void main(String[] args) {

    view = new ImageGridView();

    final List<BufferedImage> images;
    try {
      String filename = args[0];
      images = loadImages(filename);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    SamplingStrategy strategy = new SamplingStrategy() {

      int iteration = 0;
      Random random = new Random(124);
      List<BufferedImage> imgs = new ArrayList<>(images);

      @Override
      public double[] next() {
        if (iteration == 0) {
          Collections.shuffle(imgs, random);
        }
        BufferedImage image = imgs.get(iteration);
        iteration = (iteration + 1) % imgs.size();
        double[] ret = imageToVector(image);

        return ret;
      }

      @Override
      public int cycle() {
        return imgs.size();
      }
    };

    int width = images.get(0).getWidth();
    int height = images.get(0).getHeight();
    SelfOrganizingMap target = new SelfOrganizingMap(width * height, 24, 24);

    int eLife = 100;
    SOMTrainer trainer =
        new SOMTrainer(target, strategy, new GaussianLearningCoefficients(target, eLife, 4));

    view.setTitle("Self-Organizing map demo");
    view.setSize(600, 600);
    view.setVisible(true);
    view.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    trainer.initialize(123);
    int iterations = (int) (Math.log(100) * eLife + 0.5);
    System.out.printf("Running %d iterations...\n", iterations);
    for (int i = 0; i < iterations; i++) {
      view.setTitle("Self-Organizing map demo, iteration " + (i));
      drawMap(width, height, target, view);
      trainer.iterate();
    }
    view.setTitle("Self-Organizing map demo, iteration " + iterations);
    drawMap(width, height, target, view);

    try {
      String fileName = "SelfOrganizingMap.smile";
      File outfile = new File(fileName);
      if (outfile.exists()) {
        outfile.delete();
      }
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outfile));
      out.writeObject(target);
      out.close();
    } catch (IOException e1) {
      e1.printStackTrace();
    }

  }

  public static List<BufferedImage> loadImages(String filename)
      throws IOException, FileNotFoundException {
    final List<BufferedImage> images;
    UByteImageStream stream = new UByteImageStream(new FileInputStream(filename));
    images = new ArrayList<>(stream.getNumImages());
    for (int i = 0; i < stream.getNumImages(); i++) {
      BufferedImage readImage = stream.readImage();
      images.add(cropImage(readImage, 10, 10));
      if (i % 10000 == 0) {
        System.out.println(i);
      }
    }
    stream.close();
    return images;
  }

  public static BufferedImage cropImage(BufferedImage original, int width, int height) {
    BufferedImage ret = new BufferedImage(height, width, BufferedImage.TYPE_BYTE_GRAY);

    int firstY = original.getHeight();
    int firstX = original.getWidth();
    int lastY = 0;
    int lastX = 0;

    for (int i = 0; i < original.getWidth(); i++) {
      for (int j = 0; j < original.getHeight(); j++) {
        double[] pixel = new double[1];
        original.getRaster().getPixel(i, j, pixel);
        if (pixel[0] < 255.0) {
          if (firstX > j) {
            firstX = j;
          }
          if (firstY > i) {
            firstY = i;
          }
        }
      }
    }

    for (int i = original.getWidth() - 1; i > firstX; i--) {
      for (int j = original.getHeight() - 1; j > firstY; j--) {
        double[] pixel = new double[1];
        original.getRaster().getPixel(i, j, pixel);
        if (pixel[0] < 255.0) {
          if (lastX < j) {
            lastX = j;
          }
          if (lastY < i) {
            lastY = i;
          }
        }
      }
    }

    ret.getGraphics().drawImage(original, 0, 0, ret.getHeight(), ret.getWidth(), firstX, firstY,
        lastX, lastY, view);

    return ret;
  }

  public static void drawMap(int width, int height, SelfOrganizingMap target, ImageGridView view) {
    double[][] weights = target.getWeights();
    for (int j = 0; j < weights.length; j++) {
      double[] coords = target.indexToCoordinates(j);
      BufferedImage img = vectorToImage(weights[j], height, width);
      view.setImage(img, (int) coords[0], (int) coords[1]);
    }
  }

  public static double[] imageToVector(BufferedImage image) {
    double[] ret = new double[image.getWidth() * image.getHeight()];

    for (int i = 0; i < ret.length; i++) {
      int x = i / image.getHeight();
      int y = i % image.getHeight();
      double[] pixel = image.getData().getPixel(x, y, new double[1]);
      ret[i] = pixel[0] / 255;
    }
    return ret;
  }

  public static BufferedImage vectorToImage(double[] vector, int h, int w) {
    BufferedImage ret = new BufferedImage(h, w, BufferedImage.TYPE_BYTE_GRAY);
    double minimum = Double.POSITIVE_INFINITY;
    double maximum = Double.NEGATIVE_INFINITY;
    for (double d : vector) {
      minimum = Math.min(minimum, d);
      maximum = Math.max(maximum, d);
    }
    double range = maximum - minimum;
    if (range <= 0) {
      range = 1;
    }

    WritableRaster raster = ret.getRaster();

    for (int i = 0; i < vector.length; i++) {
      int x = i / h;
      int y = i % h;
      double[] ds = new double[] {Math.min(Math.max((vector[i] - minimum) / range, 0), 1) * 255};
      raster.setPixel(x, y, ds);
    }
    return ret;
  }

}
