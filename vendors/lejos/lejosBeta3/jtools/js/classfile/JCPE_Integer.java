package js.classfile;
import java.io.*;

public class JCPE_Integer extends JConstantPoolEntry
{
  private int iValue;

  public JCPE_Integer (IConstantPool aConstantPool,
                        int aValue) 
  {
    super (aConstantPool);
    iValue = aValue;
  }

  public JCPE_Integer (IConstantPool aConstantPool)
  {
    super (aConstantPool);
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    aOut.write (JConstantPoolEntry.CONSTANT_Integer); // 1 byte
    JIO.writeU4 (aOut, iValue);
  }
  
  public void read (InputStream aIn)
  throws Exception
  {
    // Assume tag already read
    iValue = JIO.readU4 (aIn);
  }

  public final void update() 
  {
  }

  public int getValue()
  {
    return iValue;
  }

  public int hashCode()
  {
    return iValue;
  }

  public boolean equals (Object aObj)
  {
      if (aObj instanceof JCPE_Integer)
         return (iValue == ((JCPE_Integer) aObj).iValue);
      return false;
  }

  public String toString()
  {
    return String.valueOf (iValue);
  }
}

