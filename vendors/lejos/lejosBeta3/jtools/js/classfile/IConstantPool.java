package js.classfile;

public interface IConstantPool
{
  public IConstantPoolEntry getEntry (int aIndex)
                            throws Exception;
  public IConstantPoolEntry getEntry (IConstantPoolEntry aEntry)
                            throws Exception;
}
