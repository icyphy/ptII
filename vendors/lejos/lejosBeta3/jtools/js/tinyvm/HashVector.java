package js.tinyvm;

import java.util.*;

public class HashVector
{
  private Hashtable iHashtable;
  private Vector iVector;

  private class IntWrap
  {
    int iV;

    IntWrap (int aV)
    {
      iV = aV;
    }
  }

  public HashVector()
  {
    super();
    iHashtable = new Hashtable();
    iVector = new Vector();
  }

  public void insertElementAt (Object aElement, int aIndex)
  {
    synchronized (iVector)
    {
      if (iHashtable.containsKey (aElement))
        return;
      iHashtable.put (aElement, new IntWrap (aIndex));
      iVector.insertElementAt (aElement, aIndex);
    }
  }

  public void addElement (Object aElement)
  {
    synchronized (iVector)
    {
      if (iHashtable.containsKey (aElement))
        return;
      iHashtable.put (aElement, new IntWrap (iVector.size()));
      iVector.addElement (aElement);
    }
  }

  public void put (Object aKey, Object aElement)
  {
    synchronized (iVector)
    {
      if (iHashtable.containsKey (aKey))
        return;
      iHashtable.put (aKey, aElement);
      iVector.addElement (aKey);
    }
  }

  public boolean containsKey (Object aKey)
  {
    return iHashtable.containsKey (aKey);
  }

  public int indexOf (Object aKey)
  {
    synchronized (iVector)
    {
      Object pElm = iHashtable.get (aKey);
      if (pElm instanceof IntWrap)
        return ((IntWrap) pElm).iV;
      if (pElm == null)
        return -1;
      return iVector.indexOf (aKey);
    }
  }

  public Enumeration elements()
  {
    return iVector.elements();
  }

  public int size()
  {
    return iVector.size();
  }

  public Object elementAt (int aIndex)
  {
    return iVector.elementAt (aIndex);
  }
}



