/*
  File: ImplementationCheckable.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Changed to extend Assertable

*/

package collections;

/**
 * ImplementationCheckable is an interface for classes possessing
 * a method that checks the consistency of an objects internal state.
 * <P>
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/

public interface ImplementationCheckable extends Assertable {

/**
 * Check the consistency of internal state, and raise exception if
 * not OK.
 * These should be `best-effort' checks. You cannot always locally
 * determine full consistency, but can usually approximate it,
 * and validate the most important representation invariants.
 * The most common kinds of checks are cache checks. For example,
 * A linked list that also maintains a separate record of the
 * number of items on the list should verify that the recorded
 * count matches the number of elements in the list.
 * <P>
 * This method should either return normally or throw:
 * @exception ImplementationError if check fails
**/

  public void checkImplementation() throws ImplementationError;

}

