package ptolemy.data.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.lattice.PropertyConstraintAttribute;
import ptolemy.data.properties.token.PropertyTokenAttribute;
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
        Property property = (Property) _resolvedProperties.get(object);

        //        if (property == null) { // get value from attribute
        //            String solverName = getSolver()._solverName;
        /*            Parameter propertyAttribute = (Parameter) ((NamedObj)object).getAttribute("typeSystem_EDC");
                    if (propertyAttribute != null) {
                        try {
                            ObjectToken ot = (ObjectToken)propertyAttribute.getToken();
                            System.out.println(ot);
                        } catch (IllegalActionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
        //        }
        */
        return property;
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
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public void updateProperty(boolean isTraining)
            throws IllegalActionException, NameDuplicationException {
        List propertyables = _getPropertyables();

        Iterator iterator = propertyables.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();

            if (object instanceof NamedObj) {
                NamedObj namedObj = (NamedObj) object;

                Property property = getProperty(namedObj);

                PropertyAttribute attribute = null;

                // write results to attribute
                if (getSolver().getExtendedUseCaseName().startsWith("lattice")) {
                    attribute = (PropertyConstraintAttribute) namedObj
                            .getAttribute(getSolver().getExtendedUseCaseName());
                    if (attribute == null) {
                        attribute = new PropertyConstraintAttribute(namedObj,
                                getSolver().getExtendedUseCaseName());
                    }
                } else if (getSolver().getExtendedUseCaseName().startsWith(
                        "token")) {
                    attribute = (PropertyTokenAttribute) namedObj
                            .getAttribute(getSolver().getExtendedUseCaseName());
                    if (attribute == null) {
                        attribute = new PropertyTokenAttribute(namedObj,
                                getSolver().getExtendedUseCaseName());
                    }
                } else {
                    //FIXME:
                }

                if (isTraining) {
                    StringParameter showAttribute = (StringParameter) namedObj
                            .getAttribute("_showInfo");

                    if (showAttribute == null) {
                        showAttribute = new StringParameter(namedObj,
                                "_showInfo");
                    }

                    if (property != null) {

                        showAttribute.setToken(property.toString());

                        // write results to attribute
                        attribute.setExpression(property.toString());

                    } else {
                        showAttribute.setToken("");
                    }
                } else { // testing.
                    String propertyString = (property == null) ? "" : property
                            .toString();

                    if (!attribute.getExpression().equals(propertyString)) {
                        throw new IllegalActionException("Regression test"
                                + " failed in property resolution for "
                                + namedObj.getFullName()
                                + ". \nThe trained property value is: \""
                                + attribute.getExpression()
                                + "\", but resolved value is: \""
                                + propertyString + "\".\n");
                    }
                }
            }
        }
    }

    /** The associated component of this helper. */
    protected Object _component;

    /** The associated property lattice. */
    protected PropertySolver _solver;

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
     * Create a constraint that set the given object to be equal
     * to the given property. Mark the property of the given object
     * to be non-settable. 
     * @param object The given object.
     * @param property The given property.
     */
    public void setEquals(Object object, Property property) {
        _declaredProperties.put(object, property);
        _resolvedProperties.put(object, property);
        _nonSettables.add(object);
    }

    /**
     * 
     * @exception IllegalActionException 
     */
    public abstract void reinitialize() throws IllegalActionException;

}
