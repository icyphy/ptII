/*
  File: LinkedList.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  2Oct95  dl@cs.oswego.edu   repack from LLSeq.java

*/
  
package collections;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * LinkedList implementation.
 * Publically implements only those methods defined in its interfaces.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class LinkedList extends    UpdatableSeqImpl 
                        implements UpdatableSeq,
                                   SortableCollection {
// instance variables

/**
 * The head of the list. Null iff count_ == 0
**/
 
  protected LLCell list_;

// constructors

/**
 * Create a new empty list
**/

  public LinkedList() { this(null, null, 0); }

/**
 * Create a list with a given element screener
**/

  public LinkedList(Predicate screener) { this(screener, null, 0); }

/**
 * Special version of constructor needed by clone()
**/

  protected LinkedList(Predicate s, LLCell l, int c) { 
    super(s); list_ = l; count_ = c; 
  }

/**
 * Build an independent copy of the list.
 * The elements themselves are not cloned
**/

  protected Object clone() throws CloneNotSupportedException { 
    if (list_ == null) return new LinkedList(screener_, null, 0); 
    else return new LinkedList(screener_, list_.copyList(), count_);  
  }

      
// Collection methods

/**
 * Implements collections.Collection.includes.
 * Time complexity: O(n).
 * @see collections.Collection#includes
**/
  public synchronized boolean includes(Object element) {
    if (element == null || count_ == 0) return false;
    return list_.find(element) != null;
  }

/**
 * Implements collections.Collection.occurrencesOf.
 * Time complexity: O(n).
 * @see collections.Collection#occurrencesOf
**/
  public synchronized int occurrencesOf(Object element) {
    if (element == null || count_ == 0) return 0;
    return list_.count(element);
  }

/**
 * Implements collections.Collection.elements.
 * Time complexity: O(1).
 * @see collections.Collection#elements
**/
  public synchronized CollectionEnumeration elements() { 
    return new LLCellEnumeration(this, list_); 
  }



// Seq Methods

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
 * Time complexity: O(n).
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
    if (element == null || list_ == null) return -1;
    if (startingIndex < 0) startingIndex = 0;
    LLCell p = list_.nth(startingIndex);
    if (p != null) {
      int i = p.index(element);
      if (i >= 0) return i + startingIndex;
    }
    return -1;
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
  public synchronized int lastIndexOf(Object element, int startingIndex) {
    if (element == null || list_ == null) return -1;
    int i = 0;
    if (startingIndex >= size()) startingIndex = size()-1;
    int index = -1;
    LLCell p = list_;
    while (i <= startingIndex && p != null) {
      if (p.element().equals(element)) 
        index = i;
      ++i;
      p = p.next();
    }
    return index;
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
  public synchronized /* LinkedList */ Seq subseq(int from, int length) 
  throws  NoSuchElementException {
    if (length > 0) {
      LLCell p = cellAt(from);
      LLCell newlist = new LLCell(p.element(), null);
      LLCell current = newlist;
      for (int i = 1; i < length; ++i) {
        p = p.next();
        if (p == null) checkIndex(from+i); // force exception
        current.linkNext(new LLCell(p.element(), null));
        current = current.next();
      }
      return new LinkedList(screener_, newlist, length);
    }
    else
      return new LinkedList(screener_, null, 0);
  }


// UpdatableCollection methods

/**
 * Implements collections.UpdatableCollection.clear.
 * Time complexity: O(1).
 * @see collections.UpdatableCollection#clear
**/
  public synchronized void clear() { 
    if (list_ != null) { list_ = null; setCount(0);  }
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
 * takes the first element on the list
 * @see collections.UpdatableCollection#take
**/
  public synchronized Object take() 
  throws NoSuchElementException {
    Object v = first();
    removeFirst();
    return v;
  }

// SortableCollection methods

/**
 * Implements collections.SortableCollection.sort.
 * Time complexity: O(n log n).
 * Uses a merge-sort-based algorithm.
 * @see collections.SortableCollection#sort
**/
  public synchronized void sort(Comparator cmp) {
    if (list_ != null) {
      list_ = LLCell.mergeSort(list_, cmp);
      incVersion();
    }
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
    list_ = new LLCell(element, list_);
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
    list_ = firstCell().next();
    decCount();
  }

/**
 * Implements collections.UpdatableSeq.insertLast.
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#insertLast
**/
  public synchronized void insertLast(Object element) 
  throws IllegalElementException {
    checkElement(element);
    if (list_ == null) insertFirst(element);
    else { 
      list_.last().next(new LLCell(element)); 
      incCount(); 
    }
  }

/**
 * Implements collections.UpdatableSeq.replaceLast.
 * Time complexity: O(n).
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
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#removeLast
**/
  public synchronized void removeLast()
  throws NoSuchElementException {
    if (firstCell().next() == null ) removeFirst();
    else {
      LLCell trail = list_;
      LLCell p = trail.next();
      while (p.next() != null) { trail = p; p = p.next(); }
      trail.next(null);
      decCount();
    }
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
      cellAt(index - 1).linkNext(new LLCell(element)); 
      incCount(); 
    }
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
 * Implements collections.UpdatableSeq.replaceAt.
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#replaceAt
**/
  public synchronized void replaceAt(int index, Object element) 
  throws IllegalElementException, NoSuchElementException {
    cellAt(index).element(element);
    incVersion();
  }

/**
 * Implements collections.UpdatableSeq.prependElements.
 * Time complexity: O(number of elements in e).
 * @see collections.UpdatableSeq#prependElements
**/
  public synchronized void prependElements(Enumeration e) 
  throws IllegalElementException, CorruptedEnumerationException {
    splice_(e, null, list_);
  }

/**
 * Implements collections.UpdatableSeq.appendElements.
 * Time complexity: O(n + number of elements in e).
 * @see collections.UpdatableSeq#appendElements
**/
  public synchronized void appendElements(Enumeration e) 
  throws IllegalElementException, CorruptedEnumerationException {
    if (list_ == null) splice_(e, null, null);
    else splice_(e, list_.last(), null);
  }

/**
 * Implements collections.UpdatableSeq.insertElementsAt.
 * Time complexity: O(n + number of elements in e).
 * @see collections.UpdatableSeq#insertElementsAt
**/
  public synchronized void insertElementsAt(int index, Enumeration e) 
  throws IllegalElementException, CorruptedEnumerationException,
  NoSuchElementException {
    if (index == 0) splice_(e, null, list_);
    else {
      LLCell p = cellAt(index - 1);
      splice_(e, p, p.next());
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
    if (fromIndex <= toIndex) {
      if (fromIndex == 0) {
        LLCell p = firstCell();
        for (int i = fromIndex; i <= toIndex; ++i) p = p.next();
        list_ = p;  
      }
      else {
        LLCell f = cellAt(fromIndex-1);
        LLCell p = f;
        for (int i = fromIndex; i <= toIndex; ++i) p = p.next();
        f.next(p.next());
      }
      addToCount(-(toIndex-fromIndex+1));
    }
  }
    


// helper methods

  private final LLCell firstCell() throws NoSuchElementException { 
    if (list_ != null) return list_; 
    checkIndex(0);
    return null; // not reached!
  }

  private final LLCell lastCell()  throws NoSuchElementException {
    if (list_ != null) return list_.last();
    checkIndex(0);
    return null; // not reached!
  }

  private final LLCell cellAt(int index) throws NoSuchElementException {
    checkIndex(index);
    return list_.nth(index);
  }

/**
 * Helper method for removeOneOf()
**/

  private void remove_(Object element, boolean allOccurrences)  { 
    if (element == null || count_ == 0) return;
    LLCell p = list_;
    LLCell trail = p;
    while (p != null) {
      LLCell n = p.next();
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
    if (count_ == 0 || oldElement == null || oldElement.equals(newElement))
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

/**
 * Splice elements of e between hd and tl. if hd is null return new hd
**/

  private void splice_(Enumeration e, LLCell hd, LLCell tl) 
    throws IllegalElementException, CorruptedEnumerationException {
    if (e.hasMoreElements()) {
      LLCell newlist = null;
      LLCell current = null;
      while (e.hasMoreElements()) {
        Object v = e.nextElement();
        checkElement(v);
        incCount();
        LLCell p = new LLCell(v, null);
        if (newlist == null) 
          newlist = p;
        else 
          current.next(p);
        current = p;
      }
      if (current != null) current.next(tl);
      if (hd == null) list_ = newlist;
      else hd.next(newlist);
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

    int c = 0;
    for (LLCell p = list_; p != null; p = p.next()) {
      assert(canInclude(p.element()));
      assert(occurrencesOf(p.element()) > 0);
      assert(includes(p.element()));
      ++c;
    }
    assert(c == count_);

  }

}



