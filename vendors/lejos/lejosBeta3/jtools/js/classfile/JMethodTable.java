package js.classfile;
import java.io.*;

public class JMethodTable extends JTable
{
  private JConstantPool iConstantPool;

  public JMethodTable (JConstantPool aConstantPool)
  {
    super();
    iConstantPool = aConstantPool;
  }

  public void read (InputStream aIn)
  throws Exception
  {
    int pSize = JIO.readU2 (aIn);
    for (int pIndex = 0; pIndex < pSize; pIndex++)
    {
      JMethod pObj = new JMethod (iConstantPool);
      pObj.read (aIn);
      add (pObj);
    }
  }
}

