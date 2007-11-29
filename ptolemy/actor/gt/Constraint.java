/**
 *
 */
package ptolemy.actor.gt;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 * @author tfeng
 *
 */
public class Constraint extends ParameterAttribute {

    /**
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public Constraint(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    public Constraint(Workspace workspace) {
        super(workspace);
    }

    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            _checkContainerClass(container, Pattern.class, false);
        }
    }

    protected void _initParameter() throws IllegalActionException,
    NameDuplicationException {
        parameter = new ConstraintParameter(this, "constraint");
    }
    
    public static class ConstraintParameter extends Parameter {
    	
    	public ConstraintParameter(NamedObj container, String name)
    	throws IllegalActionException, NameDuplicationException {
    		super(container, name);
    		setTypeEquals(BaseType.BOOLEAN);
    	}
    	
    	protected void _evaluate() throws IllegalActionException {
    		super._evaluate();
    	}
    }

}
