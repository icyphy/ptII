package js.classfile;
import java.io.*;

public class JCPE_Long extends JConstantPoolEntry
{
  long iValue;

  public JCPE_Long (IConstantPool aConstantPool,
                       long aValue) 
  {
    super (aConstantPool);
    iValue = aValue;
  }

  public JCPE_Long (IConstantPool aConstantPool)
  {
    super (aConstantPool);
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    aOut.write (JConstantPoolEntry.CONSTANT_Long); // 1 byte
    JIO.writeU4 (aOut, (int) (iValue >>> 32));
    JIO.writeU4 (aOut, (int) (iValue &  0xFFFFFFFFL));
  }
  
  public void read (InputStream aIn)
  throws Exception
  {
    // Assume tag already read
    iValue = (JIO.readU4 (aIn) << 32) | 
              (JIO.readU4 (aIn) & 0xFFFFFFFFL);
  }

  public final void update()
  {
  }

  public int hashCode()
  {
    return (int) iValue;
  }

  public long getValue()
  {
    return iValue;
  }

  public boolean equals (Object aObj)
  {
      if (aObj instanceof JCPE_Long)
         return (iValue == ((JCPE_Long) aObj).iValue);
      return false;
  }

  public String toString()
  {
    return String.valueOf (iValue);
  }
}
