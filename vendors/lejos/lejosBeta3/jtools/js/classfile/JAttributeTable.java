package js.classfile;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class JAttributeTable extends JTable
{
  private JConstantPool iConstantPool;

  public JAttributeTable (JConstantPool aConstantPool)
  {
    super();
    iConstantPool = aConstantPool;
  }

  public int length()
  throws Exception
  {
    int pLength = 2;
    Enumeration pEnum = elements();
    while (pEnum.hasMoreElements())
    {
      JAttribute pAttrib = (JAttribute) pEnum.nextElement();
      pLength += pAttrib.length();
    }
    return pLength;
  }

  public void read (InputStream aIn)
  throws Exception
  {
    int pSize = JIO.readU2 (aIn);
    for (int pIndex = 0; pIndex < pSize; pIndex++)
    {
      JAttribute pObj = new JAttribute (iConstantPool);
      String pName = pObj.readName (aIn);
      if (pName.equals ("Code"))
        pObj = new JCodeAttribute (iConstantPool);
      pObj.readInfo (aIn);
      add (pObj);
    }
  }
}


