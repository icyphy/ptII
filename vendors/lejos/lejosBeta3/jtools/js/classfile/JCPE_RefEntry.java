package js.classfile;
import java.io.*;

public abstract class JCPE_RefEntry extends JConstantPoolEntry
{
  private int              iTag;
  private int              iClassIndex;
  private int              iNameAndTypeIndex;
  private JCPE_Class       iClass;
  private JCPE_NameAndType iNameAndType;

  public JCPE_RefEntry (IConstantPool aConstantPool,
                        int aTag, JCPE_Class aClass, 
                        JCPE_NameAndType aNameAndType)
  throws Exception
  {
    super (aConstantPool);
    iTag  = aTag;
    iClass = (JCPE_Class) iConstantPool.getEntry (aClass);
    iNameAndType = (JCPE_NameAndType) iConstantPool.getEntry (aNameAndType);
  }

  public JCPE_RefEntry (IConstantPool aConstantPool, int aTag)
  {
    super (aConstantPool);
    iTag = aTag;
  }

  public JCPE_Class getClassEntry()
  {
    return iClass;
  }

  void setClass (JCPE_Class aClass)
  {
    iClass = aClass;
  }

  void setNameAndType (JCPE_NameAndType aNameAndType)
  {
    iNameAndType = aNameAndType;
  }

  public JCPE_NameAndType getNameAndType()
  {
    return iNameAndType;
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    aOut.write (iTag);
    JIO.writeU2 (aOut, iClass.getEntryNumber());
    JIO.writeU2 (aOut, iNameAndType.getEntryNumber());
  }
  
  public void read (InputStream aIn)
  throws Exception
  {
    // Assume tag already read
    iClassIndex = JIO.readU2 (aIn);
    iNameAndTypeIndex = JIO.readU2 (aIn);
    //System.out.println ("# iClassIndex = " + iClassIndex);
    //System.out.println ("# iNameAndTypeIndex = " + iNameAndTypeIndex);
  }

  public final void update()
  throws Exception
  {
    if (iClassIndex == -1)
      throw new EConstantPool ("update: class index not valid");
    iClass = (JCPE_Class) iConstantPool.getEntry (iClassIndex);
    if (iNameAndTypeIndex == -1)
      throw new EConstantPool ("update: NameAndType index not valid");
    iNameAndType = (JCPE_NameAndType) 
                   iConstantPool.getEntry (iNameAndTypeIndex);
  }

  public int hashCode()
  {
    return iClass.hashCode() + iNameAndType.hashCode();
  }

  public boolean equals (Object aObj)
  {
      if (aObj instanceof JCPE_RefEntry)
      {
          JCPE_RefEntry pEntry = (JCPE_RefEntry) aObj;
          return (iClass.equals (pEntry.iClass) &&
                    iNameAndType.equals (pEntry.iNameAndType));
       }
       return false;
  }

  public String toString()
  {
    return iClass.toString() + " : " + iNameAndType.toString();
  }
}









