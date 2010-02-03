package ptolemy.data.ontologies;

import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Specialized Inequality class for the LatticeOntologyAdapter class.
 */
public class OntologyInequality extends ptolemy.graph.Inequality {

    /** The constructor for the Inequality constraint.
     *  @param ontologyAdapter The adapter that generated this constraint.
     *  @param lesserTerm The lesser term of the Inequality
     *  @param greaterTerm The greater term of the Inequality
     */
    public OntologyInequality(
            OntologyAdapter ontologyAdapter, 
            InequalityTerm lesserTerm,
            InequalityTerm greaterTerm) {
        super(lesserTerm, greaterTerm);
        _adapter = ontologyAdapter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the OntologyAdapter associated with this Inequality.
     * 
     * @return The associated OntologyAdapter
     */
    public OntologyAdapter getHelper() {
        return _adapter;
    }

    /**
     * Test if this inequality is satisfied with the current value of
     * variables.
     * @param cpo A CPO over which this inequality is defined.
     * @return True if this inequality is satisfied; false otherwise.
     * @exception IllegalActionException If thrown while getting the value
     * of the terms.
     */
    public boolean isSatisfied(CPO cpo) throws IllegalActionException {
        InequalityTerm lesserTerm = getLesserTerm();
        InequalityTerm greaterTerm = getGreaterTerm();

        if (lesserTerm.getValue() == null) {
            return true;
        } else if (greaterTerm.getValue() == null) {
            return false;
        }

        return super.isSatisfied(cpo);
    }

    private final OntologyAdapter _adapter;
}