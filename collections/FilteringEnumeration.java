/*
  File: FilteringEnumeration.java

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
 * FilteringEnumerations allow you to filter out elements from
 * other enumerations before they are seen by their `consumers'
 * (i.e., the callers of `nextElement').
 * <P>
 * FilteringEnumerations work as wrappers around other Enumerations.
 * To build one, you need an existing Enumeration (perhaps one
 * from coll.elements(), for some Collection coll), and a Predicate
 * object (i.e., implementing interface Predicate). 
 * For example, if you want to screen out everything but Panel
 * objects from a collection coll that might hold things other than Panels,
 * write something of the form:
 * <PRE>
 * Enumeration e = coll.elements();
 * Enumeration panels = FilteringEnumeration(e, IsPanel);
 * while (panels.hasMoreElements()) 
 *  doSomethingWith((Panel)(panels.nextElement()));
 * </PRE>
 * To use this, you will also need to write a little class of the form:
 * <PRE>
 * class IsPanel implements Predicate {
 *  boolean predicate(Object v) { return (v instanceof Panel); }
 * }
 * </PRE>
 * @see collections.Predicate#predicate
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

public class FilteringEnumeration implements Enumeration {

// instance variables

/**
 * The enumeration we are wrapping
**/

  private Enumeration src_;

/**
 * The screening predicate
**/

  private Predicate pred_;

/**
 * The sense of the predicate. False means to invert
**/

  private boolean sign_;

/**
 * The next element to hand out
**/

  private Object  nextElement_;

/**
 * True if we have a next element 
**/

  private boolean haveNext_;

/**
 * Make a Filter using src for the elements, and p as the screener,
 * selecting only those elements of src for which p is true
**/

  public FilteringEnumeration(Enumeration src, Predicate p) { this(src, p, true); }

/**
 * Make a Filter using src for the elements, and p as the screener,
 * selecting only those elements of src for which p.predicate(v) == sense.
 * A value of true for sense selects only values for which p.predicate
 * is true. A value of false selects only those for which it is false.
**/
  public FilteringEnumeration(Enumeration src, Predicate p, boolean sense) {
    src_ = src;
    pred_ = p;
    sign_ = sense;
    haveNext_ = false;
    findNext();
  }

/**
 * Implements java.util.Enumeration.hasMoreElements
**/

  public synchronized boolean hasMoreElements() { return haveNext_; }

/**
 * Implements java.util.Enumeration.nextElement.
**/
  public synchronized Object nextElement() {
    if (!hasMoreElements())
      throw new NoSuchElementException("exhausted enumeration");
    else {
      Object result = nextElement_;
      findNext();
      return result;
    }
  }

/**
 * Traverse through src_ elements finding one passing predicate
**/
  private void findNext() {
    haveNext_ = false;
    nextElement_ = null;
    for (;;) {
      if (!src_.hasMoreElements()) 
        return;
      else {
        try {
          Object v = src_.nextElement();
          if (pred_.predicate(v) == sign_) {
            haveNext_ = true;
            nextElement_ = v;
            return;
          }
        }
        catch (NoSuchElementException ex) {
          return;
        }
      }
    }
  }


}
  

