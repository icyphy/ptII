/*
  File: LLPairEnumeration.java

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
 * Enumerator for pair collections based on LLPairs
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/
final class LLPairEnumeration extends CEImpl {
  private LLPair cur_;
  private boolean useKeys_;

  public LLPairEnumeration(UpdatableCollection c, LLPair fst,boolean useKeys) {
    super(c);
    cur_ = fst;
    useKeys_ = useKeys;
  }

/**
 * Implements java.util.Enumeration.nextElement.
 * @see java.util.Enumeration#nextElement
**/
  public Object nextElement() {
    decRemaining();
    Object v = (useKeys_)? cur_.key(): cur_.element();
    cur_ = (LLPair)(cur_.next());
    return v;
  }

}

