package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

public abstract class RecordTable extends WritableDataWithOffset
implements Constants
{
  int iLength = -1;
  private boolean iAlign;
  
  public abstract Enumeration elements();
  public abstract int size();
  public abstract Object elementAt (int aIndex);
  public abstract void add (WritableData aElement);
  
  public RecordTable()
  {
    this (false);
  }
  
  public RecordTable (boolean aAlign)
  {
    super();
    iAlign = aAlign;	  
  }
  
  public void dump (ByteWriter aOut)
  throws Exception
  {
    boolean pDoVerify = VERIFY_LEVEL > 0;
    Enumeration pEnum = elements();
    while (pEnum.hasMoreElements())
    {
      int pLength = 0;
      int pPrevSize = 0;
      WritableData pData = (WritableData) pEnum.nextElement();
      if (pDoVerify)
      {
        pLength = pData.getLength();
        pPrevSize = aOut.size();
      }
      pData.dump (aOut);
      if (pDoVerify)
      {
        if (aOut.size() != pPrevSize + pLength)
	{
	  if (pData instanceof RecordTable)
	    System.err.println ("Aligned sequence: " + ((RecordTable) pData).iAlign);
          Utilities.fatal ("Bug RT-1: Written=" + (aOut.size() - pPrevSize) + 
                           " Length=" + pLength + " Class=" +
                           pData.getClass().getName());
	}
      }
    }
    if (iAlign)
      IOUtilities.writePadding (aOut, 2);
  }

  public int getLength()
  {
    if (iLength != -1)
      return iLength;
    iLength = 0;
    Enumeration pEnum = elements();
    while (pEnum.hasMoreElements())
    {
      iLength += ((WritableData) pEnum.nextElement()).getLength();
    }
    Utilities.trace ("RT.getLength: " + iLength);
    if (iAlign)
      iLength = IOUtilities.adjustedSize (iLength, 2); 
    return iLength;
  }

  public void initOffset (int aStart)
  {
    Utilities.trace ("RT.initOffset: " + aStart);
    super.initOffset (aStart);
    Enumeration pEnum = elements();
    while (pEnum.hasMoreElements())
    {
      WritableData pElem = (WritableData) pEnum.nextElement();
      if (pElem instanceof WritableDataWithOffset)
      {
        ((WritableDataWithOffset) pElem).initOffset (aStart);
      }
      aStart += pElem.getLength();
    }    
  }
}


