/*
  File: UpdatableImpl.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Add assert
  22Oct95  dl                 Add excludeElements, removeElements

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * UpdatableImpl serves as a convenient base class for most 
 * implementations of updatable collections. It maintains
 * a version number and element count.
 * It also provides default implementations of many
 * collection operations. 
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

abstract class UpdatableImpl implements UpdatableCollection { 

// instance variables

/** 
 * version_ represents the current version number
**/
  protected int version_;

/** 
 * screener_ hold the supplied element screener
**/

  protected Predicate screener_;

/**
 * count_ holds the number of elements.
**/
  protected int count_;

// constructors

/**
 * Initialize at version 0, an empty count, and null screener
**/

  protected UpdatableImpl() { version_ = 0; count_ = 0; screener_ = null; }

/**
 * Initialize at version 0, an empty count, and supplied screener
**/
  protected UpdatableImpl(Predicate screener) { 
    version_ = 0; count_ = 0; screener_ = screener; }



// Default implementations of Collection methods

/**
 * Wrapper for clone()
 * @see clone
**/
 
 public synchronized Collection duplicate() { 
   Collection c = null;
   try {
     c = (Collection)(this.clone()); 
    } catch (CloneNotSupportedException ex) {}
   return c;
 }

/**
 * Implements collections.Collection.isEmpty.
 * Time complexity: O(1).
 * @see collections.Collection#isEmpty
**/

  public synchronized boolean isEmpty()  { return count_ == 0; }

/**
 * Implements collections.Collection.size.
 * Time complexity: O(1).
 * @see collections.Collection#size
**/
  public synchronized int size() { return count_; }

/**
 * Implements collections.Collection.canInclude.
 * Time complexity: O(1) + time of screener, if present
 * @see collections.Collection#canInclude
**/
  public boolean canInclude(Object element) {
    return element != null && (screener_ == null || screener_.predicate(element));
  }

/**
 * Implements collections.Collection.sameStructure
 * Time complexity: O(n).
 * Default implementation. Fairly sleazy approach.
 * (Defensible only when you remember that it is just a default impl.)
 * It tries to cast to one of the known collection interface types
 * and then applies the corresponding comparison rules.
 * This suffices for all currently supported collection types,
 * but must be overridden if you define new Collection subinterfaces
 * and/or implementations.
 * 
 * @see collections.Collection#sameStructure
**/

  public synchronized boolean sameStructure(Collection other) {
    if (other == null) 
      return false;
    else if (other == this) 
      return true;
    else if (this instanceof KeySortedCollection) {
      if (!(other instanceof Map)) return false;
      else return sameOrderedPairs((Map)this, (Map)other);
    }
    else if (this instanceof Map) {
      if (!(other instanceof Map)) return false;
      else return samePairs((Map)(this), (Map)(other));
    }
    else if ((this instanceof Seq) || 
             (this instanceof ElementSortedCollection))
      return sameOrderedElements(this, other);
    else if (this instanceof Bag)
      return sameOccurrences(this, other);
    else if (this instanceof Set)
      return sameInclusions(this, (Collection)(other));
    else
      return false;
  }


/**
 * Implements collections.Collection.removingOneOf
 * @see collections.Collection#removingOneOf
**/
  public synchronized Collection removingOneOf(Object element) {
    UpdatableCollection c = null;
    try {
      c = ((UpdatableCollection)clone());
      c.removeOneOf(element);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }

/**
 * Implements collections.Collection.excluding.
 * @see collections.Collection#excluding
**/
  public synchronized Collection excluding(Object element) {
    UpdatableCollection c = null;
    try {
      c = ((UpdatableCollection)clone());
      c.exclude(element);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }


/**
 * Implements collections.Collection.replacingOneOf
 * @see collections.Collection#replacingOneOf
**/
  public synchronized Collection replacingOneOf(Object oldElement,
                                                Object newElement) 
  throws IllegalElementException {
    UpdatableCollection c = null;
    try {
      c = ((UpdatableCollection)clone());
      c.replaceOneOf(oldElement, newElement);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }

/**
 * Implements collections.Collection.replacingAllOf
 * @see collections.Collection#replacingAllOf
**/
  public synchronized Collection replacingAllOf(Object oldElement,
                                                    Object newElement) 
  throws IllegalElementException {
    UpdatableCollection c = null;
    try {
      c = ((UpdatableCollection)clone());
      c.replaceAllOf(oldElement, newElement);
    } catch (CloneNotSupportedException ex) {}
    return c;
  }


// Default implementations of UpdatableCollection methods

/** 
 * Implements collections.UpdatableCollection.version.
 * Time complexity: O(1).
 * @see collections.UpdatableCollection#version
**/
  public synchronized int version() { return version_; }


/** 
 * Implements collections.UpdatableCollection.excludeElements
 * @see collections.UpdatableCollection#excludeElements
**/
  public synchronized void excludeElements(Enumeration e) 
  throws CorruptedEnumerationException {
    while (e.hasMoreElements()) exclude(e.nextElement());
  }


/** 
 * Implements collections.UpdatableCollection.removeElements
 * @see collections.UpdatableCollection#removeElements
**/
  public synchronized void removeElements(Enumeration e) 
  throws CorruptedEnumerationException {
    while (e.hasMoreElements()) removeOneOf(e.nextElement());
  }

// Object methods

/**
 * Default implementation of toString for Collections. Not
 * very pretty, but parenthesizing each element means that
 * for most kinds of elements, it's conceivable that the
 * strings could be parsed and used to build other collections.
 * <P>
 * Not a very pretty implementation either. Casts are used
 * to get at elements/keys
**/

  public synchronized String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("( (class: " + getClass().toString() + ")");
    buf.append(" (size:" + size() + ")");
    buf.append(" (elements:");
    if (!(this instanceof Map)) {
      Enumeration e = elements();
      try {
        while (e.hasMoreElements()) {
          buf.append(" (");
          buf.append(e.nextElement().toString());
          buf.append(")");
        }
      }
      catch (NoSuchElementException ex) {
        buf.append("? Cannot access elements?");
      }
    }
    else {
      Map m = (Map)this;
      Enumeration k = m.keys();
      Enumeration e = m.elements();
      try {
        while (k.hasMoreElements()) {
          buf.append(" (");
          
          buf.append(" (");
          buf.append(k.nextElement().toString());
          buf.append(")");
          
          buf.append(" (");
          buf.append(e.nextElement().toString());
          buf.append(")");
          
          buf.append(" )");
        }
      }
      catch (NoSuchElementException ex) {
        buf.append("? Cannot access elements?");
      }
    }
    buf.append(" ) )");
    return buf.toString();
  }

// protected operations on version_ and count_

/**
 * change the version number
**/

  protected synchronized void incVersion() { ++version_; }


/**
 * Increment the element count and update version_
**/
  protected final void incCount() { count_++; version_++; }

/**
 * Decrement the element count and update version_
**/
  protected final void decCount() { count_--; version_++; }


/**
 * add to the element count and update version_ if changed
**/
  protected final void addToCount(int c) { 
    if (c != 0) { count_ += c; version_++; }
  }

/**
 * set the element count and update version_ if changed
**/
  protected final void setCount(int c) { 
    if (c != count_) { count_ = c; version_++; }
  }


// Helper methods left public since they might be useful

  public static boolean sameInclusions(Collection s, Collection t) {
    if (s.size() != t.size()) return false;

    try { // set up to return false on collection exceptions
      Enumeration ts = t.elements();
      while (ts.hasMoreElements()) {
        if (!s.includes(ts.nextElement()))
          return false;
      }
      return true;
    }
    catch (NoSuchElementException ex) { return false; }
  }

  public static boolean sameOccurrences(Collection s, Collection t) {
    if (s.size() != t.size()) return false;

    Enumeration ts = t.elements();
    Object last = null; // minor optimization -- skip two successive if same

    try { // set up to return false on collection exceptions
      while (ts.hasMoreElements()) {
        Object m = ts.nextElement();
        if (m != last) {
          if (s.occurrencesOf(m) != t.occurrencesOf(m)) return false;
        }
        last = m;
      }
      return true;
    }
    catch (NoSuchElementException ex) { return false; }
  }

  public static boolean sameOrderedElements(Collection s, Collection t) {
    if (s.size() != t.size()) return false;

    Enumeration ts = t.elements();
    Enumeration ss = s.elements();

    try { // set up to return false on collection exceptions
      while (ts.hasMoreElements()) {
        Object m = ts.nextElement();
        Object o = ss.nextElement();
        if (!m.equals(o)) return false;
      }
      return true;
    }
    catch (NoSuchElementException ex) { return false; }
  }

  public static boolean samePairs(Map s, Map t) {
    if (s.size() != t.size()) return false;
        
    Enumeration ts = t.keys();
        
    try { // set up to return false on collection exceptions
      while (ts.hasMoreElements()) {
        Object k = ts.nextElement();
        Object v = t.at(k);
        if (!s.includesAt(k, v)) return false;
      }
      return true;
    }
    catch (NoSuchElementException ex) { return false; }
  }

  public static boolean sameOrderedPairs(Map s, Map t) {
    if (s.size() != t.size()) return false;
        
    Enumeration ts = t.keys();
    Enumeration ss = s.keys();    

    try { // set up to return false on collection exceptions
      while (ts.hasMoreElements()) {
        Object sk = ss.nextElement();
        Object k = ts.nextElement();
        if (!sk.equals(k)) return false;
        Object v = t.at(k);
        if (!s.includesAt(k, v)) return false;
      }
      return true;
    }
    catch (NoSuchElementException ex) { return false; }
  }

// misc common helper methods 

/**
 * Principal method to throw a NoSuchElementException.
 * Besides index checks in Seqs, you can use it to check for
 * operations on empty collections via checkIndex(0)
**/
  protected void checkIndex(int index) throws NoSuchElementException {
    if (index < 0 || index >= count_) {
      String msg;
      if (count_ == 0) msg = "Element access on empty collection";
      else msg = "Index " + String.valueOf(index) + " out of range for collection of size" + String.valueOf(count_);
      throw new NoSuchElementException(msg);
    }
  }

/**
 * Principal method to throw a IllegalElementException
**/

  protected void checkElement(Object element) throws IllegalElementException {
    if (!canInclude(element)) {
      throw new IllegalElementException(element, "Attempt to include invalid element in Collection");
    }
  }


/**
 * Implements collections.ImplementationCheckable.assert.
 * @see collections.ImplementationCheckable#assert
**/
  public void assert(boolean pred) 
  throws ImplementationError {
    ImplementationError.assert(this, pred);
  }

/**
 * Implements collections.ImplementationCheckable.checkImplementation.
 * @see collections.ImplementationCheckable#checkImplementation
**/
  public synchronized void checkImplementation() 
  throws ImplementationError {
    assert(count_ >= 0);
  }
}


