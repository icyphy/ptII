/*
  File: CircularList.java

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
 * Circular linked lists. Publically Implement only those
 * methods defined in interfaces.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/


public class CircularList extends UpdatableSeqImpl implements UpdatableSeq {

// instance variables

/**
 * The head of the list. Null if empty
**/
  protected CLCell list_;

// constructors

/**
 * Make an empty list with no element screener
**/

  public CircularList() { this(null, null, 0); }

/**
 * Make an empty list with supplied element screener
**/
  public CircularList(Predicate screener) { this(screener, null, 0); }

/**
 * Special version of constructor needed by clone()
**/

  protected CircularList(Predicate s, CLCell h, int c) {
    super(s); list_ = h; count_ = c;
  }

/**
 * Make an independent copy of the list. Elements themselves are not cloned
**/

  protected Object clone() throws CloneNotSupportedException {
    if (list_ == null) return new CircularList(screener_, null, 0);
    else return new CircularList(screener_, list_.copyList(), count_);
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
    return new CLEnumeration(this, list_);
  }


// Seq methods

/**
 * Implements collections.Seq.first.
 * Time complexity: O(1).
 * @see collections.Seq#first
**/
  public synchronized Object first()
  throws  NoSuchElementException {
    return firstCell().element();
  }

/**
 * Implements collections.Seq.last.
 * Time complexity: O(1).
 * @see collections.Seq#last
**/
  public synchronized Object last()
  throws  NoSuchElementException {
    return lastCell().element();
  }

/**
 * Implements collections.Seq.at.
 * Time complexity: O(n).
 * @see collections.Seq#at
**/
  public synchronized Object at(int index)
    throws  NoSuchElementException {
    return cellAt(index).element();
  }

/**
 * Implements collections.Seq.firstIndexOf.
 * Time complexity: O(n).
 * @see collections.Seq#firstIndexOf
**/
  public synchronized int firstIndexOf(Object element, int startingIndex) {
    if (startingIndex < 0) startingIndex = 0;
    CLCell p = list_;
    if (p == null || element == null) return -1;
    for (int i = 0; true; ++i) {
      if (i >= startingIndex && p.element().equals(element)) return i;
      p = p.next();
      if (p == list_) return -1;
    }
  }


/**
 * Implements collections.Seq.lastIndexOf.
 * Time complexity: O(n).
 * @see collections.Seq#lastIndexOf
**/
  public synchronized int lastIndexOf(Object element, int startingIndex) {
    if (element == null || count_ == 0) return -1;
    if (startingIndex >= size()) startingIndex = size() -1;
    if (startingIndex < 0) startingIndex = 0;
    CLCell p = cellAt(startingIndex);
    int i = startingIndex;
    for (;;) {
      if (p.element().equals(element)) return i;
      else if (p == list_) return -1;
      else {
        p = p.prev();
        --i;
      }
    }
  }

/**
 * Implements collections.Seq.firstIndexOf.
 * Time complexity: O(n).
 * @see collections.Seq#firstIndexOf
**/
  public synchronized int firstIndexOf(Object element) {
    return firstIndexOf(element, 0);
  }

/**
 * Implements collections.Seq.lastIndexOf.
 * Time complexity: O(n).
 * @see collections.Seq#lastIndexOf
**/
  public synchronized int lastIndexOf(Object element) {
    return lastIndexOf(element, size()-1);
  }


/**
 * Implements collections.Seq.subseq.
 * Time complexity: O(length).
 * @see collections.Seq#subseq
**/
  public synchronized /* CircularList */ Seq subseq(int from, int length)
  throws  NoSuchElementException {
    if (length > 0) {
      checkIndex(from);
      CLCell p = cellAt(from);
      CLCell newlist = new CLCell(p.element());
      CLCell current = newlist;
      for (int i = 1; i < length; ++i) {
        p = p.next();
        if (p == null) checkIndex(from+i); // force exception
        current.addNext(p.element());
        current = current.next();
      }
      return new CircularList(screener_, newlist, length);
    }
    else
      return new CircularList();
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
  public synchronized void removeOneOf(Object element) {
    remove_(element, false);
  }

/**
 * Implements collections.UpdatableCollection.replaceOneOf
 * Time complexity: O(n).
 * @see collections.UpdatableCollection#replaceOneOf
**/
  public synchronized void replaceOneOf(Object oldElement, Object newElement)  throws IllegalElementException {
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
 * Implements collections.UpdatableCollection.take.
 * Time complexity: O(1).
 * takes the last element on the list.
 * @see collections.UpdatableCollection#take
**/
  public synchronized Object take()
  throws  NoSuchElementException {
    Object v = last();
    removeLast();
    return v;
  }



// UpdatableSeq methods

/**
 * Implements collections.UpdatableSeq.insertFirst.
 * Time complexity: O(1).
 * @see collections.UpdatableSeq#insertFirst
**/
  public synchronized void insertFirst(Object element)
  throws IllegalElementException {
    checkElement(element);
    if (list_ == null) list_ = new CLCell(element);
    else list_ = list_.addPrev(element);
    incCount();
  }

/**
 * Implements collections.UpdatableSeq.replaceFirst.
 * Time complexity: O(1).
 * @see collections.UpdatableSeq#replaceFirst
**/
  public synchronized void replaceFirst(Object element)
  throws IllegalElementException {
    checkElement(element);
    firstCell().element(element);
    incVersion();
  }

/**
 * Implements collections.UpdatableSeq.removeFirst.
 * Time complexity: O(1).
 * @see collections.UpdatableSeq#removeFirst
**/
  public synchronized void removeFirst()
  throws NoSuchElementException {
    if (firstCell().isSingleton())
      list_ = null;
    else {
      CLCell n = list_.next();
      list_.unlink();
      list_ = n;
    }
    decCount();
  }

/**
 * Implements collections.UpdatableSeq.insertLast.
 * Time complexity: O(1).
 * @see collections.UpdatableSeq#insertLast
**/
  public synchronized void insertLast(Object element)
  throws IllegalElementException {
    if (list_ == null) insertFirst(element);
    else {
      checkElement(element);
      list_.prev().addNext(element);
      incCount();
    }
  }

/**
 * Implements collections.UpdatableSeq.replaceLast.
 * Time complexity: O(1).
 * @see collections.UpdatableSeq#replaceLast
**/
  public synchronized void replaceLast(Object element)
  throws IllegalElementException, NoSuchElementException {
    checkElement(element);
    lastCell().element(element);
    incVersion();
  }


/**
 * Implements collections.UpdatableSeq.removeLast.
 * Time complexity: O(1).
 * @see collections.UpdatableSeq#removeLast
**/
  public synchronized void removeLast()
  throws NoSuchElementException {
    CLCell l = lastCell();
    if (l == list_) list_ = null;
    else l.unlink();
    decCount();
  }

/**
 * Implements collections.UpdatableSeq.insertAt.
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#insertAt
**/
  public synchronized void insertAt(int index, Object element)
  throws IllegalElementException, NoSuchElementException {
    if (index == 0) insertFirst(element);
    else {
      checkElement(element);
      cellAt(index - 1).addNext(element);
      incCount();
    }
  }

/**
 * Implements collections.UpdatableSeq.replaceAt.
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#replaceAt
**/
  public synchronized void replaceAt(int index, Object element)
  throws IllegalElementException, NoSuchElementException {
    checkElement(element);
    cellAt(index).element(element);
    incVersion();
  }

/**
 * Implements collections.UpdatableSeq.removeAt.
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#removeAt
**/
  public synchronized void removeAt(int index)
  throws NoSuchElementException {
    if (index == 0) removeFirst();
    else {
      cellAt(index - 1).unlinkNext();
      decCount();
    }
  }

/**
 * Implements collections.UpdatableSeq.prependElements.
 * Time complexity: O(number of elements in e).
 * @see collections.UpdatableSeq#prependElements
**/
  public synchronized void prependElements(Enumeration e)
  throws IllegalElementException, CorruptedEnumerationException {
    CLCell hd = null;
    CLCell current = null;
    while (e.hasMoreElements()) {
      Object element = e.nextElement();
      checkElement(element);
      incCount();
      if (hd == null) {
        hd = new CLCell(element);
        current = hd;
      }
      else {
        current.addNext(element);
        current = current.next();
      }
    }
    if (list_ == null)
      list_ = hd;
    else if (hd != null) {
      CLCell tl = list_.prev();
      current.next(list_); list_.prev(current);
      tl.next(hd); hd.prev(tl);
      list_ = hd;
    }
  }

/**
 * Implements collections.UpdatableSeq.appendElements.
 * Time complexity: O(number of elements in e).
 * @see collections.UpdatableSeq#appendElements
**/
  public synchronized void appendElements(Enumeration e)
  throws IllegalElementException, CorruptedEnumerationException {
    if (list_ == null) prependElements(e);
    else {
      CLCell current = list_.prev();
      while (e.hasMoreElements()) {
        Object element = e.nextElement();
        checkElement(element);
        incCount();
        current.addNext(element);
        current = current.next();
      }
    }
  }

/**
 * Implements collections.UpdatableSeq.insertElementsAt.
 * Time complexity: O(size() + number of elements in e).
 * @see collections.UpdatableSeq#insertElementsAt
**/
  public synchronized void insertElementsAt(int index, Enumeration e)
  throws IllegalElementException, CorruptedEnumerationException,
  NoSuchElementException {
    if (list_ == null || index == 0) prependElements(e);
    else {
      CLCell current = cellAt(index - 1);
      while (e.hasMoreElements()) {
        Object element = e.nextElement();
        checkElement(element);
        incCount();
        current.addNext(element);
        current = current.next();
      }
    }
  }


/**
 * Implements collections.UpdatableSeq.removeFromTo.
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#removeFromTo
**/
  public synchronized void removeFromTo(int fromIndex, int toIndex)
  throws NoSuchElementException {
    checkIndex(toIndex);
    CLCell p = cellAt(fromIndex);
    CLCell last = list_.prev();
    for (int i = fromIndex; i <= toIndex; ++i) {
      decCount();
      CLCell n = p.next();
      p.unlink();
      if (p == list_) {
        if (p == last) {
          list_ = null;
          return;
        }
        else
          list_ = n;
      }
      p = n;
    }
  }


// helper methods

/**
 * return the first cell, or throw exception if empty
**/
  private CLCell firstCell() throws NoSuchElementException {
    if (list_ != null) return list_;
    checkIndex(0);
    return null; // not reached!

  }

/**
 * return the last cell, or throw exception if empty
**/
  private CLCell lastCell() throws NoSuchElementException {
    if (list_ != null) return list_.prev();
    checkIndex(0);
    return null; // not reached!
  }

/**
 * return the index'th cell, or throw exception if bad index
**/
  private CLCell cellAt(int index) throws NoSuchElementException {
    checkIndex(index);
    return list_.nth(index);
  }

/**
 * helper for remove/exclude
**/
  private void remove_(Object element, boolean allOccurrences)
  throws IllegalElementException {
    if (element == null || list_ == null) return;
    CLCell p = list_;
    for (;;) {
      CLCell n = p.next();
      if (p.element().equals(element)) {
        decCount();
        p.unlink();
        if (p == list_) {
          if (p == n) {
            list_ = null;
            break;
          }
          else list_ = n;
        }
        if (!allOccurrences) break;
        else p = n;
      }
      else if (n == list_)
        break;
      else
        p = n;
    }
  }


/**
 * helper for replace*
**/
  private void replace_(Object oldElement, Object newElement,
                          boolean allOccurrences)
  throws IllegalElementException {
    if (oldElement == null || list_ == null) return;
    CLCell p = list_;
    do {
      if (p.element().equals(oldElement)) {
        checkElement(newElement);
        incVersion();
        p.element(newElement);
        if (!allOccurrences) return;
      }
      p = p.next();
    } while (p != list_);
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

    if (list_ == null) return;

    int c = 0;
    CLCell p = list_;
    do {
      assert(p.prev().next() == p);
      assert(p.next().prev() == p);
      assert(canInclude(p.element()));
      assert(occurrencesOf(p.element()) > 0);
      assert(includes(p.element()));
      p = p.next();
      ++c;
    }
    while (p != list_);

    assert(c == count_);

  }
}

