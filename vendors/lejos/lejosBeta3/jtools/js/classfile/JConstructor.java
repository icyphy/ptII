package js.classfile;
import java.lang.reflect.*;

public class JConstructor extends JMethod
{
  public JConstructor (JConstantPool aConstantPool)
  {
    super (aConstantPool);
  }

  public JConstructor (JConstantPool aConstantPool,
                     int aFlags,
                     JCPE_Utf8 aDescriptor)
  throws Exception
  {
    super (aConstantPool, aFlags, 
           new JCPE_Utf8 ("<init>"), 
           aDescriptor);
  }

  public JConstructor (JConstantPool aConstantPool,
                       Constructor aMethod)
  throws Exception
  {
    this (aConstantPool, aMethod.getModifiers(),
          new JCPE_Utf8 (JClassName.getMethodDescriptor (
            aMethod.getParameterTypes(), void.class)));
  }

  public boolean equals (Object aObj)
  {
    if (aObj instanceof JConstructor)
      return getDescriptor().equals (((JConstructor) aObj).getDescriptor());
    return false;
  }
}
