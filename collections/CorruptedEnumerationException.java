/*
  File: CorruptedEnumerationException.java

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
 * CorruptedEnumerationException is thrown by CollectionEnumeration
 * nextElement if a versioning inconsistency is detected in the process
 * of returning the next element
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class CorruptedEnumerationException extends NoSuchElementException {

/**
 * The collection that this is an enumeration of
**/

 public Collection collection;

/**
 * The version expected of the collection
**/
 public int oldVersion;

/**
 * The current version of the collection
**/

 public int newVersion;

 public CorruptedEnumerationException() { super(); }

 public CorruptedEnumerationException(int oldv, int newv, Collection coll, String msg) { 
   super(msg); 
   oldVersion = oldv;
   newVersion = newv;
   collection = coll;
 }

}

