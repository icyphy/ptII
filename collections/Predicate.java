/*
  File: Predicate.java

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
 * Predicate is an interface for any class supporting a
 * predicate(Object obj) method that returns true or false
 * depending on whether obj obeys the maintained predicate.
 * <P>
 * When used as an element screener, check should
 * return false if the
 * element should not be allowed in the collection, in which case
 * any attempted add or replace operation will raise an exception. Use of
 * screened collections is a simple way to dynamically ensure
 * that all elements have some desired property.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/


public interface Predicate {

/**
 * Report whether obj obeys maintained predicate
 * @param obj any object (possibly null)
 * @return true is obeys predicate
**/

  public boolean     predicate(Object obj);
}


