/* An evaluation scope that consists of a list of nested scopes.

 Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (liuxj@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

import ptolemy.graph.InequalityTerm;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// NestedScope
/**
An implementation of ParserScope that consists of a list of nested scopes.
A lookup starts from the first scope in the list, and proceeds through the
list until a mapping is found.

@author Xiaojun Liu
@version $Id$
@since Ptolemy II 2.1
*/

public class NestedScope implements ParserScope {

    /** Construct a new scope that consists of the given list of scopes.
     */
    public NestedScope(List scopeList) {
        _scopeList = scopeList;
    }

    /** Look up and return the value with the specified name in the
     *  scope. Start from the first scope in the list supplied to the
     *  constructor. If a value is found, return the value, otherwise
     *  continue to look up through the list. Return null if no mapping
     *  is defined in any scope in the list.
     *  @return The token associated with the given name in the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    public ptolemy.data.Token get(String name) throws IllegalActionException {
        Iterator scopes = _scopeList.iterator();
        while (scopes.hasNext()) {
            ParserScope scope = (ParserScope)scopes.next();
            ptolemy.data.Token result = scope.get(name);
            if (result != null)
                return result;
        }
        return null;
    }

    /** Look up and return the type of the value with the specified
     *  name in the scope. Return null if the name is not defined in
     *  this scope.
     *  @return The token associated with the given name in the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    public ptolemy.data.type.Type getType(String name)
            throws IllegalActionException {
        Iterator scopes = _scopeList.iterator();
        while (scopes.hasNext()) {
            ParserScope scope = (ParserScope)scopes.next();
            ptolemy.data.type.Type result = scope.getType(name);
            if (result != null)
                return result;
        }
        return null;
    }

    /** Look up and return the type term for the specified name
     *  in the scope. Return null if the name is not defined in this
     *  scope, or is a constant type.
     *  @return The InequalityTerm associated with the given name in
     *  the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    public ptolemy.graph.InequalityTerm getTypeTerm(String name)
            throws IllegalActionException {
        Iterator scopes = _scopeList.iterator();
        while (scopes.hasNext()) {
            ParserScope scope = (ParserScope)scopes.next();
            InequalityTerm result = scope.getTypeTerm(name);
            if (result != null)
                return result;
        }
        return null;
    }

    /** Return the list of identifiers within the scope.
     *  @return The union of the identifierSets of the contained scopes.
     *  @exception IllegalActionException If the identifierSet()
     *  method of a contained scope throws it.
     */
    public Set identifierSet() throws IllegalActionException {
        Set set = new HashSet();
        for (Iterator scopes = _scopeList.iterator();
            scopes.hasNext();) {
            ParserScope scope = (ParserScope) scopes.next();
            set.addAll(scope.identifierSet());
        }
        return set;
    }

    private List _scopeList;
}

