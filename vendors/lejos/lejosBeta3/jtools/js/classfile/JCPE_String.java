package js.classfile;

public class JCPE_String extends JCPE_NamedEntry
{
  public JCPE_String (IConstantPool aConstantPool, JCPE_Utf8 aName)
  throws Exception
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_String, aName);
  }

  public JCPE_String (IConstantPool aConstantPool)
  {
    super (aConstantPool, JConstantPoolEntry.CONSTANT_String);
  }

  public boolean equals (Object aObj)
  {
    if (!(aObj instanceof JCPE_String))
      return false;
    return super.equals (aObj);
  }
}
