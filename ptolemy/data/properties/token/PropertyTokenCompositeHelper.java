package ptolemy.data.properties.token;

import java.util.Iterator;
import java.util.List;

import ptolemy.data.properties.PropertyHelper;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class PropertyTokenCompositeHelper extends PropertyTokenHelper {

    public PropertyTokenCompositeHelper(PropertyTokenSolver solver, Object component) {
        super(solver, component);
    }
    
    public void addListener() throws IllegalActionException {
        super.addListener();
        
        Iterator iterator = _getSubHelpers().iterator();
        
        while (iterator.hasNext()) {
            PropertyTokenHelper helper = 
                (PropertyTokenHelper) iterator.next();
            
            helper.addListener();
        }
    }
    
    public void removeListener() throws IllegalActionException {
        super.removeListener();
        
        Iterator iterator = _getSubHelpers().iterator();
        
        while (iterator.hasNext()) {
            PropertyTokenHelper helper = 
                (PropertyTokenHelper) iterator.next();
            
            helper.removeListener();
        }
    }

    protected List<PropertyHelper> _getSubHelpers() throws IllegalActionException {
        List<PropertyHelper> helpers = super._getSubHelpers();
        
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
