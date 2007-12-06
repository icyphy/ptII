package ptolemy.data.properties.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

public class PortValueHelper extends PropertyHelper {

    public PortValueHelper(PortValueSolver solver, Object component) {

        _component = component;
        _solver = solver;
    }

    public PortValueSolver getSolver() {
        return (PortValueSolver) _solver;
    }

    public Property getProperty(Object object) {
        return (Property) _resolvedProperties.get(object);
    }

    /**
     * Return a list of property-able NamedObj contained by
     * the component. All ports and parameters are considered
     * property-able.
     * @return The list of property-able named object.
     */
    protected List _getPropertyables() {
        List list = new ArrayList();

        // Add all ports.
        list.addAll(((Entity) _component).portList());

        return list;
    }

    public void reinitialize() throws IllegalActionException {
        List propertyables = _getPropertyables();

        Iterator iterator = propertyables.iterator();

        while (iterator.hasNext()) {
            Object propertyable = iterator.next();

            if (!_declaredProperties.containsKey(propertyable)) {
                _resolvedProperties.remove(propertyable);
            }

            //_resolvedProperties.put(propertyable, _lattice.getInitialProperty());
        }
    }
}
