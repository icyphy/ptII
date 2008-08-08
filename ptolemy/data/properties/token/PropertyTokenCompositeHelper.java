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
    
    public void addListener(boolean listenInputs, boolean listenOutputs) throws IllegalActionException {
        super.addListener(listenInputs, listenOutputs);
        
        Iterator iterator = _getSubHelpers().iterator();
        
        while (iterator.hasNext()) {
            PropertyTokenHelper helper = 
                (PropertyTokenHelper) iterator.next();
            
            helper.addListener(listenInputs, listenOutputs);
        }
    }
    
    public void removeListener(boolean listenInputs, boolean listenOutputs) throws IllegalActionException {
        super.removeListener(listenInputs, listenOutputs);
        
        Iterator iterator = _getSubHelpers().iterator();
        
        while (iterator.hasNext()) {
            PropertyTokenHelper helper = 
                (PropertyTokenHelper) iterator.next();
            
            helper.removeListener(listenInputs, listenOutputs);
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
