package js.classfile;
import java.util.*;
import java.io.*;

public abstract class JTable implements IDumpable
{
  private static final boolean DEBUG = false;
  private Hashtable iHashtable;
  private final Vector iVector = new Vector();

  public JTable()
  {
    if (DEBUG)
      iHashtable = new Hashtable();
  }

  public void add (IDumpable aObj)
  throws Exception
  {
    if (iHashtable != null)
    {
      if (iHashtable.contains (aObj))
        throw new EClassFileFormat ("JTable.add: " + aObj + " already exists");
      iHashtable.put (aObj, aObj);
    }
    iVector.addElement (aObj);
  }

  public final Enumeration elements()
  {
    return iVector.elements();
  }

  public int size()
  {
    return iVector.size();
  }

  public final void dump (OutputStream aOut)
  throws Exception
  {
    int pSize = iVector.size();
    JIO.writeU2 (aOut, pSize);
    Enumeration pEnum = iVector.elements();
    while (pEnum.hasMoreElements())
      ((IDumpable) pEnum.nextElement()).dump (aOut);
  }

  public String toString()
  {
    StringBuffer pStr = new StringBuffer();
    Enumeration pEnum = iVector.elements();
    while (pEnum.hasMoreElements())
      pStr.append ("  " + ((IDumpable) pEnum.nextElement()).toString() +
                   "\n");
    return pStr.toString();
  }

  public abstract void read (InputStream aIn)
  throws Exception;
}


