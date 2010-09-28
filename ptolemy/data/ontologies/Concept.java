package ptolemy.data.ontologies;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Flowable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public abstract class Concept extends ComponentEntity implements InequalityTerm {
    
    /** Create a new concept with the specified name and the specified
     *  ontology.
     *  
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public Concept(Ontology ontology, String name)
            throws IllegalActionException, NameDuplicationException {
        super(ontology, name);

        _name = name;

        isAcceptable = new Parameter(this, "isAcceptable");
        isAcceptable.setTypeEquals(BaseType.BOOLEAN);
        isAcceptable.setExpression("true");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                   parameters and ports                    ////
    
    /** A parameter indicating whether this concept is an acceptable outcome
     *  during inference. This is a boolean that defaults to true.
     */
    public Parameter isAcceptable;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return null.
     *  For variable InequaliyTerms, this method will return a reference to the
     *  model object associated with that InequalityTerm. For concepts,
     *  there is no associated model object, hence returning null is the
     *  right thing to do.
     *  
     *  @return Null.
     */
    public Object getAssociatedObject() {
        return null;
    }
    
    public abstract Ontology getOntology();

    /** Return the current value of the inequality term. Since a concept
     *  is a constant, not a variable, its value is just itself.
     *  
     *  @return This concept.
     *  @see #setValue
     */
    public Object getValue() {
        return this;
    }

    /** Return an array of variables contained in this term, or in this
     *  case, an empty array. A concept is a single constant, so
     *  it has no variables.
     *  @return An empty array.
     */
    public InequalityTerm[] getVariables() {
        return _EMPTY_ARRAY;
    }

    /** Throw an exception. This object is not a variable.
     * 
     *  @param object The object used to initialize the InequalityTerm; not used
     *  since a Concept is a static value that cannot be initialized.
     *  @exception IllegalActionException Always thrown.
     */
    public void initialize(Object object) throws IllegalActionException {
        throw new IllegalActionException(this,
                "Cannot initialize an ontology concept.");
    }

    /** Return true if this concept is greater than or equal to the
     *  specified concept in the partial ordering.
     *  @param concept The concept to compare.
     *  @return True if this concept is greater than or equal to the
     *   specified concept.
     *  @exception IllegalActionException If the specified concept
     *   does not have the same ontology as this one.
     */
    public abstract boolean isAboveOrEqualTo(Concept concept) throws IllegalActionException;

    /** Return false, because this inequality term is a constant.
     *  @return False.
     */
    public boolean isSettable() {
        return false;
    }

    /** Return whether this concept is a valid inference result. This method is required
     *  to implement the InequalityTerm interface, but we do not want to use this method
     *  going forward for ontology inferences. Acceptability criteria should be of the form
     *  variable <= Concept. Acceptability criteria prevent a variable from being promoted
     *  in the ontology lattice.
     *  @return True, if this concept is a valid result of inference.
     *   False, otherwise.
     */
    public boolean isValueAcceptable() {
        try {
            return ((BooleanToken) isAcceptable.getToken()).booleanValue();
        } catch (IllegalActionException e) {
            // If isAcceptable parameter cannot be read, fallback to
            // assumption that value is acceptable.
            return true;
        }
    }

    /** Throw an exception. This object is not a variable.
     * 
     *  @param value The Object being passed in to set the value for the
     *  InequalityTerm; not used since a Concept is a static value that
     *  cannot be changed.
     *  @exception IllegalActionException Always thrown.
     *  @see #getValue
     */
    public void setValue(Object value) throws IllegalActionException {
        throw new IllegalActionException(this,
                "Cannot set an ontology concept.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////
    
    /**
     * The name of this Concept.
     */
    protected String _name;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Empty array. */
    private static InequalityTerm[] _EMPTY_ARRAY = new InequalityTerm[0];
    
}
