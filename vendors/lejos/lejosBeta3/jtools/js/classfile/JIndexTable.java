package js.classfile;
import java.io.*;

public class JIndexTable extends JTable
{
  private JConstantPool iConstantPool;

  public JIndexTable (JConstantPool aConstantPool)
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
      JCP_Index pObj = new JCP_Index (iConstantPool);
      pObj.read (aIn);
      add (pObj);
    }
  }
}

