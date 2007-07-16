package ptolemy.data.properties.token;

import ptolemy.data.properties.PropertyAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class PropertyTokenAttribute extends PropertyAttribute {

    public PropertyTokenAttribute(NamedObj container, String name)
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
        if (expression.length() > 0) {
            String tokenValue = getName().substring(getName().indexOf("::") + 2);


/*            _property = (PropertyToken) new PropertyToken();*/
        }
        super.setExpression(expression);
    }

}
