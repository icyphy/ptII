/*
  File: CheckedMap.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Misc clean-up

*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 *
 * CheckedMap supports standard update operations on maps.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/


public class CheckedMap extends CheckedCollection implements UpdatableMap {

/**
 * Wrap Map m inside a checker
**/
  public CheckedMap(UpdatableMap m) { super(m); }

/**
 * Make a Checked clone of underlying collection
**/
  protected Object clone() throws CloneNotSupportedException  {
    return new CheckedMap((UpdatableMap)(thys.duplicate()));
  }

/**
 * return casted version of CheckedCollection.thys.
**/
  public UpdatableMap thys() { return (UpdatableMap)thys; }

/**
 * return casted version of CheckedCollection.prev.
**/

  public UpdatableMap prev() { return (UpdatableMap)prev; }


/**
 * Checks collections.Map.canIncludeKey according to its specification
 * @see collections.Map#canIncludeKey
**/
  public boolean canIncludeKey(Object key) {
    preCheck();
    boolean result = thys().canIncludeKey(key);
    assert(!(result == true && key == null));
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }


/**
 * Checks collections.Map.includesKey according to its specification
 * @see collections.Map#includesKey
**/
  public synchronized boolean     includesKey(Object key) {
    preCheck();
    boolean result = thys().includesKey(key);
    assert(!(result == true && !thys().canInclude(key)));
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }

/**
 * Checks collections.Map.includesAt according to its specification
 * @see collections.Map#includesAt
**/
  public synchronized boolean     includesAt(Object key, Object value) {
    preCheck();
    boolean result = thys().includesAt(key, value);
    assert(!(result == true && (!thys().includesKey(key) || !thys().includes(value))));
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }


/**
 * Checks collections.Map.keys according to its specification
 * @see collections.Map#keys
**/
  public synchronized CollectionEnumeration keys() {
    int c = thys().size();
    CollectionEnumeration k = keys();
    while (k.hasMoreElements()) {
      assert(k.numberOfRemainingElements() == c);
      --c;
      Object key = k.nextElement();
      Object v = thys().at(k);
      assert(thys().canIncludeKey(key));
      assert(thys().canInclude(v));
      assert(thys().aKeyOf(v) != null);
      assert(thys().includesKey(key));
      assert(thys().includes(v));
      assert(thys().includesAt(k, v));
    }

    postCheck();
    // return a new one
    return thys().keys();
  }


/**
 * Checks collections.Map.at according to its specification
 * @see collections.Map#at
**/
  public synchronized Object      at(Object key)
  throws  NoSuchElementException {
    preCheck();
    try {
      Object result = thys().at(key);
      assert(thys().includesAt(key, result));
      assert(thys.sameStructure(prev));
      postCheck();
      return result;
    }
    catch (NoSuchElementException ex) {
      assert(!thys().includesKey(key));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }

/**
 * Checks collections.Map.aKeyOf according to its specification
 * @see collections.Map#aKeyOf
**/
  public synchronized Object aKeyOf(Object element) {
    preCheck();
    Object result = thys().aKeyOf(element);
    assert((result == null && !thys().includes(element)) ||
      (thys().at(result).equals(element)));
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }



/**
 * Checks collections.UpdatableMap.putAt according to its specification
 * @see collections.UpdatableMap#putAt
**/
  public synchronized void putAt(Object key, Object element)
  throws IllegalElementException {
    preCheck();
    try {
      thys().putAt(key, element);
      checkPut(thys(), prev(), key, element, true);
      postCheck();
    }
    catch (IllegalElementException ex) {
      assert(!canIncludeKey(key) || !canInclude(element));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
  }

/**
 * Checks collections.Map.puttingAt.
 * @see collections.Map#puttingAt
**/
  public synchronized  Map  puttingAt(Object key, Object element)
  throws IllegalElementException {
    preCheck();
    try {
      Map nc = thys().puttingAt(key, element);
      checkPut(nc, thys(), key, element, false);
      assert(thys.sameStructure(prev));
      postCheck();
      return nc;
    }
    catch (IllegalElementException ex) {
      assert(!canIncludeKey(key) || !canInclude(element));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }



/**
 * Checks collections.UpdatableMap.removeAt according to its specification
 * @see collections.UpdatableMap#removeAt
**/
  public synchronized void removeAt(Object key) {
    preCheck();
    thys().removeAt(key);
    checkRemoveAt(thys(), prev(), key, true);
    postCheck();
  }


/**
 * Checks collections.Map.removingAt
 * @see collections.Map#removingAt
**/
  public synchronized  Map   removingAt(Object key) {
    preCheck();
    Map nc = thys().removingAt(key);
    checkRemoveAt(nc, thys(), key, false);
    assert(thys.sameStructure(prev));
    postCheck();
    return nc;
  }


/**
 * Checks collections.UpdatableMap.replaceElement according to its specification
 * @see collections.UpdatableMap#replaceElement
**/
  public synchronized void replaceElement(Object key, Object oldElement,
                                          Object newElement)
  throws IllegalElementException {
    preCheck();
    try {
      thys().replaceElement(key, oldElement, newElement);
      assert((!thys().includesAt(key, oldElement) ||
         thys().includesAt(key, newElement)));
      assert(thys().size() == prev().size());
      assert(!(thys().version() == prevVersion &&
          prev().includesAt(key, oldElement)));
      CollectionEnumeration prevKeys = prev().keys();
      while (prevKeys.hasMoreElements()) {
        Object k = prevKeys.nextElement();
        assert((k.equals(key) || thys().includesAt(k, prev().at(k))));
      }
      postCheck();
    }
    catch (IllegalElementException ex) {
      assert(!canInclude(newElement));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
  }


/**
 * Helper for checking put*
**/

  protected void checkPut(Map nc, Map oc, Object key,
                          Object element, boolean verchk) {

    assert(nc.canIncludeKey(key));
    assert(nc.canInclude(element));

    int reqSize = oc.size();
    if (!oc.includesKey(key)) reqSize = reqSize + 1;
    assert(nc.includesAt(key, element));
    assert(nc.size() == reqSize);

    CollectionEnumeration os = oc.keys();
    while (os.hasMoreElements()) {
      Object k = os.nextElement();
      assert((k.equals(key) || nc.includesAt(k, oc.at(k))));
    }

    if (verchk) {
      int nv = ((UpdatableCollection)nc).version();
      assert((nv == prevVersion) == oc.includesAt(key, element));
    }
    nc.checkImplementation();

  }

/**
 * Helper for checking remov*
**/

  protected void checkRemoveAt(Map nc, Map oc, Object key,
                               boolean verchk) {

    int reqSize = oc.size();
    if (oc.includesKey(key)) reqSize = reqSize - 1;
    assert(!nc.includesKey(key));
    assert(nc.size() == reqSize);

    CollectionEnumeration os = oc.keys();
    while (os.hasMoreElements()) {
      Object k = os.nextElement();
      assert((k.equals(key) || nc.includesAt(k, oc.at(k))));
    }

    if (verchk) {
      int nv = ((UpdatableCollection)nc).version();
      assert((nv == prevVersion) == !oc.includesKey(key));
    }

    nc.checkImplementation();

  }


};


