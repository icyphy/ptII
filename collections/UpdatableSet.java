/*
  File: UpdatableSet.java

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
 * UpdatableSets support an include operations to add
 * an element only if it not present. 
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

public interface UpdatableSet extends UpdatableCollection, Set  {

/**
 * Include the indicated element in the collection.
 * No effect if the element is already present.
 * @param element the element to add
 * @return condition: 
 * <PRE>
 * includes(element) &&
 * no spurious effects &&
 * Version change iff !PREV(this).includes(element)
 * </PRE>
 * @exception IllegalElementException if !canInclude(element)
**/


  public void include(Object element) throws IllegalElementException;


/**
 * Include all elements of the enumeration in the collection.
 * Behaviorally equivalent to
 * <PRE>
 * while (e.hasMoreElements()) include(e.nextElement());
 * </PRE>
 * @param e the elements to include
 * @exception IllegalElementException if !canInclude(element)
 * @exception CorruptedEnumerationException propagated if thrown
**/


  public void includeElements(Enumeration e) 
   throws IllegalElementException, CorruptedEnumerationException;


}
