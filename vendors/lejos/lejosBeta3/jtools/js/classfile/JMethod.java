package js.classfile;

import java.lang.reflect.*;
import java.util.*;

public class JMethod extends JComponent
{
  public JMethod (JConstantPool aConstantPool)
  {
    super (aConstantPool);
  }

  public JMethod (JConstantPool aConstantPool,
                     int aFlags,
                     JCPE_Utf8 aName,
                     JCPE_Utf8 aDescriptor)
  throws Exception
  {
    super (aConstantPool, aFlags, aName, aDescriptor);
  }

  public JMethod (JConstantPool aConstantPool,
                  Method aMethod)
  throws Exception
  {
    this (aConstantPool, aMethod.getModifiers(),
          new JCPE_Utf8 (aMethod.getName()),
          new JCPE_Utf8 (JClassName.getMethodDescriptor (
            aMethod.getParameterTypes(), aMethod.getReturnType())));
  }

  public boolean equals (Object aObj)
  {
    if (!(aObj instanceof JMethod))
      return false;
    return (getName().equals (((JMethod) aObj).getName()) &&
            getDescriptor().equals (((JMethod) aObj).getDescriptor()));
  }

  public JCodeAttribute getCode()
  {
    Enumeration pEnum = iAttributes.elements();
    while (pEnum.hasMoreElements())
    {
      Object pEntry = pEnum.nextElement();
      if (pEntry instanceof JCodeAttribute)
      {
        return (JCodeAttribute) pEntry;
      }
    }
    return null;
  }
}


