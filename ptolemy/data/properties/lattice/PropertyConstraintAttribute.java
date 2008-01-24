package ptolemy.data.properties.lattice;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class PropertyConstraintAttribute extends PropertyAttribute {

    /**
     * 
     * @param container
     * @param name
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public PropertyConstraintAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Set the expression. This method takes the descriptive form and
     * determines the internal form (by parsing the descriptive form) and stores
     * it.
     * @param expression A String that is the descriptive form of either a Unit
     * or a UnitEquation.
     * @see ptolemy.kernel.util.Settable#setExpression(java.lang.String)
     */

    public void setExpression(String expression) throws IllegalActionException {
        super.setExpression(expression);

        if (expression.length() > 0) {
            String latticeName = getName().substring(
                    getName().indexOf("::") + 2);

            PropertyLattice lattice = PropertyLattice
                    .getPropertyLattice(latticeName);

            try {
                String fieldName = expression.toUpperCase();

                _property = (Property) lattice.getClass().getField(fieldName)
                        .get(lattice);

            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Cannot resolve the property expression: \""
                                + expression + "\"");
            }
        }
    }
}
