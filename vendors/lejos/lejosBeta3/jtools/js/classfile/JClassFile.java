package js.classfile;
import java.io.*;
import java.lang.reflect.*;

/**
  Instances of JClassFile represent a memory image
  of a parsed class file. The structure of a JClassFile
  instance consists of flags (int), fields (JTable of JComponent),
  methods (JTable of JComponent), interfaces (JTable of JCP_Index),
  attributes (JTable of JAttribute) and a constant pool, i.e.
  an instance of JConstantPool. An instance of JClassFile can
  be constructed, and JClassFile.read() can be invoked to read
  and parse a class file. The internal structure of a JClassFile
  instance can also be constructed manually 
  by invoking some of its methods. The JClassFile instance can
  then be turned into a class file by invoking JClassFile.dump().  
  */

public class JClassFile implements IDumpable, IConstants
{ 
  private int           iFlags;
  private JTable        iFields;
  private JTable        iMethods;
  private JTable        iInterfaces;
  private JTable        iAttributes;
  private JConstantPool iConstantPool;
  
  private JCP_Index     iThisClass;
  private JCP_Index     iSuperClass;

  public static final int ACC_PUBLIC = 0x0001;
  public static final int ACC_PRIVATE = 0x0002;
  public static final int ACC_PROTECTED = 0x0004;
  public static final int ACC_STATIC = 0x0008;
  public static final int ACC_FINAL = 0x0010;
  public static final int ACC_SYNCHRONIZED = 0x0020;
  public static final int ACC_VOLATILE = 0x0040;
  public static final int ACC_TRANSIENT = 0x0080;
  public static final int ACC_NATIVE = 0x0100;
  public static final int ACC_INTERFACE = 0x0200;
  public static final int ACC_ABSTRACT = 0x0400;
  
  public static final int kMinorVersion = 3;
  public static final int kMajorVersion = 45;

  public JClassFile()
  {
    iConstantPool = new JConstantPool();
    iFlags = 0;
    iFields = new JFieldTable(iConstantPool);
    iMethods = new JMethodTable(iConstantPool);
    iInterfaces = new JIndexTable(iConstantPool);
    iAttributes = new JAttributeTable(iConstantPool);
    iThisClass = null;
    iSuperClass = null;
  }

  public JCPE_Class getSuperClass()
  {
    return (JCPE_Class) iSuperClass.getEntry();
  }

  public JCPE_Class getThisClass()
  {
    return (JCPE_Class) iThisClass.getEntry();
  }

  public String getName()
  {
    return getThisClass().getName().toString();
  }

  public void addMethod (JMethod aMethod)
  throws Exception
  {
    iMethods.add (aMethod);
  }

  public void addConstructor (JConstructor aMethod)
  throws Exception
  {
    iMethods.add (aMethod);
  }

  public void addStatic (JStatic aMethod)
  throws Exception
  {
    iMethods.add (aMethod);
  }

  public void addField (JField aField)
  throws Exception
  {
    iFields.add (aField);
  }

  public JMethod getMethod (Method aMethod)
  throws Exception
  {
    return new JMethod (iConstantPool, aMethod);
  }

  public JTable getMethods()
  {
    return iMethods;
  }

  public JTable getFields()
  {
    return iFields;
  }

  public JConstructor getConstructor (Constructor aConstructor)
  throws Exception
  {
    return new JConstructor (iConstantPool, aConstructor);
  }

  public void setSuperClass (JCP_Index aIndex)
  {
    iSuperClass = aIndex;
  }

  public void setSuperClass (String aName)
  throws Exception
  {
    JCPE_Class pClass = new JCPE_Class (iConstantPool, aName);
    iSuperClass = new JCP_Index (iConstantPool, pClass);
  }

  public void setSuperClass (Class aClass)
  throws Exception
  {
    setSuperClass (aClass.getName());
  }

  public void setThisClass (JCP_Index aIndex)
  {
    iThisClass = aIndex;
  }

  public void setThisClass (String aName)
  throws Exception
  {
    JCPE_Class pClass = new JCPE_Class (iConstantPool, aName);
    iThisClass = new JCP_Index (iConstantPool, pClass);
  }

  public void addInterface (JCP_Index aEntry)
  throws Exception
  {
    iInterfaces.add (aEntry);
  }
    
  public void addInterface (String aName)
  throws Exception
  {
    JCPE_Class pClass = new JCPE_Class (iConstantPool, aName);
    iInterfaces.add (new JCP_Index (iConstantPool, pClass));
  }

  public JConstantPool getConstantPool()
  {
    return iConstantPool;
  }

  public void setFlags (int aFlags)
  {
    iFlags = aFlags;
  }

  public void addFlags (int aFlags)
  {
    iFlags |= aFlags;
  }

  public JCPE_Methodref getConstructorref (Class aClass, Constructor aCons)
  throws Exception
  {
    return (JCPE_Methodref) iConstantPool.getEntry (
                            new JCPE_Methodref (iConstantPool, aClass, aCons));
  }

  public JCPE_Methodref getMethodref (Class aClass, Method aMeth)
  throws Exception
  {
    return (JCPE_Methodref) iConstantPool.getEntry (
                            new JCPE_Methodref (iConstantPool, aClass, aMeth));
  }

  private void dumpMagicAndVersions (OutputStream aOut)
  throws Exception
  {
    JIO.writeU4 (aOut, 0xCAFEBABE);
    JIO.writeU2 (aOut, kMinorVersion);
    JIO.writeU2 (aOut, kMajorVersion);
  }

  public final void dump (OutputStream aOut)
  throws Exception
  {
    dumpMagicAndVersions (aOut);
    iConstantPool.dump (aOut);  // also numbers entries
    JIO.writeU2 (aOut, iFlags);
    if (iThisClass == null)
      throw new EClassFileFormat ("ThisClass has not been set");
    iThisClass.dump (aOut);  
    if (iSuperClass == null)
      throw new EClassFileFormat ("SuperClass has not been set");
    iSuperClass.dump (aOut);
    iInterfaces.dump (aOut);
    iFields.dump (aOut);
    iMethods.dump (aOut);
    iAttributes.dump (aOut);
  }

  private void readMagicAndVersions (InputStream aIn)
  throws Exception
  {
    int pMagic = JIO.readU4 (aIn);
    if (pMagic != 0xCAFEBABE)
      throw new EClassFileFormat ("Magic Number is " + pMagic);
    int pMinorVersion = JIO.readU2 (aIn);
    int pMajorVersion = JIO.readU2 (aIn);
    if (pMinorVersion != kMinorVersion || pMajorVersion != kMajorVersion)
      throw new EClassFileFormat ("Version not recognized: " +
        pMajorVersion + "." + pMinorVersion);
  }  
        
  private void readAccessFlags (InputStream aIn)
  throws Exception
  {
    iFlags = JIO.readU2 (aIn);
  }

  private void readThisClass (InputStream aIn)
  throws Exception
  {
    if (iThisClass == null)
      iThisClass = new JCP_Index (iConstantPool);
    iThisClass.read (aIn);
  }

  private void readSuperClass (InputStream aIn)
  throws Exception
  {
    if (iSuperClass == null)
      iSuperClass = new JCP_Index (iConstantPool);
    iSuperClass.read (aIn);
  }
    
  public final void read (InputStream aIn)
  throws Exception
  {
    readMagicAndVersions (aIn);
    iConstantPool.read (aIn);
    //System.out.println (iConstantPool);
    if (DEBUG_READ)
      System.out.println ("# Reading access flags");
    readAccessFlags (aIn);
    if (DEBUG_READ)
      System.out.println ("# Access flags = " + iFlags);
    readThisClass (aIn);
    if (DEBUG_READ)
      System.out.println ("# Reading superclass");
    readSuperClass (aIn);
    if (DEBUG_READ)
      System.out.println ("# Reading interfaces");
    iInterfaces.read (aIn);
    if (DEBUG_READ)
      System.out.println ("# Reading fields");
    iFields.read (aIn);
    if (DEBUG_READ)
      System.out.println ("# Reading methods");
    iMethods.read (aIn);
    if (DEBUG_READ)
      System.out.println ("# Reading attributes");
    iAttributes.read (aIn);

    // JDK 1.2 doesn't like this:
//     if (aIn.available() != 0)
//       throw new EExtraBytes ("Extra bytes in input stream of class file");
  }

  public boolean isInterface()
  {
    return (iFlags & ACC_INTERFACE) != 0;
  }

  public String toString()
  {
    return "Major Version: " + kMajorVersion + "\n" +
           "Minor Version: " + kMinorVersion + "\n" +
           "Flags: " + iFlags + "\n" +
           "This Class: " + iThisClass.toString() + "\n" + 
           "Superclass: " + iSuperClass.toString() + "\n" +
           "Interfaces: " + iInterfaces.toString() + "\n" +
           "Fields:\n" + iFields.toString() + "\n" +
           "Methods:\n" + iMethods.toString() + "\n" +
           "Attrubutes:\n" + iAttributes.toString() + "\n" +
           "Constant pool:\n" +
           iConstantPool.toString();
  }                      
}
