/*
  File: Bag.java

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
 * Bags are collections supporting multiple occurrences of elements.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

public interface Bag extends Collection { 

/**
 * Construct a new Bag that is a clone of self except
 * that it includes indicated element. This can be used
 * to create a series of Bag, each differing from the
 * other only in that they contain additional elements.
 *
 * @param the element to add to the new Bag
 * @return the new Bag c, with the sameStructure as this except that
 * c.occurrencesOf(element) == occurrencesOf(element)+1 
 * @exception IllegalElementException if !canInclude(element)
**/

  public Bag   adding(Object element) 
                       throws IllegalElementException;

/**
 * Construct a new Collection that is a clone of self except
 * that it adds the indicated element if not already present. This can be used
 * to create a series of collections, each differing from the
 * other only in that they contain additional elements.
 *
 * @param element the element to include in the new collection
 * @return a new collection c, with the sameStructure as this, except that
 * c.occurrencesOf(element) = min(1, occurrencesOfElement)
 * @exception IllegalElementException if !canInclude(element)
**/

  public Bag  addingIfAbsent(Object element) 
                       throws IllegalElementException;

};


