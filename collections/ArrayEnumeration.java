/*
  File: ArrayEnumeration.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Changed protection statuses

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * ArrayEnumeration allows you to use arrays as Enumerations
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public final class ArrayEnumeration implements CollectionEnumeration {
  private Object [] arr_;
  private int cur_;
  private int size_;

/**
 * Build an enumeration that returns successive elements of the array
**/  
  public ArrayEnumeration(Object arr[]) { 
    arr_ = arr; cur_ = 0; size_ = arr.length;
  }

/**
 * Implements collections.CollectionEnumeration.numberOfRemainingElements
 * @see collections.CollectionEnumeration#numberOfRemainingElements
**/
  public int numberOfRemainingElements() { return size_; }

/**
 * Implements java.util.Enumeration.hasMoreElements.
 * @see java.util.Enumeration#hasMoreElements
**/
  public boolean hasMoreElements() { return size_ > 0; }

/**
 * Implements collections.CollectionEnumeration.corrupted.
 * Always false. Inconsistency cannot be reliably detected for arrays
 * @return false
 * @see collections.CollectionEnumeration#corrupted
**/

  public boolean corrupted() { return false; }

/**
 * Implements java.util.Enumeration.nextElement().
 * @see java.util.Enumeration#nextElement()
**/
  public Object nextElement() {  
    if (!hasMoreElements())
      throw new NoSuchElementException("exhausted enumeration");
    else {
      size_--;
      return  arr_[cur_++];  
    }
  }
}


