package js.classfile;
import java.io.*;
import java.lang.reflect.*;

public class JCPE_NameAndType extends JConstantPoolEntry
{
  JCPE_Utf8 iName;
  JCPE_Utf8 iDescriptor;
  int       iNameIndex;
  int       iDescriptorIndex;

  public JCPE_NameAndType (IConstantPool aConstantPool,
                           JCPE_Utf8 aName,
                           JCPE_Utf8 aDescriptor)
  throws Exception
  {
    super (aConstantPool);    
    iName = (JCPE_Utf8) iConstantPool.getEntry (aName);
    iDescriptor = (JCPE_Utf8) iConstantPool.getEntry (aDescriptor);
  }

  public JCPE_NameAndType (IConstantPool aConstantPool,
                           String aName,
                           String aDescriptor)
  throws Exception
  {
    this (aConstantPool, new JCPE_Utf8 (aName), new JCPE_Utf8 (aDescriptor));
  }

  public JCPE_NameAndType (IConstantPool aConstantPool,
                           Method aMethod)
  throws Exception
  {
    this (aConstantPool, aMethod.getName(), 
           JClassName.getMethodDescriptor (
           aMethod.getParameterTypes(), 
           aMethod.getReturnType()));
  }

  public JCPE_NameAndType (IConstantPool aConstantPool,
                           Constructor aConstructor)
  throws Exception
  { 
    this (aConstantPool, "<init>",
           JClassName.getMethodDescriptor (
           aConstructor.getParameterTypes(),
           void.class));
  }

  public JCPE_NameAndType (IConstantPool aConstantPool,
                           Field aField)
  throws Exception
  {
    this (aConstantPool, aField.getName(),
          JClassName.getDescriptorForClass (aField.getType()));
  }
    
  public JCPE_NameAndType (IConstantPool aConstantPool)
  {
    super (aConstantPool);
  }

  public String getName()
  {
    return iName.toString();
  }

  public String getDescriptor()
  {
    return iDescriptor.toString();
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    aOut.write (JConstantPoolEntry.CONSTANT_NameAndType);
    JIO.writeU2 (aOut, iName.getEntryNumber());
    JIO.writeU2 (aOut, iDescriptor.getEntryNumber());
  }
  
  public void read (InputStream aIn)
  throws Exception
  {
    // Assume tag already read
    iNameIndex = JIO.readU2 (aIn);
    iDescriptorIndex = JIO.readU2 (aIn);
    iName = null;
    iDescriptor = null;
    // Use update() to update iName and iDescriptor.
    //System.out.println ("# iNameIndex = " + iNameIndex);
    //System.out.println ("# iDescriptorIndex = " + iDescriptorIndex);
  }

  public final void update()
  throws Exception
  {
    if (iNameIndex == -1)
      throw new EConstantPool ("update: NameIndex not valid");
    iName = (JCPE_Utf8) iConstantPool.getEntry (iNameIndex);
    if (iDescriptorIndex == -1)
      throw new EConstantPool ("update: DescriptorIndex not valid");
    iDescriptor = (JCPE_Utf8) iConstantPool.getEntry (iDescriptorIndex);
  }

  public int hashCode()
  {
    return iName.hashCode() + iDescriptor.hashCode();
  }

  public boolean equals (Object aObj)
  {
      if (aObj instanceof JCPE_NameAndType)
          return (iName.equals(((JCPE_NameAndType) aObj).iName) &&
                     iDescriptor.equals(((JCPE_NameAndType) aObj).iName));
      return false;
  }

  public String toString()
  {
    return iName.toString() + " : " + iDescriptor.toString();
  }
}
