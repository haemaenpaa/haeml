package com.haem.ubyte;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class UByteImageStream extends InputStream {

  private InputStream underlyingStream;
  private int columns;
  private int rows;
  private int numImages;

  public int getColumns() {
    return columns;
  }

  public int getRows() {
    return rows;
  }

  public int getNumImages() {
    return numImages;
  }

  public UByteImageStream(InputStream underlyingStream) throws IOException {
    super();
    this.underlyingStream = underlyingStream;
    initialize();
  }

  private void initialize() throws IOException {
    byte[] intBuffer = new byte[4];
    underlyingStream.read(intBuffer);
    long magicNumber = bufferToInt(intBuffer);
    if (magicNumber != 0x803) {
      throw new IllegalArgumentException(
          "Not an ubyte stream, magic number was " + Long.toHexString(magicNumber));
    }

    underlyingStream.read(intBuffer);
    numImages = (int) bufferToInt(intBuffer);

    underlyingStream.read(intBuffer);
    rows = (int) bufferToInt(intBuffer);
    underlyingStream.read(intBuffer);
    columns = (int) bufferToInt(intBuffer);
  }

  private long bufferToInt(byte[] intBuffer) {
    long ret = 0;
    for (int i = 0; i < intBuffer.length; i++) {
      long b = intBuffer[i];
      if (b < 0) {
        b = 256 + b;
      }
      ret = (ret << 8) | b;
    }
    return ret;
  }

  public BufferedImage readImage() throws IOException {
    BufferedImage image = new BufferedImage(columns, rows, BufferedImage.TYPE_BYTE_GRAY);
    Graphics g = image.getGraphics();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        int pixel = 255 - read();
        g.setColor(new Color(pixel, pixel, pixel));
        g.drawRect(j, i, 1, 1);
      }
    }
    return image;
  }


  @Override
  public int read() throws IOException {
    return underlyingStream.read();
  }

  @Override
  public void close() throws IOException {
    underlyingStream.close();
    super.close();
  }

}
