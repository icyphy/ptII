/*
  File: LLMap.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Changed protection statuses
  21Oct95  dl                 Fixed error in removeAt

*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 *
 * Linked lists of (key, element) pairs
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/


public class LLMap extends UpdatableMapImpl implements UpdatableMap {

// instance variables

/**
 * The head of the list. Null if empty
**/

  protected LLPair list_;

// constructors

/**
 * Make an empty list
**/

  public LLMap() { this(null, null, 0); }

/**
 * Make an empty list with the supplied element screener
**/

  public LLMap(Predicate screener) { this(screener, null, 0); }

/**
 * Special version of constructor needed by clone()
**/
  protected LLMap(Predicate s, LLPair l, int c) {
    super(s); list_ = l;  count_ = c;
  }

/**
 * Make an independent copy of the list. Does not clone elements
**/

  protected Object clone()  throws CloneNotSupportedException {
    if (list_ == null) return new LLMap(screener_, null, 0);
    else return new LLMap(screener_, (LLPair)(list_.copyList()), count_);
  }


// Collection methods

/**
 * Implements collections.Collection.includes.
 * Time complexity: O(n).
 * @see collections.Collection#includes
**/
  public synchronized boolean includes(Object element) {
    if (element == null || list_ == null) return false;
    return list_.find(element) != null;
  }

/**
 * Implements collections.Collection.occurrencesOf.
 * Time complexity: O(n).
 * @see collections.Collection#occurrencesOf
**/
  public synchronized int occurrencesOf(Object element) {
    if (element == null || list_ == null) return 0;
    return list_.count(element);
  }

/**
 * Implements collections.Collection.elements.
 * Time complexity: O(1).
 * @see collections.Collection#elements
**/
  public synchronized CollectionEnumeration elements() {
    return new LLPairEnumeration(this, list_, false);
  }

// Map methods


/**
 * Implements collections.Map.includesKey.
 * Time complexity: O(n).
 * @see collections.Map#includesKey
**/
  public synchronized boolean  includesKey(Object key) {
    if (key == null || list_ == null) return false;
    return list_.findKey(key) != null;
  }

/**
 * Implements collections.Map.includesAt
 * Time complexity: O(n).
 * @see collections.Map#includesAt
**/
  public synchronized boolean includesAt(Object key, Object element) {
    if (key == null || element == null || list_ == null) return false;
    return list_.find(key, element) != null;
  }

/**
 * Implements collections.Map.keys.
 * Time complexity: O(1).
 * @see collections.Map#keys
**/
  public synchronized CollectionEnumeration keys() {
    return new LLPairEnumeration(this, list_, true);
  }

/**
 * Implements collections.Map.at.
 * Time complexity: O(n).
 * @see collections.Map#at
**/
  public synchronized Object at(Object key)
  throws  NoSuchElementException {
    checkKey(key);
    if (list_ != null) {
      LLPair p = list_.findKey(key);
      if (p != null) return p.element();
    }
    throw new NoSuchElementException("no Key matching argument " + key);
  }

/**
 * Implements collections.Map.aKeyOf.
 * Time complexity: O(n).
 * @see collections.Map#aKeyOf
**/
  public synchronized Object aKeyOf(Object element) {
    if (element == null || count_ == 0) return null;
    LLPair p = ((LLPair)(list_.find(element)));
    if (p != null)
      return p.key();
    else
      return null;
  }


// UpdatableCollection methods

/**
 * Implements collections.UpdatableCollection.clear.
 * Time complexity: O(1).
 * @see collections.UpdatableCollection#clear
**/
  public synchronized void clear() {
    list_ = null;
    setCount(0);
  }

/**
 * Implements collections.UpdatableCollection.replaceOneOf
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#replaceOneOf
**/
  public synchronized void replaceOneOf(Object oldElement, Object newElement)
  throws IllegalElementException {
    replace_(oldElement, newElement, false);
  }

/**
 * Implements collections.UpdatableCollection.replaceAllOf.
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#replaceAllOf
**/
  public synchronized void replaceAllOf(Object oldElement,
                                                 Object newElement)
  throws IllegalElementException {
    replace_(oldElement, newElement, true);
  }

/**
 * Implements collections.UpdatableCollection.exclude.
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#exclude
**/
  public synchronized void exclude(Object element)  {
    remove_(element, true);
  }

/**
 * Implements collections.UpdatableCollection.removeOneOf.
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#removeOneOf
**/
  public synchronized void removeOneOf(Object element)  {
    remove_(element, false);
  }

/**
 * Implements collections.UpdatableCollection.take.
 * Time complexity: O(1).
 * takes the first element on the list
 * @see collections.UpdatableCollection#take
**/
  public synchronized Object take()
  throws NoSuchElementException {
    if (list_ != null) {
      Object v = list_.element();
      list_ = (LLPair)(list_.next());
      decCount();
      return v;
    }
    checkIndex(0);
    return null; // not reached
  }


// UpdatableMap methods

/**
 * Implements collections.UpdatableMap.putAt.
 * Time complexity: O(n).
 * @see collections.UpdatableMap#putAt
**/
  public synchronized void putAt(Object key, Object element) {
    checkKey(key);
    checkElement(element);
    if (list_ != null) {
      LLPair p = list_.findKey(key);
      if (p != null) {
        if (!p.element().equals(element)) {
          p.element(element);
          incVersion();
        }
        return;
      }
    }
    list_ = new LLPair(key, element, list_);
    incCount();
  }


/**
 * Implements collections.UpdatableMap.removeAt.
 * Time complexity: O(n).
 * @see collections.UpdatableMap#removeAt
**/
  public synchronized void  removeAt(Object key) {
    if (key == null || list_ == null) return;
    LLPair p = list_;
    LLPair trail = p;
    while (p != null) {
      LLPair n = (LLPair)(p.next());
      if (p.key().equals(key)) {
        decCount();
        if (p == list_) list_ = n;
        else trail.unlinkNext();
        return;
      }
      else {
        trail = p;
        p = n;
      }
    }
  }

/**
 * Implements collections.UpdatableMap.replaceElement.
 * Time complexity: O(n).
 * @see collections.UpdatableMap#replaceElement
**/
  public synchronized void replaceElement(Object key, Object oldElement,
                                          Object newElement)
  throws IllegalElementException {
    if (key == null || oldElement == null || list_ == null) return;
    LLPair p = list_.find(key, oldElement);
    if (p != null) {
      checkElement(newElement);
      p.element(newElement);
      incVersion();
    }
  }

  private void remove_(Object element, boolean allOccurrences)  {
    if (element == null || count_ == 0) return;
    LLPair p = list_;
    LLPair trail = p;
    while (p != null) {
      LLPair n = (LLPair)(p.next());
      if (p.element().equals(element)) {
        decCount();
        if (p == list_) { list_ = n; trail = n; }
        else trail.next(n);
        if (!allOccurrences || count_ == 0) return;
        else p = n;
      }
      else {
        trail = p;
        p = n;
      }
    }
  }

/**
 * Helper for replace
**/

  private void replace_(Object oldElement, Object newElement,
                          boolean allOccurrences)
  throws IllegalElementException {
    if (list_ == null || oldElement == null || oldElement.equals(newElement))
      return;
    LLCell p = list_.find(oldElement);
    while (p != null) {
    checkElement(newElement);
      p.element(newElement);
      incVersion();
      if (!allOccurrences) return;
      p = p.find(oldElement);
    }
  }

// ImplementationCheckable methods

/**
 * Implements collections.ImplementationCheckable.checkImplementation.
 * @see collections.ImplementationCheckable#checkImplementation
**/
  public synchronized void checkImplementation()
  throws ImplementationError {

    super.checkImplementation();

    assert(((count_ == 0) == (list_ == null)));
    assert((list_ == null || list_.length() == count_));

    for (LLPair p = list_; p != null; p = (LLPair)(p.next())) {
      assert(canInclude(p.element()));
      assert(canIncludeKey(p.key()));
      assert(includesKey(p.key()));
      assert(includes(p.element()));
      assert(occurrencesOf(p.element()) >= 1);
      assert(includesAt(p.key(), p.element()));
    }

  }


}

