/*
  File: HTPairEnumeration.java

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
 * Enumerator for paircollections based on hash tables
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/
final class HTPairEnumeration extends CEImpl {
  private LLPair tab_[];
  private LLPair cell_;
  private int row_;
  private boolean usekeys_;

  public HTPairEnumeration(UpdatableCollection c, LLPair tab[], boolean usekeys) {
    super(c);
    tab_ = tab;
    row_ = 0;
    cell_ = null;
    usekeys_ = usekeys;
  }

/**
 * Implements java.util.Enumeration.nextElement.
 * @see java.util.Enumeration#nextElement
**/
  public Object nextElement() {
    decRemaining();
    // if this loop fails, then we've not detected a version change?
    while (cell_ == null) cell_ = tab_[row_++];
    Object v = (usekeys_)? cell_.key() : cell_.element();
    cell_ = (LLPair)(cell_.next());
    return v;
  }

}

