package js.classfile;
import java.io.*;

public class JIO
{ 
  static void writeU2 (OutputStream aOut, int aValue)
  throws Exception
  {
    if (aValue < 0 || aValue > 0xFFFF)
      throw new EClassFileFormat ("Value is not valid U2");
    aOut.write ((aValue >>> 8) & 0xFF);
    aOut.write (aValue & 0xFF);
  }
    
  static void writeU4 (OutputStream aOut, int aValue)
  throws Exception
  {
    aOut.write ((aValue >>> 24) & 0xFF);
    aOut.write ((aValue >>> 16) & 0xFF);
    aOut.write ((aValue >>> 8) & 0xFF);
    aOut.write (aValue & 0xFF);
  }

  static int readU2 (InputStream aIn)
  throws Exception
  {
    int pByte1 = aIn.read();
    int pByte2 = aIn.read();
    if ((pByte1 | pByte2) < 0)
      throw new EClassFileFormat ("Unexpected EOF");
    int pValue = (pByte1 << 8) | pByte2;
    if (pValue < 0 || pValue > 0xFFFF)
      throw new EClassFileFormat ("Value is not valid U2");
    return pValue;
  }
    
  static int readU4 (InputStream aIn)
  throws Exception
  {
    int pByte1 = aIn.read();
    int pByte2 = aIn.read();
    int pByte3 = aIn.read();
    int pByte4 = aIn.read();
    if ((pByte1 | pByte2 | pByte3 | pByte4) < 0)
      throw new EClassFileFormat ("Unexpected EOF");
    return (pByte1 << 24) | (pByte2 << 16) | (pByte3 << 8) | pByte4;
  }
}

