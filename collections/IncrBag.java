/*
  File: IncrBag.java

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
 * Incremental Implementation of Immutable Bag
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public final class IncrBag extends IncrImpl implements Immutable, Bag {

  private int occurrencesArg_;

/**
 * Make a new pure bag using the default underlying Bag implementation
**/
  public IncrBag() { this(DefaultImplementations.bag()); }

/**
 * Make a pure bag managing the given updatable bag s.
 * Warning: Do not modify s during the the lifetime of the constructed pure bag!
**/
  public IncrBag(UpdatableBag s) { super(s); occurrencesArg_ = 0; }


/**
 * Make a copy. Uses lazy update.
**/
  protected Object clone() throws CloneNotSupportedException {
    undelta();
    IncrBag s = new IncrBag((UpdatableBag)(updatable_));
    nextVersion_ = s;
    updatable_ = null;
    op_ = NO_EDIT;
    return s;
  }


/**
 * Implements collections.Bag.addingIfAbsent
 * @see collections.Bag#addingIfAbsent
**/
  public synchronized /* IncrBag */ Bag addingIfAbsent(Object element)
  throws IllegalElementException {
    undelta();
    UpdatableBag u = (UpdatableBag)updatable_;
    boolean has = u.includes(element);
    if (has)
      return this;
    else {
      u.add(element);
      IncrBag s = new IncrBag(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = element;
      occurrencesArg_ = 1;
      op_ = REMOVE_EDIT;
      return s;
    }
  }

/**
 * Implements collections.Bag.adding
 * @see collections.Bag#adding
**/
  public synchronized /* IncrBag */ Bag adding(Object element)
  throws IllegalElementException {
    undelta();
    UpdatableBag u = (UpdatableBag)updatable_;
    u.add(element);
    IncrBag s = new IncrBag(u);
    nextVersion_ = s;
    updatable_ = null;
    occurrencesArg_ = 1;
    firstObjectArg_ = element;
    op_ = REMOVE_EDIT;
    return s;
  }

/**
 * Implements collections.Collection.excluding.
 * @see collections.Collection#excluding
**/
  public synchronized /* IncrBag */ Collection excluding(Object element) {
    undelta();
    UpdatableBag u = (UpdatableBag)updatable_;
    int occ = u.occurrencesOf(element);
    if (occ == 0)
      return this;
    else {
      u.exclude(element);
      IncrBag s = new IncrBag(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = element;
      occurrencesArg_ = occ;
      op_ = ADD_EDIT;
      return s;
    }
  }

/**
 * Implements collections.Collection.removingOneOf
 * @see collections.Collection#removingOneOf
**/
  public synchronized /* IncrBag */ Collection removingOneOf(Object element) {
    undelta();
    UpdatableBag u = (UpdatableBag)updatable_;
    int occ = u.occurrencesOf(element);
    if (occ == 0)
      return this;
    else {
      u.removeOneOf(element);
      IncrBag s = new IncrBag(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = element;
      occurrencesArg_ = 1;
      op_ = ADD_EDIT;
      return s;
    }
  }

/**
 * Implements collections.Collection.replacingAllOf
 * @see collections.Collection#replacingAllOf
**/
  public synchronized /* IncrBag */ Collection replacingAllOf(Object oldElement,
                                                        Object newElement)
  throws IllegalElementException {
    undelta();
    UpdatableBag u = (UpdatableBag)updatable_;
    int oldocc = u.occurrencesOf(oldElement);
    if (oldocc == 0)
      return this;
    else {
      u.replaceAllOf(oldElement, newElement);
      IncrBag s = new IncrBag(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = newElement;
      secondObjectArg_ = oldElement;
      op_ = REPLACE_EDIT;
      occurrencesArg_ = oldocc;
      return s;
    }
  }

/**
 * Implements collections.Collection.replacingOneOf
 * @see collections.Collection#replacingOneOf
**/
  public synchronized /* IncrBag */ Collection replacingOneOf(Object oldElement,
                                                    Object newElement)
  throws IllegalElementException {
    undelta();
    UpdatableBag u = (UpdatableBag)updatable_;
    boolean has = u.includes(oldElement);
    if (!has)
      return this;
    else {
      u.replaceOneOf(oldElement, newElement);
      IncrBag s = new IncrBag(u);
      nextVersion_ = s;
      updatable_ = null;
      firstObjectArg_ = newElement;
      secondObjectArg_ = oldElement;
      op_ = REPLACE_EDIT;
      occurrencesArg_ = 1;
      return s;
    }
  }


/**
 * Perform updates within an edit chain
**/
  protected synchronized UpdatableCollection doEdit(UpdatableCollection c) {
    UpdatableBag u = (UpdatableBag)c;
    try {
      for (int i = 0; i < occurrencesArg_; ++i) {
        if (op_ == ADD_EDIT)
          u.add(firstObjectArg_);
        else if (op_ == REMOVE_EDIT)
          u.removeOneOf(firstObjectArg_);
        else if (op_ == REPLACE_EDIT)
          u.replaceOneOf(firstObjectArg_, secondObjectArg_);
      }
    }
    catch (IllegalElementException ex) {} // we've screened for all possible
    return u;
  }


}

