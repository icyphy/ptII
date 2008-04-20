/**
 * 
 */
package ptolemy.data.properties.lattice;

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintHelper.Inequality;
import ptolemy.data.properties.util.MultiHashMap;
import ptolemy.data.properties.util.MultiMap;
import ptolemy.graph.InequalityTerm;

/**
 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */

public class ConstraintManager {
    
    public ConstraintManager(PropertyConstraintSolver solver) {
        _solver = solver;
    }
    
    /**
     * 
     * @param constraints
     */
    public void setConstraints(List<Inequality> constraints) {
        for (Inequality constraint : constraints) {
            if (constraint.isBase()) {
                InequalityTerm greaterTerm = constraint.getGreaterTerm();
                InequalityTerm lesserTerm = constraint.getLesserTerm();
                
                _greaterTermMap.put(greaterTerm, lesserTerm);
                _lesserTermMap.put(lesserTerm, greaterTerm);
            }
        }
    }
    
    /**
     * 
     * @param object
     * @return
     */
    public List<PropertyTerm> getConstraintingTerms(Object object) {        
        boolean least = _solver.solvingFixedPoint.getExpression().equals("least");
        
        if (least) {
            return (List<PropertyTerm>) _greaterTermMap.get(_solver.getPropertyTerm(object));
        } else {
            return (List<PropertyTerm>) _lesserTermMap.get(_solver.getPropertyTerm(object));            
        }
    }
    

    /** The property constraint solver that uses this manager. */
    private PropertyConstraintSolver _solver;

    /** The multi-map of the greater terms (key) to the lesser terms (values). */
    private MultiMap _greaterTermMap = new MultiHashMap();

    /** The multi-map of the lesser terms (key) to the greater terms (values). */
    private MultiMap _lesserTermMap = new MultiHashMap();
}

