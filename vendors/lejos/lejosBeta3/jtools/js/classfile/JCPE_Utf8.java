package js.classfile;

import java.io.*;

public class JCPE_Utf8 extends JConstantPoolEntry
{
  private byte iBytes[];
  private int  iLength;

  public JCPE_Utf8 (String aString)
  {
    super (null);
    iBytes = new byte[JUtf8.getMaxLength (aString)];
    iLength = JUtf8.stringToUtf8 (aString, iBytes, 0);
  }

  public JCPE_Utf8 ()
  {
    super (null);
  }

  public byte[] getBytes()
  {
    byte[] pBytes = new byte[iLength];
    System.arraycopy (iBytes, 0, pBytes, 0, iLength);
    return pBytes;
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    aOut.write (JConstantPoolEntry.CONSTANT_Utf8);
    JIO.writeU2 (aOut, iLength);
    aOut.write (iBytes, 0, iLength);
  }
  
  public void read (InputStream aIn)
  throws Exception
  {
    // Assume tag already read
    iLength = JIO.readU2 (aIn);

    if (iBytes == null || iBytes.length < iLength)
      iBytes = new byte[iLength];
    int pRead = 0;
    do {
      pRead += aIn.read (iBytes, pRead, iLength - pRead);
    } while (pRead < iLength);

    //System.out.println ("(" + iLength + ") |" +
    //          new String (iBytes, 0, iLength) + "|");
  }

  public final void update()
  {
  }

  public int getSize()
  {
    return iLength;
  }

  public int hashCode()
  {
    return JMisc.hashSignature(iBytes, 0, iLength);
  }

  public boolean equals (Object aObj)
  {
      if (aObj instanceof JCPE_Utf8)
      {
          JCPE_Utf8 pUtf = (JCPE_Utf8) aObj;
          return JMisc.equalArrays (iBytes, 0, iLength,
                   pUtf.iBytes, 0, pUtf.iLength);
      }
      return false;
  }

  public String toString()
  {
    return JUtf8.Utf8ToString (iBytes, 0, iLength);
  }
}









