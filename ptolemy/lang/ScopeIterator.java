/* An iterator for declarations from an scope. Instead of looking up
all matches of a declaration at once, declarations are found on an
as-needed basis.

Copyright (c) 1998-2001 The Regents of the University of California.
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

/**  An iterator for declarations from an scope. Instead of looking up
all matches of a declaration at once, declarations are found on an
as-needed basis.
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay
@version $Id$
 */
public class ScopeIterator implements Iterator {

    public ScopeIterator() {
        _nextScope = null;
        _declIter = null;
        _name = null;
        _mask = 0;
    }

    public ScopeIterator(Scope nextScope, ListIterator declIter, String name,
            int mask) {
        _nextScope = nextScope;
        _declIter = declIter;
        _name = name;
        _mask = mask;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public boolean hasNext() {
        //System.out.println("ScopeIterator : hasNext for " + _name);

        try {
            nextDecl();

            // Rewind to valid Decl.
            _declIter.previous();
        } catch (NoSuchElementException e) {
            //System.out.println("ScopeIterator : hasNext for " + _name +
            // " = false");
            return false;
        }
        //System.out.println("ScopeIterator : hasNext for " + _name +
        // " = true");
        return true;
    }

    /** Return the next Decl. */
    public Object next() {
        return nextDecl();
    }

    /** Return the next Decl. */
    public Decl nextDecl() {

        if (_declIter == null) {
            throw new NoSuchElementException("No elements in ScopeIterator.");
        }

        do {

            while (_declIter.hasNext()) {
                Decl decl = (Decl) _declIter.next();

                if (decl.matches(_name, _mask)) {
                    //System.out.println("ScopeIterator : found match " +
                    //" for " +  _name);
                    return decl;
                }
            }

            if (_nextScope == null) {
                //System.out.println("ScopeIterator : no more elements " +
                //  "looking for " + _name);

                throw new NoSuchElementException(
                        "No more elements in ScopeIterator.");
            }

            //System.out.println("ScopeIterator : going to next " +
            // "scope looking for " + _name);

            _declIter = _nextScope.allLocalDecls();
            _nextScope = _nextScope.parent();

        } while (true);
    }

    /** Return true if there is more than one matching Decl that
     *  can be reached.
     */
    public boolean moreThanOne() {

        //System.out.println("ScopeIterator: moreThanOne for " + _name);
        if (_declIter == null) {
            // empty list
            return false;
        }

        Decl lastMatch = null;
        int movesAfterMatch = 0;
        int matches = 0;

        while (_declIter.hasNext() && (matches < 2)) {

            Decl d = (Decl) _declIter.next();

            // make sure we don't have a reference to the last found match
            if (d.matches(_name, _mask) && (d != lastMatch)) {
                matches++;
                lastMatch = d;
            }

            if (matches > 0) {
                movesAfterMatch++;
            }
        }

        if (matches >= 1) {

            // rewind back to first matching Decl
            for (; movesAfterMatch > 0; movesAfterMatch--) {
                _declIter.previous();
            }

            if (matches >= 2) {
                //System.out.println("ScopeIterator: moreThanOne = true" +
                //        " for " + _name);
                return true;
            }

            if (_nextScope == null) {
                // just one match
                return false;
            }

            ScopeIterator nextScopeIterator =
                _nextScope.lookupFirst(_name, _mask);

            while (nextScopeIterator.hasNext()) {
                Decl nextMatch = nextScopeIterator.nextDecl();

                // make sure we don't have a reference to the last found match
                if (lastMatch != nextMatch) {
                    return true;
                }
            }

            return false;

        } else {
            // matches == 0
            // don't bother to move the iterator back, since there are
            // no matches

            if (_nextScope == null) {
                //System.out.println("ScopeIterator: moreThanOne = " +
                // false for " +  _name);
                return false;
            }

            // move on to the next scope, discarding last scope

            _declIter = _nextScope.allLocalDecls();
            _nextScope = _nextScope.parent();

            // try again on this modified ScopeIterator
            return moreThanOne();
        }
    }


    /** Return the first Decl without advancing the iterator */
    public Decl peek() {
        Decl returnValue = nextDecl();

        // Rewind back to valid Decl.
        _declIter.previous();

        return returnValue;
    }

    /** Throw a RuntimeException, because we do not support the optional
     *  remove() method of the Iterator interface.
     */
    public void remove() {
        // Can't do this!!!
        throw new RuntimeException("remove() not supported on ScopeIterator");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected Scope _nextScope;
    protected ListIterator _declIter;
    protected String _name;
    protected int _mask;
}
