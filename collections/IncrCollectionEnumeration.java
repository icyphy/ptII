/*
  File: IncrCollectionEnumeration.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  13Oct    dl@cs.oswego.edu   Create.

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 *
 * Enumerator for IncrCollections
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public final class IncrCollectionEnumeration implements CollectionEnumeration {
  private IncrImpl owner_;
  private CollectionEnumeration enum_;

/**
 * Wrap the enumeration e created by o in an Incr version
**/

  IncrCollectionEnumeration(IncrImpl o, CollectionEnumeration e) {
    owner_ = o; enum_ = e;
  }


/**
 * Implements java.util.Enumeration.hasMoreElements.
 * @see java.util.Enumeration#hasMoreElements
**/
  public synchronized boolean hasMoreElements() {
    // call back owner_ if exhausted
    boolean has = enum_.hasMoreElements();
    if (!has) owner_.unpin(this);
    return has;
  }

/**
 * Implements collections.CollectionEnumeration.numberOfRemainingElements
 * @see collections.CollectionEnumeration#numberOfRemainingElements
**/
  public synchronized int numberOfRemainingElements() {
    return enum_.numberOfRemainingElements();
  }

/**
 * Implements java.util.Enumeration.nextElement().
 * @see java.util.Enumeration#nextElement()
**/
  public synchronized Object nextElement() {
    return enum_.nextElement();
  }

/**
 * Implements collections.CollectionEnumeration.corrupted.
 * Should always return false unless underlying collection
 * that has been wrapped in Incr version has been independently
 * modified.
 * @return false
 * @see collections.CollectionEnumeration#corrupted
**/
  public synchronized boolean corrupted() {
    return enum_.corrupted(); 
  }


/**
 * IncrCollectionEnumerations are NOT copyable!
**/

}

