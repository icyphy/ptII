package js.classfile;

public class JStatic extends JMethod
{
  public JStatic (JConstantPool aConstantPool)
  {
    super (aConstantPool);
  }

  public JStatic (JConstantPool aConstantPool,
                     int aFlags,
                     JCPE_Utf8 aDescriptor)
  throws Exception
  {
    super (aConstantPool, aFlags, 
           new JCPE_Utf8 ("<clinit>"),
           new JCPE_Utf8 ("()V"));
  }

  public boolean equals (Object aObj)
  {
    if (aObj instanceof JStatic)
      return getDescriptor().equals (((JStatic) aObj).getDescriptor());
    return false;
  }
}
