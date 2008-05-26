/** A class representing a property term factory.

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
package ptolemy.data.properties.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.properties.Property;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// PropertyTermManager.

/**
 A class representing a property term manager.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyTermManager implements PropertyTermFactory {

    /**
     * Construct a new PropertyTerm factory.  
     */
    public PropertyTermManager(PropertyConstraintSolver solver) {
        _solver = solver;
    }

    public List<PropertyTerm> terms() {
        List<PropertyTerm>  result = new LinkedList<PropertyTerm>();
        result.addAll(_propertyTerms.values());
        return result;
    }
    
    /**
     * 
     * @param object
     * @return
     * @throws IllegalActionException
     */
    public PropertyTerm getPropertyTerm(Object object) {
        if (object == null || object instanceof PropertyTerm) {
            return (PropertyTerm) object;
        }
        
        if (object instanceof NamedObj) {
    
            // Use the property term for the ParameterPort, if it is connected.
            if (object instanceof PortParameter) {
                PortParameter parameter = (PortParameter) object;
                if (parameter.getPort().numLinks() > 0) {
                    return getPropertyTerm(parameter.getPort());
                }
            }
            
            // The property term for an Attribute is its root ASTNode.
            //if (object instanceof Attribute) {
            //    ASTPtRootNode node = _solver.getParseTree((Attribute) object);
            //    return getPropertyTerm(node);
            //}
        }

        if (!_propertyTerms.containsKey(object)) {
            _propertyTerms.put(object, new InequalityTerm(object));                        
        }
        return (PropertyTerm) _propertyTerms.get(object);         
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The mapping between property-able objects and their PropertyTerm. */
    private HashMap<Object, PropertyTerm> _propertyTerms = 
        new HashMap<Object, PropertyTerm>();

    protected PropertyConstraintSolver _solver;
    
    
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    public class InequalityTerm implements PropertyTerm {
        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////
        protected Object _object;
        private boolean _isEffective;
        
        protected InequalityTerm (Object object) {
            _object = object;
            _isEffective = true;
        }

        /** Return this TypedIOPort.
         *  @return A TypedIOPort.
         */
        public Object getAssociatedObject() {
            return _object;
        }

        /** Return null if this term is not effective. Otherwise, return 
         *  the resolved property of this PropertyTerm.
         * @throws IllegalActionException 
         */
        public Object getValue() {
            return _isEffective ? 
                    _solver.getResolvedProperty(_object) : null;
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

            // FIX: Check with Jackie if this is the right implementation! This fix is for OIL 182.
            if (_object instanceof LatticeProperty) {
                _solver.setResolvedProperty(_object, (LatticeProperty)_object);                
            } else {
                _solver.setResolvedProperty(_object, (Property) property);
            }
        }

        public boolean isEffective() {
            return _isEffective;
        }
        
        /** Test if the property of the port associated with this Term
         *  can be changed. The property can be changed if setEquals()
         *  is not called.
         *  @return True if the property can be changed; false otherwise.
         */
        public boolean isSettable() {
            return _solver.isSettable(_object);
        }

        /** Check whether the current value of this term is acceptable.
         *  This method delegates the check to the isTypeAcceptable()
         *  method of the outer class.
         *  @return True if the current value is acceptable.
         */
        public boolean isValueAcceptable() {
            Property property = (Property) getValue();

            if (property == null) {
                return true;
            }
            if (property.isInstantiable()) {
                return true;
            }
            
            return false;
        }

        public void setEffective (boolean isEffective) {
            _isEffective = isEffective; 
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

            Property declaredProperty = _solver.getDeclaredProperty(_object); 
            if (declaredProperty != null &&
                    !declaredProperty.isSubstitutionInstance((Property) property)) {
                throw new IllegalActionException("Property conflict on object "
                        + _object.toString() + ".\n"
                        + "Declared property is "
                        + declaredProperty.toString() + ".\n"
                        + "The connection or property constraints, however, "
                        + "require property " + property.toString());
            }
            
            _solver.setResolvedProperty(_object, (Property) property);
        }

        /** Override the base class to give a description of the port
         *  and its property.
         *  @return A description of the port and its property.
         */
        public String toString() {

            //return "( " + _object.hashCode() + "--" + hashCode() + 
            //" " + _object.toString() + ", " + getValue() + ")";
            return "(" + _object.toString() + ", " + getValue() + ")";
        }
    }

    public List<PropertyTerm> getAffectedTerms(PropertyTerm updateTerm) throws IllegalActionException {
        return new ArrayList<PropertyTerm>();
    }
}
