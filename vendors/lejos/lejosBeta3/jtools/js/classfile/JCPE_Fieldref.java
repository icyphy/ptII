package js.classfile;

public class JCPE_Fieldref extends JCPE_RefEntry
{
  public JCPE_Fieldref (IConstantPool aConstantPool, 
                        JCPE_Class aClass, JCPE_NameAndType aNameAndType)
  throws Exception
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_Fieldref,
           aClass, aNameAndType);
  }

  public JCPE_Fieldref (IConstantPool aConstantPool)
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_Fieldref);
  }
}
