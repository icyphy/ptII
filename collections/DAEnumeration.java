/*
  File: DAEnumeration.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Changed protection statuses

*/

package collections;

/**
 *
 * Enumerator for collections based on dynamic arrays.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/
final class DAEnumeration extends CEImpl {
  private Object [] arr_;
  private int cur_;

  public DAEnumeration(UpdatableCollection c, Object arr[]) {
    super(c); arr_ = arr; cur_ = 0;
  }

/**
 * Implements java.util.Enumeration.nextElement().
 * @see java.util.Enumeration#nextElement()
**/
  public Object nextElement() {
    decRemaining();
    return  arr_[cur_++];
  }
}
