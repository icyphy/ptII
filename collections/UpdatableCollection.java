/*
  File: UpdatableCollection.java

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
 * UpdatableCollection is the root interface of all mutable collections; i.e.,
 * collections that may have elements dynamically added, removed,
 * and/or replaced in accord with their collection semantics.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/


public interface UpdatableCollection  extends Collection {

/**
 * All updatable collections maintain a `version number'. The numbering
 * scheme is arbitrary, but is guaranteed to change upon every
 * modification that could possibly affect an elements() enumeration traversal.
 * (This is true at least within the precision of the `int' representation;
 * performing more than 2^32 operations will lead to reuse of version numbers).
 * Versioning
 * <EM>may</EM> be conservative with respect to `replacement' operations.
 * For the sake of versioning replacements may be considered as
 * removals followed by additions. Thus version numbers may change 
 * even if the old and new  elements are identical.
 * <P>
 * All element() enumerations for Updatable Collections track version
 * numbers, and raise inconsistency exceptions if the enumeration is
 * used (via nextElement()) on a version other than the one generated
 * by the elements() method.
 * <P>
 * You can use versions to check if update operations actually have any effect
 * on observable state.
 * For example, clear() will cause cause a version change only
 * if the collection was previously non-empty.
 * @return the version number
**/

  public int         version(); 

/**
 * Cause the collection to become empty. 
 * @return condition:
 * <PRE>
 * isEmpty() &&
 * Version change iff !PREV(this).isEmpty();
 * </PRE>
**/

  public void        clear();

/**
 * Exclude all occurrences of the indicated element from the collection. 
 * No effect if element not present.
 * @param element the element to exclude.
 * @return condition: 
 * <PRE>
 * !includes(element) &&
 * size() == PREV(this).size() - PREV(this).occurrencesOf(element) &&
 * no other element changes &&
 * Version change iff PREV(this).includes(element)
 * </PRE>
**/

  public void exclude(Object element);


/**
 * Remove an instance of the indicated element from the collection. 
 * No effect if !includes(element)
 * @param element the element to remove
 * @return condition: 
 * <PRE>
 * let occ = max(1, occurrencesOf(element)) in
 *  size() == PREV(this).size() - occ &&
 *  occurrencesOf(element) == PREV(this).occurrencesOf(element) - occ &&
 *  no other element changes &&
 *  version change iff occ == 1
 * </PRE>
**/

  public void removeOneOf(Object element);

/**
 * Replace an occurrence of oldElement with newElement.
 * No effect if does not hold oldElement or if oldElement.equals(newElement).
 * The operation has a consistent, but slightly special interpretation
 * when applied to Sets. For Sets, because elements occur at
 * most once, if newElement is already included, replacing oldElement with
 * with newElement has the same effect as just removing oldElement.
 * @return condition:
 * <PRE>
 * let int delta = oldElement.equals(newElement)? 0 : 
 *               max(1, PREV(this).occurrencesOf(oldElement) in
 *  occurrencesOf(oldElement) == PREV(this).occurrencesOf(oldElement) - delta &&
 *  occurrencesOf(newElement) ==  (this instanceof Set) ? 
 *         max(1, PREV(this).occurrencesOf(oldElement) + delta):
 *                PREV(this).occurrencesOf(oldElement) + delta) &&
 *  no other element changes &&
 *  Version change iff delta != 0
 * </PRE>
 * @exception IllegalElementException if includes(oldElement) and !canInclude(newElement)
**/

  public void replaceOneOf(Object oldElement, Object newElement) 
                   throws IllegalElementException;

/**
 * Replace all occurrences of oldElement with newElement.
 * No effect if does not hold oldElement or if oldElement.equals(newElement).
 * The operation has a consistent, but slightly special interpretation
 * when applied to Sets. For Sets, because elements occur at
 * most once, if newElement is already included, replacing oldElement with
 * with newElement has the same effect as just removing oldElement.
 * @return condition:
 * <PRE>
 * let int delta = oldElement.equals(newElement)? 0 : 
                   PREV(this).occurrencesOf(oldElement) in
 *  occurrencesOf(oldElement) == PREV(this).occurrencesOf(oldElement) - delta &&
 *  occurrencesOf(newElement) ==  (this instanceof Set) ? 
 *         max(1, PREV(this).occurrencesOf(oldElement) + delta):
 *                PREV(this).occurrencesOf(oldElement) + delta) &&
 *  no other element changes &&
 *  Version change iff delta != 0
 * </PRE>
 * @exception IllegalElementException if includes(oldElement) and !canInclude(newElement)
**/

  public void replaceAllOf(Object oldElement, Object newElement) 
                throws IllegalElementException;

/**
 * Remove and return an element.  Implementations
 * may strengthen the guarantee about the nature of this element.
 * but in general it is the most convenient or efficient element to remove.
 * <P>
 * Example usage. One way to transfer all elements from 
 * UpdatableCollection a to UpdatableBag b is:
 * <PRE>
 * while (!a.empty()) b.add(a.take());
 * </PRE>
 * @return an element v such that PREV(this).includes(v) 
 * and the postconditions of removeOneOf(v) hold.
 * @exception NoSuchElementException iff isEmpty.
**/

  public Object take() throws NoSuchElementException;


/**
 * Exclude all occurrences of each element of the Enumeration.
 * Behaviorally equivalent to
 * <PRE>
 * while (e.hasMoreElements()) exclude(e.nextElement());
 * @param e the enumeration of elements to exclude.
 * @exception CorruptedEnumerationException is propagated if thrown
**/

  public void excludeElements(Enumeration e) 
    throws CorruptedEnumerationException;


/**
 * Remove an occurrence of each element of the Enumeration.
 * Behaviorally equivalent to
 * <PRE>
 * while (e.hasMoreElements()) removeOneOf(e.nextElement());
 * @param e the enumeration of elements to remove.
 * @exception CorruptedEnumerationException is propagated if thrown
**/

  public void removeElements(Enumeration e) 
    throws CorruptedEnumerationException;

};

