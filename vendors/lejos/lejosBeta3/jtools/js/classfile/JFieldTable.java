package js.classfile;
import java.io.*;

public class JFieldTable extends JTable
{
  private JConstantPool iConstantPool;

  public JFieldTable (JConstantPool aConstantPool)
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
      JField pObj = new JField (iConstantPool);
      pObj.read (aIn);
      add (pObj);
    }
  }
}

