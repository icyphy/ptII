package ptolemy.data.properties.lattice;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
        List<Inequality> errorList = constraintList;
//        boolean exception = false;
        // _resolveProperties;
        // if (exception) {
        //   errorList = constraintList
        //   size = errorList.size()/2
        //   while (size > 1) {
        //     for (testList of size size in errorList) (errorlist[0:size] , errorlist[size+1, 2*size], ...) {
        //       if _resolveProperties(testList) == error:
        //          errorList = testList;
        //          size = errorList.size()/2
        //          continue while loop;
        //     }
        //     size = size/2;
        //   }
        // }
        List<Inequality> testList = errorList;
        boolean exception = false;
        
        try {
            //modify the list of constraints
           _resolveProperties(toplevel, toplevelHelper, testList);
           checkResolutionErrors();
        } catch (TypeConflictException ex) {
            System.err.println("Found TypeConflictException in DeltaConstraintSolver");
            throw ex;
        } catch (PropertyResolutionException ex) {
            System.err.println("Found PropertyResolutionException in DeltaConstraintSolver");
            //              blockSize /= 2;
            exception = true;
        }
          
        if(!exception)
            return;
        
        int blockSize = errorList.size()/2;
        
WHILE_LOOP:        
        while(blockSize >= 1) {
            
            for(int i = 0;  i < errorList.size(); i += blockSize) {
                try {
                  //modify the list of constraints
                    
                    Set<Inequality> tmpSet = new HashSet(errorList.subList(i, Math.min(errorList.size(), i+blockSize)));
                    testList = new LinkedList(errorList);
                    testList.removeAll(tmpSet);
                    if(testList.size() > 0) {
                        _resolveProperties(toplevel, toplevelHelper, testList);
                        checkResolutionErrors();
                    }
                } catch (TypeConflictException ex) {
                    System.err.println("Found TypeConflictException in DeltaConstraintSolver");
                    throw ex;
                } catch (PropertyResolutionException ex) {
                    System.err.println("Found PropertyResolutionException in DeltaConstraintSolver");
        //            throw ex;  
                    
                    errorList = testList;
//                    blockSize /= 2;
                    
                    continue WHILE_LOOP;
                }
            }
            
            blockSize /= 2;
        }
        
        System.out.println(errorList);
        
    }

}
