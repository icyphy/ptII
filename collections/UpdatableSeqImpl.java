/*
  File: UpdatableSeqImpl.java

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
 * UpdatableSeqImpl extends UpdatableImpl to provide
 * default implementations of some Seq operations. 
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

abstract class UpdatableSeqImpl extends UpdatableImpl implements UpdatableSeq { 


/**
 * Initialize at version 0, an empty count, and null screener
**/

  protected UpdatableSeqImpl() { super(); }

/**
 * Initialize at version 0, an empty count, and supplied screener
**/
  protected UpdatableSeqImpl(Predicate screener) { super(screener); }


// Default implementations of Seq methods

/**
 * Implements collections.Seq.insertingAt.
 * @see collections.Seq#insertingAt
**/
  public synchronized Seq  insertingAt(int index, Object element) 
  throws IllegalElementException, NoSuchElementException {
    UpdatableSeq c = null;
    try {
      c = ((UpdatableSeq)clone());
      c.insertAt(index, element);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }

/**
 * Implements collections.Seq.removingAt.
 * @see collections.Seq#removingAt
**/
  public synchronized Seq  removingAt(int index) 
  throws NoSuchElementException {
    UpdatableSeq c = null;
    try {
      c = ((UpdatableSeq)clone());
      c.removeAt(index);
    } catch (CloneNotSupportedException ex) {}      
    return c;
  }

/**
 * Implements collections.Seq.replacingAt
 * @see collections.Seq#replacingAt
**/
  public synchronized  Seq  replacingAt(int index, Object element) 
  throws IllegalElementException, NoSuchElementException {
    UpdatableSeq c = null;
    try {
      c = ((UpdatableSeq)clone());
      c.replaceAt(index, element);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }

}


