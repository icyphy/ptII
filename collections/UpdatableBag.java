/*
  File: UpdatableBag.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  22Oct95  dl                 add addElements

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 *
 * UpdatableBags support operations to add multiple occurrences of elements
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public interface UpdatableBag extends UpdatableCollection, Bag  {


/**
 * Add an occurrence of the indicated element to the collection.
 * @param element the element to add
 * @return condition: 
 * <PRE>
 * occurrences(element) == PREV(this).occurrencesOf(element)+1 &&
 * Version change: always
 * </PRE>
 * @exception IllegalElementException if !canInclude(element)
**/

  public void add(Object element) throws IllegalElementException;

/**
 * Add an occurrence of the indicated element if it
 * is not already present in the collection.
 * No effect if the element is already present.
 * @param element the element to add
 * @return condition: 
 * <PRE>
 * occurrencesOf(element) == min(1, PREV(this).occurrencesOf(element) &&
 * no spurious effects &&
 * Version change iff !PREV(this).includes(element)
 * </PRE>
 * @exception IllegalElementException if !canInclude(element)
**/


  public void addIfAbsent(Object element) throws IllegalElementException;

/**
 * Add all elements of the enumeration to the collection.
 * Behaviorally equivalent to
 * <PRE>
 * while (e.hasMoreElements()) add(e.nextElement());
 * </PRE>
 * @param e the elements to include
 * @exception IllegalElementException if !canInclude(element)
 * @exception CorruptedEnumerationException propagated if thrown
**/


  public void addElements(Enumeration e) 
   throws IllegalElementException, CorruptedEnumerationException;


}

