/* The parser scope that resolves names as attributes of a given container and
those of a superscope.

@Copyright (c) 2007-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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



 */

package ptolemy.domains.ptera.kernel;

import java.util.Set;

import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// VariableScope

/**
 The parser scope that resolves names as attributes of a given container and
 those of a superscope. To resolve a name, it first tries to find an attribute
 belonging to the container given to its constructor. If not found, it passes
 the name resolution request to the superscope, if given.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class VariableScope extends ModelScope {

    /** Construct a scope for the given container without a superscope.
     *
     *  @param container The container in which attributes are looked up.
     */
    public VariableScope(NamedObj container) {
        this(container, null);
    }

    /** Construct a scope for the given container with a superscope.
     *
     *  @param container The container in which attributes are looked up.
     *  @param superScope If not null, the scope to search if no attribute of
     *  a given name can be found in the container.
     */
    public VariableScope(NamedObj container, ParserScope superScope) {
        _container = container;
        _superScope = superScope;
    }

    /** Look up and return the value with the specified name in the
     *  scope. Return null if the name is not defined in this scope.
     *  @param name The name of the variable to be looked up.
     *  @return The token associated with the given name in the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    @Override
    public Token get(String name) throws IllegalActionException {
        Variable result = getScopedVariable(null, _container, name);

        if (result != null) {
            return result.getToken();
        } else if (_superScope != null) {
            return _superScope.get(name);
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
        Variable result = getScopedVariable(null, _container, name);

        if (result != null) {
            return result.getType();
        } else if (_superScope != null) {
            return _superScope.getType(name);
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
        Variable result = getScopedVariable(null, _container, name);

        if (result != null) {
            return result.getTypeTerm();
        } else if (_superScope != null) {
            return _superScope.getTypeTerm(name);
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
    public Set<?> identifierSet() throws IllegalActionException {
        Set<String> set = getAllScopedVariableNames(null, _container);
        if (_superScope != null) {
            set.addAll(_superScope.identifierSet());
        }
        return set;
    }

    /** The container in which attributes are looked up. */
    private NamedObj _container;

    /** The superscope. */
    private ParserScope _superScope;

}
