package js.classfile;
import java.io.*;
import java.lang.reflect.*;

public abstract class JComponent implements IDumpable, IConstants
{
  protected JTable      iAttributes;
  private int           iFlags;
  private JConstantPool iConstantPool;
  private JCPE_Utf8     iName;
  private JCPE_Utf8     iDescriptor;   

  public JComponent (JConstantPool aConstantPool)
  {
    iConstantPool = aConstantPool;
    iFlags = 0;
    iAttributes = new JAttributeTable (iConstantPool);
  }

  public JComponent (JConstantPool aConstantPool,
                     int aFlags,
                     JCPE_Utf8 aName,
                     JCPE_Utf8 aDescriptor)
  throws Exception
  {
    iConstantPool = aConstantPool;
    iFlags = aFlags;
    iName = (JCPE_Utf8) iConstantPool.getEntry (aName);
    iDescriptor = (JCPE_Utf8) iConstantPool.getEntry (aDescriptor);
    iAttributes = new JAttributeTable (iConstantPool);
  }

  public JComponent (JConstantPool aConstantPool,
                     int aFlags,
                     String aName,
                     String aDescriptor)
  throws Exception
  {
    this (aConstantPool, aFlags, new JCPE_Utf8 (aName),
           new JCPE_Utf8 (aDescriptor));
  }

  public JComponent (JConstantPool aConstantPool,
                     int aFlags, String aName,
                     Class aParamTypes[], Class aRetType)
  throws Exception
  {
    this (aConstantPool, aFlags,
          aName, "");
    iDescriptor = (JCPE_Utf8) iConstantPool.getEntry(
                  new JCPE_Utf8 (
                  JClassName.getMethodDescriptor (aParamTypes, aRetType)));
  }

  public JComponent (JConstantPool aConstantPool,
                     int aFlags, String aName,
                     Class aType)
  throws Exception
  {
    this (aConstantPool, aFlags,
          aName, "");
    iDescriptor = (JCPE_Utf8) iConstantPool.getEntry(
                  new JCPE_Utf8 (
                  JClassName.getDescriptorForClass (aType)));
  }

  public JComponent (JConstantPool aConstantPool,
                     Method aMethod)
  throws Exception
  {
    this (aConstantPool, aMethod.getModifiers(), aMethod.getName(),
          aMethod.getParameterTypes(), aMethod.getReturnType());
  }

  public JComponent (JConstantPool aConstantPool,
                     Constructor aConstructor)
  throws Exception
  {
    this (aConstantPool, aConstructor.getModifiers(), "<init>",
          aConstructor.getParameterTypes(), void.class);
  }

  public JComponent (JConstantPool aConstantPool,
                     Field aField)
  throws Exception
  {
    this (aConstantPool, aField.getModifiers(), aField.getName(),
          aField.getType());
  }

  public void addAttribute (JAttribute aAttribute)
  throws Exception
  {
    iAttributes.add (aAttribute);
  }

  public void setAccessFlags (int aFlags)
  {
    iFlags = aFlags;
  }

  public void setName (JCPE_Utf8 aName)
  {
    iName = (JCPE_Utf8) iConstantPool.getEntry (aName);
  }

  public void setDescriptor (JCPE_Utf8 aDescriptor)
  {
    iDescriptor = (JCPE_Utf8) iConstantPool.getEntry (aDescriptor);
  }
    
  public void setCode (int aMaxStack, int aMaxLocals,
                       byte aCode[])
  throws Exception
  {
    addAttribute (new JCodeAttribute (iConstantPool,
                     aMaxStack, aMaxLocals, aCode));   
  }

  public void setCode (int aMaxStack, int aMaxLocals,
                       byte aCode[], JExcepTable aExcepTable,
                       JAttributeTable aAttribTable)
  throws Exception
  {
    addAttribute (new JCodeAttribute (iConstantPool,
                  aMaxStack, aMaxLocals, aCode, aExcepTable, aAttribTable));  
  }

  public String toString()
  {
    return iName.toString() + " : " + iDescriptor.toString();
  }

  public final String getName()
  {
    return iName.toString();
  }
 
  public final JCPE_Utf8 getDescriptor()
  {
    return iDescriptor;
  }

  public boolean isNative()
  {
    return (iFlags & JClassFile.ACC_NATIVE) != 0;
  }

  public boolean isAbstract()
  {
    return (iFlags & JClassFile.ACC_ABSTRACT) != 0;
  }

  public boolean isStatic()
  {
    return (iFlags & JClassFile.ACC_STATIC) != 0;
  }

  public boolean isSynchronized()
  {
    return (iFlags & JClassFile.ACC_SYNCHRONIZED) != 0;
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    int pNameIndex = iName.getEntryNumber();
    int pDescriptorIndex = iDescriptor.getEntryNumber();
    JIO.writeU2 (aOut, iFlags);
    JIO.writeU2 (aOut, pNameIndex);
    JIO.writeU2 (aOut, pDescriptorIndex);
    iAttributes.dump (aOut);
  }

  public void read (InputStream aIn)
  throws Exception
  {
    iFlags = JIO.readU2 (aIn);
    int pNameIndex = JIO.readU2 (aIn);
    int pDescriptorIndex = JIO.readU2 (aIn);
    iName = (JCPE_Utf8) iConstantPool.getEntry (pNameIndex);
    iDescriptor = (JCPE_Utf8) iConstantPool.getEntry (pDescriptorIndex);
    if (DEBUG_READ)
    {
      System.out.println ("# Reading component: " + iName + "" + iDescriptor);
    }
    iAttributes.read (aIn);
  }

}
  






