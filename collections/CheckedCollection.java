/*
  File: CheckedCollection.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Misc clean-up.
  19Oct95  dl                 more misc clean-up.

*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * Base class for  Collection testing.
 * CheckCollections are simple harnesses around other Collections.
 * They include constructors that accept any other kind of collection, c.
 * They mirror each operation in the corresponding interface.
 * For each operation, they invoke c.checkImplementation(),
 * then operate upon c,
 * then check for as many testable effects of the operation
 * as is feasible, and then recheck c.
 * <P>
 * This is not usually very fast. Among other reasons,
 * checks often require that copies of state be made to compare against later.
 * <P>
 * @author Doug Lea
 * @version 0.94
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class CheckedCollection implements UpdatableCollection  {

/**
 * The collection performing the actual work.
 * (All instance variables are public so can be inspected easily in tests)
**/

  public UpdatableCollection thys;

/**
 * A clone of the thys made before an operation
**/

  public UpdatableCollection prev;


/**
 * The version number of thys before an operation
**/

  public int prevVersion;

/**
 * Wrap collection c in inside a Checker
**/

  public CheckedCollection(UpdatableCollection c) {
    thys = c;
    thys.checkImplementation();
  }

/**
 * Make a Checked clone of underlying collection
**/
  protected Object clone() throws CloneNotSupportedException {
    return new CheckedCollection((UpdatableCollection)(thys.duplicate()));
  }


/**
 * Wrapper for clone()
 * @see clone
**/

 public synchronized Collection duplicate() {
   Collection c = null;
   try {
     c = (Collection)(this.clone());
    } catch (CloneNotSupportedException ex) {}
   return c;
 }


/**
 * Call at the end of any checked method
**/

  protected void postCheck() {
    thys.checkImplementation();
    prev = null; // side effect -- detach prev so GC can claim it
  }

/**
 * Call at the beginnning of any checked method requiring
 * use of prev clone
**/

  protected void preCheck() {
    prev = (UpdatableCollection)(thys.duplicate());
    prevVersion = thys.version();
  }

/**
 * Checks collections.Collection.canInclude according to its specification
 * @see collections.Collection#canInclude
**/
  public synchronized boolean     canInclude(Object element) {
    preCheck();
    boolean result = thys.canInclude(element);
    // not allowed to report true for null
    assert(!(result == true && element == null));
    // must be side-effect free
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }

/**
 * Checks collections.Collection.isEmpty according to its specification
 * @see collections.Collection#isEmpty
**/
  public synchronized boolean     isEmpty() {
    preCheck();
    boolean result = thys.isEmpty();
    // must be behaviorally equivalent to asking if size() == 0
    assert(result == (thys.size() == 0));
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }

/**
 * Checks collections.Collection.size according to its specification
 * @see collections.Collection#size
**/
  public synchronized  int         size() {
    preCheck();
    int result = thys.size();
    // sizes are non-negative
    assert(result >= 0);
    // cross-check of isEmpty assertion
    assert( (result == 0) == thys.isEmpty());
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }



/**
 * Checks collections.Collection.occurrencesOf according to its specification
 * @see collections.Collection#occurrencesOf
**/
  public synchronized int         occurrencesOf(Object element) {
    preCheck();
    int result = thys.occurrencesOf(element);
    // occurrences are non-negative
    assert((result >= 0));
    // cannot be greater than size()
    assert(result <= thys.size());
    // occurrences of things you canot include must be 0
    assert(thys.canInclude(element) || result == 0);
    // cross-check of include condition
    assert( !((result == 0) && thys.includes(element)));
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }

/**
 * Checks collections.Collection.includes according to its specification
 * @see collections.Collection#includes
**/
  public synchronized boolean     includes(Object element) {
    preCheck();
    boolean result = thys.includes(element);
    // must be equiv to asking if occurrences >= 1
    assert(result == (thys.occurrencesOf(element) >= 1));
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }

/**
 * Checks collections.Collection.elements according to its specification
 * @see collections.Collection#elements
**/
  public synchronized CollectionEnumeration elements() {
    // get one and make sure it is OK
    int c = thys.size();
    CollectionEnumeration e = thys.elements();
    while (e.hasMoreElements()) {
      // reports correct number of elements
      assert(e.numberOfRemainingElements() == c);
      --c;
      Object v = e.nextElement();
      // each element reported actually occurs
      assert(thys.includes(v));
    }
    // We've exhausted e; return another one
    postCheck();
    return thys.elements();
  }


/**
 * Checks collections.Collection.sameStructure according to its specification
 * @see collections.Collection#sameStructure
**/
  public synchronized boolean sameStructure(Collection c) {
    // cannot check since we rely on it to check other assertions
    boolean result = thys.sameStructure(c);
    postCheck();
    return result;
  }

/**
 * Implements Object.toString
**/
  public String toString() {
    // cannot check
    String result = thys.toString();
    postCheck();
    return result;
  }

/**
 * Checks collections.UpdatableCollection.version according to its specification
 * @see collections.UpdatableCollection#version
**/
  public synchronized int         version() {
    // nothing to check
    int result = thys.version();
    postCheck();
    return result;
  }

/**
 * Checks collections.UpdatableCollection.clear according to its specification
 * @see collections.UpdatableCollection#clear
**/
  public synchronized void        clear() {
    preCheck();
    thys.clear();
    // mst now be empty
    assert(thys.isEmpty());
    // version change only if not previously empty
    assert((thys.version() == prevVersion) == prev.isEmpty());
    postCheck();
  }


/**
 * Checks collections.UpdatableCollection.exclude according to its specification
 * @see collections.UpdatableCollection#exclude
**/
  public synchronized void exclude(Object element) {
    preCheck();
    thys.exclude(element);
    checkRemove(thys, prev, element, true, true);
    postCheck();
  }


/**
 * Checks collections.Collection.excluding.
 * @see collections.Collection#excluding
**/
  public synchronized Collection excluding(Object element) {
    preCheck();
    Collection nc = thys.excluding(element);
    checkRemove(nc, thys, element, true, false);
    assert(thys.sameStructure(prev));
    postCheck();
    return nc;
  }


/**
 * Checks collections.UpdatableCollection.removeOneOf according to its specification
 * @see collections.UpdatableCollection#removeOneOf
**/
  public synchronized void removeOneOf(Object element) {
    preCheck();
    thys.removeOneOf(element);
    checkRemove(thys, prev, element, false, true);
    postCheck();
  }

/**
 * Checks collections.Collection.removingOneOf
 * @see collections.Collection#removingOneOf
**/
  public synchronized Collection removingOneOf(Object element) {
    preCheck();
    Collection nc = thys.removingOneOf(element);
    checkRemove(nc, thys, element, false, false);
    assert(thys.sameStructure(prev));
    postCheck();
    return nc;
  }



/**
 * Checks collections.UpdatableCollection.replaceOneOf according to its specification
 * @see collections.UpdatableCollection#replaceOneOf
**/
  public synchronized void replaceOneOf(Object oldElement, Object newElement)
  throws IllegalElementException {
    preCheck();
    try {
      thys.replaceOneOf(oldElement, newElement);
      checkReplace(thys, prev, oldElement, newElement, false, true);
      postCheck();
    }
    catch(IllegalElementException ex) {
      assert(thys.includes(oldElement) && !thys.canInclude(newElement));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
  }

/**
 * Checks collections.Collection.replacingOneOf
 * @see collections.Collection#replacingOneOf
**/
  public synchronized Collection replacingOneOf(Object oldElement,
                                                Object newElement)
  throws IllegalElementException {
    preCheck();
    try {
      Collection nc = thys.replacingOneOf(oldElement, newElement);
      checkReplace(nc, thys, oldElement, newElement, false, false);
      assert(thys.sameStructure(prev));
      postCheck();
      return nc;
    }
    catch(IllegalElementException ex) {
      assert(thys.includes(oldElement) && !thys.canInclude(newElement));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }

/**
 * Checks collections.UpdatableCollection.replaceAllOf according to its specification
 * @see collections.UpdatableCollection#replaceAllOf
**/
  public synchronized void replaceAllOf(Object oldElement, Object newElement)
  throws IllegalElementException {
    preCheck();
    try {
      thys.replaceAllOf(oldElement, newElement);
      checkReplace(thys, prev, oldElement, newElement, true, true);
      postCheck();
    }
    catch(IllegalElementException ex) {
      assert(thys.includes(oldElement) && !thys.canInclude(newElement));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
  }

/**
 * Checks collections.Collection.replacingAllOf
 * @see collections.Collection#replacingAllOf
**/
  public synchronized Collection replacingAllOf(Object oldElement,
                                                Object newElement)
  throws IllegalElementException {
    preCheck();
    try {
      Collection nc = thys.replacingAllOf(oldElement, newElement);
      checkReplace(nc, thys, oldElement, newElement, true, false);
      assert(thys.sameStructure(prev));
      postCheck();
      return nc;
    }
    catch(IllegalElementException ex) {
      assert(thys.includes(oldElement) && !thys.canInclude(newElement));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }

/**
 * Checks collections.UpdatableCollection.take according to its specification
 * @see collections.UpdatableCollection#take
**/
  public Object take() throws NoSuchElementException {
    preCheck();
    try {
      Object result = thys.take();
      assert(result != null);
      checkRemove(thys, prev, result, false, true);
      postCheck();
      return result;
    }
    catch (NoSuchElementException ex) {
      assert(prev.isEmpty());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
  }


/**
 * Checks collections.UpdatableCollection.excludeElements
 * @see collections.UpdatableCollection#excludeElements
**/
  public synchronized void excludeElements(Enumeration e)
  throws CorruptedEnumerationException {
    preCheck();
    thys.excludeElements(e);
    // can't check elements since e has been exhausted --
    // we have no handle to them.
    postCheck();
  }


/**
 * Checks collections.UpdatableCollection.removeElements
 * @see collections.UpdatableCollection#removeElements
**/
  public synchronized void removeElements(Enumeration e)
  throws CorruptedEnumerationException {
    preCheck();
    thys.removeElements(e);
    // Same problem as above
    postCheck();
  }

/**
 * Implements collections.ImplementationCheckable.assert.
 * @see collections.ImplementationCheckable#assert
**/
  public void assert(boolean pred)
  throws ImplementationError {
    ImplementationError.assert(thys, pred);
  }


/**
 * Implements collections.ImplementationCheckable.checkImplementation
 * @see collections.ImplementationCheckable#checkImplementation
**/
  public synchronized void checkImplementation()
  throws ImplementationError {
    // we cannot check ourselves!
  }

/**
 * Helper for checking remov*, exclud*, take
**/

  protected void checkRemove(Collection nc, Collection oc,
			     Object element,
			     boolean allOccurrences, boolean verchk) {
    int prevOcc = oc.occurrencesOf(element);
    int reqOcc = 0;
    if (!allOccurrences && prevOcc > 1)
      reqOcc =  prevOcc - 1;
    int newOcc = nc.occurrencesOf(element);

    assert(newOcc == reqOcc);

    // size reflects removals
    assert(nc.size() == oc.size() - (prevOcc - newOcc));

    if (verchk) {
      // version change only if occurrences change
      int nv = ((UpdatableCollection)nc).version();
      assert((nv == prevVersion) == (prevOcc == newOcc));
    }

    // all other elements the same
    CollectionEnumeration os = oc.elements();
    while (os.hasMoreElements()) {
      Object v = os.nextElement();
      assert(!v.equals(element) ==
             (nc.occurrencesOf(v) == oc.occurrencesOf(v)));
    }

    nc.checkImplementation();

  }

/**
 * Helper for checking replac*
**/
  protected void checkReplace(Collection nc, Collection oc,
			      Object oldElement, Object newElement,
			      boolean allOccurrences, boolean verchk) {
    int delta = prev.occurrencesOf(oldElement);
    // for OneOf, occurrences of old element == max(0, old occurrences - 1)
    if (!allOccurrences && delta > 1) delta = 1;
    // ... unless they are equal, in which case unchanged
    if (oldElement.equals(newElement)) delta = 0;

    assert(nc.occurrencesOf(oldElement) ==
	   oc.occurrencesOf(oldElement) - delta);

    if (delta != 0)
      assert(nc.canInclude(newElement));

    // new occurrences added by one
    int reqNewOcc = prev.occurrencesOf(newElement) + delta;
    // .. unless it obeys Set semantics, in which case max of 1
    if (reqNewOcc > 1 && (nc instanceof Set)) reqNewOcc = 1;

    assert(nc.occurrencesOf(newElement) == reqNewOcc);

    if (verchk) {
      // version change only if occurrences change
      int nv = ((UpdatableCollection)nc).version();
      assert(!((nv == prevVersion) && (delta != 0)));;
    }

    // all other elements the same

    CollectionEnumeration os = oc.elements();
    while (os.hasMoreElements()) {
      Object v = os.nextElement();
      if (!v.equals(oldElement) && !v.equals(newElement))
	assert(nc.occurrencesOf(v) == oc.occurrencesOf(v));
    }

    nc.checkImplementation();
  }

}

