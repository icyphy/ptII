/*
  File: UpdatableSetImpl.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  13Oct95  dl                 Create
  22Oct95  dl                 add includeElements

*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * UpdatableSetImpl extends UpdatableImpl to provide
 * default implementations of some Set operations.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

abstract class UpdatableSetImpl extends UpdatableImpl implements UpdatableSet {


/**
 * Initialize at version 0, an empty count, and null screener
**/

  protected UpdatableSetImpl() { super(); }

/**
 * Initialize at version 0, an empty count, and supplied screener
**/
  protected UpdatableSetImpl(Predicate screener) { super(screener); }


// Default implementations of Set methods

/**
 * Implements collections.Set.including
 * @see collections.Set#including
**/
  public synchronized  Set including(Object element)
  throws IllegalElementException {
    UpdatableSet c = null;
    try {
      c = ((UpdatableSet)clone());
      c.include(element);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }

/**
 * Implements collections.UpdatableSet.includeElements
 * @see collections.UpdatableSet#includeElements
**/

  public synchronized  void includeElements(Enumeration e)
   throws IllegalElementException, CorruptedEnumerationException {
    while (e.hasMoreElements()) include(e.nextElement());
  }

}


