/**
 * The base class for a an element in a property lattice.
 * 
 * Copyright (c) 2007-2009 The Regents of the University of California. All
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

import ptolemy.actor.IOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Concept

/**
 * A concept in an ontology.
 * An instance of this class is always associated with
 * a particular ontology, which is specified in the constructor.
 * 
 * @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Concept extends ComponentEntity implements InequalityTerm {

    /** Create a new concept with the specified name and the specified
     *  ontology.
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @throws NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @throws IllegalActionException If the base class throws it.
     */
    public Concept(CompositeEntity ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);
        
        _name = name;
        
        isAcceptable = new Parameter(this, "isAcceptable");
        isAcceptable.setTypeEquals(BaseType.BOOLEAN);
        isAcceptable.setExpression("true");
        
        belowPort = new IOPort(this, "belowPort", true, false);
        belowPort.setMultiport(true);
        abovePort = new IOPort(this, "abovePort", false, true);
        abovePort.setMultiport(true);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    ////                   parameters and ports                             ////
    
    /** The port linked to concepts above this one in the lattice. */
    public IOPort abovePort;

    /** The port linked to concepts below this one in the lattice. */
    public IOPort belowPort;

    /** A parameter indicating whether this concept is an acceptable outcome
     *  during inference. This is a boolean that defaults to true.
     */
    public Parameter isAcceptable;

    ////////////////////////////////////////////////////////////////////////////
    ////                   public methods                                   ////

    /** Return null.
     *  For variables, this method will return a reference to the
     *  propertyable object associated with that variable. For concepts,
     *  there is no associated propertyable object, hence returning null is the
     *  right thing to do.
     *  @return Null.
     */
    public Object getAssociatedObject() {
        return null;
    }
    
    /** Get the property lattice associated with this concept.
     *  @return The associated property lattice.
     *  @throws IllegalActionException If this property is not contained
     *   by an Ontology or if the structure in the Ontology is
     *   not a lattice.
     */
    public PropertyLattice getPropertyLattice() throws IllegalActionException {
        NamedObj container = getContainer();
        if (!(container instanceof Ontology)) {
            throw new IllegalActionException(this, "Not contained by an Ontology");
        }
        return ((Ontology)container).getLatticeGraph();
    }

    /** Return the value of the inequality term. Since a concept
     *  is a constant, not a variable, its value is just itself.
     *  @return This concept.
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
     *  @exception IllegalActionException Always thrown.
     */
    public void initialize(Object object) throws IllegalActionException {
        throw new IllegalActionException(this, "Cannot initialize a lattice property.");
    }

    /** Return true if this concept is greater than or equal to the
     *  specified concept in the partial ordering.
     *  @param property The concept to compare.
     *  @return True if this concept is greater than or equal to the
     *   specified concept.
     *  @throws IllegalActionException If the specified concept
     *   does not have the same lattice as this one.
     */
    public boolean isAboveOrEqualTo(Concept property) throws IllegalActionException {
        if (!((Concept)property)._lattice.equals(_lattice)) {
            throw new IllegalActionException(this,
                    "Attempt to compare elements of two distinct lattices");
        }
        int propertyInfo = _lattice.compare(this, property);
        return propertyInfo == CPO.SAME || propertyInfo == CPO.HIGHER;
    }

    /** Return false, because this inequality term is a constant.
     *  @return False.
     */
    public boolean isSettable() {
        return false;
    }

    /** Return true. By default, any concept is acceptable.
     * Return true if this concept is a valid result of inference.
     * Otherwise, return false. Return true in this base class by default.
     * @return True.
     */
    public boolean isValueAcceptable() {
        return true;
    }

    /** Throw an exception. This object is not a variable.
     *  @exception IllegalActionException Always thrown.
     */
    public void setValue(Object value) throws IllegalActionException {
        throw new IllegalActionException(this, "Cannot set a lattice property.");
    }

    /**
     * Return the string that represents this concept, its name.
     */
    public String toString() {
        return _name;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /**
     * The property lattice containing this concept.
     */
    protected PropertyLattice _lattice;
    
    /**
     * The name of this Property.
     */
    protected String _name;
    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////
    
    /** Empty array. */
    private static InequalityTerm[] _EMPTY_ARRAY = new InequalityTerm[0];
}
