package js.tinyvm;

import java.io.*;
import java.util.*;

public abstract class WritableDataWithOffset implements WritableData
{
  int iOffset = -1;

  public int getOffset()
  {
    if (iOffset == -1)
    {
      new Error().printStackTrace();
      System.out.println ("--------------------------");
      Utilities.fatal ("Bug WDWO-1: Premature getOffset call: Class=" + 
                       getClass().getName());
    }
    return iOffset;
  }

  public void initOffset (int aStart)
  {
    Utilities.assert (aStart != -1);
    iOffset = aStart;
  }
}
