package js.tinyvm;

import java.util.*;
import js.classfile.*;

public class EnumerableSet extends RecordTable
{
  final HashVector iVector = new HashVector();

  public void add (WritableData aElement)
  {
    iVector.addElement (aElement);
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

  public boolean contains (Object aElement)
  {
    return iVector.containsKey (aElement);
  }

  public int indexOf (Object aElement)
  {
    return iVector.indexOf (aElement);
  }
}
