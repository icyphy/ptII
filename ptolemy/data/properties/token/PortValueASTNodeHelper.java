package ptolemy.data.properties.token;

import java.util.ArrayList;
import java.util.List;

import ptolemy.data.properties.Property;
import ptolemy.kernel.util.IllegalActionException;

public class PortValueASTNodeHelper extends PortValueHelper {

    public PortValueASTNodeHelper(PortValueSolver solver, Object component)
    throws IllegalActionException {
        super(solver, component);
    }

    public void calculateStaticValue() throws IllegalActionException {        
        return;
    }

        public void setProperty(Object object, Property property) {
//          _declaredProperties.put(object, property);
          _resolvedProperties.put(object, property);
          
//          _nonSettables.add(object);
      }   
        
       /**
        * Return a list of property-able NamedObj contained by
        * the component. All ports and parameters are considered
        * property-able.
        * @return The list of property-able named object.
        */
       protected List _getPropertyables() {
           List list = new ArrayList();
           list.add(_component);
           return list;
       }
}
