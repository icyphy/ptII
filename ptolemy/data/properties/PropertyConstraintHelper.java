/** An Interface representing a property.

 Copyright (c) 1997-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */
package ptolemy.data.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.StructuredType;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// Property

/**
 An interface representing a property.

 @author Man-Kit Leung, Thomas Mandl, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintHelper {

    
    /** Construct the property constraint helper associated
     *  with the given component.
     *  @param component The associated component.
     */
    public PropertyConstraintHelper(NamedObj component) {
        _component = component;
    }
    
    /** Return all constraints of this component.  The constraints is
     *  a list of inequalities. This base class returns a empty list.
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public List constraintList() throws IllegalActionException {
        ArrayList constraints = new ArrayList();
        constraints.addAll(_constraints.entrySet());
        return constraints;
    }

    /** Return the constraints of this component.  The constraints is
     *  a list of inequalities. This base class returns a empty list.
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public List constraintList(PropertyLattice _lattice) throws IllegalActionException {
        return _constraints.get(_lattice);
    }
    
    /**
     * Return the property value associated with the given property lattice
     * and the given port.  
     * @param port The given port.
     * @param lattice The given lattice.
     * @return The property value of the given port. 
     */
    public Property getProperty(IOPort port, PropertyLattice lattice) {
        // return something from _properties.
        return null;
    }
    
    /**
     * Return the property term from the given port and lattice.
     * @param port The given port.
     * @param lattice The given property lattice.
     * @return The property term of the given port.
     */
    public InequalityTerm getPropertyTerm(IOPort port, PropertyLattice lattice) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAtLeast (InequalityTerm term1, 
            InequalityTerm term2, PropertyLattice lattice) {
        Inequality inequality = new Inequality(term2, term1);
        _addConstraint(lattice, inequality);        
    }

    /**
     * Create a constraint that set the port1 property to be at least
     * the port2 property.
     * @param port1 The first given port.
     * @param port2 The second given port.
     * @param lattice The given lattice.
     */
    public void setAtLeast(TypedIOPort port1, TypedIOPort port2, PropertyLattice lattice) {
        setAtLeast(getPropertyTerm(port1, lattice),
                getPropertyTerm(port2, lattice), lattice);
    }

    
    public void setAtLeast(TypedIOPort port, MonotonicFunction term, PropertyLattice lattice) {
        setAtLeast(getPropertyTerm(port, lattice), term, lattice);
    }
    
    
    public void setAtMost(TypedIOPort port1, TypedIOPort port2, PropertyLattice lattice) {
        setAtLeast(getPropertyTerm(port2, lattice),
                getPropertyTerm(port1, lattice), lattice);
    }

    /** Constrain the type of this port to be equal to or greater
     *  than the type represented by the specified InequalityTerm.
     *  <p>Actors that call this method should have a clone() method that
     *  repeats the relative type constraints that were specified in
     *  the constructor.
     *  @param typeTerm An InequalityTerm.
     */
    public void setAtMost(TypedIOPort port, MonotonicFunction term, PropertyLattice lattice) {
        setAtLeast(term, getPropertyTerm(port, lattice), lattice);
    }

    /**
     * Add the given inequality to the list of constraint for
     * the given lattice.
     * @param lattice The given property lattice.
     * @param inequality The given inequality to be added.
     */
    private void _addConstraint(PropertyLattice lattice, 
            Inequality inequality) {
        List constraintList = _constraints.get(lattice);
        
        if (constraintList == null) {
            constraintList = new ArrayList();
            _constraints.put(lattice, constraintList);
        } 
        constraintList.add(inequality);
    }

    /** The map between property lattices and the associated
     *  list of constraints 
     */
    protected HashMap <PropertyLattice, List<Inequality>> _constraints = new HashMap();

    /** The map between port property values and property lattices.
     */
    protected HashMap <PropertyLattice, Map <IOPort, Property>> _properties = new HashMap();

    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    /** The associated component of this helper. */
    private NamedObj _component;
    
}
