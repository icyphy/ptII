/*
  File: IncrSeq.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Changed to use accessOnly

*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * Implementation of Immutable Seq
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public final class IncrSeq extends IncrImpl implements Seq {

  private int indexArg_;
/**
 * Make a new pure Seq using the default underlying Seq implementation
**/
  public IncrSeq() { this(DefaultImplementations.seq()); }

/**
 * Make a pure seq managing the given updatable seq s.
 * Warning: Do not modify s during the the lifetime of the constructed pure seq!
**/
  public IncrSeq(UpdatableSeq s) { super(s); indexArg_ = 0; }

/**
 * Make a copy. Uses lazy update.
**/
  protected Object clone() throws CloneNotSupportedException {
    undelta();
    IncrSeq s = new IncrSeq((UpdatableSeq)(updatable_));
    nextVersion_ = s;
    updatable_ = null;
    op_ = NO_EDIT;
    return s;
  }

/**
 * Implements collections.Seq.at.
 * @see collections.Seq#at
**/
  public synchronized Object at(int index)
  throws  NoSuchElementException {
    return ((UpdatableSeq)(accessOnly())).at(index);
  }

/**
 * Implements collections.Seq.first.
 * @see collections.Seq#first
**/
  public synchronized Object first()
  throws  NoSuchElementException {
    return ((UpdatableSeq)(accessOnly())).first();
  }

/**
 * Implements collections.Seq.last.
 * @see collections.Seq#last
**/
  public synchronized Object last()
  throws  NoSuchElementException {
    return ((UpdatableSeq)(accessOnly())).last();
  }

/**
 * Implements collections.Seq.firstIndexOf.
 * @see collections.Seq#firstIndexOf
**/
  public synchronized int firstIndexOf(Object element, int startingIndex) {
    return ((UpdatableSeq)(accessOnly())).firstIndexOf(element, startingIndex);
  }

/**
 * Implements collections.Seq.firstIndexOf.
 * @see collections.Seq#firstIndexOf
**/
  public synchronized int firstIndexOf(Object element) {
    return ((UpdatableSeq)(accessOnly())).firstIndexOf(element);
  }

/**
 * Implements collections.Seq.lastIndexOf.
 * @see collections.Seq#lastIndexOf
**/
  public synchronized int lastIndexOf(Object element, int startingIndex) {
    return ((UpdatableSeq)(accessOnly())).lastIndexOf(element, startingIndex);
  }

/**
 * Implements collections.Seq.lastIndexOf.
 * @see collections.Seq#lastIndexOf
**/
  public synchronized int lastIndexOf(Object element) {
    return ((UpdatableSeq)(accessOnly())).lastIndexOf(element);
  }

/**
 * Implements collections.Seq.subseq.
 * Makes a full new Seq, not a lazy update.
 * @see collections.Seq#subseq
**/
  public synchronized /* IncrSeq */ Seq  subseq(int index, int length)
  throws  NoSuchElementException {
    return new IncrSeq(((UpdatableSeq)(((UpdatableSeq)accessOnly()).subseq(index, length))));
  }


/**
 * Implements collections.Collection.removingOneOf
 * @see collections.Collection#removingOneOf
**/
  public synchronized /* IncrSeq */ Collection  removingOneOf(Object element) {
    undelta();
    UpdatableSeq u = (UpdatableSeq)updatable_;
    int idx = u.firstIndexOf(element);
    if (idx < 0)
      return this;
    else {
      try {
        u.removeAt(idx);
      }
      catch (NoSuchElementException ex) {} // can't happen
      IncrSeq s = new IncrSeq(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = element;
      indexArg_ = idx;
      op_ = ADD_EDIT;
      return s;
    }
  }

/**
 * Implements collections.Collection.excluding.
 * If more than one occurrence of element exists, it makes
 * a full, non-lazy copy.
 * @see collections.Collection#excluding
**/
  public synchronized /* IncrSeq */ Collection  excluding(Object element) {
    undelta();
    UpdatableSeq u = (UpdatableSeq)updatable_;
    int occ = u.occurrencesOf(element);
    if (occ == 0)
      return this;
    else if (occ == 1)
      return this.removingOneOf(element);
    else {
      UpdatableSeq c = (UpdatableSeq)(u.duplicate());
      c.exclude(element);
      return new IncrSeq(c);
    }
  }


/**
 * Implements collections.Collection.replacingOneOf
 * @see collections.Collection#replacingOneOf
**/
  public synchronized /* IncrSeq */ Collection  replacingOneOf(Object oldElement,
                                                    Object newElement)
  throws IllegalElementException {
    undelta();
    UpdatableSeq u = (UpdatableSeq)updatable_;
    int idx = u.firstIndexOf(oldElement);
    if (idx < 0)
      return this;
    else {
      try {
        u.replaceAt(idx, newElement);
      }
      catch (NoSuchElementException ex) {} // can't happen
      IncrSeq s = new IncrSeq(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = oldElement;
      op_ = REPLACE_EDIT;
      indexArg_ = idx;
      return s;
    }
  }

/**
 * Implements collections.Collection.replacingAllOf.
 * If more than one occurrence of element exists, it makes
 * a full, non-lazy copy.
 * @see collections.Collection#replacingAllOf
**/
  public synchronized /* IncrSeq */ Collection  replacingAllOf(Object oldElement,
                                                        Object newElement)
  throws IllegalElementException {
    undelta();
    UpdatableSeq u = (UpdatableSeq)updatable_;
    int oldocc = u.occurrencesOf(oldElement);
    if (oldocc == 0)
      return this;
    else if (oldocc == 1)
      return this.replacingOneOf(oldElement, newElement);
    else {
      UpdatableSeq c = (UpdatableSeq)(u.duplicate());
      c.replaceAllOf(oldElement, newElement);
      return new IncrSeq(c);
    }
  }



/**
 * Implements collections.Seq.insertingAt.
 * @see collections.Seq#insertingAt
**/
  public synchronized /* IncrSeq */ Seq  insertingAt(int index, Object element)
  throws IllegalElementException, NoSuchElementException {
    undelta();
    UpdatableSeq u = (UpdatableSeq)updatable_;
    u.insertAt(index, element);
    IncrSeq s = new IncrSeq(u);
    nextVersion_ = s;
    updatable_ = null;
    indexArg_ = index;
    op_ = REMOVE_EDIT;
    return s;
  }

/**
 * Implements collections.Seq.removingAt.
 * @see collections.Seq#removingAt
**/
  public synchronized /* IncrSeq */ Seq  removingAt(int index)
  throws NoSuchElementException {
    undelta();
    UpdatableSeq u = (UpdatableSeq)updatable_;
    Object element = u.at(index);
    u.removeAt(index);
    IncrSeq s = new IncrSeq(u);
    nextVersion_ = s;
    updatable_ = null;
    indexArg_ = index;
    firstObjectArg_ = element;
    op_ = ADD_EDIT;
    return s;
  }

/**
 * Implements collections.Seq.replacingAt
 * @see collections.Seq#replacingAt
**/
  public synchronized /* IncrSeq */ Seq  replacingAt(int index, Object element)
  throws IllegalElementException, NoSuchElementException {
    undelta();
    UpdatableSeq u = (UpdatableSeq)updatable_;
    Object oldElement = u.at(index);
    if (oldElement.equals(element))
      return this;
    else {
      u.replaceAt(index, element);
      IncrSeq s = new IncrSeq(u);
      nextVersion_ = s;
      updatable_ = null;
      indexArg_ = index;
      firstObjectArg_ = oldElement;
      op_ = REPLACE_EDIT;
      return s;
    }
  }


/**
 * Perform updates within an edit chain
**/
  protected synchronized UpdatableCollection doEdit(UpdatableCollection c) {
    UpdatableSeq u = (UpdatableSeq)c;
    try {
      if (op_ == ADD_EDIT)
        u.insertAt(indexArg_, firstObjectArg_);
      else if (op_ == REMOVE_EDIT)
        u.removeAt(indexArg_);
      else if (op_ == REPLACE_EDIT)
        u.replaceAt(indexArg_, firstObjectArg_);
    }
    catch (IllegalElementException ex) {} // we've screened for all possible
    catch (NoSuchElementException ex) {}
    return u;
  }


}

