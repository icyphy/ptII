/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package ptolemy.vergil.scr;

import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.kernel.util.IllegalActionException;

/**
 * VariableScope class.
 *
 * @author pd
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class VariableScope extends ModelScope {

    /** Construct a VariableScope for a model.
     *  @param model The model.
     */
    public VariableScope(FSMActor model) {
        _model = model;
    }

    /** Look up and return the attribute with the specified name in the
     *  scope. Return null if such an attribute does not exist.
     *  @return The attribute with the specified name in the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    @Override
    public Token get(String name) throws IllegalActionException {
        if (name.equals("time")) {
            return new DoubleToken(_model.getDirector().getModelTime()
                    .getDoubleValue());
        }

        Variable result = getScopedVariable(null, _model, name);

        if (result != null) {
            return result.getToken();
        }

        return null;
    }

    /** Look up and return the type of the attribute with the
     *  specified name in the scope. Return null if such an
     *  attribute does not exist.
     *  @return The attribute with the specified name in the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
    @Override
    public Type getType(String name) throws IllegalActionException {
        if (name.equals("time")) {
            return BaseType.DOUBLE;
        }

        // Check the port names.
        TypedIOPort port = (TypedIOPort) _model.getPort(name);

        if (port != null) {
            return port.getType();
        }

        Variable result = getScopedVariable(null, _model, name);

        if (result != null) {
            return (Type) result.getTypeTerm().getValue();
        }

        return null;
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
    public ptolemy.graph.InequalityTerm getTypeTerm(String name)
            throws IllegalActionException {
        if (name.equals("time")) {
            return new TypeConstant(BaseType.DOUBLE);
        }

        // Check the port names.
        TypedIOPort port = (TypedIOPort) _model.getPort(name);

        if (port != null) {
            return port.getTypeTerm();
        }

        Variable result = getScopedVariable(null, _model, name);

        if (result != null) {
            return result.getTypeTerm();
        }

        return null;
    }

    /** Return the list of identifiers within the scope.
     *  @return The list of identifiers within the scope.
     */
    @Override
    public Set identifierSet() {
        return getAllScopedVariableNames(null, _model);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private FSMActor _model;
}
