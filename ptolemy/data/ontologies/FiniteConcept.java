/**
 * An element in an ontology.
 * 
 * Copyright (c) 2007-2010 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 * 
 * 
 */
package ptolemy.data.ontologies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Flowable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Concept

/**
 * A concept in an ontology.
 * An instance of this class is always associated with
 * a particular ontology, which is specified in the constructor.
 * 
 * @author Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (blickly)
 * @Pt.AcceptedRating Red (blickly)
 */
public class FiniteConcept extends ComponentEntity implements InequalityTerm,
        Flowable {

    /** Create a new concept with the specified name and the specified
     *  ontology.
     *  
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public FiniteConcept(CompositeEntity ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);

        _name = name;

        isAcceptable = new Parameter(this, "isAcceptable");
        isAcceptable.setTypeEquals(BaseType.BOOLEAN);
        isAcceptable.setExpression("true");

        belowPort = new ComponentPort(this, "belowPort");
        abovePort = new ComponentPort(this, "abovePort");
    }

    ///////////////////////////////////////////////////////////////////
    ////                   parameters and ports                             ////

    /** The port linked to concepts above this one in the lattice. */
    public ComponentPort abovePort;

    /** The port linked to concepts below this one in the lattice. */
    public ComponentPort belowPort;

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

    /** Return the ontology that contains this concept.
     *
     *  @return The containing ontology.
     */
    public Ontology getOntology() {
        return (Ontology)getContainer();
    }

    /** Return the outgoing port.
     *  @return The outgoing port.
     */
    public ComponentPort getIncomingPort() {
        return belowPort;
    }

    /** Return the outgoing port.
     *  @return The outgoing port.
     */
    public ComponentPort getOutgoingPort() {
        return abovePort;
    }

    /** Return the concepts that are directly above this one.
     *  @return A set of concepts that strictly dominate this one.
     */
    public Set<FiniteConcept> getStrictDominators() {
        Set<FiniteConcept> dominators = new HashSet<FiniteConcept>();
        List<ComponentPort> ports = abovePort.deepConnectedPortList();
        for (ComponentPort port : ports) {
            dominators.add((FiniteConcept) port.getContainer());
        }
        return dominators;
    }

    /** Return the concepts that are directly below this one.
     *  @return A set of concepts that are strictly dominated by this one.
     */
    public Set<FiniteConcept> getStrictPostdominators() {
        Set<FiniteConcept> postdominators = new HashSet<FiniteConcept>();
        List<ComponentPort> ports = belowPort.deepConnectedPortList();
        for (ComponentPort port : ports) {
            postdominators.add((FiniteConcept) port.getContainer());
        }
        return postdominators;
    }
    
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
    public boolean isAboveOrEqualTo(FiniteConcept concept)
            throws IllegalActionException {
        if (!(concept.getContainer().equals(getContainer()))) {
            throw new IllegalActionException(this,
                    "Attempt to compare elements from two distinct ontologies");
        }
        if (!(getContainer() instanceof Ontology)) {
            throw new IllegalActionException(this,
                    "Concept is not contained by an Ontology.");
        }
        Ontology ontology = (Ontology) getContainer();
        int comparisonResult = ontology.getGraph().compare(this, concept);
        return comparisonResult == CPO.SAME || comparisonResult == CPO.HIGHER;
    }

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

    /**
     * Return the string that represents this concept, its name.
     * 
     * @return The string name that represents this concept.
     */
    public String toString() {
        return _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The name of this Concept.
     */
    protected String _name;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Empty array. */
    private static InequalityTerm[] _EMPTY_ARRAY = new InequalityTerm[0];
}
