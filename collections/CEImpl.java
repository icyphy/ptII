/*
  File: CEImpl.java

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
 * A convenient base class for implementations of CollectionEnumeration
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

class CEImpl implements CollectionEnumeration {

    /**
     * The collection being enumerated
     **/

    protected UpdatableCollection coll_;

    /**
     * The version number of the collection we got upon construction
     **/

    protected int version_;

    /**
     * The number of elements we think we have left.
     * Initialized to coll_.size() upon construction
     **/

    protected int remaining_;

    protected CEImpl(UpdatableCollection c) {
        coll_ = c;
        version_ = c.version();
        remaining_ = c.size();
    }

    /**
     * Implements collections.CollectionEnumeration.corrupted.
     * Claim corruption if version numbers differ
     * @see collections.CollectionEnumeration#corrupted
     **/

    public boolean corrupted() {
        return version_ != coll_.version();
    }

    /**
     * Implements collections.CollectionEnumeration.numberOfRemainingElements.
     * @see collections.CollectionEnumeration#numberOfRemainingElements
     **/
    public int numberOfRemainingElements() {
        return remaining_;
    }

    /**
     * Implements java.util.Enumeration.hasMoreElements.
     * Return true if numberOfRemainingElements > 0 and not corrupted
     * @see java.util.Enumeration#hasMoreElements
     **/
    public boolean hasMoreElements() {
        return !corrupted() && remaining_ > 0;
    }

    /**
     * Subclass utility.
     * Tries to decrement remaining_, raising exceptions
     * if it is already zero or if corrupted()
     * Always call as the first line of nextElement.
     **/
    protected void decRemaining() throws NoSuchElementException {
        if (corrupted())
            throw new CorruptedEnumerationException(version_, coll_.version(), coll_, "Using version " + version_ + "but now at version " + coll_.version());
        else if (numberOfRemainingElements() <= 0)
            throw new NoSuchElementException("exhausted enumeration");
        else
            --remaining_;
    }

    /**
     * Implements java.util.Enumeration.nextElement.
     * No-Op default version
     * @see java.util.Enumeration#nextElement
     **/
    public Object nextElement() { return null; }
}

