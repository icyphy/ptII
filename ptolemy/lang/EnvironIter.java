/*
An iterator for declarations from an environment. Instead of looking up
all matches of a declaration at once, declarations are found on an
as-needed basis. Inspired by Paul Hilfinger's EnvironIter in Titanium.

Copyright (c) 1998-1999 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/


package ptolemy.lang;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class EnvironIter implements Iterator {

  // no elements
  public EnvironIter() {
    _nextEnviron = null;
    _declIter = null;
    _name = null;
    _mask = 0;
  }

  public EnvironIter(Environ nextEnviron, ListIterator declIter, String name,
   int mask) {
    _nextEnviron = nextEnviron;
    _declIter = declIter;
    _name = name;
    _mask = mask;
  }

  public boolean hasNext() {
     try {
       nextDecl();

       // rewind to valid Decl
       _declIter.previous();
     } catch (NoSuchElementException e) {
       return false;
     }
     return true;
  }

  public Object next() {
    return (Object) nextDecl();
  }

  public Decl nextDecl() {

     if (_declIter == null) {
        throw new NoSuchElementException("No elements in EnvironIter.");
     }

     do {

        while (_declIter.hasNext()) {
           Decl decl = (Decl) _declIter.next();

           if (decl.matches(_name, _mask)) {
              return decl;
           }
        }

        if (_nextEnviron == null) {
           throw new NoSuchElementException("No more elements in EnvironIter.");
        }

        _declIter = _nextEnviron.allProperDecls();
        _nextEnviron = _nextEnviron.parent();

     } while (true);
  }

  public Decl head() {
    Decl retval = nextDecl();

    // rewind back to valid Decl
    _declIter.previous();

    return retval;
  }

  public boolean moreThanOne() {

    // a naive way of testing for more than one
    // a better way would be to rewind to the first element that matched,
    // instead of rewinding to the starting point

    if (_declIter == null) {
       // empty list
       return false;
    }

    int moves = 0;
    int matches = 0;

    while (_declIter.hasNext() && (matches < 2)) {

      Decl d = (Decl) _declIter.next();
      moves++;

      if (d.matches(_name, _mask)) {
         matches++;
      }
    }

    if (matches >= 1) {
       // rewind back to starting point
       for (; moves > 0; moves--) {
           _declIter.previous();
       }

       if (matches >= 2) {
          return true;
       }

    } else {
       // matches == 0

       if (_nextEnviron == null) {
          return false;
       }

       _declIter = _nextEnviron.allProperDecls();
       _nextEnviron = _nextEnviron.parent();
       return moreThanOne();
    }

    // matches == 1

    if (_nextEnviron == null) {
       return false;
    }

    return (_nextEnviron.lookup(_name, _mask) != null);
  }

  public void remove() {
    // Can't do this!!!
    throw new RuntimeException("remove() not supported on EnvionIter");
  }

  protected Environ _nextEnviron;
  protected ListIterator _declIter;
  protected String _name;
  protected int _mask;
  protected Decl _cacheDecl = null;
}
