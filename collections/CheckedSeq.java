/*
  File: CheckedSeq.java.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Misc clean-up
  19Oct95  dl                 More misc clean-up

*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * @author Doug Lea
 * @version 0.94
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/


public class CheckedSeq extends CheckedCollection implements UpdatableSeq {

/**
 * Wrap Seq s inside a checker
**/

  public CheckedSeq(UpdatableSeq s) { super(s); }

/**
 * Make a Checked clone of underlying collection
**/
  protected Object clone() throws CloneNotSupportedException {
    return new CheckedSeq((UpdatableSeq)(thys.duplicate()));
  }

/**
 * return casted version of CheckedCollection.thys.
**/

  public UpdatableSeq thys() { return (UpdatableSeq)thys; }

/**
 * return casted version of CheckedCollection.prev.
**/

  public UpdatableSeq prev() { return (UpdatableSeq)prev; }



/**
 * Checks collections.Collection.elements according to its specification
 * @see collections.Collection#elements
**/
  public synchronized CollectionEnumeration elements() {
    // get one and make sure it is OK
    int c = thys.size();
    CollectionEnumeration e = thys.elements();
    int i = 0;
    while (e.hasMoreElements()) {
      assert(e.numberOfRemainingElements() == c);
      --c;
      Object v = e.nextElement();
      // Make sure all forms of indexing v work
      assert(thys().at(i).equals(v));
      assert(thys().firstIndexOf(v, i) == i);
      assert(thys().lastIndexOf(v, i) == i);
      assert(thys().firstIndexOf(v) <= i);
      assert(thys().lastIndexOf(v) >= i);
      ++i;
    }
    postCheck();
    return thys.elements();
  }

/**
 * Checks collections.Seq.at according to its specification
 * @see collections.Seq#at
**/
  public synchronized Object at(int index)
  throws  NoSuchElementException {
    preCheck();
    try {
      Object result = thys().at(index);
      assert(result != null);
      assert(thys.includes(result));
      assert(index >= 0 && index < thys.size());
      assert(thys.sameStructure(prev));
      postCheck();
      return result;
    }
    catch (NoSuchElementException ex) {
      assert(index < 0 || index >= thys.size());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }

/**
 * Checks collections.Seq.first according to its specification
 * @see collections.Seq#first
**/
  public synchronized Object first()
  throws  NoSuchElementException {
    preCheck();
    try {
      Object result = thys().first();
      assert(result.equals(thys().at(0)));

      assert(thys.sameStructure(prev));
      postCheck();
      return result;
    }
    catch (NoSuchElementException ex) {
      assert(thys.isEmpty());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }

/**
 * Checks collections.Seq.last according to its specification
 * @see collections.Seq#last
**/
  public synchronized Object last()
  throws  NoSuchElementException {
    preCheck();
    try {
      Object result = thys().last();
      assert(result.equals(thys().at(thys.size()-1)));

      assert(thys.sameStructure(prev));
      postCheck();
      return result;
    }
    catch (NoSuchElementException ex) {
      assert(thys.isEmpty());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
  }

/**
 * Checks collections.Seq.firstIndexOf according to its specification
 * @see collections.Seq#firstIndexOf
**/
  public synchronized int firstIndexOf(Object element, int startingIndex) {
    preCheck();
    int result = thys().firstIndexOf(element, startingIndex);
    int si = (startingIndex < 0)? 0 : startingIndex;
    int n = result;
    if (n < 0) n = thys.size();
    for (int i = si; i < n; ++i)
      assert(!thys().at(i).equals(element));
    if (result != -1)  {
      assert(thys().at(result).equals(element));
      assert(result >= startingIndex);
      assert(result < thys.size());
    }


    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }

/**
 * Checks collections.Seq.firstIndexOf according to its specification
 * @see collections.Seq#firstIndexOf
**/
  public synchronized int firstIndexOf(Object element) {
    preCheck();
    int result = thys().firstIndexOf(element);
    assert(result == thys().firstIndexOf(element, 0));
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }

/**
 * Checks collections.Seq.lastIndexOf according to its specification
 * @see collections.Seq#lastIndexOf
**/
  public synchronized int lastIndexOf(Object element, int startingIndex) {
    preCheck();
    int result = thys().lastIndexOf(element, startingIndex);
    int n = result+1;
    if (result < 0) n = 0;
    for (int i = n; i <= startingIndex; ++i)
      assert(!thys().at(i).equals(element));

    if (result != -1)  {
      assert(thys().at(result).equals(element));
      assert(result <= startingIndex);
      assert(result < thys.size());
    }

    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }

/**
 * Checks collections.Seq.lastIndexOf according to its specification
 * @see collections.Seq#lastIndexOf
**/
  public synchronized int lastIndexOf(Object element) {
    preCheck();
    int result = thys().lastIndexOf(element);
    assert(result == thys().lastIndexOf(element, thys.size()-1));
    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }

/**
 * Checks collections.Seq.subseq according to its specification
 * @see collections.Seq#subseq
**/
  public synchronized Seq subseq(int index, int length)
  throws  NoSuchElementException {
    preCheck();

    Seq result = thys().subseq(index, length);
    if (length <= 0)
      assert(result.isEmpty());
    else {
      assert(result.size() == length);
      for (int i = 0; i < length; ++i) {
        assert(result.at(i).equals(thys().at(i+index)));
      }
    }
    result.checkImplementation();

    assert(thys.sameStructure(prev));
    postCheck();
    return result;
  }

/**
 * Checks collections.UpdatableSeq.insertAt according to its specification
 * @see collections.UpdatableSeq#insertAt
**/

  public synchronized void insertAt(int index, Object element)
  throws IllegalElementException, NoSuchElementException {
    preCheck();
    try {
      thys().insertAt(index, element);
      checkInsert(thys(), prev(), index, element, true);
      postCheck();
    }
    catch (IllegalArgumentException ex) {
      assert(!thys.canInclude(element));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
    catch (NoSuchElementException ex) {
      assert(index < 0 || index >= thys.size());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }

/**
 * Checks collections.Seq.insertingAt.
 * @see collections.Seq#insertingAt
**/
  public synchronized Seq  insertingAt(int index, Object element)
  throws IllegalElementException, NoSuchElementException {
    preCheck();
    try {
      Seq nc = thys().insertingAt(index, element);
      checkInsert(nc, thys(), index, element, false);
      assert(thys.sameStructure(prev));
      postCheck();
      return nc;
    }
    catch (IllegalArgumentException ex) {
      assert(!thys.canInclude(element));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
    catch (NoSuchElementException ex) {
      assert(index < 0 || index >= thys.size());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }



/**
 * Checks collections.UpdatableSeq.replaceAt according to its specification
 * @see collections.UpdatableSeq#replaceAt
**/
  public synchronized void        replaceAt(int index, Object element)
  throws IllegalElementException, NoSuchElementException {
    preCheck();
    try {
      thys().replaceAt(index, element);
      checkReplaceAt(thys(), prev(), index, element, true);
      postCheck();
    }
    catch (IllegalArgumentException ex) {
      assert(!thys.canInclude(element));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
    catch (NoSuchElementException ex) {
      assert(index < 0 || index >= thys.size());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }


/**
 * Checks collections.Seq.replacingAt
 * @see collections.Seq#replacingAt
**/
  public synchronized  Seq  replacingAt(int index, Object element)
  throws IllegalElementException, NoSuchElementException {
    preCheck();
    try {
      Seq nc = thys().replacingAt(index, element);
      checkReplaceAt(nc, thys(), index, element, false);
      assert(thys.sameStructure(prev));
      postCheck();
      return nc;
    }
    catch (IllegalArgumentException ex) {
      assert(!thys.canInclude(element));
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }
    catch (NoSuchElementException ex) {
      assert(index < 0 || index >= thys.size());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }

/**
 * Checks collections.UpdatableSeq.removeAt according to its specification
 * @see collections.UpdatableSeq#removeAt
**/
  public synchronized void        removeAt(int index)
  throws NoSuchElementException {
    preCheck();
    try {
      thys().removeAt(index);
      checkRemoveAt(thys(), prev(), index, true);
      postCheck();
    }
    catch (NoSuchElementException ex) {
      assert(index < 0 || index >= thys.size());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }


/**
 * Checks collections.Seq.removingAt.
 * @see collections.Seq#removingAt
**/
  public synchronized Seq  removingAt(int index)
  throws NoSuchElementException {
    preCheck();
    try {
      Seq nc = thys().removingAt(index);
      checkRemoveAt(nc, thys(), index, false);
      assert(thys.sameStructure(prev));
      postCheck();
      return nc;
    }
    catch (NoSuchElementException ex) {
      assert(index < 0 || index >= thys.size());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }

/**
 * Checks collections.UpdatableSeq.insertFirst according to its specification
 * @see collections.UpdatableSeq#insertFirst
**/
  public synchronized void insertFirst(Object element) throws IllegalElementException {
    preCheck();
    IllegalElementException e = null; // make sure the two calls throw the same exceptions
    try {
      prev().insertAt(0, element);
    }
    catch (IllegalElementException ex) { e = ex; }
    catch (NoSuchElementException ex) {
      assert(false); // should not occur!
    }
    try {
      thys().insertFirst(element);
    }
    catch (IllegalElementException ex) {
      assert(e != null);
    }
    assert(thys.sameStructure(prev));
    postCheck();
    if (e != null) throw e;
  }


/**
 * Checks collections.UpdatableSeq.replaceFirst according to its specification
 * @see collections.UpdatableSeq#replaceFirst
**/
  public synchronized void        replaceFirst(Object element)
  throws IllegalElementException, NoSuchElementException {
    preCheck();
    IllegalElementException e1 = null;
    NoSuchElementException e2 = null;
    try {
      prev().replaceAt(0, element);
    }
    catch (IllegalElementException ex) { e1 = ex; }
    catch (NoSuchElementException ex) { e2 = ex; }
    try {
      thys().replaceFirst(element);
    }
    catch (IllegalElementException ex) {
      assert(e1 != null);
    }
    catch (NoSuchElementException ex) {
      assert(e2 != null);
    }

    assert(thys.sameStructure(prev));
    postCheck();
    if (e1 != null) throw e1;
    if (e2 != null) throw e2;
  }


/**
 * Checks collections.UpdatableSeq.removeFirst according to its specification
 * @see collections.UpdatableSeq#removeFirst
**/
  public synchronized void        removeFirst()
  throws NoSuchElementException {
    preCheck();
    NoSuchElementException e = null; // make sure the two calls throw the same exceptions
    try {
      prev().removeAt(0);
    }
    catch (NoSuchElementException ex) { e = ex; }
    try {
      thys().removeFirst();
    }
    catch (IllegalElementException ex) {
      assert(e != null);
    }
    assert(thys.sameStructure(prev));
    postCheck();
    if (e != null) throw e;
  }



/**
 * Checks collections.UpdatableSeq.insertLast according to its specification
 * @see collections.UpdatableSeq#insertLast
**/
  public synchronized void insertLast(Object element) throws IllegalElementException {
    preCheck();
    IllegalElementException e = null; // make sure the two calls throw the same exceptions
    try {
      prev().insertAt(thys.size(), element);
    }
    catch (IllegalElementException ex) { e = ex; }
    catch (NoSuchElementException ex) {
      assert(false); // should not occur
    }
    try {
      thys().insertLast(element);
    }
    catch (IllegalElementException ex) {
      assert(e != null);
    }
    assert(thys.sameStructure(prev));
    postCheck();
    if (e != null) throw e;
  }


/**
 * Checks collections.UpdatableSeq.replaceLast according to its specification
 * @see collections.UpdatableSeq#replaceLast
**/
  public synchronized void        replaceLast(Object element)
  throws IllegalElementException, NoSuchElementException {
    preCheck();
    IllegalElementException e1 = null;
    NoSuchElementException e2 = null;
    try {
      prev().replaceAt(thys.size()-1, element);
    }
    catch (IllegalElementException ex) { e1 = ex; }
    catch (NoSuchElementException ex) { e2 = ex; }
    try {
      thys().replaceLast(element);
    }
    catch (IllegalElementException ex) {
      assert(e1 != null);
    }
    catch (NoSuchElementException ex) {
      assert(e2 != null);
    }

    assert(thys.sameStructure(prev));
    postCheck();
    if (e1 != null) throw e1;
    if (e2 != null) throw e2;
  }


/**
 * Checks collections.UpdatableSeq.removeLast according to its specification
 * @see collections.UpdatableSeq#removeLast
**/
  public synchronized void        removeLast()
  throws NoSuchElementException {
    preCheck();
    NoSuchElementException e = null; // make sure the two calls throw the same exceptions
    try {
      prev().removeAt(thys.size()-1);
    }
    catch (NoSuchElementException ex) { e = ex; }
    try {
      thys().removeLast();
    }
    catch (IllegalElementException ex) {
      assert(e != null);
    }
    assert(thys.sameStructure(prev));
    postCheck();
    if (e != null) throw e;
  }




/**
 * Checks collections.UpdatableSeq.removeFromTo according to its specification
 * @see collections.UpdatableSeq#removeFromTo
**/
  public synchronized void        removeFromTo(int fromIndex, int toIndex)
  throws NoSuchElementException {
    preCheck();
    try {
      thys().removeFromTo(fromIndex, toIndex);
      int n = toIndex - fromIndex + 1;
      if (n < 0) n = 0;
      assert(thys.size() == prev.size() - n);
      assert(fromIndex >= 0 && fromIndex < prev.size());
      assert(toIndex >= 0 && toIndex < prev.size());
      assert(!((thys.version() == prevVersion) && n > 0));
      for (int i = 0; i < fromIndex; ++i) {
        assert(thys().at(i).equals(prev().at(i)));
      }
      for (int i = fromIndex; i < thys.size(); ++i) {
        assert(thys().at(i).equals(prev().at(i+n)));
      }
      postCheck();
    }
    catch (NoSuchElementException ex) {
      assert(fromIndex < 0 || fromIndex >= thys.size() ||
	        toIndex < 0 || toIndex >= thys.size());
      assert(thys.sameStructure(prev));
      postCheck();
      throw ex;
    }

  }


/**
 * Checks collections.UpdatableSeq.insertElementsAt according to its specification
 * @see collections.UpdatableSeq#insertElementsAt
**/
  public synchronized void        insertElementsAt(int index, Enumeration e)
  throws IllegalElementException, CorruptedEnumerationException,
  NoSuchElementException {
    preCheck();
    try {
      int n = -1;
      if (e instanceof CollectionEnumeration)
        n = ((CollectionEnumeration)e).numberOfRemainingElements();
      thys().insertElementsAt(index, e);
      if (n < 0)
        n = thys.size() - prev.size(); // best we can do

      assert(thys.size() == prev.size() + n);
      assert(!e.hasMoreElements());
      assert(index >= 0 && index < prev.size());
      assert(!((thys.version() == prevVersion) && n > 0));
      for (int i = 0; i < index; ++i) {
        assert(thys().at(i).equals(prev().at(i)));
      }
      // We cannot check the inserted elements themselves
      // since e has been exhausted. We can't even
      // first clone it since Enumerations don't claim to be
      // usefully clonable.
      for (int i = index + n; i < thys.size(); ++i) {
        assert(thys().at(i).equals(prev().at(i-n)));
      }
      postCheck();
    }
    catch (CorruptedEnumerationException ex) {
      postCheck();
      throw ex;
    }
    catch (NoSuchElementException ex) {
      assert(index < 0 || index >= thys.size());
      assert(thys.sameStructure(prev));
      throw ex;
    }
    catch (IllegalElementException ex) {
      postCheck();
      throw ex;
    }
  }


/**
 * Checks collections.UpdatableSeq.prependElements according to its specification
 * @see collections.UpdatableSeq#prependElements
**/
  public synchronized void prependElements(Enumeration e)
  throws IllegalElementException, CorruptedEnumerationException {
    // too hard to check via behavioral equivalence, so
    // we just adapt insertElementsAt code
    preCheck();
    try {
      int n = -1;
      if (e instanceof CollectionEnumeration)
        n = ((CollectionEnumeration)e).numberOfRemainingElements();
      thys().prependElements(e);
      if (n < 0)
        n = thys.size() - prev.size(); // best we can do
      thys().prependElements(e);
      assert(thys.size() == prev.size() + n);
      assert(!e.hasMoreElements());
      assert(!((thys.version() == prevVersion) && n > 0));
      for (int i = n; i < thys.size(); ++i) {
        assert(thys().at(i).equals(prev().at(i-n)));
      }
      postCheck();
    }
    catch (IllegalElementException ex) {
      postCheck();
      throw ex;
    }
    catch (CorruptedEnumerationException ex) {
      postCheck();
      throw ex;
    }
  }

/**
 * Checks collections.UpdatableSeq.appendElements according to its specification
 * @see collections.UpdatableSeq#appendElements
**/
  public synchronized void        appendElements(Enumeration e)
  throws IllegalElementException, CorruptedEnumerationException {
    // As above, too hard to check via behavioral equivalence, so
    // we just adapt insertElementsAt code
    preCheck();
    try {
      int n = -1;
      if (e instanceof CollectionEnumeration)
        n = ((CollectionEnumeration)e).numberOfRemainingElements();
      thys().appendElements(e);
      if (n < 0)
        n = thys.size() - prev.size(); // best we can do

      assert(thys.size() == prev.size() + n);
      assert(!e.hasMoreElements());
      assert(!((thys.version() == prevVersion) && n > 0));
      for (int i = 0; i < prev.size(); ++i) {
        assert(thys().at(i).equals(prev().at(i)));
      }
      postCheck();
    }
    catch (IllegalElementException ex) {
      postCheck();
      throw ex;
    }
    catch (CorruptedEnumerationException ex) {
      postCheck();
      throw ex;
    }
  }

/**
 * Helper for checking insert*
**/

  protected void checkInsert(Seq nc, Seq oc, int index,
                             Object element, boolean verchk) {

    assert(nc.canInclude(element));
    assert(nc.size() == oc.size() + 1);
    assert(index >= 0 && index < nc.size());
    assert(nc.at(index).equals(element));
    for (int i = 0; i < index; ++i) {
      assert(nc.at(i).equals(oc.at(i)));
    }
    for (int i = index+1; i < thys.size(); ++i) {
      assert(nc.at(i).equals(oc.at(i-1)));
    }

    if (verchk) {
      int nv = ((UpdatableCollection)nc).version();
      assert(nv != prevVersion);
    }

    nc.checkImplementation();

  }

/**
 * Helper for checking replac*
**/

  protected void checkReplaceAt(Seq nc, Seq oc, int index,
                             Object element, boolean verchk) {

    assert(nc.canInclude(element));
    assert(nc.size() == oc.size());
    assert(index >= 0 && index < nc.size());
    assert(nc.at(index).equals(element));
    for (int i = 0; i < index; ++i)
      assert(nc.at(i).equals(oc.at(i)));
    for (int i = index+1; i < nc.size(); ++i)
      assert(nc.at(i).equals(oc.at(i)));

    if (verchk) {
      int nv = ((UpdatableCollection)nc).version();
      assert(!((nv == prevVersion) && !nc.at(index).equals(oc.at(index))));
    }

    nc.checkImplementation();

  }

/**
 * Helper for checking remov*
**/

  protected void checkRemoveAt(Seq nc, Seq oc, int index,
                               boolean verchk) {

    assert(nc.size() == oc.size() - 1);
    assert(index >= 0 && index < oc.size());
    for (int i = 0; i < index; ++i) {
      assert(nc.at(i).equals(oc.at(i)));
    }
    for (int i = index; i < nc.size(); ++i) {
      assert(nc.at(i).equals(oc.at(i+1)));
    }

    if (verchk) {
      int nv = ((UpdatableCollection)nc).version();
      assert(nv != prevVersion);
    }

    nc.checkImplementation();

  }

}


