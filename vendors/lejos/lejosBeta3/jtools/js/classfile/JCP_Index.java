package js.classfile;
import java.io.*;

public class JCP_Index implements IDumpable
{
  private JConstantPool iConstantPool;
  private JConstantPoolEntry iEntry;
  
  public JCP_Index (JConstantPool aConstantPool, JConstantPoolEntry aEntry)
  {
    iConstantPool = aConstantPool;
    iEntry = (JConstantPoolEntry) iConstantPool.getEntry (aEntry);
  }

  public JCP_Index (JConstantPool aConstantPool)
  {
    iConstantPool = aConstantPool;
    iEntry = null;
  }

  public final void dump (OutputStream aOut)
  throws Exception
  {
    int pIndex = iEntry.getEntryNumber();
    //System.out.println ("# Dumping: " + pIndex);
    JIO.writeU2 (aOut, pIndex);
  }

  public final void read (InputStream aIn)
  throws Exception
  {
    int pIndex = JIO.readU2 (aIn);
    //System.out.println ("# JCP_Index: " + pIndex);
    iEntry = (JConstantPoolEntry) iConstantPool.getEntry (pIndex);
  }

  public JConstantPoolEntry getEntry()
  {
    return iEntry;
  }

  public boolean equals (Object aObj)
  {
    if (aObj instanceof JCP_Index)  
      return iEntry.equals (((JCP_Index) aObj).iEntry);
    return false;
  }
 
  public String toString()
  {
    return iEntry.toString();
  }
}

