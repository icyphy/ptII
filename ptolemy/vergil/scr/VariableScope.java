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
	
	private FSMActor _model;
	
	public VariableScope(FSMActor model) {
		_model = model;
	}
	
    /** Look up and return the attribute with the specified name in the
     *  scope. Return null if such an attribute does not exist.
     *  @return The attribute with the specified name in the scope.
     */
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
     */
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
     *  @return The InequalityTerm associated with the given name in
     *  the scope.
     *  @exception IllegalActionException If a value in the scope
     *  exists with the given name, but cannot be evaluated.
     */
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
    public Set identifierSet() {
        return getAllScopedVariableNames(null, _model);
    }
}
