/*
  File: Seq.java

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
 *
 * Seqs are indexed, sequentially ordered collections.
 * Indices are always in the range 0 .. size() -1. All accesses by index
 * are checked, raising exceptions if the index falls out of range.
 * <P>
 * The elements() enumeration for all seqs is guaranteed to be
 * traversed (via nextElement) in sequential order.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public interface Seq extends Collection {

    /**
     * Return the element at the indicated index
     * @param index
     * @return the element at the index
     * @exception NoSuchElementException if index is not in range 0..size()-1
     **/

    public Object      at(int index)
            throws  NoSuchElementException;

    /**
     * Return the first element, if it exists.
     * Behaviorally equivalent to at(0)
     * @exception NoSuchElementException if isEmpty
     **/

    public Object      first()
            throws NoSuchElementException;

    /**
     * Return the last element, if it exists.
     * Behaviorally equivalent to at(size()-1)
     * @exception NoSuchElementException if isEmpty
     **/

    public Object      last()
            throws NoSuchElementException;

    /**
     * Report the index of leftmost occurrence of an element from a
     * given starting point, or -1 if there is no such index.
     * @param element the element to look for
     * @param startingIndex the index to start looking from. The startingIndex
     * need not be a valid index. If less than zero it is treated as 0.
     * If greater than or equal to size(), the result will always be -1.
     * @return index such that
     * <PRE>
     * let int si = max(0, startingIndex) in
     *  index == -1 &&
     *   foreach (int i in si .. size()-1) !at(index).equals(element)
     *  ||
     *  at(index).equals(element) &&
     *   foreach (int i in si .. index-1) !at(index).equals(element)
     * </PRE>
     **/

    public int         firstIndexOf(Object element, int startingIndex);

    /**
     * Find the leftmost occurrence of an element.
     * Behaviorally equivalent to firstIndexOf(element, 0)
     **/

    public int         firstIndexOf(Object element);

    /**
     * Report the index of righttmost occurrence of an element from a
     * given starting point, or -1 if there is no such index.
     * @param element the element to look for
     * @param startingIndex the index to start looking from. The startingIndex
     * need not be a valid index. If less than zero the result
     * will always be -1.
     * If greater than or equal to size(), it is treated as size()-1.
     * @return index such that
     * <PRE>
     * let int si = min(size()-1, startingIndex) in
     *  index == -1 &&
     *   foreach (int i in 0 .. si) !at(index).equals(element)
     *  ||
     *  at(index).equals(element) &&
     *   foreach (int i in index+1 .. si) !at(index).equals(element)
     * </PRE>
     *
     **/
    public int         lastIndexOf(Object element, int startingIndex);


    /**
     * Find the rightmost occurrence of an element.
     * Behaviorally equivalent to lastIndexOf(element, size()-1)
     **/
    public int         lastIndexOf(Object element);

    /**
     * Construct a new Seq that is a clone of self except
     * that it does not contain the elements before index or
     * after index+length. If length is less than or equal to zero,
     * return an empty Seq.
     * @param index of the element that will be the 0th index in new Seq
     * @param length the number of elements in the new Seq
     * @return new seq such that
     * <PRE>
     * s.size() == max(0, length) &&
     * foreach (int i in 0 .. s.size()-1) s.at(i).equals(at(i+index));
     * </PRE>
     * @exception NoSuchElementException if index is not in range 0..size()-1
     **/
    public Seq         subseq(int index, int length)
            throws NoSuchElementException;

    /**
     * Construct a new Seq that is a clone of self except
     * that it adds (inserts) the indicated element at the
     * indicated index.
     * @param index the index at which the new element will be placed
     * @param element The element to insert in the new collection
     * @return new seq s, such that
     * <PRE>
     *  s.at(index) == element &&
     *  foreach (int i in 1 .. s.size()-1) s.at(i).equals(at(i-1));
     * </PRE>
     * @exception NoSuchElementException if index is not in range 0..size()-1
     **/

    public Seq  insertingAt(int index, Object element)
            throws IllegalElementException,
            NoSuchElementException;

    /**
     * Construct a new Seq that is a clone of self except
     * that the indicated element is placed at the indicated index.
     * @param index the index at which to replace the element
     * @param element The new value of at(index)
     * @return new seq, s, such that
     * <PRE>
     *  s.at(index) == element &&
     *  foreach (int i in 0 .. s.size()-1)
     *     (i != index) --&gt; s.at(i).equals(at(i));
     * </PRE>
     * @exception NoSuchElementException if index is not in range 0..size()-1
     **/

    public Seq   replacingAt(int index, Object element)
            throws IllegalElementException,
            NoSuchElementException;

    /**
     * Construct a new Seq that is a clone of self except
     * that it does not contain the element at the indicated index; all
     * elements to its right are slid left by one.
     *
     * @param index the index at which to remove an element
     * @return new seq such that
     * <PRE>
     *  foreach (int i in 0.. index-1) s.at(i).equals(at(i)); &&
     *  foreach (int i in index .. s.size()-1) s.at(i).equals(at(i+1));
     * </PRE>
     * @exception NoSuchElementException if index is not in range 0..size()-1
     **/
    public Seq   removingAt(int index)
            throws NoSuchElementException;


}


