package js.tinyvm;

import java.io.*;

public interface WritableData
{
  public void dump (ByteWriter aOut) throws Exception;
  
  /**
   * Returns the length of the record, in bytes.
   */
  public int getLength();
}
