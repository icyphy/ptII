/*
  File: ElementSortedCollection.java

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
 *
 * ElementSorted is a mixin interface for Collections that
 * are always in sorted order with respect to a Comparator
 * held by the Collection.
 * <P>
 * ElementSorted Collections guarantee that enumerations
 * appear in sorted order;  that is if a and b are two Elements
 * obtained in succession from elements().nextElement(), that 
 * <PRE>
 * elementComparator().compare(a, b) <= 0.
 * </PRE>
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public interface ElementSortedCollection extends Collection {

/**
 * Report the Comparator used for ordering
**/

  public Comparator  elementComparator();
};

