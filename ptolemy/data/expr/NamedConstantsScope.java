/* A class representing an evaluation scope that contains a set of named
constants.

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

import ptolemy.data.type.TypeConstant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// NamedConstantsScope
/**
An implementation of ParserScope that contains a map from names to value
tokens.

@author Xiaojun Liu
@version $Id$
@since Ptolemy II 2.1
*/

public class NamedConstantsScope implements ParserScope {

    /** Construct a new scope that contains the given map from names to
     *  value tokens.
     */
    public NamedConstantsScope(Map map) {
        _map = map;
    }

    /** Look up and return the value with the specified name in the
     *  scope. Return null if the name is not defined in this scope.
     *  @return The token associated with the given name in the scope.
     */
    public ptolemy.data.Token get(String name) {
        ptolemy.data.Token result = (ptolemy.data.Token)_map.get(name);
        return result;
    }

    /** Look up and return the type of the value with the specified
     *  name in the scope. Return null if the name is not defined in
     *  this scope.
     *  @return The token associated with the given name in the scope.
     */
    public ptolemy.data.type.Type getType(String name) {
        ptolemy.data.Token value = (ptolemy.data.Token)_map.get(name);
        if (value == null) {
            return null;
        } else {
            return value.getType();
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
    public ptolemy.graph.InequalityTerm getTypeTerm(String name) {
        ptolemy.data.Token value = (ptolemy.data.Token)_map.get(name);
        if (value == null) {
            return null;
        } else {
            return new TypeConstant(value.getType());
        }
    }

    /** Return the set of identifiers defined in this scope.
     *  @return A set containing the key defined in the map.
     */
    public Set identifierSet() {
        Set set = new HashSet();
        set.addAll(_map.keySet());
        return set;
    }

    private Map _map;
}

