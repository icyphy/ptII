package js.classfile;
import java.io.*;

public class JCPE_Dummy extends JConstantPoolEntry
{
  public JCPE_Dummy()
  {
    super (null);
  }

  public int hashCode()
  {
    return System.identityHashCode (this);
  }

  public boolean equals (Object aObj)
  {
    return false;
  }

  public void update()
  throws Exception
  {
  }

  public void read (InputStream aIn)
  throws Exception
  {
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
  }
}
