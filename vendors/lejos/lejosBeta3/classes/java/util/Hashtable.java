package java.util;

/**
 * Maps keys to objects. It has a fixed-size table, so don't
 * expect it to scale well.
 */
public class Hashtable
{
  private static final int TABLE_SIZE = 32;
  private Object[] iTable = new Object[TABLE_SIZE];
  
  public Hashtable()
  {  
  }

  public synchronized Object get (Object aKey)
  {
    Object pElement = iTable[getTableIndex(aKey)];
    if (pElement == null)
      return null;
    KeyValuePair pKeyValuePair = getKeyValuePair (pElement, aKey);
    if (pKeyValuePair == null)
      return null;
    return pKeyValuePair.iValue;
  }
  
  public synchronized void put (Object aKey, Object aValue)
  {
    int pIndex = getTableIndex (aKey);    
    Object pElement = iTable[pIndex];
    KeyValuePair pKeyValuePair = null;
    if (pElement != null)
      pKeyValuePair = getKeyValuePair (pElement, aKey);
    if (pKeyValuePair == null)
    {
      pKeyValuePair = new KeyValuePair();
      pKeyValuePair.iKey = aKey;
      pKeyValuePair.iValue = aValue;
    }
    if (pElement == null)      
    {
      iTable[pIndex] = pKeyValuePair;
    } 
    else if (pElement == pKeyValuePair)
    {
      pKeyValuePair.iValue = aValue;	    
    }
    else if (pElement instanceof KeyValuePair)
    {	    
      Vector pVector = new Vector();
      pVector.addElement (pElement);
      pVector.addElement (pKeyValuePair);
    }
    else
    {
      // pElement must be a Vector
      ((Vector) pElement).addElement (pKeyValuePair);	    
    }
  }

  private KeyValuePair getKeyValuePair (Object aPivot, Object aKey)
  {
    if (aPivot instanceof Vector)
    {
      Vector pVec = (Vector) aPivot;
      int pSize = pVec.size();
      for (int i = 0; i < pSize; i++)
      {
	KeyValuePair pPair = (KeyValuePair) pVec.elementAt (i);
	if (aKey.equals (pPair.iKey))
          return pPair;
      }
      return null;
    }
    // Not a Vector, must be a lone KeyValuePair
    KeyValuePair pPair = (KeyValuePair) aPivot;
    if (aKey.equals (pPair.iKey))
      return pPair;
    return null;
  }
  
  private int getTableIndex (Object aKey)
  {
    int pHash = aKey.hashCode();
    if (pHash < 0)
      pHash = -pHash;
    return pHash % TABLE_SIZE;
  }

  private static class KeyValuePair
  { 
    Object iKey;
    Object iValue;
  }
}
