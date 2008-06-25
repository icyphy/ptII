package ptolemy.data.properties.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.PropertySolver;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class PropertyCombineHelper extends PropertyHelper {

    public PropertyCombineHelper(PropertyCombineSolver solver, Object component) {
        
        setComponent(component);
        _solver = solver; 
    }

    public PropertyCombineSolver getSolver() {
        return (PropertyCombineSolver)_solver;
    }

    public void determineProperty() 
    throws IllegalActionException, NameDuplicationException {

        Iterator portIterator = getPropertyables().iterator();
        
        while (portIterator.hasNext()) {
            IOPort port = (IOPort) portIterator.next();
        
            // Get the shared parser.
            PtParser parser = PropertySolver.getParser();
            
            // create parse tree
            ASTPtRootNode parseTree = parser.generateParseTree(getSolver().getPropertyExpression());
        
            // do evaluation for port
            PropertyCombineParseTreeEvaluator evaluator = new PropertyCombineParseTreeEvaluator(port, _solver);                
            Token token = evaluator.evaluateParseTree(parseTree);        
            PropertyToken property = (PropertyToken) new PropertyToken(token);
            if (!((getSolver().getUnconnectedPorts()) && port.connectedPortList().isEmpty())) {                
                setEquals(port, property);
            }            
        }
        
        Iterator helpers = _getSubHelpers().iterator();        
        while (helpers.hasNext()) {
            PropertyCombineHelper helper = 
                (PropertyCombineHelper) helpers.next();
            helper.determineProperty();
        }        
    }
    
    /**
     * Return a list of property-able NamedObj contained by
     * the component. All ports and parameters are considered
     * property-able.
     * @return The list of property-able named object.
     */
    public List<Object> getPropertyables() {
        List<Object> list = new ArrayList<Object>();
        
        // Add all ports.
        list.addAll(((Entity) getComponent()).portList());
        
        return list;
    }
    
    public void setEquals(Object object, PropertyToken property) {
        super.setEquals(object, property);
        if (property != null) {
            getSolver().putToken(object, property.getToken());
        }
    }

    protected List<PropertyHelper> _getSubHelpers() throws IllegalActionException {        
        return new ArrayList<PropertyHelper>();
    }   

}
