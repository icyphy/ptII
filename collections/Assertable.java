/*
  File: Assertable.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  13Oct95  dl@cs.oswego.edu  Created.

*/

package collections;

/**
 * Assertable is an interface for classes possessing
 * an assert method that raises an exception if a boolean
 * argument is false.
 * <P>
 * 
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

public interface Assertable {

/**
 * Raise an exception if predicate is false.
 * 
 * Suggested default implementation is:
 * <PRE>
 * {
 *  ImplementationError.assert(this, predicate);
 * }
 * </PRE>
 * This method should either return normally or throw:
 * @exception ImplementationError if predicate is false.
**/

  public void assert(boolean predicate) throws ImplementationError; 
}

