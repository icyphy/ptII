/*
  File: MappingEnumeration.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  22Oct95  dl@cs.oswego.edu   Created.

*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * MappingEnumerations allow you to transform elements from
 * other enumerations before they are seen by their `consumers'
 * (i.e., the callers of `nextElement').
 * <P>
 * MappingEnumerations work as wrappers around other Enumerations.
 * To build one, you need an existing Enumeration (perhaps one
 * from coll.elements(), for some Collection coll), and a Function
 * object (i.e., implementing interface Function).
 * For example, if you want to process only the parent() fields
 * of java.awt.Component elements held by a collection coll
 * you could write something of the form:
 * <PRE>
 * Enumeration e = coll.elements();
 * Enumeration parents = MappingEnumeration(e, ParentFunction);
 * while (parents.hasMoreElements())
 *  doSomethingWith((Container)(parents.nextElement()));
 * </PRE>
 * To use this, you will also need to write a little class of the form:
 * <PRE>
 * class ParentFunction implements Function {
 *  Object function(Object v) {
 *    if (v instanceOf Component) return ((Component)v).getParent();
 *    else return null;
 *  }
 * }
 * </PRE>
 * <P>
 * (This requires too much set-up to be reasonable in most
 * situations, but is invaluable in others.)
 * @see collections.Function#function
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

public class MappingEnumeration implements Enumeration {

// instance variables

/**
 * The enumeration we are wrapping
**/

  private Enumeration src_;

/**
 * The transformation function
**/

  private Function fn_;

/**
 * Make an enumeration wrapping src, returning p.function for each nextElement
**/

  public MappingEnumeration(Enumeration src, Function p) {
    src_ = src;
    fn_ = p;
  }

/**
 * Implements java.util.Enumeration.hasMoreElements
**/

  public synchronized boolean hasMoreElements() {
    return src_.hasMoreElements();
  }

/**
 * Implements java.util.Enumeration.nextElement.
**/
  public synchronized Object nextElement() {
    if (!hasMoreElements())
      throw new NoSuchElementException("exhausted enumeration");
    else {
      Object v = src_.nextElement();
      Object result = fn_.function(v);
      return result;
    }
  }

}


