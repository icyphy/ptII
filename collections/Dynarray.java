/*
  File: Dynarray.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  2Oct95  dl@cs.oswego.edu   refactored from DASeq.java
  13Oct95  dl                 Changed protection statuses

*/
  
package collections;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * Dynamically allocated and resized Arrays.
 * 
 * Beyond implementing its interfaces, adds methods
 * to adjust capacities. The default heuristics for resizing
 * usually work fine, but you can adjust them manually when
 * you need to.
 *
 * Dynarrays are generally like java.util.Vectors. But unlike them,
 * Dynarrays do not actually allocate arrays when they are constructed.
 * Among other consequences, you can adjust the capacity `for free'
 * after construction but before adding elements. You can adjust
 * it at other times as well, but this may lead to more expensive
 * resizing. Also, unlike Vectors, they release their internal arrays
 * whenever they are empty.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

public class Dynarray extends    UpdatableSeqImpl 
                      implements UpdatableSeq,
                                 SortableCollection {
/**
 * The minimum capacity of any non-empty buffer
**/

  public static final int minCapacity = 16;


// instance variables

/**
 * The elements, or null if no buffer yet allocated.
**/

  protected Object array_[];


// constructors

/**
 * Make a new empty Dynarray. 
**/

  public Dynarray() { this(null, null, 0); }

/**
 * Make an empty Dynarray with given element screener
**/

  public Dynarray(Predicate screener) { this(screener, null, 0); }

/**
 * Special version of constructor needed by clone()
**/
  protected Dynarray(Predicate s, Object b[], int c) { 
    super(s); array_ = b; count_ = c;  
  }

/**
 * Make an independent copy. The elements themselves are not cloned
**/

  protected Object clone() throws CloneNotSupportedException { 
    int cap = count_;
    if (cap == 0) 
      return new Dynarray(screener_, null, 0);
    else {
      if (cap < minCapacity) cap = minCapacity;
      Object newArray[] = new Object[cap];
      System.arraycopy(array_, 0, newArray, 0, count_);
      return new Dynarray(screener_, newArray, count_);
    }
  }

// methods introduced in Dynarray

/**
 * return the current internal buffer capacity (zero if no buffer allocated).
 * @return capacity (always greater than or equal to size())
**/

  public synchronized int capacity() { 
    return (array_ == null)? 0 : array_.length; 
  }

/**
 * Set the internal buffer capacity to max(size(), newCap).
 * That is, if given an argument less than the current
 * number of elements, the capacity is just set to the
 * current number of elements. Thus, elements are never lost
 * by setting the capacity. 
 * 
 * @param newCap the desired capacity.
 * @return condition: 
 * <PRE>
 * capacity() >= size() &&
 * version() != PREV(this).version() == (capacity() != PREV(this).capacity())
 * </PRE>
**/

  public synchronized void capacity(int newCap) {
    if (newCap < count_) newCap = count_;
    if (newCap == 0) {
      clear();
    }
    else if (array_ == null) {
      array_ = new Object[newCap];
      incVersion();
    }
    else if (newCap != array_.length) {
      Object newArray[] = new Object[newCap];
      System.arraycopy(array_, 0, newArray, 0, count_);
      array_ = newArray;
      incVersion();
    }
  }


// Collection methods

/**
 * Implements collections.Collection.includes.
 * Time complexity: O(n).
 * @see collections.Collection#includes
**/
  public synchronized boolean includes(Object element) {
    if (element == null) return false;
    for (int i = 0; i < count_; ++i) 
      if (array_[i].equals(element))
        return true;
    return false;
  }

/**
 * Implements collections.Collection.occurrencesOf.
 * Time complexity: O(n).
 * @see collections.Collection#occurrencesOf
**/
  public synchronized int occurrencesOf(Object element) {
    if (element == null) return 0;
    int c = 0;
    for (int i = 0; i < count_; ++i) 
      if (array_[i].equals(element)) ++c;
    return c;
  }

/**
 * Implements collections.Collection.elements.
 * Time complexity: O(1).
 * @see collections.Collection#elements
**/
  public synchronized CollectionEnumeration elements() { 
    return new DAEnumeration(this, array_); 
  }



// Seq methods:

/**
 * Implements collections.Seq.first.
 * Time complexity: O(1).
 * @see collections.Seq#first
**/
  public synchronized Object first()
  throws  NoSuchElementException {
    checkIndex(0);
    return array_[0];
  }

/**
 * Implements collections.Seq.last.
 * Time complexity: O(1).
 * @see collections.Seq#last
**/
  public synchronized Object last()
  throws  NoSuchElementException {
    checkIndex(count_-1);
    return array_[count_-1];
  }

/**
 * Implements collections.Seq.at.
 * Time complexity: O(1).
 * @see collections.Seq#at
**/
  public synchronized Object at(int index) 
  throws  NoSuchElementException {
    checkIndex(index);  
    return array_[index]; 
  }

/**
 * Implements collections.Seq.firstIndexOf.
 * Time complexity: O(n).
 * @see collections.Seq#firstIndexOf
**/
  public synchronized int firstIndexOf(Object element, int startingIndex) {
    if (startingIndex < 0) startingIndex = 0;
    for (int i = startingIndex; i < count_; ++i) 
      if (array_[i].equals(element))
        return i;
    return -1;
  }

/**
 * Implements collections.Seq.lastIndexOf.
 * Time complexity: O(n).
 * @see collections.Seq#lastIndexOf
**/
  public synchronized int lastIndexOf(Object element, int startingIndex) {
    if (startingIndex >= count_) startingIndex = count_ -1;
    for (int i = startingIndex; i >= 0; --i) 
      if (array_[i].equals(element))
        return i;
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
  public synchronized int lastIndexOf(Object element) {
    return lastIndexOf(element, size()-1);
  }


/**
 * Implements collections.Seq.subseq.
 * Time complexity: O(length).
 * @see collections.Seq#subseq
**/
  public synchronized /* Dynarray */ Seq subseq(int from, int length) 
  throws  NoSuchElementException {
    if (length > 0) {
      checkIndex(from);
      checkIndex(from+length-1);
      Object newArray[] = new Object[length];
      System.arraycopy(array_, from, newArray, 0, length);
      return new Dynarray(screener_, newArray, length);
    }
    else
      return new Dynarray(screener_);
  }


// UpdatableCollection methods

/**
 * Implements collections.UpdatableCollection.clear.
 * Time complexity: O(1).
 * @see collections.UpdatableCollection#clear
**/
  public synchronized void clear() { 
    array_ = null;
    setCount(0);
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
 * Time complexity: O(n * number of replacements).
 * @see collections.UpdatableCollection#replaceAllOf
**/
  public synchronized void replaceAllOf(Object oldElement, 
                                                 Object newElement)  
  throws IllegalElementException { 
    replace_(oldElement, newElement, true);
  }

/**
 * Implements collections.UpdatableCollection.exclude.
 * Time complexity: O(n * occurrencesOf(element)).
 * @see collections.UpdatableCollection#exclude
**/
  public synchronized void exclude(Object element)  { 
    remove_(element, true);
  }

/**
 * Implements collections.UpdatableCollection.take.
 * Time complexity: O(1).
 * Takes the rightmost element of the array.
 * @see collections.UpdatableCollection#take
**/
  public synchronized Object take() 
  throws  NoSuchElementException {
    Object v = last();
    removeLast();
    return v;
  }


// SortableCollection methods:


/**
 * Implements collections.SortableCollection.sort.
 * Time complexity: O(n log n).
 * Uses a quicksort-based algorithm.
 * @see collections.SortableCollection#sort
**/
  public void sort(Comparator cmp) {
    if (count_ > 0) {
      quickSort(array_, 0, count_ - 1, cmp);
      incVersion();
    }
  }


// UpdatableSeq methods     

/**
 * Implements collections.UpdatableSeq.insertFirst.
 * Time complexity: O(n)
 * @see collections.UpdatableSeq#insertFirst
**/
  public synchronized void insertFirst(Object element) 
  throws IllegalElementException {
    checkElement(element);
    growBy_(1);
    for (int i = count_-1; i > 0; --i) array_[i] = array_[i-1];
    array_[0] = element;
  }

/**
 * Implements collections.UpdatableSeq.replaceFirst.
 * Time complexity: O(1).
 * @see collections.UpdatableSeq#replaceFirst
**/
  public synchronized void replaceFirst(Object element) 
  throws IllegalElementException {
    checkElement(element);
    array_[0] = element;
    incVersion();
  }

/**
 * Implements collections.UpdatableSeq.removeFirst.
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#removeFirst
**/
  public synchronized void removeFirst()
  throws NoSuchElementException {
    removeAt(0);
  }

/**
 * Implements collections.UpdatableSeq.insertLast.
 * Time complexity: normally O(1), but O(n) if size() == capacity().
 * @see collections.UpdatableSeq#insertLast
**/
  public synchronized void insertLast(Object element) 
  throws IllegalElementException {
    checkElement(element);
    int last = count_;
    growBy_(1);
    array_[last] = element;
  }

/**
 * Implements collections.UpdatableSeq.replaceLast.
 * Time complexity: O(1).
 * @see collections.UpdatableSeq#replaceLast
**/
  public synchronized void replaceLast(Object element) 
  throws IllegalElementException, NoSuchElementException {
    checkElement(element);
    array_[count_-1] = element;
    incVersion();
  }

/**
 * Implements collections.UpdatableSeq.removeLast.
 * Time complexity: O(1).
 * @see collections.UpdatableSeq#removeLast
**/
  public synchronized void removeLast()
  throws NoSuchElementException {
    checkIndex(0);
    array_[count_-1] = null;
    growBy_(-1);
  }

/**
 * Implements collections.UpdatableSeq.insertAt.
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#insertAt
**/
  public synchronized void insertAt(int index, Object element) 
  throws IllegalElementException, NoSuchElementException {
    if (index != count_) checkIndex(index);
    checkElement(element);
    growBy_(1);
    for (int i = count_-1; i > index; --i) array_[i] = array_[i-1];
    array_[index] = element;
  }

/**
 * Implements collections.UpdatableSeq.removeAt.
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#removeAt
**/
  public synchronized void removeAt(int index) 
  throws NoSuchElementException {
    checkIndex(index);
    for (int i = index+1; i < count_; ++i) array_[i-1] = array_[i];
    array_[count_-1] = null;
    growBy_(-1);
  }
    

/**
 * Implements collections.UpdatableSeq.replaceAt.
 * Time complexity: O(1).
 * @see collections.UpdatableSeq#replaceAt
**/
  public synchronized void replaceAt(int index, Object element) 
  throws IllegalElementException, NoSuchElementException {
    checkIndex(index);
    checkElement(element);
    array_[index] = element;
    incVersion();
  }

/**
 * Implements collections.UpdatableSeq.prependElements.
 * Time complexity: O(n + number of elements in e) if (e 
 * instanceof CollectionEnumeration) else O(n * number of elements in e)
 * @see collections.UpdatableSeq#prependElements
**/
  public synchronized void prependElements(Enumeration e) 
  throws IllegalElementException, CorruptedEnumerationException {
    insertElementsAt_(0, e);
  }

/**
 * Implements collections.UpdatableSeq.appendElements.
 * Time complexity: O(number of elements in e) 
 * @see collections.UpdatableSeq#appendElements
**/
  public synchronized void appendElements(Enumeration e) 
  throws IllegalElementException, CorruptedEnumerationException {
    insertElementsAt_(count_, e);
  }

/**
 * Implements collections.UpdatableSeq.insertElementsAt.
 * Time complexity: O(n + number of elements in e) if (e 
 * instanceof CollectionEnumeration) else O(n * number of elements in e)
 * @see collections.UpdatableSeq#insertElementsAt
**/
  public synchronized void insertElementsAt(int index, Enumeration e) 
  throws IllegalElementException, CorruptedEnumerationException,
  NoSuchElementException {
    if (index != count_) checkIndex(index);
    insertElementsAt_(index, e);
  }


/**
 * Implements collections.UpdatableSeq.removeFromTo.
 * Time complexity: O(n).
 * @see collections.UpdatableSeq#removeFromTo
**/
  public synchronized void removeFromTo(int fromIndex, int toIndex) 
  throws NoSuchElementException {
    checkIndex(fromIndex);
    checkIndex(toIndex);
    if (fromIndex <= toIndex) {
      int gap = toIndex - fromIndex + 1;
      int j = fromIndex;
      for (int i = toIndex+1; i < count_; ++i) array_[j++] = array_[i];
      for (int i = 1; i <= gap; ++i) array_[count_-i] = null;
      addToCount(-gap);
    }
  }

/**
 * An implementation of Quicksort using medians of 3 for partitions.
 * Used internally by sort.
 * It is public and static so it can be used  to sort plain
 * arrays as well.
 * @param s, the array to sort
 * @param lo, the least index to sort from
 * @param hi, the greatest index
 * @param cmp, the comparator to use for comparing elements
**/

  public static void quickSort(Object s[], int lo, int hi, Comparator cmp) {

    if (lo >= hi) return;

    /* 
       Use median-of-three(lo, mid, hi) to pick a partition. 
       Also swap them into relative order while we are at it.
   */
  
    int mid = (lo + hi) / 2;
  
    if (cmp.compare(s[lo], s[mid]) > 0) {
      Object tmp = s[lo]; s[lo] = s[mid]; s[mid] = tmp; // swap
    }
    
    if (cmp.compare(s[mid], s[hi]) > 0) {
      Object tmp = s[mid]; s[mid] = s[hi]; s[hi] = tmp; // swap 
      
      if (cmp.compare(s[lo], s[mid]) > 0) {
        Object tmp2 = s[lo]; s[lo] = s[mid]; s[mid] = tmp2; // swap
      }
      
    }
    
    int left = lo+1;           // start one past lo since already handled lo
    int right = hi-1;          // similarly
    if (left >= right) return; // if three or fewer we are done
    
    Object partition = s[mid];
    
    for (;;) {
      
      while (cmp.compare(s[right], partition) > 0) --right;
      
      while (left < right && cmp.compare(s[left], partition) <= 0) ++left;
      
      if (left < right) {
        Object tmp = s[left]; s[left] = s[right]; s[right] = tmp; // swap
        --right;
      }
      else break;
    }
    
    quickSort(s, lo, left, cmp);
    quickSort(s, left+1, hi, cmp);
    
  }

// helper methods

/**
 * Main method to control buffer sizing.
 * The heuristic used for growth is:
 * <PRE>
 * if out of space:
 *   if need less than minCapacity, grow to minCapacity
 *   else grow by average of requested size and minCapacity.
 * </PRE>
 * <P>
 * For small buffers, this causes them to be about 1/2 full.
 * while for large buffers, it causes them to be about 2/3 full.
 * <P>
 * For shrinkage, the only thing we do is unlink the buffer if it is empty.
 * @param inc, the amount of space to grow by. Negative values mean shrink.
 * @return condition: adjust record of count, and if any of
 * the above conditions apply, allocate and copy into a new
 * buffer of the appropriate size.
**/

  private void growBy_(int inc) {
    int needed = count_ + inc;
    if (inc > 0) {
      /* heuristic: 
     */
      int current = capacity();
      if (needed > current) {
        incVersion();
        int newCap = needed + (needed + minCapacity) / 2;
        if (newCap < minCapacity) newCap = minCapacity;
        if (array_ == null) {
          array_ = new Object[newCap];
        }
        else {
          Object newArray[] = new Object[newCap];
          System.arraycopy(array_, 0, newArray, 0, count_);
          array_ = newArray;
        }
      }
    }
    else if (needed == 0) 
      array_ = null;
    setCount(needed);
  }


  

/**
 * Utility to splice in enumerations
**/

  private void insertElementsAt_(int index, Enumeration e) 
  throws CorruptedEnumerationException, IllegalElementException {
    if (e instanceof CollectionEnumeration) { // we know size!
      int inc = ((CollectionEnumeration)(e)).numberOfRemainingElements();
      int oldcount = count_;
      int oldversion = version_;
      growBy_(inc); 
      for (int i = oldcount-1; i >= index; --i) array_[i+inc] = array_[i];
      int j = index;
      while (e.hasMoreElements()) {
        Object element = e.nextElement();
        if (!canInclude(element)) { // Ugh. Can only do full rollback
          for (int i = index; i < oldcount; ++i) array_[i] = array_[i+inc];
          version_ = oldversion;
          count_ = oldcount;
          checkElement(element); // force throw
        }
        array_[j++] = element;
      }
    }
    else if (index == count_) { // next best; we can append 
      while (e.hasMoreElements()) {
        Object element = e.nextElement();
        checkElement(element);
        growBy_(1);
        array_[count_-1] = element;
      }
    }
    else { // do it the slow way
      int j = index;
      while (e.hasMoreElements()) {
        Object element = e.nextElement();
        checkElement(element);
        growBy_(1);
        for (int i = count_-1; i > j; --i) array_[i] = array_[i-1];
        array_[j++] = element;
      }
    }
  }

  private void remove_(Object element, boolean allOccurrences)  {
    if (element == null) return;
    for (int i = 0; i < count_; ++i) {
      while (i < count_ && array_[i].equals(element)) {
        for (int j = i+1; j < count_; ++j) array_[j-1] = array_[j];
        array_[count_-1] = null;
        growBy_(-1);
        if (!allOccurrences || count_ == 0) return;
      }
    }
  }

  private void replace_(Object oldElement, Object newElement, boolean allOccurrences)  
  throws IllegalElementException { 
    if (oldElement == null || count_ == 0) return;
    for (int i = 0; i < count_; ++i) {
      if (array_[i].equals(oldElement)) {
        checkElement(newElement);
        array_[i] = newElement;
        incVersion();
        if (!allOccurrences) return;
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
    assert(!(array_ == null && count_ != 0));
    assert((array_ == null || count_ <= array_.length));

    for (int i = 0; i < count_; ++i) {
      assert(canInclude(array_[i]));
      assert(occurrencesOf(array_[i]) > 0);
      assert(includes(array_[i]));
    }

  }


}

