/**
 * 
 */
package ptolemy.data.properties;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 * @author eal
 *
 */
public class PropertyConstraintSolver extends Attribute {
    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public PropertyConstraintSolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        latticeClassName = new StringParameter(this, "latticeClassName");
        latticeClassName.setExpression("ptolemy.data.properties.PropertyLattice");
    }

    StringParameter latticeClassName;
    
    public void resolveProperties(CompositeEntity toplevel) {
        
    }
}
