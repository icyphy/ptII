/*
  File: UpdatableMap.java

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
 * UpdatableMap supports standard update operations on maps.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/


public interface UpdatableMap extends UpdatableCollection, Map {

/**
 * Include the indicated pair in the Map
 * If a different pair
 * with the same key was previously held, it is replaced by the
 * new pair.
 *
 * @param key the key for element to include
 * @param element the element to include
 * @return condition: 
 * <PRE>
 * includes(key, element) &&
 * no spurious effects &&
 * Version change iff !PREV(this).includesAt(key, element))
 * </PRE>
**/

  public void putAt(Object key, Object element) 
                 throws IllegalElementException;


/**
 * Remove the pair with the given key
 * @param  key the key
 * @return condition: 
 * <PRE>
 * !includesKey(key)
 * foreach (k in keys()) at(k).equals(PREV(this).at(k)) &&
 * foreach (k in PREV(this).keys()) (!k.equals(key)) --> at(k).equals(PREV(this).at(k)) 
 * (version() != PREV(this).version()) == 
 * includesKey(key) !=  PREV(this).includesKey(key))
 * </PRE>
**/

  public void removeAt(Object key);


/**
 * Replace old pair with new pair with same key.
 * No effect if pair not held. (This includes the case of
 * having no effect if the key exists but is bound to a different value.)
 * @param key the key for the pair to remove
 * @param oldElement the existing element
 * @param newElement the value to replace it with
 * @return condition: 
 * <PRE>
 * !includesAt(key, oldElement) || includesAt(key, newElement);
 * no spurious effects &&
 * Version change iff PREV(this).includesAt(key, oldElement))
 * </PRE>
**/

  public void replaceElement(Object key, Object oldElement, Object newElement) 
                throws IllegalElementException;
};



