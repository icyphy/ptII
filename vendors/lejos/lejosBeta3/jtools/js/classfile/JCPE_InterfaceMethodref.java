package js.classfile;

public class JCPE_InterfaceMethodref extends JCPE_RefEntry
{
  public JCPE_InterfaceMethodref (IConstantPool aConstantPool, 
                        JCPE_Class aClass, JCPE_NameAndType aNameAndType)
  throws Exception
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_InterfaceMethodref,
           aClass, aNameAndType);
  }

  public JCPE_InterfaceMethodref (IConstantPool aConstantPool)
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_InterfaceMethodref);
  }
}
