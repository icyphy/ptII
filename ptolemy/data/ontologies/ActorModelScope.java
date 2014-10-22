/* A Ptolemy expression language parser scope that refers to a specific actor
 * and its contained elements.
 *
 * Copyright (c) 2010-2014 The Regents of the University of California. All
 * rights reserved.
 *
 * Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 *
 */

package ptolemy.data.ontologies;

import java.util.Set;

import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ActorModelScope

/** A Ptolemy expression language parser scope that refers to a specific actor
 *  and its contained elements.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ActorModelScope extends ModelScope {

    /** Create a new ActorModelScope for the given Ptolemy NamedObj element.
     *  @param modelObject The Ptolemy NamedObj which defines the expression
     *   parser model scope.
     */
    public ActorModelScope(NamedObj modelObject) {
        _modelObject = modelObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Look up and return the value with the specified name in the
     *  scope. Return null if the name is not defined in this scope.
     *  @param name The name of the variable to be looked up.
     *  @return The token associated with the given name in the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    @Override
    public Token get(String name) throws IllegalActionException {
        if (_modelObject != null) {
            NamedObj element = getScopedObject(_modelObject, name);
            if (element != null) {
                return new ObjectToken(element, element.getClass());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /** Look up and return the type of the value with the specified
     *  name in the scope. Return null if the name is not defined in
     *  this scope.
     *  @param name The name of the variable to be looked up.
     *  @return The token associated with the given name in the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    @Override
    public Type getType(String name) throws IllegalActionException {
        Token result = get(name);
        if (result != null) {
            return result.getType();
        } else {
            return null;
        }
    }

    /** Look up and return the type term for the specified name
     *  in the scope. Return null if the name is not defined in this
     *  scope, or is a constant type.
     *  @param name The name of the variable to be looked up.
     *  @return The InequalityTerm associated with the given name in
     *  the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    @Override
    public InequalityTerm getTypeTerm(String name)
            throws IllegalActionException {
        NamedObj result;

        if (_modelObject != null) {
            result = getScopedObject(_modelObject, name);
            if (result != null && result instanceof Typeable) {
                return ((Typeable) result).getTypeTerm();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /** Return a list of names corresponding to the identifiers
     *  defined by this scope.  If an identifier is returned in this
     *  list, then get() and getType() will return a value for the
     *  identifier.  Note that generally speaking, this list is
     *  extremely expensive to compute, and users should avoid calling
     *  it.  It is primarily used for debugging purposes.
     *  @return A list of names corresponding to the identifiers
     *  defined by this scope.
     *  @exception IllegalActionException If constructing the list causes
     *  it.
     */
    @Override
    public Set<String> identifierSet() throws IllegalActionException {
        return getAllScopedObjectNames(_modelObject);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Ptolemy NamedObj that defined the expression parser model element
     *  scope.
     */
    NamedObj _modelObject;
}
