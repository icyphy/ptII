package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

public class ConstantRecord implements WritableData, Constants
{
  JConstantPoolEntry iEntry;
  ConstantValue iConstantValue;
  int iSize = -1;

  public ConstantRecord (JConstantPoolEntry aEntry)
  {
    iEntry = aEntry;
    if (aEntry instanceof JCPE_String)
    {
      iSize = ((JCPE_String) aEntry).getSize();
      if (iSize > MAX_STRING_CONSTANT_LENGTH)
      {
        Utilities.fatal ("String constant of length more than " +
        MAX_STRING_CONSTANT_LENGTH + " not accepted: " +
        aEntry);
      }
    }  
    else if (aEntry instanceof JCPE_Double || aEntry instanceof JCPE_Long)
      iSize = 8;
    else if (aEntry instanceof JCPE_Integer || aEntry instanceof JCPE_Float)
      iSize = 4;
    else
      Utilities.assert (false);
  }

  public static int getType (JConstantPoolEntry aEntry)
  {
    if (aEntry instanceof JCPE_String)
      return T_REFERENCE;
    else if (aEntry instanceof JCPE_Double || aEntry instanceof JCPE_Long)
      return T_LONG;
    else if (aEntry instanceof JCPE_Integer)
      return T_INT;
    else if (aEntry instanceof JCPE_Float)
      return T_FLOAT;
    else
    {
      Utilities.assert (false);
      return -1;
    }
  }

  public void setConstantValue (ConstantValue aValue)
  {
    iConstantValue = aValue;
  }

  public int getLength()
  {
    return IOUtilities.adjustedSize (2 + // offset
                                     1 + // type
                                     1,  // size
           2);
  }

  public int getOffset()
  {
    return iConstantValue.getOffset();
  }

  public int getConstantSize()
  {
    return iSize;
  }

  public void dump (ByteWriter aOut) throws Exception
  {
    Utilities.assert (iSize != -1);
    Utilities.assert (iConstantValue != null);
    aOut.writeU2 (iConstantValue.getOffset());
    aOut.writeU1 (getType (iEntry));
    aOut.writeU1 (iSize);
    IOUtilities.writePadding (aOut, 2);
  }

  public boolean equals (Object aOther)
  {
    if (!(aOther instanceof ConstantRecord))
      return false;
    return ((ConstantRecord) aOther).iEntry.equals (iEntry);    
  }  

  public int hashCode()
  {
    return iEntry.hashCode();
  }
}
  
