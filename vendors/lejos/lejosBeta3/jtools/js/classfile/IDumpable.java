package js.classfile;
import java.io.*;

public interface IDumpable
{
  public void dump (OutputStream aOut) throws Exception;
  public void read (InputStream aIn) throws Exception;
}
