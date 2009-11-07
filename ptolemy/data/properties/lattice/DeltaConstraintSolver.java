package ptolemy.data.properties.lattice;

import java.util.List;

import ptolemy.actor.TypeConflictException;
import ptolemy.data.properties.PropertyResolutionException;
import ptolemy.data.properties.lattice.PropertyConstraintHelper.Inequality;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class DeltaConstraintSolver extends PropertyConstraintSolver {

    public DeltaConstraintSolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Resolve the property values for the toplevel entity that contains this
     * solver, given the model analyzer that invokes this.
     * @param analyzer The given model analyzer.
     */
    protected void _resolveProperties(NamedObj analyzer) throws KernelException {
//        super._resolveProperties(analyzer);

        NamedObj toplevel = _toplevel();
        PropertyConstraintHelper toplevelHelper = (PropertyConstraintHelper) getHelper(toplevel);

        toplevelHelper.reinitialize();

        toplevelHelper
                ._addDefaultConstraints(_getConstraintType(actorConstraintType
                        .stringValue()));

        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        toplevelHelper._setConnectionConstraintType(
                _getConstraintType(connectionConstraintType.stringValue()),
                _getConstraintType(compositeConnectionConstraintType
                        .stringValue()), _getConstraintType(fsmConstraintType
                        .stringValue()),
                _getConstraintType(expressionASTNodeConstraintType
                        .stringValue()));
        
        // Collect and solve type constraints.
        List<Inequality> constraintList = toplevelHelper.constraintList();

        try {
            _resolveProperties(toplevel, toplevelHelper, constraintList);
            checkResolutionErrors();
        } catch (TypeConflictException ex) {
            System.err.println("Found TypeConflictException in DeltaConstraintSolver");
            throw ex;
        } catch (PropertyResolutionException ex) {
            System.err.println("Found PropertyResolutionException in DeltaConstraintSolver");
            throw ex;   
        }
    }

}
