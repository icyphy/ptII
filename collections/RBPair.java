/*
  File: RBPair.java

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
 * RBPairs are RBCells with keys.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class RBPair extends RBCell implements Pair {

// instance variable

  private Object   key_;

/**
 * Make a cell with given key and element values, and null links
**/

  public RBPair(Object k, Object v)           { super(v); key_ = k; }

/**
 * Make a new node with same key and element values, but null links
**/

  protected Object clone() throws CloneNotSupportedException {
    RBPair t = new RBPair(key_, element());
    t.color_ = color_;
    return t;
  }

/**
 * return the key
**/

  public final Object key()           { return key_; }

/**
 * set the key
**/

  public final void   key(Object k)   { key_ = k; }

/**
 * Implements RBCell.find.
 * Override RBCell version since we are ordered on keys, not elements, so
 * element find has to search whole tree.
 * comparator argument not actually used.
 * @see RBCell#find
**/

  public RBCell find(Object element, Comparator cmp) {

    RBCell t = this;
    while (t != null) {
      if (t.element().equals(element)) return t;
      else if (t.right_ == null)
        t = t.left_;
      else if (t.left_ == null)
        t = t.right_;
      else {
        RBCell p = t.left_.find(element, cmp);
        if (p != null) return p;
        else
          t = t.right_;
      }
    }
    return null; // not reached
  }

/**
 * Implements RBCell.count.
 * @see RBCell#count
**/
  public int count(Object element, Comparator cmp) {
    int c = 0;
    RBCell t = this;
    while (t != null) {
      if (t.element().equals(element)) ++c;
      if (t.right_ == null)
        t = t.left_;
      else if (t.left_ == null)
        t = t.right_;
      else {
        c += t.left_.count(element, cmp);
        t = t.right_;
      }
    }
    return c;
  }

/**
 * find and return a cell holding key, or null if no such
**/

  public RBPair findKey(Object key, Comparator cmp) {
    RBPair t = this;
    for (;;) {
      int diff = cmp.compare(key, t.key_);
      if (diff == 0) return t;
      else if (diff < 0) t = (RBPair)(t.left_);
      else t = (RBPair)(t.right_);
      if (t == null) return null;
    }
  }

/**
 * find and return a cell holding (key, element), or null if no such
**/
  public RBPair find(Object key, Object element, Comparator cmp) {
    RBPair t = this;
    for (;;) {
      int diff = cmp.compare(key, t.key_);
      if (diff == 0 && t.element().equals(element)) return t;
      else if (diff <= 0) t = (RBPair)(t.left_);
      else t = (RBPair)(t.right_);
      if (t == null) return null;
    }
  }

/**
 * return number of nodes of subtree holding key
**/
  public int countKey(Object key, Comparator cmp) {
    int c = 0;
    RBPair t = this;
    while (t != null) {
      int diff = cmp.compare(key, t.key_);
      // rely on insert to always go left on <=
      if (diff == 0) ++c;
      if (diff <= 0) t = (RBPair)(t.left_);
      else t = (RBPair)(t.right_);
    }
    return c;
  }

/**
 * return number of nodes of subtree holding (key, element)
**/
  public int count(Object key, Object element, Comparator cmp) {
    int c = 0;
    RBPair t = this;
    while (t != null) {
      int diff = cmp.compare(key, t.key_);
      if (diff == 0) {
        if (t.element().equals(element)) ++c;
        if (t.left_ == null)
          t = (RBPair)(t.right_);
        else if (t.right_ == null)
          t = (RBPair)(t.left_);
        else {
          c += ((RBPair)(t.right_)).count(key, element, cmp);
          t = (RBPair)(t.left_);
        }
      }
      else if (diff < 0) t = (RBPair)(t.left());
      else t = (RBPair)(t.right());
    }
    return c;
  }
}

