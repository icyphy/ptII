package js.classfile;
import java.io.*;

public class JAttribute implements IDumpable
{
  private byte iInfo[];
  private JCPE_Utf8 iName;
  protected JConstantPool iConstantPool;

  JAttribute (JConstantPool aConstantPool)
  {
    iConstantPool = aConstantPool;
  }

  public JAttribute (JConstantPool aConstantPool,
                     JCPE_Utf8 aName, byte aInfo[])
  {
    iConstantPool = aConstantPool;
    iName = (JCPE_Utf8) iConstantPool.getEntry (aName);
    iInfo = aInfo;
  }

  public JAttribute (JConstantPool aConstantPool,
                     String aName, byte aInfo[])
  {
    this (aConstantPool, new JCPE_Utf8 (aName), aInfo); 
  }

  public JAttribute (JConstantPool aConstantPool,
                     String aName)
  {
    this (aConstantPool, aName, null);
  }
    
  public String toString()
  {
    return iName.toString();
  }

  public void setInfo (byte aInfo[])
  {
    iInfo = aInfo;
  }
    
  public byte[] getInfo()
  {
    return iInfo;
  }

  public void setName (JCPE_Utf8 aName)
  {
    iName = (JCPE_Utf8) iConstantPool.getEntry (aName);
  }

  public void setName (String aName)
  {
    setName (new JCPE_Utf8 (aName));
  }

  public JCPE_Utf8 getName()
  {
    return iName;
  }

  public int length()
  throws Exception
  {
    if (iInfo == null)
      return 6;
    return 6 + iInfo.length;
  }

  public boolean equals (Object aObj)
  {
    if (aObj instanceof JAttribute)
      return (iName.equals (((JAttribute) aObj).iName) &&
              iInfo.equals (((JAttribute) aObj).iInfo));
    return false;
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    int pNameIndex = iName.getEntryNumber();
    JIO.writeU2 (aOut, pNameIndex);
    JIO.writeU4 (aOut, iInfo.length);
    aOut.write (iInfo, 0, iInfo.length);
  }

  /**
    Reads only the name (index) of the attribute.
    */

  public String readName (InputStream aIn)
  throws Exception
  {
    int pNameIndex = JIO.readU2 (aIn);
    iName = (JCPE_Utf8) iConstantPool.getEntry (pNameIndex);
    return iName.toString();
  }

  /**
    Reads everything after the name; that is,
    the part of the attribute that starts at
    attribute_length.
    */

  public void readInfo (InputStream aIn)
  throws Exception
  {
    int pLength = JIO.readU4 (aIn);
    iInfo = new byte[pLength];    
    int pRead = 0;
    do {
      pRead += aIn.read (iInfo, pRead, pLength - pRead);
    } while (pRead < pLength);
  }

  /**
    Reads the entire attribute from an InputStream.
    */

  public void read (InputStream aIn)
  throws Exception
  {
    int pNameIndex = JIO.readU2 (aIn);
    iName = (JCPE_Utf8) iConstantPool.getEntry (pNameIndex);
    readInfo (aIn);
  }
}
  

