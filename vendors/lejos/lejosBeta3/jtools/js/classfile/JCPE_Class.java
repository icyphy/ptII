package js.classfile;

public class JCPE_Class extends JCPE_NamedEntry
{
  public JCPE_Class (IConstantPool aConstantPool, JCPE_Utf8 aName)
  throws Exception
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_Class, aName);
  }

  public JCPE_Class (IConstantPool aConstantPool, String aName)
  throws Exception
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_Class,
           JClassName.getQualifiedName(aName));
  }

  public JCPE_Class (IConstantPool aConstantPool, Class aClass)
  throws Exception
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_Class,
           JClassName.getQualifiedName(aClass.getName()));

  }

  public JCPE_Class (IConstantPool aConstantPool)
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_Class);
  }

  public boolean equals (Object aObj)
  {
    if (!(aObj instanceof JCPE_Class))
      return false;
    return super.equals (aObj);
  }
}
