package js.classfile;
import java.io.*;

public class JCodeAttribute extends JAttribute
implements IConstants
{
  private int iMaxStack;
  private int iMaxLocals;
  private JExcepTable iExcepTable;
  private JAttributeTable iAttribTable;
  public static final String kName = "Code";

  public JCodeAttribute (JConstantPool aConstantPool,
         int aMaxStack, int aMaxLocals,
         byte aCode[], JExcepTable aExcepTable, 
         JAttributeTable aAttribTable)
  {
    super (aConstantPool, kName, aCode);
    setName (kName);
    iMaxStack = aMaxStack;
    iMaxLocals = aMaxLocals;
    iExcepTable = aExcepTable;
    iAttribTable = aAttribTable;
  }

  public JCodeAttribute (JConstantPool aConstantPool,
         int aMaxStack, int aMaxLocals,
         byte aCode[], JExcepTable aTable)
  {
    this (aConstantPool, aMaxStack, aMaxLocals, aCode, aTable, null);
    iAttribTable = new JAttributeTable (iConstantPool);
  }

  public JCodeAttribute (JConstantPool aConstantPool,
         int aMaxStack, int aMaxLocals,
         byte aCode[])
  {
    this (aConstantPool, aMaxStack, aMaxLocals, aCode, null, null);
    iAttribTable = new JAttributeTable (iConstantPool);
    iExcepTable = new JExcepTable();    
  }

  public JCodeAttribute (JConstantPool aConstantPool)
  {
    this (aConstantPool, -1, -1, null);
    iAttribTable = new JAttributeTable (iConstantPool);
    iExcepTable = new JExcepTable();    
  }

  private int dumplength()
  throws Exception
  {
    return (length() - 6);
  }

  public final int length()
  throws Exception
  {
    int pLength = 14 + getInfo().length +
                  iExcepTable.length() +
                  iAttribTable.length();
    return pLength;
  }

  public int getMaxLocals()
  {
    return iMaxLocals;
  }

  public int getMaxStack()
  {
    return iMaxStack;
  }

  public JExcepTable getExceptionTable()
  {
    return iExcepTable;
  }

  public boolean equals (Object aObj)
  {
    return (System.identityHashCode(this) == System.identityHashCode (aObj));
  }

  public final void dump (OutputStream aOut)
  throws Exception
  {
    byte pCode[] = getInfo();
    int pCodeLength = pCode.length;
    JIO.writeU2 (aOut, getName().getEntryNumber());
    JIO.writeU4 (aOut, dumplength());
    JIO.writeU2 (aOut, iMaxStack);
    JIO.writeU2 (aOut, iMaxLocals);
    JIO.writeU4 (aOut, pCodeLength);
    aOut.write (pCode, 0, pCodeLength);
    iExcepTable.dump (aOut);
    iAttribTable.dump (aOut);
  }  

  /**
    Reads starting at attribute_length
    */

  public final void readInfo (InputStream aIn)
  throws Exception
  {
    int pDumpLength = JIO.readU4 (aIn);
    iMaxStack = JIO.readU2(aIn); 
    iMaxLocals = JIO.readU2(aIn);
    int pCodeLength = JIO.readU4(aIn);
    if (DEBUG_READ)
    {
      System.out.println ("# Max stack: " + iMaxStack);
      System.out.println ("# Max locals: " + iMaxLocals);
      System.out.println ("# Code length: " + pCodeLength);
    }
    byte pCode[] = new byte[pCodeLength];
    int pTotalRead = 0;
    do {
      pTotalRead += aIn.read (pCode, pTotalRead, pCodeLength - pTotalRead);
    } while (pTotalRead < pCodeLength);
    if (DEBUG_READ && pCodeLength >= 2)
    {
      System.out.println ("# Bytecodes: ");
      for (int i = 0; i < pCodeLength; i++)
        System.out.println ("   " + i + ": " + (pCode[i] & 0xFF));
    }
    setInfo (pCode);
    iExcepTable = new JExcepTable();
    iExcepTable.read (aIn);
    iAttribTable = new JAttributeTable (iConstantPool);
    iAttribTable.read (aIn);
    if (dumplength() != pDumpLength)
    {
      //System.out.println ("# ExcepTable.length = " + iExcepTable.length());
      //System.out.println ("# AttribTable.length = " + iAttribTable.length());
      //System.out.println ("# pCodeLength = " + pCodeLength);
      throw new EClassFileFormat ("Attribute length mistatch: " + 
                                  dumplength() +
                                  "/" + pDumpLength);
    }
  }

  public final void read (InputStream aIn)
  throws Exception
  {
    int pIndex = JIO.readU2 (aIn);
    setName ((JCPE_Utf8) iConstantPool.getEntry (pIndex));
    readInfo (aIn);
  }
}



