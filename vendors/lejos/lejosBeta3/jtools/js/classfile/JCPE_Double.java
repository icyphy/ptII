package js.classfile;
import java.io.*;

public class JCPE_Double extends JConstantPoolEntry
{
  double iValue;

  public JCPE_Double (IConstantPool aConstantPool,
                      double aValue) 
  {
    super (aConstantPool);
    iValue = aValue;
  }

  public JCPE_Double (IConstantPool aConstantPool)
  {
    super (aConstantPool);
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    aOut.write (JConstantPoolEntry.CONSTANT_Double); // 1 byte
    long pLong = Double.doubleToLongBits (iValue);
    JIO.writeU4 (aOut, (int) (pLong >>> 32));
    JIO.writeU4 (aOut, (int) (pLong & 0x00000000FFFFFFFFL));
  }
  
  public void read (InputStream aIn)
  throws Exception
  {
    // Assume tag already read
    int pInt1 = JIO.readU4 (aIn);
    //System.out.println ("# pInt1 = " + pInt1);
    int pInt2 = JIO.readU4 (aIn);
    //System.out.println ("# pInt2 = " + pInt2);
    long pLong = ((long) pInt1 << 32) | (pInt2 & 0xFFFFFFFFL);
    iValue = Double.longBitsToDouble (pLong);
    //System.out.println ("# iValue = " + iValue);
  }

  public final void update()
  {
  }
  
  public double getValue()
  {
    return iValue;
  }

  public int hashCode()
  {
    return (int) Double.doubleToLongBits(iValue);
  }

  public boolean equals (Object aObj)
  {
    if (aObj instanceof JCPE_Double)
    {
      if (aObj == this)
	return true;
      double pOther = ((JCPE_Double) aObj).iValue;
      // Note: NaN is not equal to anything.      
      return (iValue == pOther);
    }
    return false;
  }

  public String toString()
  {
    return String.valueOf (iValue);
  }
}
