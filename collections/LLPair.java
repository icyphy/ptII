/*
  File: LLPair.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 *
 * LLPairs are LLCells with keys, and operations that deal with them.
 * As with LLCells, the are pure implementation tools.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class LLPair extends LLCell implements Pair {

// instance variables

  private Object   key_;

/**
 * Make a cell with given key, elment, and next link
**/

  public LLPair(Object k, Object v, LLPair n) { super(v, n); key_ = k; }

/**
 * Make a pair with given key and element, and null next link
**/

  public LLPair(Object k, Object v)           { super(v, null); key_ = k; }

/**
 * Make a pair with null key, elment, and next link
**/

  public LLPair()                             { super(null, null); key_ = null;}

/**
 * return the key
**/

  public final Object key()           { return key_; }

/**
 * set the key
**/

  public final void   key(Object k)   { key_ = k; }


/**
 * return a cell with key() key or null if no such
**/

  public final LLPair findKey(Object key) {
    for (LLPair p = this; p != null; p = (LLPair)(p.next()))
      if (p.key().equals(key)) return p;
    return null;
  }

/**
 * return a cell holding the indicated pair or null if no such
**/

  public final LLPair find(Object key, Object element) {
    for (LLPair p = this; p != null; p = (LLPair)(p.next()))
      if (p.key().equals(key) && p.element().equals(element)) return p;
    return null;
  }

/**
 * Return the number of cells traversed to find a cell with key() key,
 * or -1 if not present
**/

  public final int indexKey(Object key) {
    int i = 0;
    for (LLPair p = this; p != null; p = (LLPair)(p.next())) {
      if (p.key().equals(key)) return i;
      else ++i;
    }
    return -1;
  }

/**
 * Return the number of cells traversed to find a cell with indicated pair
 * or -1 if not present
**/
  public final int index(Object key, Object element) {
    int i = 0;
    for (LLPair p = this; p != null; p = (LLPair)(p.next())) {
      if (p.key().equals(key) && p.element().equals(element)) return i;
      else ++i;
    }
    return -1;
  }

/**
 * Return the number of cells with key() key.
**/
  public final int countKey(Object key) {
    int c = 0;
    for (LLPair p = this; p != null; p = (LLPair)(p.next())) 
      if (p.key().equals(key)) ++c;
    return c;
  }

/**
 * Return the number of cells with indicated pair
**/
  public final int count(Object key, Object element) {
    int c = 0;
    for (LLPair p = this; p != null; p = (LLPair)(p.next())) 
      if (p.key().equals(key) && p.element().equals(element)) ++c;
    return c;
  }

  protected Object clone()  throws CloneNotSupportedException { 
    return new LLPair(key(), element(), (LLPair)(next())); 
  }

}
