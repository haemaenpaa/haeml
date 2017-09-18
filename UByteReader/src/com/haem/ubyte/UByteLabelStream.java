package com.haem.ubyte;

import java.io.IOException;
import java.io.InputStream;

public class UByteLabelStream extends InputStream {
  private InputStream underlyingStream;
  private int numImages;

  public int getNumImages() {
    return numImages;
  }

  public UByteLabelStream(InputStream underlyingStream) throws IOException {
    super();
    this.underlyingStream = underlyingStream;
    initialize();
  }

  private void initialize() throws IOException {
    byte[] intBuffer = new byte[4];
    underlyingStream.read(intBuffer);
    long magicNumber = bufferToInt(intBuffer);
    if (magicNumber != 0x801) {
      throw new IllegalArgumentException(
          "Not an ubyte stream, magic number was " + Long.toHexString(magicNumber));
    }

    underlyingStream.read(intBuffer);
    numImages = (int) bufferToInt(intBuffer);

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

  public byte readLabel() throws IOException {
    return (byte) underlyingStream.read();
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
