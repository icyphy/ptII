/*
  File: HashedSet.java

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
 * Hash table implementation of set
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class HashedSet extends    UpdatableSetImpl
                       implements UpdatableSet, HashTableParams  {

// instance variables

/**
 * The table. Each entry is a list. Null if no table allocated
**/
  protected LLCell table_[];
/**
 * The threshold load factor
**/
  protected float loadFactor_;


// constructors

/**
 * Make an empty HashedSet.
**/

  public HashedSet() { this(null, defaultLoadFactor); }

/**
 * Make an empty HashedSet using given element screener
**/

  public HashedSet(Predicate screener) { this(screener, defaultLoadFactor); }

/**
 * Special version of constructor needed by clone()
**/

  protected HashedSet(Predicate s, float f) {
    super(s); table_ = null; loadFactor_ = f;
  }

/**
 * Make an independent copy of the table. Does not clone elements.
**/

  protected Object clone() throws CloneNotSupportedException {
    HashedSet c =  new HashedSet(screener_, loadFactor_);
    if (count_ != 0) {
      int cap = 2 * (int)(count_ / loadFactor_) + 1;
      if (cap < defaultInitialBuckets) cap = defaultInitialBuckets;
      c.buckets(cap);
      for (int i = 0; i < table_.length; ++i)
        for (LLCell p = table_[i]; p != null; p = p.next())
          c.include(p.element());
    }
    return c;
  }


// HashTableParams methods

/**
 * Implements collections.HashTableParams.buckets.
 * Time complexity: O(1).
 * @see collections.HashTableParams#buckets.
**/

  public synchronized int buckets() {
    return (table_ == null)? 0 : table_.length;
  }

/**
 * Implements collections.HashTableParams.buckets.
 * Time complexity: O(n).
 * @see collections.HashTableParams#buckets.
**/

  public synchronized void buckets(int newCap)
  throws IllegalArgumentException {
    if (newCap == buckets())
      return;
    else if (newCap >= 1)
      resize(newCap);
    else
      throw new IllegalArgumentException("Impossible Hash table size:" + newCap);

  }

/**
 * Implements collections.HashTableParams.thresholdLoadfactor
 * Time complexity: O(1).
 * @see collections.HashTableParams#thresholdLoadfactor
**/

  public synchronized float thresholdLoadFactor() {
    return loadFactor_;
  }

/**
 * Implements collections.HashTableParams.thresholdLoadfactor
 * Time complexity: O(n).
 * @see collections.HashTableParams#thresholdLoadfactor
**/

  public synchronized void thresholdLoadFactor(float desired)
  throws IllegalArgumentException {
    if (desired > 0.0) {
      loadFactor_ = desired;
      checkLoadFactor();
    }
    else
      throw new IllegalArgumentException("Impossible Hash table load factor:" + desired);
  }





// Collection methods

/**
 * Implements collections.Collection.includes.
 * Time complexity: O(1) average; O(n) worst.
 * @see collections.Collection#includes
**/
  public synchronized boolean includes(Object element) {
    if (element == null || count_ == 0) return false;
    LLCell p = table_[hashOf(element)];
    if (p != null) return p.find(element) != null;
    else return false;
  }

/**
 * Implements collections.Collection.occurrencesOf.
 * Time complexity: O(n).
 * @see collections.Collection#occurrencesOf
**/
  public synchronized int occurrencesOf(Object element) {
    if (includes(element)) return 1; else return 0;
  }

/**
 * Implements collections.Collection.elements.
 * Time complexity: O(1).
 * @see collections.Collection#elements
**/
  public synchronized CollectionEnumeration elements() {
    return new HTEnumeration(this, table_);
  }

// UpdatableCollection methods

/**
 * Implements collections.UpdatableCollection.clear.
 * Time complexity: O(1).
 * @see collections.UpdatableCollection#clear
**/
  public synchronized void clear() {
    setCount(0);
    table_ = null;
  }

/**
 * Implements collections.UpdatableCollection.exclude.
 * Time complexity: O(1) average; O(n) worst.
 * @see collections.UpdatableCollection#exclude
**/
  public synchronized void exclude(Object element) {
    removeOneOf(element);
  }

  public synchronized void removeOneOf(Object element) {
    if (element == null || count_ == 0) return;
    int h = hashOf(element);
    LLCell hd = table_[h];
    LLCell p = hd;
    LLCell trail = p;
    while (p != null) {
      LLCell n = p.next();
      if (p.element().equals(element)) {
        decCount();
        if (p == table_[h]) { table_[h] = n; trail = n; }
        else trail.next(n);
        return;
      }
      else {
        trail = p;
        p = n;
      }
    }
  }

  public synchronized void replaceOneOf(Object oldElement, Object newElement)
  throws IllegalElementException {

    if (count_ == 0 || oldElement == null || oldElement.equals(newElement))
      return;
    if (includes(oldElement)) {
      checkElement(newElement);
      exclude(oldElement);
      include(newElement);
    }
  }

  public synchronized void replaceAllOf(Object oldElement, Object newElement)
  throws IllegalElementException {
    replaceOneOf(oldElement, newElement);
  }

/**
 * Implements collections.UpdatableCollection.take.
 * Time complexity: O(number of buckets).
 * @see collections.UpdatableCollection#take
**/
  public synchronized Object take()
  throws NoSuchElementException {
    if (count_ != 0) {
      for (int i = 0; i < table_.length; ++i) {
        if (table_[i] != null) {
          decCount();
          Object v = table_[i].element();
          table_[i] = table_[i].next();
          return v;
        }
      }
    }
    checkIndex(0);
    return null; // not reached
  }


// UpdatableSet methods

/**
 * Implements collections.UpdatableSet.include.
 * Time complexity: O(1) average; O(n) worst.
 * @see collections.UpdatableSet#include
**/
  public synchronized void include(Object element) {
    checkElement(element);
    if (table_ == null) resize(defaultInitialBuckets);
    int h = hashOf(element);
    LLCell hd = table_[h];
    if (hd != null && hd.find(element) != null) return;
    LLCell n = new LLCell(element, hd);
    table_[h] = n;
    incCount();
    if (hd != null) checkLoadFactor(); // only check if bin was nonempty
  }



// Helper methods

/**
 * Check to see if we are past load factor threshold. If so, resize
 * so that we are at half of the desired threshold.
 * Also while at it, check to see if we are empty so can just
 * unlink table.
**/
  protected void checkLoadFactor() {
    if (table_ == null) {
      if (count_ != 0) resize(defaultInitialBuckets);
    }
    else {
      float fc = (float)(count_);
      float ft = table_.length;
      if (fc / ft > loadFactor_) {
        int newCap = 2 * (int)(fc / loadFactor_) + 1;
        resize(newCap);
      }
    }
  }

/**
 * Mask off and remainder the hashCode for element
 * so it can be used as table index
**/

  protected final int hashOf(Object element) {
    return (element.hashCode() & 0x7FFFFFFF) % table_.length;
  }


/**
 * resize table to new capacity, rehashing all elements
**/
  protected void resize(int newCap) {
    LLCell newtab[] = new LLCell[newCap];

    if (table_ != null) {
      for (int i = 0; i < table_.length; ++i) {
        LLCell p = table_[i];
        while (p != null) {
          LLCell n = p.next();
          int h =  (p.element().hashCode() & 0x7FFFFFFF) % newCap;
          p.next(newtab[h]);
          newtab[h] = p;
          p = n;
        }
      }
    }
    table_ = newtab;
    incVersion();
  }

// ImplementationCheckable methods

/**
 * Implements collections.ImplementationCheckable.checkImplementation.
 * @see collections.ImplementationCheckable#checkImplementation
**/
  public synchronized void checkImplementation()
  throws ImplementationError {

    super.checkImplementation();

    assert(!(table_ == null && count_ != 0));
    assert((table_ == null || table_.length > 0));
    assert(loadFactor_ > 0.0f);

    if (table_ != null) {
      int c = 0;
      for (int i = 0; i < table_.length; ++i) {
        for (LLCell p = table_[i]; p != null; p = p.next()) {
          ++c;
          assert(canInclude(p.element()));
          assert(includes(p.element()));
          assert(occurrencesOf(p.element()) == 1);
          assert(hashOf(p.element()) == i);
        }
      }
      assert(c == count_);
    }
  }


}

