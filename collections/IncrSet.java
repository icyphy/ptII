/*
  File: IncrSet.java

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
 *
 * Implementation of pure set
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public final class IncrSet extends IncrImpl implements Set {

/**
 * Make a new pure set using the default underlying Set implementation
**/
  public IncrSet() { this(DefaultImplementations.set()); }

/**
 * Make a pure set managing the given updatable set s.
 * Warning: Do not modify s during the the lifetime of the constructed pure set!
**/
  public IncrSet(UpdatableSet s) { super(s); }


/**
 * Make a copy. Uses lazy update.
**/

  protected Object clone() throws CloneNotSupportedException {
    undelta();
    IncrSet s = new IncrSet((UpdatableSet)(updatable_));
    nextVersion_ = s;
    updatable_ = null;
    op_ = NO_EDIT;
    return s;
  }

/**
 * Implements collections.Set.including
 * @see collections.Set#including
**/
  public synchronized /* IncrSet */ Set including(Object element)
  throws IllegalElementException {
    undelta();
    UpdatableSet u = (UpdatableSet)updatable_;
    boolean has = u.includes(element);
    if (has)
      return this;
    else {
      u.include(element);
      IncrSet s = new IncrSet(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = element;
      op_ = REMOVE_EDIT;
      return s;
    }
  }


/**
 * Implements collections.Collection.removingOneOf
 * @see collections.Collection#removingOneOf
**/
  public synchronized /* IncrSet */ Collection removingOneOf(Object element) {
    undelta();
    UpdatableSet u = (UpdatableSet)updatable_;
    boolean has = u.includes(element);
    if (!has)
      return this;
    else {
      u.exclude(element);
      IncrSet s = new IncrSet(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = element;
      op_ = ADD_EDIT;
      return s;
    }
  }

/**
 * Implements collections.Collection.excluding.
 * @see collections.Collection#excluding
**/
  public synchronized /* IncrSet */ Collection excluding(Object element) {
    return this.removingOneOf(element);
  }


/**
 * Implements collections.Collection.replacingOneOf
 * @see collections.Collection#replacingOneOf
**/
  public synchronized /* IncrSet */ Collection replacingOneOf(Object oldElement,
                                                    Object newElement)
  throws IllegalElementException {
    undelta();
    UpdatableSet u = (UpdatableSet)updatable_;
    boolean hasOld = u.includes(oldElement);
    if (!hasOld)
      return this;
    else {
      boolean hasNew = u.includes(newElement);
      if (hasNew)
        return this.excluding(oldElement);
      else {
        u.replaceAllOf(oldElement, newElement);
        IncrSet s = new IncrSet(u);
        nextVersion_ = s;
        updatable_ = null;
        firstObjectArg_ = newElement;
        secondObjectArg_ = oldElement;
        op_ = REPLACE_EDIT;
        return s;
      }
    }
  }

/**
 * Implements collections.Collection.replacingAllOf
 * @see collections.Collection#replacingAllOf
**/
  public synchronized /* IncrSet */ Collection replacingAllOf(Object oldElement,
                                                        Object newElement)
  throws IllegalElementException {
    return this.replacingOneOf(oldElement, newElement);
  }


/**
 * Perform updates within an edit chain
**/
  protected synchronized UpdatableCollection doEdit(UpdatableCollection c) {
    UpdatableSet u = (UpdatableSet)c;
    try {
      if (op_ == ADD_EDIT)
        u.include(firstObjectArg_);
      else if (op_ == REMOVE_EDIT)
        u.exclude(firstObjectArg_);
      else if (op_ == REPLACE_EDIT)
        u.replaceOneOf(firstObjectArg_, secondObjectArg_);
    }
    catch (IllegalElementException ex) {} // we've screened for all possible
    return u;
  }

}

