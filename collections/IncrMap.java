/*
  File: IncrMap.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Changed to use accessOnly

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 *
 * Implementation of Immutable Map
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public final class IncrMap extends IncrImpl implements Map {

/**
 * Make a new pure map using the default underlying Map implementation
**/
  public IncrMap() { this(DefaultImplementations.map()); }

/**
 * Make a pure map managing the given updatable map s.
 * Warning: Do not modify s during the the lifetime of the constructed pure map!
**/
  public IncrMap(UpdatableMap s) { super(s); }


/**
 * Make a copy. Uses lazy update.
**/

  protected Object clone() throws CloneNotSupportedException {
    undelta();
    IncrMap s = new IncrMap((UpdatableMap)(updatable_));
    nextVersion_ = s;
    updatable_ = null;
    op_ = NO_EDIT;
    return s;
  }

/**
 * Implements collections.Map.canIncludeKey.
 * @see collections.Map#canIncludeKey
**/
  public boolean canIncludeKey(Object key) {
    return ((UpdatableMap)accessOnly()).canIncludeKey(key);
  }


/**
 * Implements collections.Map.includesKey.
 * @see collections.Map#includesKey
**/
  public synchronized boolean     includesKey(Object key) {
    return ((UpdatableMap)accessOnly()).includesKey(key);
  }

/**
 * Implements collections.Map.includesAt.
 * @see collections.Map#includesAt
**/
  public synchronized boolean     includesAt(Object key, Object value) {
    return ((UpdatableMap)accessOnly()).includesAt(key, value);
  }

/**
 * Implements collections.Map.at.
 * @see collections.Map#at
**/
  public synchronized Object      at(Object key) 
  throws  NoSuchElementException {
    return ((UpdatableMap)accessOnly()).at(key);
  }

/**
 * Implements collections.Map.aKeyOf.
 * @see collections.Map#aKeyOf
**/
  public synchronized Object aKeyOf(Object element) {
    return ((UpdatableMap)accessOnly()).aKeyOf(element);
  }


/**
 * Implements collections.Map.keys.
 * @see collections.Map#keys
**/
  public synchronized CollectionEnumeration keys() {
    undelta(); 
    // wrap the underlying enumeration in Incr version 
    CollectionEnumeration e = ((UpdatableMap)(updatable_)).keys();
    IncrCollectionEnumeration ie = new IncrCollectionEnumeration(this, e);
    pin(ie);
    return ie;
  }



/**
 * Implements collections.Map.puttingAt.
 * @see collections.Map#puttingAt
**/
  public synchronized /* IncrMap */ Map  puttingAt(Object key, Object element) 
  throws IllegalElementException {
    undelta(); 
    UpdatableMap u = (UpdatableMap)updatable_;
    boolean has = u.includesAt(key, element);
    if (has) 
      return this;
    else { // if already has key, undo via another include
      boolean hasKey = u.includesKey(key);
      if (hasKey) {
        try {
          secondObjectArg_ = u.at(key);
        }
        catch (NoSuchElementException ex) {}
        u.putAt(key, element);
        IncrMap s = new IncrMap(u);
        nextVersion_ = s;
        updatable_ = null;
        firstObjectArg_ = key;
        op_ = ADD_EDIT;
        return s;
      }
      else {
        u.putAt(key, element);
        IncrMap s = new IncrMap(u);
        nextVersion_ = s;
        updatable_ = null;
        firstObjectArg_ = key;
        op_ = REMOVE_EDIT;
        return s;
      }
    }
  }

/**
 * Implements collections.Map.removingAt
 * @see collections.Map#removingAt
**/
  public synchronized /* IncrMap */ Map   removingAt(Object key) {
    undelta(); 
    UpdatableMap u = (UpdatableMap)updatable_;
    boolean has = u.includesKey(key);
    if (!has) 
      return this;
    else {
      try {
        secondObjectArg_ = u.at(key);
      }
      catch (NoSuchElementException ex) {}
      u.removeAt(key);
      IncrMap s = new IncrMap(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = key;
      op_ = ADD_EDIT;
      return s;
    }
  }


/**
 * Implements collections.Collection.removingOneOf
 * @see collections.Collection#removingOneOf
**/
  public synchronized /* IncrMap */ Collection  removingOneOf(Object element) {
    undelta(); 
    UpdatableMap u = (UpdatableMap)updatable_;
    Object key = u.aKeyOf(element);
    if (key == null)
      return this;
    else {
      u.removeAt(key);
      IncrMap s = new IncrMap(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = key;
      secondObjectArg_ = element;
      op_ = ADD_EDIT;
      return s;
    }
  }


/**
 * Implements collections.Collection.excluding.
 * If more than one occurrence of element exists, it makes
 * a full, non-lazy copy.
 * @see collections.Collection#excluding
**/
  public synchronized /* IncrMap */ Collection  excluding(Object element) {
    undelta(); 
    UpdatableMap u = (UpdatableMap)updatable_;
    int occ = u.occurrencesOf(element);
    if (occ == 0)
      return this;
    else if (occ == 1)
      return this.removingOneOf(element);
    else {
      UpdatableMap c = (UpdatableMap)(u.duplicate());
      c.exclude(element);
      return new IncrMap(c);
    }
  }


/**
 * Implements collections.Collection.replacingAllOf
 * If more than one occurrence of element exists, it makes
 * a full, non-lazy copy.
 * @see collections.Collection#replacingAllOf
**/
  public synchronized /* IncrMap */ Collection  replacingAllOf(Object oldElement,
                                             Object newElement) 
  throws IllegalElementException {
    undelta(); 
    UpdatableMap u = (UpdatableMap)updatable_;
    int oldocc = u.occurrencesOf(oldElement);
    if (oldocc == 0)
      return this;
    else if (oldocc == 1)
      return this.replacingOneOf(oldElement, newElement);
    else {
      UpdatableMap c = (UpdatableMap)(u.duplicate());
      c.replaceAllOf(oldElement, newElement);
      return new IncrMap(c);
    }
  }

/**
 * Implements collections.Collection.replacingOneOf
 * @see collections.Collection#replacingOneOf
**/
  public synchronized /* IncrMap */ Collection replacingOneOf(Object oldElement,
                                                    Object newElement) 
  throws IllegalElementException {
    undelta(); 
    UpdatableMap u = (UpdatableMap)updatable_;
    Object key = u.aKeyOf(oldElement);
    if (key == null)
      return this;
    else
      return this.puttingAt(key, newElement);
  }


/**
 * Perform updates within an edit chain
**/
  protected synchronized UpdatableCollection doEdit(UpdatableCollection c) { 
    UpdatableMap u = (UpdatableMap)c;
    try { 
      if (op_ == ADD_EDIT) 
        u.putAt(firstObjectArg_, secondObjectArg_);
      else if (op_ == REMOVE_EDIT)
        u.removeAt(firstObjectArg_);
    }
    catch (IllegalElementException ex) {} // we've screened for all possible
    return u;
  }


}


