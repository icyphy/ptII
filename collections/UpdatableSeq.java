/*
  File: UpdatableSeq.java.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;
  
/**
 *
 * UpdatableSeqs are Seqs possessing standard modification methods
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/


public interface UpdatableSeq extends UpdatableCollection, Seq {

/**
 * Insert element at indicated index. The index can range from
 * 0..size() (i.e., one past the current last index). If the index is
 * equal to size(), the element is appended as the new last element.
 * @param index the index to add at
 * @param element the element to add
 * @return condition:
 * <PRE>
 * size() == PREV(this).size()+1 &&
 * at(index).equals(element) &&
 * foreach (int i in 0 .. index-1)      at(i).equals(PREV(this).at(i))
 * foreach (int i in index+1..size()-1) at(i).equals(PREV(this).at(i-1))
 * Version change: always
 * </PRE>
 * @exception NoSuchElementException if index is not in range 0..size()
 * @exception IllegalElementException if !canInclude(element)
**/

  public void insertAt(int index, Object element)
                       throws IllegalElementException,
                       NoSuchElementException;

/**
 * replace element at indicated index with new value
 * @param index the index at which to replace value
 * @param element the new value
 * @return condition:
 * <PRE>
 * size() == PREV(this).size() &&
 * at(index).equals(element) &&
 * no spurious effects
 * Version change <-- !element.equals(PREV(this).at(index)
 *                    (but MAY change even if equal).
 * </PRE>
 * @exception NoSuchElementException if index is not in range 0..size()-1
 * @exception IllegalElementException if !canInclude(element)
**/

  public void        replaceAt(int index, Object element)
                       throws IllegalElementException,
                              NoSuchElementException;


/**
 * Remove element at indicated index. All elements to the right
 * have their indices decremented by one.
 * @param index the index of the element to remove
 * @return condition:
 * <PRE>
 * size() = PREV(this).size()-1 &&
 * foreach (int i in 0..index-1)      at(i).equals(PREV(this).at(i)); &&
 * foreach (int i in index..size()-1) at(i).equals(PREV(this).at(i+1));
 * Version change: always
 * </PRE>
 * @exception NoSuchElementException if index is not in range 0..size()-1
**/
  public void        removeAt(int index)
                       throws NoSuchElementException;

/**
 * Insert element at front of the sequence.
 * Behaviorally equivalent to insertAt(0, element)
 * @param element the element to add
 * @exception IllegalElementException if !canInclude(element)
**/

  public void insertFirst(Object element) throws IllegalElementException;


/**
 * replace element at front of the sequence with new value.
 * Behaviorally equivalent to replaceAt(0, element);
**/
  public void        replaceFirst(Object element)
                       throws IllegalElementException,
                              NoSuchElementException;

/**
 * Remove the leftmost element. 
 * Behaviorally equivalent to removeAt(0);
**/

  public void        removeFirst()
                       throws NoSuchElementException;

/**
 * insert element at end of the sequence
 * Behaviorally equivalent to insertAt(size(), element)
 * @param element the element to add
 * @exception IllegalElementException if !canInclude(element)
**/

  public void insertLast(Object element) throws IllegalElementException;

/**
 * replace element at end of the sequence with new value
 * Behaviorally equivalent to replaceAt(size()-1, element);
**/

  public void        replaceLast(Object element)
                       throws IllegalElementException,
                              NoSuchElementException;


/**
 * Remove the rightmost element. 
 * Behaviorally equivalent to removeAt(size()-1);
 * @exception NoSuchElementException if isEmpty
**/
  public void        removeLast()
                       throws NoSuchElementException;

/**
 * Remove the elements from fromIndex to toIndex, inclusive.
 * No effect if fromIndex > toIndex.
 * Behaviorally equivalent to
 * <PRE>
 * for (int i = fromIndex; i &lt;= toIndex; ++i) removeAt(fromIndex);
 * </PRE>
 * @param index the index of the first element to remove
 * @param index the index of the last element to remove
 * @return condition:
 * <PRE>
 * let n = max(0, toIndex - fromIndex + 1 in
 *  size() == PREV(this).size() - 1 &&
 *  for (int i in 0 .. fromIndex - 1)     at(i).equals(PREV(this).at(i)) && 
 *  for (int i in fromIndex .. size()- 1) at(i).equals(PREV(this).at(i+n) 
 *  Version change iff n > 0 
 * </PRE>
 * @exception NoSuchElementException if fromIndex or toIndex is not in 
 * range 0..size()-1
**/

  public void        removeFromTo(int fromIndex, int toIndex)
                       throws NoSuchElementException;

/**
 * Insert all elements of enumeration e at a given index, preserving 
 * their order. The index can range from
 * 0..size() (i.e., one past the current last index). If the index is
 * equal to size(), the elements are appended.
 * 
 * @param index the index to start adding at
 * @param e the elements to add
 * @return condition:
 * <PRE>
 * foreach (int i in 0 .. index-1) at(i).equals(PREV(this)at(i)); &&
 * All existing elements at indices at or greater than index have their
 *  indices incremented by the number of elements 
 *  traversable via e.nextElement() &&
 * The new elements are at indices index + their order in
 *   the enumeration's nextElement traversal.
 * !(e.hasMoreElements()) &&
 * (version() != PREV(this).version()) == PREV(e).hasMoreElements() 
 * </PRE>
 * @exception IllegalElementException if !canInclude some element of e;
 * this may or may not nullify the effect of insertions of other elements.
 * @exception NoSuchElementException if index is not in range 0..size()
 * @exception CorruptedEnumerationException is propagated if raised; this
 * may or may not nullify the effects of insertions of other elements.
**/  
  public void        insertElementsAt(int index, Enumeration e)
                       throws IllegalElementException,
                              CorruptedEnumerationException,
                              NoSuchElementException;

/**
 * Prepend all elements of enumeration e, preserving their order.
 * Behaviorally equivalent to addElementsAt(0, e)
 * @param e the elements to add
**/  

  public void prependElements(Enumeration e) 
                throws IllegalElementException,
                       CorruptedEnumerationException;


/**
 * Append all elements of enumeration e, preserving their order.
 * Behaviorally equivalent to addElementsAt(size(), e)
 * @param e the elements to add
**/  
  public void        appendElements(Enumeration e) 
                        throws IllegalElementException,
                               CorruptedEnumerationException;

}


