/*
  File: InterleavingEnumeration.java

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
 * InterleavingEnumerations allow you to combine the elements
 * of two different enumerations as if they were one enumeration
 * before they are seen by their `consumers'.
 * This sometimes allows you to avoid having to use a 
 * Collection object to temporarily combine two sets of Collection elements()
 * that need to be collected together for common processing.
 * <P>
 * The elements are revealed (via nextElement()) in a purely
 * interleaved fashion, alternating between the first and second
 * enumerations unless one of them has been exhausted, in which case
 * all remaining elements of the other are revealed until it too is
 * exhausted. 
 * <P>
 * InterleavingEnumerations work as wrappers around other Enumerations.
 * To build one, you need two existing Enumerations.
 * For example, if you want to process together the elements of
 * two Collections a and b, you could write something of the form:
 * <PRE>
 * Enumeration items = InterleavingEnumeration(a.elements(), b.elements());
 * while (items.hasMoreElements()) 
 *  doSomethingWith(items.nextElement());
 * </PRE>
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/


public class InterleavingEnumeration implements Enumeration {

/**
 * The first source; nulled out once it is exhausted
**/

  private Enumeration fst_;

/**
 * The second source; nulled out once it is exhausted
**/

  private Enumeration snd_;

/**
 * The source currently being used
**/

  private Enumeration current_;



/**
 * Make an enumeration interleaving elements from fst and snd
**/

  public InterleavingEnumeration(Enumeration fst, Enumeration snd) {
    fst_ = fst;
    snd_ = snd;
    current_ = snd_; // flip will reset to fst (if it can)
    flip();
  }

/**
 * Implements java.util.Enumeration.hasMoreElements
**/
  public synchronized boolean hasMoreElements() { 
    return current_ != null;
  }

/**
 * Implements java.util.Enumeration.nextElement.
**/
  public synchronized Object nextElement() {
    if (!hasMoreElements())
      throw new NoSuchElementException("exhausted enumeration");
    else {
      // following line may also throw ex, but there's nothing 
      // reasonable to do except propagate
      Object result = current_.nextElement(); 
      flip();
      return result;
    }
  }

/**
 * Alternate sources
**/

  private void flip() {
    if (current_ == fst_) {
      if (snd_ != null && !snd_.hasMoreElements()) snd_ = null;
      if (snd_ != null)
        current_ = snd_;
      else {
        if (fst_ != null && !fst_.hasMoreElements()) fst_ = null;
        current_ = fst_;
      }
    }
    else {
      if (fst_ != null && !fst_.hasMoreElements()) fst_ = null;
      if (fst_ != null) 
        current_ = fst_;
      else {
        if (snd_ != null && !snd_.hasMoreElements()) snd_ = null;
        current_ = snd_;
      }
    }
  }


}
  
