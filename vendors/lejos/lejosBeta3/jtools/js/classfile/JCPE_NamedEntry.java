package js.classfile;
import java.io.*;

abstract class JCPE_NamedEntry extends JConstantPoolEntry
{
  JCPE_Utf8 iName;
  int       iTag;
  int       iNameIndex;

  public JCPE_NamedEntry (IConstantPool aConstantPool,
                          int aTag, JCPE_Utf8 aName)
  throws Exception
  {
    super (aConstantPool);
    iTag  = aTag;
    iName = (JCPE_Utf8) iConstantPool.getEntry (aName);
    iNameIndex = -1;
  }

  public JCPE_NamedEntry (IConstantPool aConstantPool, 
                          int aTag, String aName)
  throws Exception
  {
    this (aConstantPool, aTag, (JCPE_Utf8)
          aConstantPool.getEntry (new JCPE_Utf8 (aName)));
  }

  public JCPE_NamedEntry (IConstantPool aConstantPool, int aTag)
  {
    super (aConstantPool);
    iTag = aTag;
    iNameIndex = -1;
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    aOut.write (iTag);
    JIO.writeU2 (aOut, iName.getEntryNumber());
  }
  
  public void read (InputStream aIn)
  throws Exception
  {
    // Assume tag already read
    iNameIndex = JIO.readU2 (aIn);
    iName = null;
    // Use update() to update iName.
    //System.out.println ("# iNameIndex = " + iNameIndex);
  }

  public final void update()
  throws Exception
  {
    iName = (JCPE_Utf8) iConstantPool.getEntry (iNameIndex);
  }

  public int getSize()
  {
    return iName.getSize();
  }

  public JCPE_Utf8 getValue()
  {
    return iName;
  }

  public final String getName()
  {
    return iName.toString();
  }

  public int hashCode()
  {
    return iName.hashCode();
  }

  public boolean equals (Object aObj)
  {
    if (aObj instanceof JCPE_NamedEntry)
      return iName.equals (((JCPE_NamedEntry) aObj).iName);
    return false;
  }

  public String toString()
  {
    return iName.toString();
  }
}





