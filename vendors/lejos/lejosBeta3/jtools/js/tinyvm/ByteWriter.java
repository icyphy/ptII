package js.tinyvm;

import java.io.*;

public interface ByteWriter
{
  public void write (byte[] aBytes, int aOffset, int aLength)
  throws IOException;

  public void writeU1 (int aByte)
  throws IOException;

  public void writeU2 (int aShort)
  throws IOException;

  public void writeU4 (int aInt)
  throws IOException;

  public void writeU8 (long aLong)
  throws IOException;

  public int size();
}

