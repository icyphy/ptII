package js.classfile;
import java.lang.reflect.*;

public class JCPE_Methodref extends JCPE_RefEntry
{
  public JCPE_Methodref (IConstantPool aConstantPool, 
                        JCPE_Class aClass, JCPE_NameAndType aNameAndType)
  throws Exception
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_Methodref,
           aClass, aNameAndType);
  }

  public JCPE_Methodref (IConstantPool aConstantPool,
                         Class aClass, Method aMethod)
  throws Exception
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_Methodref);
    setClass ((JCPE_Class) 
      iConstantPool.getEntry (new JCPE_Class (aConstantPool, aClass)));
    setNameAndType ((JCPE_NameAndType)
      iConstantPool.getEntry (
        new JCPE_NameAndType (aConstantPool, aMethod)));
  }

  public JCPE_Methodref (IConstantPool aConstantPool,
                         Class aClass, Constructor aConstructor)
  throws Exception
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_Methodref);
    setClass ((JCPE_Class)
      iConstantPool.getEntry (new JCPE_Class (aConstantPool, aClass)));
    setNameAndType ((JCPE_NameAndType)
      iConstantPool.getEntry (
        new JCPE_NameAndType (aConstantPool, aConstructor)));
  }

  public JCPE_Methodref (IConstantPool aConstantPool)
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_Methodref);
  }
}
