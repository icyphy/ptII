package js.classfile;

import java.io.*;
import java.util.*;

public class JConstantPool implements IDumpable, IConstantPool
{
  private Vector    iEntryTable;
  private Hashtable iHashtable;

  public JConstantPool()
  {
    super();
    iEntryTable = new Vector();
    iHashtable = new Hashtable();
  }

  public boolean isValidIndex (int aIndex)
  {
    return (aIndex >= 1 && aIndex <= iEntryTable.size());
  }

  public IConstantPoolEntry getEntry (int aIndex)
  throws Exception
  {
     // Constant pool entries are numbered from 1 upwards.
     // Entry zero is dummy. Index zero seems to be used
     // to denote superclass of java/lang/Object.
     if (aIndex == 0)
       return null;
     return (IConstantPoolEntry) iEntryTable.elementAt (aIndex-1);
  }

  public Enumeration elements()
  {
    return iEntryTable.elements();
  }

  private void addEntryToTable (IConstantPoolEntry aObject)
  {
    iEntryTable.addElement (aObject);
    aObject.setEntryNumber (iEntryTable.size());
  }

  private void addEntry (IConstantPoolEntry aObject)
  {
    iEntryTable.addElement (aObject);
    iHashtable.put (aObject, aObject);
    aObject.setEntryNumber (iEntryTable.size());
  }

  /**
    Given an instance of IConstantPoolEntry, this method
    tries to find the entry in the constant pool (i.e. this).
    If an equivalent entry already exists, the existing
    entry is returned. If not, the new entry is appended
    to the constant pool, and it is returned. If the type
    of the entry is JCPE_Double or JCPE_Long, an additional
    JCPE_Dummy entry is appended (TJVMS 4.4.5).
    */

  public IConstantPoolEntry getEntry (IConstantPoolEntry aEntry)
  {
    IConstantPoolEntry pObj = (IConstantPoolEntry) iHashtable.get (aEntry);
    if (pObj != null)
      return pObj;
    addEntry (aEntry);
    if (aEntry instanceof JCPE_Double || aEntry instanceof JCPE_Long)
    {
      JCPE_Dummy pDummy = new JCPE_Dummy();
      addEntry (pDummy);
    }
    return aEntry;
  }

  /** 
   Updates each entry after they have been read in. An update
   normally consists of converting a constant-pool index
   into an instance of JConstantPoolEntry.
   */
  private void updateEntries()
  throws Exception
  {
    Enumeration pEntries = iEntryTable.elements();
    while (pEntries.hasMoreElements())
    {
      ((JConstantPoolEntry) pEntries.nextElement()).update();
    }
  }

  public final void dump (OutputStream aOut)
  throws Exception
  {
    int pSize = iEntryTable.size() + 1; // Size plus one
    //System.out.println ("# CP dump size: " + pSize);
    JIO.writeU2 (aOut, pSize);
    Enumeration pEntries = iEntryTable.elements();
    while (pEntries.hasMoreElements())
      ((JConstantPoolEntry) pEntries.nextElement()).dump (aOut);
  }

  public final void read (InputStream aIn)
  throws Exception
  {
    // Read proceeds as foolows:
    // Creates instances of JConstantPoolEntry, which
    // are uninitialized.
    // These entries are consecutively placed in iEntryTable.
    // updateEntryVector() is invoked to initialize references.
    // hashEntryVector() is invoked to create hash table of entries.

    iEntryTable.removeAllElements();
    iHashtable.clear();
    int pSize = JIO.readU2 (aIn) - 1;  // Always minus one
    //System.out.println ("# Size of constant pool = " + pSize);
    for (int pIndex = 0; pIndex < pSize; pIndex++)
    {
      JConstantPoolEntry pEntry = readEntry (aIn);
      addEntryToTable (pEntry);
      if (pEntry instanceof JCPE_Double || pEntry instanceof JCPE_Long)
      {
        // One more entry for double and long
        addEntryToTable (new JCPE_Dummy());
        pIndex++;
      }
    }
    updateEntries();
    hashEntryVector();
    if (pSize != iHashtable.size())
      throw new EClassFileFormat ("Duplicate constant pool entries");
  }

  private final void hashEntryVector()
  throws Exception
  {
    int pSize = iEntryTable.size();
    for (int pIndex = 0; pIndex < pSize; pIndex++)
    {
        Object pObj = iEntryTable.elementAt(pIndex);      
        //System.out.println ("Entry: " + pObj.getClass().getName() + ": " +
        //                    pObj);
        if (iHashtable.contains (pObj))
           throw new EClassFileFormat ("Duplicate Entry # " + 
                     pIndex + " : " + pObj);
        iHashtable.put (pObj, pObj);
    }
  }
    
  private final JConstantPoolEntry readEntry (InputStream aIn)
  throws Exception
  {
    JConstantPoolEntry pEntry;

    int pTag = aIn.read();
    //System.out.println (" # Tag = " + pTag);
    switch (pTag)
    {
      case JConstantPoolEntry.CONSTANT_Class: 
        pEntry = new JCPE_Class(this);
        pEntry.read (aIn);
        break;
      case JConstantPoolEntry.CONSTANT_Fieldref: 
        pEntry = new JCPE_Fieldref(this);
        pEntry.read (aIn);
        break;
      case JConstantPoolEntry.CONSTANT_Methodref: 
        pEntry = new JCPE_Methodref(this);
        pEntry.read (aIn);
        break;
      case JConstantPoolEntry.CONSTANT_InterfaceMethodref: 
        pEntry = new JCPE_InterfaceMethodref(this);
        pEntry.read (aIn);
        break;
      case JConstantPoolEntry.CONSTANT_String: 
        pEntry = new JCPE_String(this);
        pEntry.read (aIn);
        break;
      case JConstantPoolEntry.CONSTANT_Integer: 
        pEntry = new JCPE_Integer(this);
        pEntry.read (aIn);
        break;
      case JConstantPoolEntry.CONSTANT_Float: 
        pEntry = new JCPE_Float(this);
        pEntry.read (aIn);
        break;
      case JConstantPoolEntry.CONSTANT_Long: 
        pEntry = new JCPE_Long(this);
        pEntry.read (aIn);
        break;
      case JConstantPoolEntry.CONSTANT_Double: 
        pEntry = new JCPE_Double(this);
        pEntry.read (aIn);
        break;
      case JConstantPoolEntry.CONSTANT_NameAndType: 
        pEntry = new JCPE_NameAndType(this);
        pEntry.read (aIn);
        break;
      case JConstantPoolEntry.CONSTANT_Utf8: 
        pEntry = new JCPE_Utf8();
        pEntry.read (aIn);
        break;
      default:
        throw new EClassFileFormat ("Invalid Constant Pool Entry Tag: " +
                  pTag);
    }
    return pEntry; 
  }

  public String toString()
  {
    int pIndex = 1;
    StringBuffer pStrBuf = new StringBuffer();
    pStrBuf.append ("  CONSTANT POOL SIZE = " + iEntryTable.size() + "\n");
    Enumeration pEntries = iEntryTable.elements();
    while (pEntries.hasMoreElements())
    {
      pStrBuf.append ("  " + pIndex + " : ");
      pStrBuf.append (((JConstantPoolEntry) pEntries.nextElement()).
                      toString() + "\n");
      pIndex++;
    }
    return pStrBuf.toString();
  }
}


