package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

public class StaticValue extends WritableDataWithOffset
implements Constants
{
  int iType;

  public StaticValue (JField aEntry)
  {
    iType = InstanceFieldRecord.descriptorToType (
            aEntry.getDescriptor().toString());
  }

  public int getLength()
  {
    return InstanceFieldRecord.getTypeSize (iType);
  }

  public void dump (ByteWriter aOut) throws Exception
  {
    // Static values must be dumped in Big Endian order
    DataOutputStream pDataOut = (DataOutputStream) aOut;
    switch (iType)
    {
      case T_BOOLEAN:
        pDataOut.writeBoolean (false);
        break;
      case T_BYTE:
        pDataOut.writeByte (0);
        break;
      case T_CHAR:
        pDataOut.writeChar (0);
        break;
      case T_SHORT:
        pDataOut.writeShort (0);
        break;
      case T_REFERENCE:
      case T_INT:
        pDataOut.writeInt (0);
        break;
      case T_FLOAT:
        pDataOut.writeFloat ((float) 0.0);
        break;
      case T_LONG:
        pDataOut.writeLong (0L);
        break;
      case T_DOUBLE:
        pDataOut.writeInt (0);
        pDataOut.writeFloat ((float) 0.0);
        break;
      default:
        Utilities.assert (false);
    }
  }

  public boolean equals (Object aOther)
  {
    if (!(aOther instanceof StaticValue))
      return false;
    return ((StaticValue) aOther).iType == iType;    
  }  

  public int hashCode()
  {
    return iType;
  }
}
  
