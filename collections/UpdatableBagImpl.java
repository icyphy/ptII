/*
  File: UpdatableBagImpl.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  13Oct95  dl                 Create
  22Oct95  dl                 add addElements
*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * UpdatableBagImpl extends UpdatableImpl to provide
 * default implementations of some Bag operations.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

abstract class UpdatableBagImpl extends UpdatableImpl implements UpdatableBag {


/**
 * Initialize at version 0, an empty count, and null screener
**/

  protected UpdatableBagImpl() { super(); }

/**
 * Initialize at version 0, an empty count, and supplied screener
**/
  protected UpdatableBagImpl(Predicate screener) { super(screener); }


// Default implementations of Bag methods

/**
 * Implements collections.Bag.addingIfAbsent
 * @see collections.Bag#addingIfAbsent
**/
  public synchronized Bag addingIfAbsent(Object element)
  throws IllegalElementException {
    UpdatableBag c = null;
    try {
      c = ((UpdatableBag)clone());
      c.addIfAbsent(element);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }


/**
 * Implements collections.Bag.adding
 * @see collections.Bag#adding
**/

  public synchronized  Bag adding(Object element)
  throws IllegalElementException {
    UpdatableBag c = null;
    try {
      c = ((UpdatableBag)clone());
      c.add(element);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }

/**
 * Implements collections.UpdatableBag.addElements
 * @see collections.UpdatableBag#addElements
**/

  public synchronized  void addElements(Enumeration e)
   throws IllegalElementException, CorruptedEnumerationException {
    while (e.hasMoreElements()) add(e.nextElement());
  }

}

