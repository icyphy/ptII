/*
  File: ImplementationError.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file

*/

package collections;

/**
 * ImplementationError is thrown by
 * ImplementationCheckable.checkImplementation upon failure
 * to verify internal representation constraints.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

public class ImplementationError extends Error {

/**
 * The object failing the ImplementationCheck
**/

  public Object failedObject;

  public ImplementationError() { super(); }

  public ImplementationError(String msg, Object v) {
    super(msg); failedObject = v;
  }

/**
 * Assertion checking utility.
 * Throws Implementation error if pred is false.
 * @param obj -- the object making the assertion
 * @param pred -- the assertion
**/
  public static void assert(Object obj, boolean pred)
  throws ImplementationError {
    if (!pred) throw new ImplementationError("Assertion failure", obj);
  }

}

