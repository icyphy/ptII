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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.PropertyConstraintSolver.ConstraintType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


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
            PropertyLattice lattice) throws IllegalActionException {
        this(component, lattice, true);
    }
    
    /**
     * 
     * @param component
     * @param lattice
     * @param useDefaultConstraints
     * @throws IllegalActionException
     */
    public PropertyConstraintHelper(NamedObj component, 
            PropertyLattice lattice, boolean useDefaultConstraints)
            throws IllegalActionException {
        _component = component;
        _lattice = lattice;
        _useDefaultConstraints = useDefaultConstraints;
        _initialize();
        
    }

    /**
     * 
     * @param actorConstraintType
     * @throws IllegalActionException
     */
    protected void _changeDefaultConstraints(
            ConstraintType actorConstraintType) throws IllegalActionException {
        _constraints.clear();
        
        boolean constraintSource = 
            (actorConstraintType == ConstraintType.SRC_EQUALS_MEET) ||  
            (actorConstraintType == ConstraintType.SRC_LESS);

        List portList1 = (constraintSource) ?
                ((AtomicActor) _component).inputPortList() :
                ((AtomicActor) _component).outputPortList();

        List portList2 = (constraintSource) ?
                ((AtomicActor) _component).outputPortList() :
                ((AtomicActor) _component).inputPortList();
                
        Iterator ports = portList1.iterator();
        
        while (ports.hasNext()) {                    
            IOPort port = (IOPort) ports.next();                    
            _constraintPort(actorConstraintType, port, portList2);
        }
    }

    /**
     * @param constraintType
     * @param port
     * @param portList2
     * @throws IllegalActionException
     */
    protected void _constraintPort(ConstraintType constraintType, 
            IOPort port, List portList2) throws IllegalActionException {
        boolean isEquals = 
            (constraintType == ConstraintType.EQUALS) ||  
            (constraintType == ConstraintType.SINK_EQUALS_MEET) ||  
            (constraintType == ConstraintType.SRC_EQUALS_MEET);         
        
        boolean useMeetFunction = 
            (constraintType == ConstraintType.SRC_EQUALS_MEET) ||  
            (constraintType == ConstraintType.SINK_EQUALS_MEET);

        Iterator constraintingPorts = portList2.iterator();

        if (!useMeetFunction) {
            while (constraintingPorts.hasNext()) {
                IOPort port2 = (IOPort) constraintingPorts.next();

                if (isEquals) {
                    setSameAs(port, port2);
                    
                } else {
                    setAtMost(port, port2);                        
                } 
            }
        } else if (constraintType != ConstraintType.NONE) {
            if (portList2.size() > 0) {
                setSameAs(port, new MeetFunction(_lattice, portList2));
            }
        }
    }

    
    /**
     * 
     *
     */
    private void _initialize() {
        List propertyables = _getPropertyable();

        Iterator iterator = propertyables.iterator();
        
        while (iterator.hasNext()) {
            NamedObj propertyable = (NamedObj) iterator.next();

            //_resolvedProperties.put(port, _lattice.getInitialProperty());
            _declaredProperties.put(propertyable, _lattice.getInitialProperty());
            _propertyTerms.put(propertyable, new PropertyTerm(propertyable));            
        }
    }

    /**
     * @return
     */
    private List _getPropertyable() {
        List list = new ArrayList();
        
        // Add all ports.
        list.addAll(((Entity) _component).portList());
        
        // Add parameters.
        Iterator parameters = 
            ((Entity) _component).attributeList(Parameter.class).iterator();
        
        while (parameters.hasNext()) {
            Parameter parameter = (Parameter) parameters.next();

            // FIXME: We should do some sort of filtering here.            
            list.add(parameter);
        }
        return list;
    }

    /**
     * 
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public void updatePortProperty() throws IllegalActionException, NameDuplicationException {
        List propertyables = _getPropertyable();

        Iterator iterator = propertyables.iterator();

        while (iterator.hasNext()) {
            NamedObj namedObj = (NamedObj) iterator.next();
            
            Property property = getProperty(namedObj);
            
            StringParameter attribute = 
            (StringParameter) namedObj.getAttribute("_showInfo");

            if (attribute == null) {
                attribute = new StringParameter(namedObj, "_showInfo");
            }

            if (property != null) {
                attribute.setToken(property.toString());
            } else {
                attribute.setToken("");                
            }
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
    public Property getProperty(NamedObj port) {
        return (Property) _resolvedProperties.get(port);
    }
    
    /**
     * Return the property term from the given port and lattice.
     * @param port The given port.
     * @param lattice The given property lattice.
     * @return The property term of the given port.
     * @throws IllegalActionException 
     */
    public InequalityTerm getPropertyTerm(NamedObj port) throws IllegalActionException {
        if (port.getContainer() != _component) {
            return _lattice.getHelper(port.getContainer()).getPropertyTerm(port);            
        } else {
            return (InequalityTerm) _propertyTerms.get(port); 
        }
        
    }

    /**
     * 
     * @param port
     * @return
     * @throws IllegalActionException
     */
    public boolean isSettable(NamedObj port) throws IllegalActionException {
        if (port.getContainer() != _component) {
            return _lattice.getHelper(port.getContainer()).isSettable(port);            
        } else {
            return !_nonSettablePorts.contains(port);
        }
    }
    
    /**
     * Create a constraint that set the port1 property to be at least
     * the port2 property.
     * @param port1 The first given port.
     * @param port2 The second given port.
     * @param lattice The given lattice.
     * @throws IllegalActionException 
     */
    public void setAtLeast(NamedObj port1, NamedObj port2) throws IllegalActionException {
        _setAtLeast(getPropertyTerm(port1),
                getPropertyTerm(port2));
    }

    /**
     * Create a constraint that set the property of the given port 
     * to be at least the given function term.
     * @param port The given port.
     * @param term The given function term.
     * @throws IllegalActionException 
     */
    public void setAtLeast(NamedObj port, InequalityTerm term) throws IllegalActionException {
        _setAtLeast(getPropertyTerm(port), term);
    }
    
    /**
     * Create a constraint that set the property of the port1 
     * to be at least the property of port2.
     * @param port1 The first given port.
     * @param port2 The second given port.
     * @throws IllegalActionException 
     */
    public void setAtMost(NamedObj port1, IOPort port2) throws IllegalActionException {
        _setAtLeast(getPropertyTerm(port2),
                getPropertyTerm(port1));
    }

    /**
     * Create a constraint that set the property of the given port 
     * to be at most the given function term.
     * @param port The given port.
     * @param term The given function term.
     * @throws IllegalActionException 
     */
    public void setAtMost(NamedObj port, InequalityTerm term) throws IllegalActionException {
        _setAtLeast(term, getPropertyTerm(port));
    }
    

    /**
     * Create a constraint that set the port1 property to be equal
     * to the port2 property.
     * @param port1 The first given port.
     * @param port2 The second given port.
     */
    public void setEquals(NamedObj port, Property property) {
        
        _declaredProperties.put(port, property);
        _resolvedProperties.put(port, property);
        
        _nonSettablePorts.add(port);
        
        //Property oldProperty = (Property) _resolvedProperties.get(port);
    }

    /**
     * 
     * @param port1
     * @param port2
     * @throws IllegalActionException
     */
    public void setSameAs(NamedObj port1, NamedObj port2) throws IllegalActionException {
        setAtLeast(port1, port2);
        setAtLeast(port2, port1);
    }

    /**
     * Create a constraint that set the property of the given port 
     * to be same as the given function term.
     * @param port The given port.
     * @param term The given function term.
     * @throws IllegalActionException 
     */
    public void setSameAs(NamedObj port, InequalityTerm term) throws IllegalActionException {
        _setAtLeast(term, getPropertyTerm(port));
        _setAtLeast(getPropertyTerm(port), term);
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

    /**
     */
    protected HashMap _declaredProperties = new HashMap();

    /**
     */
    protected HashSet _nonSettablePorts = new HashSet();

    /**
     */
    protected HashMap _propertyTerms = new HashMap();


    
    private class PropertyTerm implements InequalityTerm {
        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////
        private NamedObj _namedObj;
        
        public PropertyTerm (NamedObj object) {
            _namedObj = object;
        }

        /** Return this TypedIOPort.
         *  @return A TypedIOPort.
         */
        public Object getAssociatedObject() {
            return _namedObj;
        }

        /** Return the type of this TypedIOPort.
         */
        public Object getValue() {
            return getProperty(_namedObj);
        }

        /** Return this PropertyTerm in an array if this term represent
         *  a property variable. This term represents a property variable
         *  if the property of this port is not set through setEquals().
         *  If the property of this port is set, return an array of size zero.
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

            _resolvedProperties.put(_namedObj, property);
        }

        /** Test if the property of the port associated with this Term
         *  can be changed. The property can be changed if setEquals()
         *  is not called.
         *  @return True if the property can be changed; false otherwise.
         */
        public boolean isSettable() {
            return !_nonSettablePorts.contains(_namedObj);
        }

        /** Check whether the current value of this term is acceptable.
         *  This method delegates the check to the isTypeAcceptable()
         *  method of the outer class.
         *  @return True if the current value is acceptable.
         */
        public boolean isValueAcceptable() {
            if (getProperty(_namedObj).isInstantiable()) {
                return true;
            }

            // For a disconnected port, any property is acceptable.
            if (_namedObj instanceof IOPort) {
                if (((IOPort) _namedObj).numLinks() == 0) {
                    return true;
                }
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

            if (!((Property) _declaredProperties.get(_namedObj))
                    .isSubstitutionInstance((Property) property)) {
                throw new IllegalActionException("Property conflict on port "
                        + _namedObj.getFullName() + ".\n"
                        + "Declared property is "
                        + _declaredProperties.get(_namedObj).toString() + ".\n"
                        + "The connection or property constraints, however, "
                        + "require property " + property.toString());
            }
            
            _resolvedProperties.put(_namedObj, property);
        }

        /** Override the base class to give a description of the port
         *  and its property.
         *  @return A description of the port and its property.
         */
        public String toString() {
            return "(" + _namedObj.toString() + ", " + getProperty(_namedObj) + ")";
        }
    }
  
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Add the given inequality to the list of constraint.
     * @param inequality The given inequality to be added.
     */
    private void _addConstraint(Inequality inequality) {
        _constraints.add(inequality);
    }

    /**
     * Create a constraint that set the
     * first term to be at least the second term.
     * @param term1 The greater term.
     * @param term2 The lesser term.
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
    protected PropertyLattice _lattice;

    /**
     */
    protected boolean _useDefaultConstraints;

}
