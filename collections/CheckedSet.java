/*
  File: CheckedSet.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Misc clean-up.
  19Oct95  dl                 More misc clean-up.

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * @author Doug Lea
 * @version 0.94
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

public class CheckedSet extends CheckedCollection implements UpdatableSet  {

/**
 * Wrap Set s inside a checker
**/
  public CheckedSet(UpdatableSet s) { super(s); }

/**
 * Make a Checked clone of underlying collection
**/
  protected Object clone() throws CloneNotSupportedException { 
    return new CheckedSet((UpdatableSet)(thys.duplicate()));
  }

/**
 * return casted version of CheckedCollection.thys.
**/
  public UpdatableSet thys() { return (UpdatableSet)thys; }

/**
 * return casted version of CheckedCollection.prev.
**/
  public UpdatableSet prev() { return (UpdatableSet)prev; }



/**
 * Checks collections.UpdatableSet.include according to its specification
 * @see collections.UpdatableSet#include
**/
  public synchronized void include(Object element) throws IllegalElementException {
    preCheck();
    try {
      thys().include(element);
      checkInclude(thys, prev, element, true);
      postCheck();
    }
    catch (IllegalElementException ex) {
      assert(!prev.canInclude(element));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
  }




/**
 * Checks collections.Set.including
 * @see collections.Set#including
**/
  public synchronized  Set including(Object element) 
  throws IllegalElementException {
    preCheck();
    try {
      Set nc = thys().including(element);
      checkInclude(nc, thys, element, false);
      assert(thys.sameStructure(prev));
      postCheck();
      return nc;
    }
    catch (IllegalElementException ex) {
      assert(!prev.canInclude(element));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }

/**
 * Checks collections.UpdatableSet.includeElements
 * @see collections.UpdatableSet#includeElements
**/

  public synchronized  void includeElements(Enumeration e) 
   throws IllegalElementException, CorruptedEnumerationException {
    preCheck();
    thys().includeElements(e);
    // can't check for elements because e is exhausted
    postCheck();
  }

/**
 * Helper for checking includ*
**/

  protected void checkInclude(Collection nc, Collection oc,
                              Object element, boolean verchk) {

    assert(nc.canInclude(element));

    assert(nc.occurrencesOf(element) == 1);

    if (verchk) {
      int nv = ((UpdatableCollection)nc).version();
      assert((nv == prevVersion) == oc.includes(element));
    }

    // all other elements the same
    CollectionEnumeration os = oc.elements();
    while (os.hasMoreElements()) {
      Object v = os.nextElement();
      assert(nc.occurrencesOf(v) == 1);
    }
    nc.checkImplementation();

  }

};

