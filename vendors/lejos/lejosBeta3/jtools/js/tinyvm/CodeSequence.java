package js.tinyvm;

import java.io.*;

public class CodeSequence extends WritableDataWithOffset
{
  private byte[] iBytes;

  public void setBytes (byte[] aBytes)
  {
    iBytes = aBytes;
  }

  public int getLength()
  {
    if (iBytes == null)
      return 0;
    return iBytes.length; 
  }

  public void dump (ByteWriter aOut) 
  throws Exception
  {
    if (iBytes == null)
    {
      Utilities.trace ("Not writing code sequence");
      return;
    }
    aOut.write (iBytes, 0, iBytes.length);
  }
}



