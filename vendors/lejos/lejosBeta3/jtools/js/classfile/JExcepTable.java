package js.classfile;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class JExcepTable extends JTable
implements IConstants
{
  public JExcepTable()
  {
    super();
  }

  public int length()
  throws Exception
  {
    int pLength = 2;
    Enumeration pEnum = elements();
    while (pEnum.hasMoreElements())
    {
      JExcep pAttrib = (JExcep) pEnum.nextElement();
      pLength += pAttrib.length();
    }
    return pLength;
  }

  public void read (InputStream aIn)
  throws Exception
  {
    int pSize = JIO.readU2 (aIn);
    if (DEBUG_READ)
      System.out.println ("JExcepTable.read: " + pSize + " bytes.");
    for (int pIndex = 0; pIndex < pSize; pIndex++)
    {
      JExcep pObj = new JExcep();
      pObj.read (aIn);
      add (pObj);
    }
  }
}

