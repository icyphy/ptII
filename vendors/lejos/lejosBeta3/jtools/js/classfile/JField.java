package js.classfile;

public class JField extends JComponent
{
  public JField (JConstantPool aConstantPool)
  {
    super (aConstantPool);
  }

  public JField (JConstantPool aConstantPool,
                     int aFlags,
                     JCPE_Utf8 aName,
                     JCPE_Utf8 aDescriptor)
  throws Exception
  {
    super (aConstantPool, aFlags, aName, aDescriptor);
  }

  public boolean equals (Object aObj)
  {
    if (aObj instanceof JField)
      return (getName().equals (((JField) aObj).getName()));
    return false;
  }
}
