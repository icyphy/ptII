package js.classfile;

public abstract class JConstantPoolEntry implements IDumpable, 
                                                    IConstantPoolEntry
{
  private   int           iEntryNumber;
  protected IConstantPool iConstantPool;

  public static final int CONSTANT_Class = 7;  
  public static final int CONSTANT_Fieldref = 9;
  public static final int CONSTANT_Methodref = 10;  
  public static final int CONSTANT_InterfaceMethodref = 11;
  public static final int CONSTANT_String = 8;  
  public static final int CONSTANT_Integer = 3;
  public static final int CONSTANT_Float = 4;  
  public static final int CONSTANT_Long = 5;
  public static final int CONSTANT_Double = 6;  
  public static final int CONSTANT_NameAndType = 12;
  public static final int CONSTANT_Utf8 = 1;
 
  public JConstantPoolEntry (IConstantPool aCP)
  {
    iConstantPool = aCP;
    iEntryNumber  = -1;
  }

  public void setEntryNumber (int aNum)
  {
    iEntryNumber = aNum;
  }

  public int getEntryNumber()
  throws Exception
  {
    if (iEntryNumber == -1)
      throw new EConstantPool ("Entry number has not been set");
    return iEntryNumber;
  }

  abstract void update()
  throws Exception;
}    






