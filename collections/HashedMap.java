/*
  File: HashedMap.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Changed protection statuses
  21Oct95  dl                 fixed error in removeAt

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * Hash table implementation of Map
 * @author Doug Lea
 * @version 0.94
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

 
public class HashedMap extends    UpdatableMapImpl 
                       implements UpdatableMap, HashTableParams {

// instance variables 

/**
 * The table. Each entry is a list. Null if no table allocated
**/
  protected LLPair table_[];

/**
 * The threshold load factor
**/

  protected float loadFactor_;


// constructors

/** 
 * Make a new empty map.
**/

  public HashedMap() { this(null, defaultLoadFactor); }

/** 
 * Make a new empty map to use given element screener.
**/

  public HashedMap(Predicate screener) { this(screener, defaultLoadFactor); }

/**
 * Special version of constructor needed by clone()
**/

  protected HashedMap(Predicate s, float f) { 
    super(s); table_ = null; loadFactor_ = f;
  }

/**
 * Make an independent copy of the table. Elements themselves are not cloned.
**/

  protected Object clone() throws CloneNotSupportedException { 
    HashedMap c =  new HashedMap(screener_, loadFactor_);
    if (count_ != 0) {
      int cap = 2 * (int)((count_ / loadFactor_)) + 1;
      if (cap < defaultInitialBuckets) cap = defaultInitialBuckets;
      c.buckets(cap);
      for (int i = 0; i < table_.length; ++i) {
        for (LLPair p = table_[i]; p != null; p = (LLPair)(p.next())) 
          c.putAt(p.key(), p.element());
      }
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
    for (int i = 0; i < table_.length; ++i) {
      LLPair hd =  table_[i];
      if (hd != null && hd.find(element) != null) return true;
    }
    return false;
  }

/**
 * Implements collections.Collection.occurrencesOf.
 * Time complexity: O(n).
 * @see collections.Collection#occurrencesOf
**/
  public synchronized int occurrencesOf(Object element) {
    if (element == null || count_ == 0) return 0;
    int c = 0;
    for (int i = 0; i < table_.length; ++i) {
      LLPair hd = table_[i];
      if (hd != null) c += hd.count(element);
    }
    return c;
  }

/**
 * Implements collections.Collection.elements.
 * Time complexity: O(1).
 * @see collections.Collection#elements
**/
  public synchronized CollectionEnumeration elements() { 
    return new HTPairEnumeration(this, table_, false); 
  }


// Map methods


/**
 * Implements collections.Map.includesKey.
 * Time complexity: O(1) average; O(n) worst.
 * @see collections.Map#includesKey
**/
  public synchronized boolean  includesKey(Object key) {
    if (key == null || count_ == 0) return false;
    LLPair p = table_[hashOf(key)];
    if (p != null) return p.findKey(key) != null;
    else return false;
  }
  
/**
 * Implements collections.Map.includesAt
 * Time complexity: O(1) average; O(n) worst.
 * @see collections.Map#includesAt
**/
  public synchronized boolean includesAt(Object key, Object element) {
    if (key == null || element == null || count_ == 0) return false;
    LLPair p = table_[hashOf(key)];
    if (p != null) return p.find(key, element) != null;
    else return false;
  }

/**
 * Implements collections.Map.keys.
 * Time complexity: O(1).
 * @see collections.Map#keys
**/
  public synchronized CollectionEnumeration keys() { 
    return new HTPairEnumeration(this, table_, true); 
  }

/**
 * Implements collections.Map.at.
 * Time complexity: O(1) average; O(n) worst.
 * @see collections.Map#at
**/
  public synchronized Object at(Object key) 
  throws  NoSuchElementException {
    checkKey(key);
    if (count_ != 0) {
      LLPair p = table_[hashOf(key)];
      if (p != null) {
        LLPair c =  p.findKey(key);
        if (c != null) return c.element();
      }
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
    for (int i = 0; i < table_.length; ++i) {
      LLPair hd =  table_[i];
      if (hd != null) {
        LLPair p = ((LLPair)(hd.find(element)));
        if (p != null) return p.key();
      }
    }
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
    table_ = null;
  }

/**
 * Implements collections.UpdatableCollection.exclude.
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#exclude
**/
  public synchronized void exclude(Object element) {
    remove_(element, true);
  }


/**
 * Implements collections.UpdatableCollection.removeOneOf.
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#removeOneOf
**/
  public synchronized void removeOneOf(Object element) {
    remove_(element, false);
  }


/**
 * Implements collections.UpdatableCollection.replaceOneOf.
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
  public synchronized void replaceAllOf(Object oldElement, Object newElement) 
  throws IllegalElementException {
    replace_(oldElement, newElement, true);
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
          table_[i] = (LLPair)(table_[i].next());
          return v;
        }
      }
    }
    checkIndex(0);
    return null; // not reached
  }

// UpdatableMap methods


/**
 * Implements collections.UpdatableMap.putAt.
 * Time complexity: O(1) average; O(n) worst.
 * @see collections.UpdatableMap#putAt
**/
  public synchronized void  putAt(Object key, Object element) {
    checkKey(key);
    checkElement(element);
    if (table_ == null) resize(defaultInitialBuckets);
    int h = hashOf(key);
    LLPair hd = table_[h];
    if (hd == null) {
      table_[h] = new LLPair(key, element, hd);
      incCount();
      return;
    }
    else {
      LLPair p = hd.findKey(key);
      if (p != null) {
        if (!p.element().equals(element)) {
          p.element(element);
          incVersion();
        }
      }
      else {
        table_[h] = new LLPair(key, element, hd);
        incCount();
        checkLoadFactor(); // we only check load factor on add to nonempty bin
      }
    }
  }


/**
 * Implements collections.UpdatableMap.removeAt.
 * Time complexity: O(1) average; O(n) worst.
 * @see collections.UpdatableMap#removeAt
**/
  public synchronized void removeAt(Object key) {
    if (key == null || count_ == 0) return;
    int h = hashOf(key);
    LLPair hd = table_[h];
    LLPair p = hd;
    LLPair trail = p;
    while (p != null) {
      LLPair n = (LLPair)(p.next());
      if (p.key().equals(key)) {
        decCount();
        if (p == hd) table_[h] = n;
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
 * Time complexity: O(1) average; O(n) worst.
 * @see collections.UpdatableMap#replaceElement
**/
  public synchronized void replaceElement(Object key, Object oldElement, 
                                          Object newElement)  
    throws IllegalElementException { 
    if (key == null || oldElement == null || count_ == 0) return;
    LLPair p = table_[hashOf(key)];
    if (p != null) {
      LLPair c = p.find(key, oldElement);
      if (c != null) {
        checkElement(newElement);
        c.element(newElement);
        incVersion();
      }
    }
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


  protected void resize(int newCap) {
    LLPair newtab[] = new LLPair[newCap];
    
    if (table_ != null) {
      for (int i = 0; i < table_.length; ++i) {
        LLPair p = table_[i];
        while (p != null) {
          LLPair n = (LLPair)(p.next());
          int h =  (p.key().hashCode() & 0x7FFFFFFF) % newCap;
          p.next(newtab[h]);
          newtab[h] = p;
          p = n;
        }
      }
    }
    table_ = newtab;
    incVersion();
  }

// helpers

  private void remove_(Object element, boolean allOccurrences) {
    if (element == null || count_ == 0) return;
    for (int h = 0; h < table_.length; ++h) {
      LLCell hd = table_[h];
      LLCell p = hd;
      LLCell trail = p;
      while (p != null) {
        LLPair n = (LLPair)(p.next());
        if (p.element().equals(element)) {
          decCount();
          if (p == table_[h]) { table_[h] = n; trail = n; }
          else trail.next(n);
          if (!allOccurrences) return;
          else p = n;
        }
        else {
          trail = p;
          p = n;
        }
      }
    }
  }

  private void replace_(Object oldElement, Object newElement, boolean allOccurrences) 
    throws IllegalElementException {
    if (count_ == 0 || oldElement == null || oldElement.equals(newElement))
      return;

    for (int h = 0; h < table_.length; ++h) {
      LLCell hd = table_[h];
      LLCell p = hd;
      LLCell trail = p;
      while (p != null) {
        LLPair n = (LLPair)(p.next());
        if (p.element().equals(oldElement)) {
          checkElement(newElement);
          incVersion();
          p.element(newElement);
          if (!allOccurrences) return;
        }
        trail = p;
        p = n;
      }
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

    assert(!(table_ == null && count_ != 0));
    assert((table_ == null || table_.length > 0));
    assert(loadFactor_ > 0.0f);

    if (table_ == null) return;
    int c = 0;
    for (int i = 0; i < table_.length; ++i) {
      for (LLPair p = table_[i]; p != null; p = (LLPair)(p.next())) {
        ++c;
        assert(canInclude(p.element()));
        assert(canIncludeKey(p.key()));
        assert(includesKey(p.key()));
        assert(includes(p.element()));
        assert(occurrencesOf(p.element()) >= 1);
        assert(includesAt(p.key(), p.element()));
        assert(hashOf(p.key()) == i);
      }
    }
    assert(c == count_);
   

  }

   
}

