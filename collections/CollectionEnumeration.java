/*
  File: CollectionEnumeration.java

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
 * CollectionEnumeration extends the standard java.util.Enumeration
 * interface with two additional methods.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

public interface CollectionEnumeration extends Enumeration {

/**
 * Return true if the collection that constructed this enumeration
 * has been detectably modified since construction of this enumeration.
 * Ability and precision of detection of this condition can vary
 * across collection class implementations.
 * hasMoreElements() is false whenever corrupted is true.
 *
 * @return true if detectably corrupted.
**/

  public boolean corrupted();

/**
 * Return the number of elements in the enumeration that have
 * not yet been traversed. When corrupted() is true, this 
 * number may (or may not) be greater than zero even if hasMoreElements() 
 * is false. Exception recovery mechanics may be able to
 * use this as an indication that recovery of some sort is
 * warranted. However, it is not necessarily a foolproof indication.
 * <P>
 * You can also use it to pack enumerations into arrays. For example:
 * <PRE>
 * Object arr[] = new Object[e.numberOfRemainingElement()]
 * int i = 0;
 * while (e.hasMoreElements()) arr[i++] = e.nextElement();
 * </PRE>
 * <P>
 * For the converse case, 
 * @see ArrayEnumeration
 * @return the number of untraversed elements
**/
    
  public int numberOfRemainingElements();
}

