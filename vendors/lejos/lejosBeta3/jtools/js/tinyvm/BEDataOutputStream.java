package js.tinyvm;

import java.io.*;

public class BEDataOutputStream extends DataOutputStream
implements ByteWriter
{
  public BEDataOutputStream (OutputStream aOut)
  {
    super (aOut);
  }

  public void writeU1 (int aByte)
  throws IOException
  {
    write (aByte);
  }

  public void writeU2 (int aShort)
  throws IOException
  {
    writeShort (aShort);
  }

  public void writeU4 (int aInt)
  throws IOException
  {
    writeInt (aInt);
  }

  public void writeU8 (long aLong)
  throws IOException
  {
    writeLong (aLong);
  }
}



