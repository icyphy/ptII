/*
  File: LBEnumeration.java

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
 * Enumerator for collections based on linked buffers
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/
final class LBEnumeration extends CEImpl {
  private CLCell list_;
  private int idx_;

  public LBEnumeration(UpdatableCollection c, CLCell l) {
    super(c);
    list_ = l;
    idx_ = 0;
  }

/**
 * Implements java.util.Enumeration.nextElement.
 * @see java.util.Enumeration#nextElement
**/
  public Object nextElement() {
    decRemaining();
    Object buff[] = (Object[])(list_.element());
    Object v = buff[idx_];
    ++idx_;
    if (idx_ >= buff.length) {
      list_ = list_.next();
      idx_ = 0;
    }
    return v;
  }

}

