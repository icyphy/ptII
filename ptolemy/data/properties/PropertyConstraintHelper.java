/** A base class representing a property constraint helper.

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
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.data.expr.StringParameter;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;


//////////////////////////////////////////////////////////////////////////
//// PropertyConstraintHelper

/**
 A base class representing a property constraint helper.

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
     * @throws IllegalActionException 
     */
    public PropertyConstraintHelper(NamedObj component, 
            PropertyLattice lattice) {
        
        _component = component;
        _lattice = lattice;
        _initialize();
    }

    private void _initialize() {
        
        Iterator ports = 
            ((Entity) _component).portList().iterator();
        
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            
            _resolvedProperties.put(port, _lattice.getInitialProperty());

            _declaredProperties.put(port, _lattice.getInitialProperty());

            _propertyTerms.put(port, new PropertyTerm(port));            
        }
        
        try {
            _updatePortProperty();

        } catch (NameDuplicationException ex) {
        } catch (IllegalActionException e) {
        }
    }

    protected void _updatePortProperty() throws IllegalActionException, NameDuplicationException {
        Iterator ports = 
            ((Entity) _component).portList().iterator();
        
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            
            StringParameter attribute = 
                (StringParameter) port.getAttribute("_showInfo");
            if (attribute == null) {
                attribute = new StringParameter(port, "_showInfo");
            }
            attribute.setToken(getProperty(port).toString());
        }
        
    }
    
    /** Return the constraints of this component.  The constraints is
     *  a list of inequalities. This base class returns a empty list.
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public List constraintList() throws IllegalActionException {
        return _constraints;
    }
    
    /**
     * Return the property value associated with the given property lattice
     * and the given port.  
     * @param port The given port.
     * @param lattice The given lattice.
     * @return The property value of the given port. 
     */
    public Property getProperty(IOPort port) {
        return (Property) _resolvedProperties.get(port);
    }
    
    /**
     * Return the property term from the given port and lattice.
     * @param port The given port.
     * @param lattice The given property lattice.
     * @return The property term of the given port.
     */
    public InequalityTerm getPropertyTerm(IOPort port) {
        return (InequalityTerm) _propertyTerms.get(port);
    }
    
    /**
     * 
     * @return
     */
    public PropertyConstraintSolver getSolver() {
        return _solver;
    }

    /**
     * Create a constraint that set the port1 property to be at least
     * the port2 property.
     * @param port1 The first given port.
     * @param port2 The second given port.
     * @param lattice The given lattice.
     */
    public void setAtLeast(IOPort port1, IOPort port2) {
        _setAtLeast(getPropertyTerm(port1),
                getPropertyTerm(port2));
    }

    /**
     * Create a constraint that set the property of the given port 
     * to be at least the given function term.
     * @param port The given port.
     * @param term The given function term.
     * @param lattice The given lattice.
     */
    public void setAtLeast(IOPort port, InequalityTerm term, PropertyLattice lattice) {
        _setAtLeast(getPropertyTerm(port), term);
    }
    
    /**
     * Create a constraint that set the property of the port1 
     * to be at least the property of port2.
     * @param port1 The first given port.
     * @param port2 The second given port.
     * @param lattice The given lattice.
     */
    public void setAtMost(IOPort port1, IOPort port2) {
        _setAtLeast(getPropertyTerm(port2),
                getPropertyTerm(port1));
    }

    /**
     * Create a constraint that set the property of the given port 
     * to be at most the given function term.
     * @param port The given port.
     * @param term The given function term.
     * @param lattice The given lattice.
     */
    public void setAtMost(IOPort port, InequalityTerm term, PropertyLattice lattice) {
        _setAtLeast(term, getPropertyTerm(port));
    }
    

    /**
     * Create a constraint that set the port1 property to be equal
     * to the port2 property.
     * @param port1 The first given port.
     * @param port2 The second given port.
     * @param lattice The given lattice.
     */
    public void setEquals(IOPort port1, IOPort port2) {
        setAtLeast(port1, port2);
        setAtLeast(port2, port1);
    }

    /**
     * Associate the given solver instance with this helper.
     * @param solver The given solver.
     */
    public void setSolver(PropertyConstraintSolver solver) {
        _solver = solver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of property constraints. 
     */
    protected List _constraints = new ArrayList();

    /** The mapping between ports and their property values.
     * Each mapping is of the form (IOPort, Property). 
     */
    protected HashMap _resolvedProperties = new HashMap();

    protected HashMap _declaredProperties = new HashMap();

    /**
     * 
     */
    protected HashMap _propertyTerms = new HashMap();


    
    private class PropertyTerm implements InequalityTerm {
        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////
        private IOPort _port;
        
        public PropertyTerm (IOPort port) {
            _port = port;
        }

        /** Return this TypedIOPort.
         *  @return A TypedIOPort.
         */
        public Object getAssociatedObject() {
            return _port;
        }

        /** Return the type of this TypedIOPort.
         */
        public Object getValue() {
            return getProperty(_port);
        }

        /** Return this TypeTerm in an array if this term represent
         *  a type variable. This term represents a type variable
         *  if the type of this port is not set through setTypeEquals().
         *  If the type of this port is set, return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = this;
                return variable;
            }

            return (new InequalityTerm[0]);
        }

        /** Reset the variable part of this type to the specified type.
         *  @param property A Type.
         *  @exception IllegalActionException If the type is not settable,
         *   or the argument is not a Type.
         */
        public void initialize(Object property) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException("PropertyTerm.initialize: "
                        + "Cannot initialize a constant property.");
            }

            if (!(property instanceof Property)) {
                throw new IllegalActionException("PropertyTerm.initialize: "
                        + "The argument is not a Property.");
            }

            _resolvedProperties.put(_port, property);
        }

        /** Test if the property of the port associated with this Term
         *  can be changed. The property can be changed if setEquals()
         *  is not called.
         *  @return True if the property can be changed; false otherwise.
         */
        public boolean isSettable() {
            return !((Property) _declaredProperties.get(_port)).isConstant();
        }

        /** Check whether the current value of this term is acceptable.
         *  This method delegates the check to the isTypeAcceptable()
         *  method of the outer class.
         *  @return True if the current value is acceptable.
         */
        public boolean isValueAcceptable() {
            if (getProperty(_port).isInstantiable()) {
                return true;
            }

            // For a disconnected port, any property is acceptable.
            if (_port.numLinks() == 0) {
                return true;
            }
            return false;
        }

        /** Set the property of this port.
         *  @parameter property The given property.
         *  @exception IllegalActionException If the new type violates
         *   the declared property of this port.
         */
        public void setValue(Object property) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "The property is not settable.");
            }

            if (!((Property) _declaredProperties.get(_port))
                    .isSubstitutionInstance((Property) property)) {
                throw new IllegalActionException("Property conflict on port "
                        + _port.getFullName() + ".\n"
                        + "Declared property is "
                        + _declaredProperties.get(_port).toString() + ".\n"
                        + "The connection or property constraints, however, "
                        + "require property " + property.toString());
            }
            
            _resolvedProperties.put(_port, property);
        }

        /** Override the base class to give a description of the port
         *  and its property.
         *  @return A description of the port and its property.
         */
        public String toString() {
            return "(" + _port.toString() + ", " + getProperty(_port) + ")";
        }
    }
  
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Add the given inequality to the list of constraint for
     * the given lattice.
     * @param lattice The given property lattice.
     * @param inequality The given inequality to be added.
     */
    private void _addConstraint(Inequality inequality) {
        _constraints.add(inequality);
    }

    /**
     * For a given lattice, create a constraint that set the
     * first term to be at least the second term.
     * @param term1 The greater term.
     * @param term2 The lesser term.
     * @param lattice The given lattice.
     */
    private void _setAtLeast(InequalityTerm term1, 
            InequalityTerm term2) {
        Inequality inequality = new Inequality(term2, term1);
        _addConstraint(inequality);        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The associated component of this helper. */
    protected NamedObj _component;
    
    /** The associated property lattice. */
    private PropertyLattice _lattice;
    
    private PropertyConstraintSolver _solver;
        
}
