/*
  File: LinkedBuffer.java

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
 * Linked Buffer implementation of Bags. The Bag consists of
 * any number of buffers holding elements, arranged in a list.
 * Each buffer holds an array of elements. The size of each
 * buffer is the value of chunkSize that was current during the
 * operation that caused the Bag to grow. The chunkSize() may
 * be adjusted at any time. (It is not considered a version change.)
 *
 * <P>
 * All but the final buffer is always kept full.
 * When a buffer has no elements, it is released (so is
 * available for garbage collection).
 * <P>
 * LinkedBuffers are good choices for collections in which
 * you merely put a lot of things in, and then look at
 * them via enumerations, but don't often look for
 * particular elements.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class LinkedBuffer extends UpdatableBagImpl implements UpdatableBag {


/**
 * The default chunk size to use for buffers
**/

  public static final int defaultChunkSize = 32;

// instance variables

/**
 * The last node of the circular list of chunks. Null if empty.
**/

  protected CLCell tail_;

/**
 * The number of elements of the tail node actually used. (all others
 * are kept full).
**/
  protected int lastCount_;

/**
 * The chunk size to use for making next buffer
**/

  protected int chunkSize_;

// constructors

/**
 * Make an empty buffer.
**/
  public LinkedBuffer() { this(null, 0, null, 0, defaultChunkSize); }

/**
 * Make an empty buffer, using the supplied element screener.
**/

  public LinkedBuffer(Predicate s) { this(s, 0, null, 0, defaultChunkSize); }

/**
 * Special version of constructor needed by clone()
**/
  protected LinkedBuffer(Predicate s, int n, CLCell t, int lc, int cs) {
    super(s);
    count_ = n;
    tail_ = t;
    lastCount_ = lc;
    chunkSize_ = cs;
  }

/**
 * Make an independent copy. Does not clone elements.
**/
  protected Object clone() throws CloneNotSupportedException {
    if (count_ == 0) return new LinkedBuffer(screener_);
    else {
      CLCell h = tail_.copyList();
      CLCell p = h;
      do {
        Object obuff[] = (Object[])(p.element());
        Object nbuff[] = new Object[obuff.length];
        for (int i = 0; i < obuff.length; ++i) nbuff[i] = obuff[i];
        p.element(nbuff);
        p = p.next();
      } while (p != h);
      return new LinkedBuffer(screener_, count_, h, lastCount_, chunkSize_);
    }
  }


/**
 * Report the chunk size used when adding new buffers to the list
**/

  public synchronized int chunkSize() { return chunkSize_; }

/**
 * Set the chunk size to be used when adding new buffers to the
 * list during future add() operations.
 * Any value greater than 0 is OK. (A value of 1 makes this a
 * into very slow simulation of a linked list!)
**/

  public synchronized void chunkSize(int newChunkSize)
  throws IllegalArgumentException {
    if (newChunkSize > 0)
      chunkSize_ = newChunkSize;
    else
      throw new IllegalArgumentException("Attempt to set impossible chunk size value of " + newChunkSize);
  }

// Collection methods

/*
  This code is pretty repetitive, but I don't know a nice way to
  separate traversal logic from actions
*/

/**
 * Implements collections.Collection.includes.
 * Time complexity: O(n).
 * @see collections.Collection#includes
**/
  public synchronized boolean includes(Object element) {
    if (element == null || count_ == 0) return false;
    CLCell p = tail_.next();
    for (;;) {
      Object buff[] = (Object[])(p.element());
      boolean isLast = p == tail_;
      int n;
      if (isLast) n = lastCount_;
      else n = buff.length;
      for (int i = 0; i < n; ++i) {
        if (buff[i].equals(element))
          return true;
      }
      if (isLast) break;
      else p = p.next();
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
    CLCell p = tail_.next();
    for (;;) {
      Object buff[] = (Object[])(p.element());
      boolean isLast = p == tail_;
      int n;
      if (isLast) n = lastCount_;
      else n = buff.length;
      for (int i = 0; i < n; ++i) {
        if (buff[i].equals(element))
          ++c;
      }
      if (isLast) break;
      else p = p.next();
    }
    return c;
  }

/**
 * Implements collections.Collection.elements.
 * Time complexity: O(1).
 * @see collections.Collection#elements
**/
  public synchronized CollectionEnumeration elements() {
    return new LBEnumeration(this, ((tail_ == null) ? null : tail_.next()));
  }

// UpdatableCollection methods

/**
 * Implements collections.UpdatableCollection.clear.
 * Time complexity: O(1).
 * @see collections.UpdatableCollection#clear
**/
  public synchronized void clear() {
    setCount(0);
    tail_ = null;
    lastCount_ = 0;
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
 * Implements collections.UpdatableCollection.take.
 * Time complexity: O(1).
 * Takes the least element.
 * @see collections.UpdatableCollection#take
**/
  public synchronized Object take()
  throws NoSuchElementException {
    if (count_ != 0) {
      Object buff[] = (Object[])(tail_.element());
      Object v = buff[lastCount_-1];
      buff[lastCount_-1] = null;
      shrink_();
      return v;
    }
    checkIndex(0);
    return null; // not reached
  }



// UpdatableBag methods

/**
 * Implements collections.UpdatableBag.addIfAbsent.
 * Time complexity: O(n).
 * @see collections.UpdatableBag#addIfAbsent
**/
  public synchronized void addIfAbsent(Object element)
  throws IllegalElementException {
    if (!includes(element)) add(element);
  }


/**
 * Implements collections.UpdatableBag.add.
 * Time complexity: O(1).
 * @see collections.UpdatableBag#add
**/
  public synchronized void add(Object element)
  throws IllegalElementException {
    checkElement(element);

    incCount();
    if (tail_ == null) {
      tail_ = new CLCell(new Object[chunkSize_]);
      lastCount_ = 0;
    }

    Object buff[] = (Object[])(tail_.element());
    if (lastCount_ == buff.length) {
      buff = new Object[chunkSize_];
      tail_.addNext(buff);
      tail_ = tail_.next();
      lastCount_ = 0;
    }
    buff[lastCount_++] = element;
  }

/**
 * helper for remove/exclude
**/

  private void remove_(Object element, boolean allOccurrences) {
    if (element == null || count_ == 0) return;
    CLCell p = tail_;
    for (;;) {
      Object buff[] = (Object[])(p.element());
      int i = (p == tail_)? lastCount_ - 1 : buff.length - 1;
      while (i >= 0) {
        if (buff[i].equals(element)) {

          Object lastBuff[] = (Object[])(tail_.element());
          buff[i] = lastBuff[lastCount_-1];
          lastBuff[lastCount_-1] = null;
          shrink_();

          if (!allOccurrences || count_ == 0)
            return;

          if (p == tail_ && i >= lastCount_) i = lastCount_-1;
        }
        else
          --i;
      }
      if (p == tail_.next()) break;
      else p = p.prev();
    }
  }

  private void replace_(Object oldElement, Object newElement, boolean allOccurrences)
  throws IllegalElementException {
    if (oldElement == null || count_ == 0 || oldElement.equals(newElement))
      return;
    CLCell p = tail_.next();
    for (;;) {
      Object buff[] = (Object[])(p.element());
      boolean isLast = p == tail_;
      int n;
      if (isLast) n = lastCount_;
      else n = buff.length;
      for (int i = 0; i < n; ++i) {
        if (buff[i].equals(oldElement)) {
          checkElement(newElement);
          incVersion();
          buff[i] = newElement;
          if (!allOccurrences)
            return;
        }
      }
      if (isLast) break;
      else p = p.next();
    }
  }

  private void shrink_() {
    decCount();
    lastCount_--;
    if (lastCount_ == 0) {
      if (count_ == 0)
        clear();
      else {
        CLCell tmp = tail_;
        tail_ = tail_.prev();
        tmp.unlink();
        Object buff[] = (Object[])(tail_.element());
        lastCount_ = buff.length;
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
    assert(chunkSize_ >= 0);
    assert(lastCount_ >= 0);
    assert(((count_ == 0) == (tail_ == null)));

    if (tail_ == null) return;
    int c = 0;
    CLCell p = tail_.next();
    for (;;) {
      Object buff[] = (Object[])(p.element());
      boolean isLast = p == tail_;
      int n;
      if (isLast) n = lastCount_;
      else n = buff.length;
      c += n;
      for (int i = 0; i < n; ++i) {
        Object v = buff[i];
        assert(canInclude(v) && includes(v));
      }
      if (isLast) break;
      else p = p.next();
    }

    assert(c == count_);

  }

}

