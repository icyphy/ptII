/*
  File: CheckedBag.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Misc clean up
  19Oct95  dl                 More misc clean up

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
**/

public class CheckedBag extends CheckedCollection implements UpdatableBag  {

/**
 * Wrap Bag b inside a checker
**/
  public CheckedBag(UpdatableBag b) { super(b); }

/**
 * Make a Checked clone of underlying collection
**/

  protected Object clone() throws CloneNotSupportedException {
    return new CheckedBag((UpdatableBag)(thys.duplicate()));
  }

/**
 * return casted version of CheckedCollection.thys.
**/
  public UpdatableBag thys() { return (UpdatableBag)thys; }

/**
 * return casted version of CheckedCollection.prev.
**/

  public UpdatableBag prev() { return (UpdatableBag)prev; }

/**
 * Checks collections.UpdatableBag.addIfAbsent according to its specification
 * @see collections.UpdatableBag#addIfAbsent
**/

  public synchronized void addIfAbsent(Object element)
  throws IllegalElementException {
    preCheck();
    try {
      thys().addIfAbsent(element);
      checkAdd(thys, prev, element, true, true);
      postCheck();
    }
    catch (IllegalElementException ex) {
      assert(!prev.canInclude(element));
    assert(thys.sameStructure(prev));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
  }

/**
 * Checks collections.Bag.addingIfAbsent
 * @see collections.Bag#addingIfAbsent
**/
  public synchronized Bag addingIfAbsent(Object element)
  throws IllegalElementException {
    preCheck();
    try {
      Bag nc = thys().addingIfAbsent(element);
      checkAdd(nc, thys, element, true, false);
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
 * Checks collections.UpdatableBag.add according to its specification
 * @see collections.UpdatableBag#add
**/
  public synchronized void add(Object element) throws IllegalElementException {
    preCheck();
    try {
      thys().add(element);
      checkAdd(thys, prev, element, false, true);
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
 * Checks collections.Bag.adding
 * @see collections.Bag#adding
**/

  public synchronized  Bag adding(Object element)
  throws IllegalElementException {
    preCheck();
    try {
      Bag nc = thys().adding(element);
      checkAdd(nc, thys, element, false, false);
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
 * Checks collections.UpdatableBag.addElements
 * @see collections.UpdatableBag#addElements
**/

  public synchronized  void addElements(Enumeration e)
   throws IllegalElementException, CorruptedEnumerationException {
    preCheck();
    thys().addElements(e);
    // can't check for elements because e is exhausted
    postCheck();
  }

/**
 * Helper for checking add*
**/

  protected void checkAdd(Collection nc, Collection oc,
                          Object element, boolean ifAbsent,
                          boolean verchk) {

    assert(nc.canInclude(element));

    int oOcc = oc.occurrencesOf(element);
    int reqOcc = oOcc + 1;
    if (ifAbsent && oOcc != 0) reqOcc = oOcc;

    int nOcc = nc.occurrencesOf(element);

    assert(nOcc == reqOcc);
    assert(nc.size() == oc.size() + (reqOcc - oOcc));

    if (verchk) {
      int nv = ((UpdatableCollection)nc).version();
      assert((nv == prevVersion) == (nOcc == oOcc));
    }

    // all other elements the same
    CollectionEnumeration os = oc.elements();
    while (os.hasMoreElements()) {
      Object v = os.nextElement();
      assert((v.equals(element) ||
              (nc.occurrencesOf(v) == oc.occurrencesOf(v))));
    }

    nc.checkImplementation();

  }

}

