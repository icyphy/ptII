package ptolemy.data.properties.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

public class PropertyTokenASTNodeHelper extends PropertyTokenHelper {

    public PropertyTokenASTNodeHelper(PropertyTokenSolver solver, Object component)
    throws IllegalActionException {
        super(solver, component);
    }

    /**
    * Return a list of property-able NamedObj contained by
    * the component. All ports and parameters are considered
    * property-able.
    * @return The list of property-able named object.
    */
    public List<Object> getPropertyables() {
        List<Object> list = new ArrayList<Object>();
        list.add(getComponent());
        return list;
    }

   /**
     * 
     * @return
     * @throws IllegalActionException
     */
    protected List<PropertyHelper> _getSubHelpers() throws IllegalActionException {
        return new ArrayList<PropertyHelper>();
    }
       
    public void determineProperty(List <Attribute>attributeList) throws IllegalActionException, NameDuplicationException {
        Iterator attributeIterator = attributeList.iterator();
        while (attributeIterator.hasNext()) {
            Attribute attribute = (Attribute)attributeIterator.next();
//FIXME: take care of all StringParameters and filter them
//       should not be necessary once proprtyable attributes are filtered (related to kernel exceptions)            
            if (((attribute instanceof StringAttribute) &&
                 (attribute.getName().equalsIgnoreCase("guardExpression"))) ||
                 (attribute instanceof Parameter) ||
                 (attribute instanceof PortParameter)) {
                
                setEquals(attribute, getSolver().getProperty(getParseTree(attribute)));
            }
        }                        
    }
       
}
