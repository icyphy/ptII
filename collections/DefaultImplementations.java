/*
  File: DefaultImplementations.java

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
 * DefaultImplementations contains only static methods
 * that make reasonable default choices for implementations
 * of the principal updatable collection types.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class DefaultImplementations {

  public static UpdatableSet   set()         {
    return new HashedSet();
  }
  public static UpdatableBag   bag()         {
    return new LinkedBuffer();
  }
  public static UpdatableBag   sortedBag()   {
    return new RBTree();
  }
  public static UpdatableSeq   seq()         {
    return new Dynarray();
  }
  public static UpdatableSeq   seqForQueue() {
    return new CircularList();
  }
  public static UpdatableMap   map()         {
    return new HashedMap();
  }
  public static UpdatableMap   sortedMap()   {
    return new RBMap();
  }

/*  TODO

 * It would be much better to use Class.forName to avoid pulling
 * in implementations that aren't needed. But these could fail?
 * Consider options...

  public static UpdatableSet   set()         {
    return (UpdatableSet)(Class.forName("HashedSet").newInstance());
  }
  public static UpdatableBag   bag()         {
    return (UpdatableBag)(Class.forName("LinkedBuffer").newInstance());
  }
  public static UpdatableBag   sortedBag()   {
    return (UpdatableSet)(Class.forName("RBTree").newInstance());
  }
  public static UpdatableSeq   seq()         {
    return (UpdatableSeq)(Class.forName("Dynarray").newInstance());
  }
  public static UpdatableSeq   seqForQueue() {
    return (UpdatableSeq)(Class.forName("CircularList").newInstance());
  }
  public static UpdatableMap   map()         {
    return (UpdatableMap)(Class.forName("HashedMap").newInstance());
  }
  public static UpdatableMap   sortedMap()   {
    return (UpdatableMap)(Class.forName("RBMap").newInstance());
  }
*/
}

