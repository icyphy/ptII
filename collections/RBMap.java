/*
  File: RBMap.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Changed protection statuses

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 *
 * RedBlack Trees of (key, element) pairs
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

  
public class RBMap extends    UpdatableMapImpl 
                   implements UpdatableMap, 
                              KeySortedCollection {

// instance variables

/**
 * The root of the tree. Null iff empty.
**/

  protected RBPair tree_;

/**
 * The comparator to use for ordering
**/

  protected Comparator cmp_;

/**
 * Make an empty tree, using DefaultComparator for ordering
**/

  public RBMap() { this(null, null, null, 0); }


/**
 * Make an empty tree, using given screener for screening elements (not keys)
**/
  public RBMap(Predicate screener) { this(screener, null, null, 0); }

/**
 * Make an empty tree, using given Comparator for ordering
**/
  public RBMap(Comparator c) { this(null, c, null, 0); }

/**
 * Make an empty tree, using given screener and Comparator.
**/
  public RBMap(Predicate s, Comparator c) { this(s, c, null, 0); }

/**
 * Special version of constructor needed by clone()
**/

  protected RBMap(Predicate s, Comparator cmp, RBPair t, int n) { 
    super(s); 
    count_ = n;
    tree_ = t;
    if (cmp != null) cmp_ = cmp;
    else cmp_ = new DefaultComparator();
  }

/**
 * Create an independent copy. Does not clone elements.
**/

  protected Object clone() throws CloneNotSupportedException { 
    if (count_ == 0) return new RBMap(screener_, cmp_);
    else return new RBMap(screener_, cmp_, (RBPair)(tree_.copyTree()), count_);
  }


// Collection methods

/**
 * Implements collections.Collection.includes.
 * Time complexity: O(log n).
 * @see collections.Collection#includes
**/
  public synchronized boolean includes(Object element) {
    if (element == null || count_ == 0) return false;
    return tree_.find(element, cmp_) != null;
  }

/**
 * Implements collections.Collection.occurrencesOf.
 * Time complexity: O(log n).
 * @see collections.Collection#occurrencesOf
**/
  public synchronized int occurrencesOf(Object element) {
    if (element == null || count_ == 0) return 0;
    return tree_.count(element, cmp_);
  }

/**
 * Implements collections.Collection.elements.
 * Time complexity: O(1).
 * @see collections.Collection#elements
**/
  public synchronized CollectionEnumeration elements() { 
    return new RBPairEnumeration(this, tree_, false);
  }

// KeySortedCollection methods

/**
 * Implements collections.KeySortedCollection.keyComparator
 * Time complexity: O(1).
 * @see collections.KeySortedCollection#keyComparator
**/
  public synchronized Comparator keyComparator() { return cmp_; }

/**
 * Use a new comparator. Causes a reorganization
**/

  public synchronized void comparator(Comparator cmp) {
    if (cmp != cmp_) {
      if (cmp != null) cmp = cmp;
      else cmp_ = new DefaultComparator();
      if (count_ != 0) {       // must rebuild tree!
        incVersion();
        RBPair t = (RBPair) (tree_.leftmost());
        tree_ = null;
        count_ = 0;
        while (t != null) {
          add_(t.key(), t.element(), false);
          t = (RBPair)(t.successor());
        }
      }
    }
  }

// Map methods


/**
 * Implements collections.Map.includesKey.
 * Time complexity: O(log n).
 * @see collections.Map#includesKey
**/
  public synchronized boolean  includesKey(Object key) {
    if (key == null || count_ == 0) return false;
    return tree_.findKey(key, cmp_) != null;
  }

/**
 * Implements collections.Map.includes.
 * Time complexity: O(n).
 * @see collections.Map#includes
**/
  public synchronized boolean includesAt(Object key, Object element) {
    if (key == null || element == null || count_ == 0) return false;
    return tree_.find(key, element, cmp_) != null;
  }

/**
 * Implements collections.Map.keys.
 * Time complexity: O(1).
 * @see collections.Map#keys
**/
  public synchronized CollectionEnumeration keys() { 
    return new RBPairEnumeration(this, tree_, true); 
  }

/**
 * Implements collections.Map.at.
 * Time complexity: O(log n).
 * @see collections.Map#at
**/
  public synchronized Object at(Object key) 
  throws  NoSuchElementException {
    if (count_ != 0) {
      RBPair p = tree_.findKey(key, cmp_);
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
    RBPair p = ((RBPair)( tree_.find(element, cmp_)));
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
    setCount(0);
    tree_ = null;
  }


/**
 * Implements collections.UpdatableCollection.exclude.
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#exclude
**/
  public synchronized void exclude(Object element) {
    if (element == null || count_ == 0) return;
    RBPair p = (RBPair)(tree_.find(element, cmp_));
    while (p != null) {
      tree_ = (RBPair)(p.delete(tree_));
      decCount();
      if (count_ == 0) return;
      p = (RBPair)(tree_.find(element, cmp_));
    }
  }

/**
 * Implements collections.UpdatableCollection.removeOneOf.
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#removeOneOf
**/
  public synchronized void removeOneOf(Object element) {
    if (element == null || count_ == 0) return;
    RBPair p = (RBPair)(tree_.find(element, cmp_));
    if (p != null) {
      tree_ = (RBPair)(p.delete(tree_));
      decCount();
    }
  }
      

/**
 * Implements collections.UpdatableCollection.replaceOneOf.
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#replaceOneOf
**/
  public synchronized void replaceOneOf(Object oldElement, Object newElement) 
  throws IllegalElementException {
    if (count_ == 0 || oldElement == null || oldElement.equals(newElement))
      return;
    RBPair p = (RBPair)(tree_.find(oldElement, cmp_));
    if (p != null) {
      checkElement(newElement);
      incVersion();
      p.element(newElement);
    }
  }

/**
 * Implements collections.UpdatableCollection.replaceAllOf.
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#replaceAllOf
**/
  public synchronized void replaceAllOf(Object oldElement, Object newElement) 
  throws IllegalElementException {
    RBPair p = (RBPair)(tree_.find(oldElement, cmp_));
    while (p != null) {
      checkElement(newElement);
      incVersion();
      p.element(newElement);
      p = (RBPair)(tree_.find(oldElement, cmp_));
    }
  }

/**
 * Implements collections.UpdatableCollection.take.
 * Time complexity: O(log n).
 * Takes the element associated with the least key.
 * @see collections.UpdatableCollection#take
**/
  public synchronized Object take() 
  throws NoSuchElementException {
    if (count_ != 0) {
      RBPair p = (RBPair)(tree_.leftmost());
      Object v = p.element();
      tree_ = (RBPair)(p.delete(tree_));
      decCount();
      return v;
    }
    checkIndex(0);
    return null; // not reached
  }


// UpdatableMap methods

/**
 * Implements collections.UpdatableMap.putAt.
 * Time complexity: O(log n).
 * @see collections.UpdatableMap#putAt
**/
  public synchronized void putAt(Object key, Object element) {
    add_(key, element, true);
  }


/**
 * Implements collections.UpdatableMap.removeAt.
 * Time complexity: O(log n).
 * @see collections.UpdatableMap#removeAt
**/
  public synchronized void removeAt(Object key) {
    if (key == null || count_ == 0) return;
    RBCell p = tree_.findKey(key, cmp_);
    if (p != null) {
      tree_ = (RBPair)(p.delete(tree_));
      decCount();
    }
  }


/**
 * Implements collections.UpdatableMap.replaceElement.
 * Time complexity: O(log n).
 * @see collections.UpdatableMap#replaceElement
**/
  public synchronized void replaceElement(Object key, Object oldElement, 
                                          Object newElement)  
  throws IllegalElementException { 
    if (key == null || oldElement == null || count_ == 0) return;
    RBPair p = tree_.find(key, oldElement, cmp_);
    if (p != null) {
      checkElement(newElement);
      p.element(newElement);
      incVersion();
    }
  }


// helper methods


  private void add_(Object key, Object element, boolean checkOccurrence) 
  throws IllegalElementException {
    checkKey(key);
    checkElement(element);
    if (tree_ == null) {
      tree_ = new RBPair(key, element);
      incCount();
    }
    else {
      RBPair t = tree_;
      for (;;) {
        int diff = cmp_.compare(key, t.key());
        if (diff == 0  && checkOccurrence) {
          if (!t.element().equals(element)) {
            t.element(element);
            incVersion();
          }
          return;
        }
        else if (diff <= 0) {
          if (t.left() != null) 
            t = (RBPair)(t.left());
          else {
            tree_ = (RBPair)(t.insertLeft(new RBPair(key, element), tree_));
            incCount();
            return;
          }
        }
        else {
          if (t.right() != null) 
            t = (RBPair)(t.right());
          else {
            tree_ = (RBPair)(t.insertRight(new RBPair(key, element), tree_));
            incCount();
            return;
          }
        }
      }
    }
  }

// ImplementationCheckable methods

/**
 * Implements collections.ImplementationCheckable.checkImplementation.
 * @see collections.ImplementationCheckable#checkImplementation
**/
  public void checkImplementation() 
  throws ImplementationError {

    super.checkImplementation();
    assert(cmp_ != null);
    assert(((count_ == 0) == (tree_ == null)));
    assert((tree_ == null || tree_.size() == count_));

    if (tree_ != null) {
      tree_.checkImplementation();
      Object last = null;
      RBPair t = (RBPair)(tree_.leftmost());
      while (t != null) {
        Object v = t.key();
        assert((last == null || cmp_.compare(last, v) <= 0));
        last = v;
        t = (RBPair)(t.successor());
      }
    }
  }

}



