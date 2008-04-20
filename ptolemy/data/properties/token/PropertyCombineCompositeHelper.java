package ptolemy.data.properties.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.properties.PropertyHelper;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class PropertyCombineCompositeHelper extends PropertyCombineHelper {

    public PropertyCombineCompositeHelper(PropertyCombineSolver solver, Object component) {
        super(solver, component);
    }

    protected List<PropertyHelper> _getSubHelpers() throws IllegalActionException {
        List<PropertyHelper> helpers = new ArrayList<PropertyHelper>();
        
        CompositeEntity component = 
            (CompositeEntity) getComponent();
        Iterator iterator = component.entityList().iterator();
        
        while (iterator.hasNext()) {
            NamedObj actor = 
                (NamedObj) iterator.next();
            
            helpers.add(_solver.getHelper(actor));
        }
        
        return helpers;
    }   

}
