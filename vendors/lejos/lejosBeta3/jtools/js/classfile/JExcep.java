package js.classfile;
import java.io.*;

public class JExcep implements IDumpable
{
  private int iStartPc;
  private int iEndPc;
  private int iHandlerPc;
  private int iCatchType;
  private static int kLength = 8;

  public JExcep()
  {
    iStartPc = iEndPc = iHandlerPc = iCatchType = -1;
  }

  public JExcep (
              int aStartPc, int aEndPc,
              int aHandlerPc, int aCatchType)
  {
    iStartPc = aStartPc;
    iEndPc = aEndPc;
    iHandlerPc = aHandlerPc;
    iCatchType = aCatchType;
  }

  public int getStartPc()
  {
    return iStartPc;
  }

  public int getEndPc()
  {
    return iEndPc;
  }

  public int getHandlerPc()
  {
    return iHandlerPc;
  }

  public int getClassIndex()
  {
    return iCatchType;
  }
  
  public String toString()
  {
    return iStartPc + ":" + iEndPc + ":" + iHandlerPc + ":" + iCatchType;
  }

  public int length()
  {
    return kLength;
  }

  public boolean equals (Object aObj)
  {
    if (aObj instanceof JExcep)
      return (iStartPc == ((JExcep) aObj).iStartPc &&
              iEndPc == ((JExcep) aObj).iEndPc &&
              iHandlerPc == ((JExcep) aObj).iHandlerPc &&
              iCatchType == ((JExcep) aObj).iCatchType);
    return false;
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    JIO.writeU2 (aOut, iStartPc);    
    JIO.writeU2 (aOut, iEndPc);
    JIO.writeU2 (aOut, iHandlerPc);
    JIO.writeU2 (aOut, iCatchType);
  }

  public void read (InputStream aIn)
  throws Exception
  {
    iStartPc = JIO.readU2 (aIn);
    iEndPc = JIO.readU2 (aIn);
    iHandlerPc = JIO.readU2 (aIn);
    iCatchType = JIO.readU2 (aIn);    
  }
}
  

