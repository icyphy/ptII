/* A class representing an explict evaluation scope.

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
import ptolemy.kernel.util.NamedList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// ExplicitScope
/**
An implementation of ParserScope that includes an explicit list of
Variables in the scope.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
*/

public class ExplicitScope implements ParserScope {

    /** Construct a new scope that includes the objects in the given
     *  list, which must contain only variables.
     */
    public ExplicitScope(NamedList list) {
        _list = list;
    }

    /** Look up and return the value with the specified name in the
     *  scope. Return null if the name is not defined in this scope.
     *  @return The token associated with the given name in the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    public ptolemy.data.Token get(String name) throws IllegalActionException {
        Variable variable = (Variable)_list.get(name);
        if (variable == null) {
            return null;
        }
        return variable.getToken();
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
        Variable variable = (Variable)_list.get(name);
        if (variable == null) {
            return null;
        } else {
            return variable.getType();
        }
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
        Variable variable = (Variable)_list.get(name);
        if (variable == null) {
            return null;
        } else {
            return variable.getTypeTerm();
        }
    }

    /** Return the list of variables within the scope.
     *  @return The list of variables within the scope.
     */
    public Set identifierSet() {
        Set set = new HashSet();
        for (Iterator variables = _list.elementList().iterator();
            variables.hasNext();) {
            Variable variable = (Variable)variables.next();
            set.add(variable.getName());
        }
        return set;
    }

    /** Return the list of variables in this scope.
     */
    public NamedList variableList() {
        return _list;
    }

    private NamedList _list;
}

