package js.classfile;
import java.io.InputStream;

public interface IVMInstruction
{
  public int    length();
  public int    getOpCode();
  public byte[] getArguments();
  public void   read (InputStream aIn) throws Exception;
}
