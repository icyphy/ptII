/*
  File: UpdatableMapImpl.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  13Oct95  dl                 Create

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * UpdatableMapImpl extends UpdatableImpl to provide
 * default implementations of some Map operations. 
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

abstract class UpdatableMapImpl extends UpdatableImpl implements UpdatableMap { 


/**
 * Initialize at version 0, an empty count, and null screener
**/

  protected UpdatableMapImpl() { super(); }

/**
 * Initialize at version 0, an empty count, and supplied screener
**/
  protected UpdatableMapImpl(Predicate screener) { super(screener); }


// Default implementations of Map methods

/**
 * Implements collections.Map.puttingAt.
 * @see collections.Map#puttingAt
**/
  public synchronized  Map  puttingAt(Object key, Object element) 
  throws IllegalElementException {
    UpdatableMap c = null;
    try {
      c = ((UpdatableMap)clone());
      c.putAt(key, element);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }

/**
 * Implements collections.Map.removingAt
 * @see collections.Map#removingAt
**/
  public synchronized  Map   removingAt(Object key) {
    UpdatableMap c = null;
    try {
      c = ((UpdatableMap)clone());
      c.removeAt(key);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }

/**
 * Implements collections.Map.canIncludeKey.
 * Default key-screen. Just checks for null.
 * @see collections.Map#canIncludeKey
**/
  public boolean canIncludeKey(Object key) {
    return  (key != null);
  }

/**
 * Principal method to throw a IllegalElementException for keys
**/
  protected void checkKey(Object key) throws IllegalElementException {
    if (!canIncludeKey(key)) {
      throw new IllegalElementException(key, "Attempt to include invalid key in Collection");
    }
  }


}


