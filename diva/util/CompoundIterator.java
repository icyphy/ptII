/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;

import java.util.Iterator;

/**
 * An iterator over two iterators.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public class CompoundIterator implements Iterator {
  // The iterators
  private Iterator _first;
  private Iterator _second;
  private boolean _onFirst = true;

  /**
   * Create a new iterator
   */
  public CompoundIterator (Iterator first, Iterator second) {
    this._first = first;
    this._second = second;
  }

  public boolean hasNext() {
    if (_onFirst) {
      if (_first.hasNext()) {
        return true;
      } else {
        _onFirst = false;
        return (_second.hasNext());
      }
    } else {
      return (_second.hasNext());
    }
  }
  public Object next() {
    if (_onFirst) {
      if (_first.hasNext()) {
        return _first.next();
      } else {
        _onFirst = false;
        return (_second.next());
      }
    } else {
      return (_second.next());
    }
  }
  public void remove() {
    if (_onFirst) {
      _first.remove();
    } else {
      _second.remove();
    }
  }
}


