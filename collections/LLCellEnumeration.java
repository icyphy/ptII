/*
  File: LLCellEnumeration.java

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
 * Enumerator for collections based on LLCells
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

final class LLCellEnumeration extends CEImpl {
  private LLCell cur_;

  public LLCellEnumeration(UpdatableCollection c, LLCell first) { 
    super(c);
    cur_ = first; 
  }

/**
 * Implements java.util.Enumeration.nextElement.
 * @see java.util.Enumeration#nextElement
**/
  public Object nextElement() { 
    decRemaining();
    Object v = cur_.element(); 
    cur_ = cur_.next();
    return v;
  }

}

