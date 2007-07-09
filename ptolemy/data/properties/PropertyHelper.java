package ptolemy.data.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public abstract class PropertyHelper {

    /**
     * Return the property value associated with the given property lattice
     * and the given port.  
     * @param object The given port.
     * @param lattice The given lattice.
     * @return The property value of the given port. 
     */
    public Property getProperty(Object object) {
        return (Property) _resolvedProperties.get(object);
    }

    /**
     * Return the associated property solver
     * @return The property solver associated with this helper.
     */
    public PropertySolver getSolver() {
        return _solver;
    }
    
    /**
     * Update the property.
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public void updateProperty(boolean isTraining) throws IllegalActionException, NameDuplicationException {
        List propertyables = _getPropertyables();
    
        Iterator iterator = propertyables.iterator();
    
        while (iterator.hasNext()) {
            Object object = iterator.next();
            
            if (object instanceof NamedObj) {
                NamedObj namedObj = (NamedObj) object;
                
                Property property = getProperty(namedObj);
                
                StringParameter attribute = 
                (StringParameter) namedObj.getAttribute("_showInfo");
        
                if (attribute == null) {
                    attribute = new StringParameter(namedObj, "_showInfo");
                }
        
                if (isTraining) {
                    if (property != null) {
                        attribute.setToken(property.toString());
                    } else {
                        attribute.setToken("");                
                    }
        
                } else {
                    String propertyString = (property == null) ? "" : property.toString();
                    
                    if (!attribute.getExpression().equals(propertyString)) {
                        throw new IllegalActionException ("Regression test" +
                                " failed in property resolution for " +
                                namedObj.getFullName() + 
                                ". \nThe trained property value is: \"" +
                                attribute.getExpression() +
                                "\", but resolved value is: \"" +
                                propertyString + "\".\n");
                    }
                }
            }
        }
    }
    

    /** The associated component of this helper. */
    protected Object _component;
    
    /** The associated property lattice. */
    protected PropertyConstraintSolver _solver;
    
    /** 
     * The mapping between property-able objects and their
     * declare property. 
     */
    protected HashMap _declaredProperties = new HashMap();
    /** 
     * The mapping between ports and their property values.
     * Each mapping is of the form (IOPort, Property). 
     */
    protected HashMap _resolvedProperties = new HashMap();

    /**
     * The set of property-able objects that??? 
     */
    protected HashSet _nonSettables = new HashSet();

    /**
     * Return a list of property-able object(s) for this helper.
     * @return a list of property-able objects.
     */
    protected abstract List _getPropertyables();

    /**
     * Create a constraint that set the port1 property to be equal
     * to the port2 property.
     * @param port1 The first given port.
     * @param port2 The second given port.
     */
    public void setEquals(Object object, Property property) {
        
        _declaredProperties.put(object, property);
        _resolvedProperties.put(object, property);
        
        _nonSettables.add(object);
        
        //Property oldProperty = (Property) _resolvedProperties.get(port);
    }

    /**
     * 
     * @throws IllegalActionException 
     */
    protected abstract void _reinitialize() throws IllegalActionException;

}
