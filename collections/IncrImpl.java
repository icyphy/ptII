/*
  File: IncrImpl.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  Thu Oct 12 1995 Doug Lea    Remove special no-copy on enum clone
  13Oct95  dl                 New strategy to check if pinned by enumerator
*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 *
 * Base class for  Immutable Collection implementations.
 * using `negative deltas'. That is, they update
 * an internally held Collection and set it as the internal Collection
 * for the Immutable Collection serving as the result, but also keep
 * a record of how to undo this change on a copy of the update, so
 * as to reconstruct the original Collection.
 * <P>
 * Since it's usually the case that applications use updates
 * rather than their sources, negative deltas are generally
 * more efficient than other schemes. Also, because updates don't
 * point to their sources, when a source becomes unreferenced,
 * it and all of its edit records can be garbage collected.
 * The price you pay for these nice aspects is that
 * reconstruction of old versions is not always all that fast or cheap.
 * <P>
 * The logic of reconstruction is to work our way to the UpdatableCollection
 * serving as the source of the edits, and then apply each undo operation
 * all the way back up to where we are. Subclasses specialize
 * on the exact edit operation to do at each step.
 * <P>
 * Reconstruction would be two-line recursive procedure of the form:
 * <PRE>
 * UpdatableCollection reconstruct() {
 *   if (the lastest version) return a clone of the held UpdatableCollection
 *   else return edit(nextVersion.reconstruct())
 * }
 * </PRE>
 * Except for two problems:
 *
 * <OL>
 * <LI> We need to prevent interference among concurrent reconstructions
 *    and/or with operations on the collections themselves.
 *    But we cannot afford to hold a lock (synch) on every single node in
 *    the chain of edits at once. For long edit chains, it would
 *    require too many
 *    simultaneous locks. The java runtime could even run out of them.
 *
 * <LI> The recursion could get very deep, which is not a good idea
 *    in java because of how it uses the stack.
 * </OL>
 * <P>
 * These problems are addressed by:
 * <OL>
 * <LI> Using a condition variable, that causes each node to
 *    lock without holding all of the locks at once. The
 *    variable used, prevVersion_ happens to be the same
 *    one used for...
 *
 * <LI> Putting next-links from each node to the one representing
 *     the edit which is to follow. This way we can iterate rather
 *     than use recursion.
 * </OL>
 * <P>
 * (All this would be a little prettier if we offloaded work
 * into Edit Command objects. But since these operations must be
 * coordinated with the Collection nodes, it works better to
 * roll them together.)
 * <P>
 * The code is highly particularized for the operations
 * currently supported in the collections package. It would
 * not be very easy to add subclasses beyond those for the four
 * basic flavors of collection subclasses currently implemented.
 *<P>
 * Some of the basic ideas for this class are due to Mark Miller
 * and unknown people at Xerox Parc.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

abstract class IncrImpl implements Immutable, Collection {

/**
 * The collection performing the actual work. Null if delta'd
**/

  protected UpdatableCollection updatable_;

/**
 * When delta'd, a ref to the collection we generated.
 * Invariant:  exactly one of updatable_ or nextVersion_
 * are non-null at any given time.
**/

  protected IncrImpl nextVersion_;

/**
 * Ref to the Incr collection that made us (or anyone
 * else requesting an edit operation).
 * Non-null only during reconstructions, so it also
 * serves as a condition variable. When it is non-null,
 * incoming updates must wait. Avoiding back-pointers
 * at other times enables GC to kill off whole chains
 * of nodes that can never be used anyway since their roots are unreferenced.
**/

  private IncrImpl prevVersion_;

/**
 * Ref to an enumerator that has us pinned. If it is in
 * the midst of a traversal and are performing an
 * incremental operation, we need to make a full clone first.
 * Only one level of pinning supported. If there's more than
 * one, we always conservatively copy.
**/

  private IncrCollectionEnumeration pin_;

/**
 * We can only handle a known number of undoable operations on
 * collections, encoded into op_
**/

  protected int op_;

/* Known Possible values of op_: */

  protected static final int NO_EDIT = 0;
  protected static final int ADD_EDIT = 1;
  protected static final int REMOVE_EDIT = 2;
  protected static final int REPLACE_EDIT = 3;


/**
 * Some opportunistic sharing: Subclasses happen
 * to need records of two Object arguments  for updates.
**/

  protected Object firstObjectArg_;
  protected Object secondObjectArg_;


  protected IncrImpl(UpdatableCollection c) {
    updatable_ = c;
    nextVersion_ = null;
    prevVersion_ = null;
    op_ = NO_EDIT;
    firstObjectArg_ = null;
    secondObjectArg_ = null;
    pin_ = null;
  }

/**
 * Wrapper for clone()
 * @see clone
**/

 public Collection duplicate() {
   Collection c = null;
   try {
     c = (Collection)(this.clone());
    } catch (CloneNotSupportedException ex) {}
   return c;
 }



/**
 * Implements collections.Collection.canInclude.
 * @see collections.Collection#canInclude
**/
  public final synchronized boolean     canInclude(Object element) {
    return accessOnly().canInclude(element);
  }

/**
 * Implements collections.Collection.isEmpty.
 * @see collections.Collection#isEmpty
**/
  public final synchronized boolean     isEmpty() {
    return accessOnly().isEmpty();
  }

/**
 * Implements collections.Collection.size.
 * @see collections.Collection#size
**/
  public final synchronized  int         size() {
    return accessOnly().size();
  }


/**
 * Implements collections.Collection.includes.
 * @see collections.Collection#includes
**/
  public final synchronized boolean     includes(Object element) {
    return accessOnly().includes(element);
  }

/**
 * Implements collections.Collection.occurrencesOf.
 * @see collections.Collection#occurrencesOf
**/
  public final synchronized int         occurrencesOf(Object element) {
    return accessOnly().occurrencesOf(element);
  }

/**
 * Implements collections.Collection.sameStructure
 * @see collections.Collection#sameStructure
**/
  public final synchronized boolean  sameStructure(Collection c) {
    return accessOnly().sameStructure(c);
  }

/**
 * Implements collections.Collection.elements.
 * @see collections.Collection#elements
**/
  public final synchronized CollectionEnumeration elements() {
    undelta();
    // wrap the underlying enumeration in Incr version
    CollectionEnumeration e = updatable_.elements();
    IncrCollectionEnumeration ie = new IncrCollectionEnumeration(this, e);
    pin(ie);
    return ie;
  }


  public final synchronized String toString() {
    undelta();
    StringBuffer buf = new StringBuffer();
    buf.append("( (class: " + getClass().toString() + ")");
    buf.append(updatable_.toString());
    buf.append(" )");
    return buf.toString();
  }


/**
 * Call this as the first statement of EVERY subclass method
 * that performs any kind of constructive update.
**/

  protected final synchronized void undelta() {
    if (updatable_ != null) {

      if (pin_ != null) { // if pinned, we must conservatively copy
        updatable_ = (UpdatableCollection)(updatable_.duplicate());
        pin_ = null;
      }

    }
    else {

      // lock everyone while forming the edit chain
      IncrImpl p = linkEditChain(this);
      IncrImpl prev = this;
      IncrImpl base = null;
      while (p != null) {
        IncrImpl nxt = p.linkEditChain(prev);
        if (nxt == null) {
          base = p;
          break;
        }
        else {
          prev = p;
          p = nxt;
        }
      }

      // loop through the edits
      UpdatableCollection c = null;
      p = base;
      // funny end-condition on loop:
      //    since we've used up null as a sentinel, we go until
      //    we are back at our own node.
      for (;;) {
        c = p.reconstruct(c);
        IncrImpl nxt = p.breakEditChain();
        if (p == this) break;
        else p = nxt;
      }
      // Reset instance variables to use new updatable_
      updatable_ = c;
      nextVersion_ = null;
      op_ = NO_EDIT;
      firstObjectArg_ = null;
      secondObjectArg_ = null;
      pin_ = null;
    }
  }

/**
 * Special case of undelta that avoids the pin-check
 * in cases where we are performing only non-mutative operations
**/
   protected final UpdatableCollection accessOnly() {
     if (updatable_ == null) undelta();
     return updatable_;
   }


/**
 * unpin from a traversal. Called only by IncrCollectionEnumeration
 * when hasMoreElements() is false.
**/

  synchronized final void unpin(IncrCollectionEnumeration e) {
    if (e == pin_) pin_ = null;
  }

/**
 * Pin during a traversal. Call from any method constructing an
 * enumeration.
**/

  protected synchronized final void pin(IncrCollectionEnumeration e) {
    pin_ = e;
  }

/**
 * Must implement in subclasses to perform subclass-specific edits
**/

  protected abstract UpdatableCollection doEdit(UpdatableCollection c);

/**
 * Do a step of reconstruction. handle cloning cases, else pass off
 * to doEdit
**/

  private synchronized UpdatableCollection reconstruct(UpdatableCollection c) {
    if (updatable_ != null) { // we have a source
      return (UpdatableCollection)(updatable_.duplicate());
    }
    else if (op_ == NO_EDIT)
      return c;
    else
      return doEdit(c);
  }


/**
 * Handle the locking mechanics while making edit chains
**/

  private synchronized IncrImpl linkEditChain(IncrImpl nxt) {
    if (prevVersion_ != null) {
      do {
        try { wait(); }
        catch (InterruptedException ex) { } // safe to ignore because of recheck
      } while (prevVersion_ != null);
    }
    prevVersion_ = nxt;
    return nextVersion_;
  }

/**
 * Handle the locking mechanics while breaking edit chains
**/
  private synchronized IncrImpl breakEditChain() {
    IncrImpl s = prevVersion_;
    prevVersion_ = null;
    notifyAll();
    return s;
  }

/**
 * Implements collections.ImplementationCheckable.assert.
 * @see collections.ImplementationCheckable#assert
**/
  public void assert(boolean pred)
  throws ImplementationError {
    ImplementationError.assert(this, pred);
  }


/**
 * Implements collections.ImplementationCheckable.checkImplementation.
 * @see collections.ImplementationCheckable#checkImplementation
**/
  public synchronized void checkImplementation()
  throws ImplementationError {
    assert(((updatable_ == null) != (nextVersion_ == null)));
    assert(prevVersion_ == null);
    assert((op_ >= NO_EDIT) && (op_ <= REPLACE_EDIT));

    if (updatable_ != null)
      updatable_.checkImplementation();
    else
      nextVersion_.checkImplementation();
  }

}

