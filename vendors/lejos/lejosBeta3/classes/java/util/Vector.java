package java.util;

/**
 * A dynamic array.
 */
public class Vector
{
  private Object[] iElements;
  private int iSize;
  
  public Vector()
  {
    iElements = new Object[7];
    iSize = 0;	  
  }
  
  public synchronized Object elementAt (int aIndex)
  {
    return iElements[aIndex];	  
  }

  public synchronized void addElement (Object aObj)
  {
    int pOldSize = iSize;
    setSize (pOldSize + 1);
    iElements[pOldSize] = aObj;
  }

  public synchronized void setSize (int aSize)
  {
    iSize = aSize;
    if (iElements.length < iSize)
    {
      int pNewCapacity = iElements.length * 2;
      if (pNewCapacity < iSize)
	pNewCapacity = iSize;
      Object[] pNewElements = new Object[pNewCapacity];
      arraycopy (iElements, 0, pNewElements, 0, iElements.length);
      iElements = pNewElements;
    }
  }
  
  public synchronized int size()
  {
    return iSize;
  }
  
  private native void arraycopy (Object aSource, int aOffset1, Object aDest, int aOffset2, int aLength);
}	
