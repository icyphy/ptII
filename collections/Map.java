/*
  File: Map.java

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
 * Maps maintain keyed elements. Any kind of Object 
 * may serve as a key for an element.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/


public interface Map extends Collection {

/**
 * Report whether the Map COULD include k as a key
 * Always returns false if k is null
**/

  public boolean     canIncludeKey(Object k);

/**
 * Report whether there exists any element with Key key.
 * @return true if there is such an element
**/

  public boolean     includesKey(Object key);

/**
 * Report whether there exists a (key, value) pair
 * @return true if there is such an element
**/

  public boolean     includesAt(Object key, Object value);


/**
 * Return an enumeration that may be used to traverse through
 * the keys (not elements) of the collection. The corresponding
 * elements can be looked at by using at(k) for each key k. For example:
 * <PRE>
 * Enumeration keys = amap.keys();
 * while (keys.hasMoreElements()) {
 *   Object key = keys.nextElement();
 *   Object value = amap.at(key)
 * // ...
 * }
 * </PRE>
 * @return the enumeration
**/

  public CollectionEnumeration keys();


/**
 * Return the element associated with Key key. 
 * @param key a key
 * @return element such that includesAt(key, element)
 * @exception NoSuchElementException if !includesKey(key)
**/

  public Object      at(Object key)
                       throws NoSuchElementException;

/**
 * Return a key associated with element. There may be any
 * number of keys associated with any element, but this returns only
 * one of them (any arbitrary one), or null if no such key exists.
 * @param element, a value to try to find a key for.
 * @return k, such that 
 * <PRE>
 * (k == null && !includes(element)) ||  includesAt(k, element)
 * </PRE>
**/

  public Object      aKeyOf(Object element);

/**
 * Construct a new Map that is a clone of self except
 * that it includes the new pair. If there already exists
 * another pair with the same key, the new collection will
 * instead have one with the new elment.
 * @param the key for element to add
 * @param the element to add
 * @return the new Map c, for which:
 * <PRE>
 * c.at(key).equals(element) &&
 * foreach (k in keys()) c.at(v).equals(at(k))
 * foreach (k in c.keys()) (!k.equals(key)) --> c.at(v).equals(at(k))
 * </PRE>
**/


  public Map  puttingAt(Object key, Object element) 
                          throws IllegalElementException;

/**
 * Construct a new Map that is a clone of self except
 * that it does not include the given key.
 * It is NOT an error to exclude a non-existent key.
 * @param key the key for the par to remove
 * @param element the element for the par to remove
 * @return the new Map c, for which:
 * <PRE>
 * foreach (v in c.keys()) includesAt(v, at(v)) &&
 * !c.includesKey(key) 
 * </PRE>
**/
  public Map  removingAt(Object key);


}


