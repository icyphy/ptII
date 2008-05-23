package ptolemy.data.properties;

import java.util.Iterator;

import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.ObjectToken;
import ptolemy.data.properties.gui.PropertyDisplayGUIFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class PropertyRemover extends Attribute {

    public PropertyRemover(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"120\" height=\"40\" "
                + "style=\"fill:white\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "Double click to\nRemove Properties</text></svg>");

        new PropertyDisplayGUIFactory(
                this, "_portValueSolverGUIFactory");        

        sharedUtilitiesWrapper = new SharedParameter(
                this, "sharedUtilitiesWrapper", PropertySolver.class);

        // Create a new shared utilities object (only once).
        if (sharedUtilitiesWrapper.getExpression().length() == 0) {
            sharedUtilitiesWrapper.setToken(new ObjectToken(new SharedUtilities(sharedUtilitiesWrapper)));
        }
        _sharedUtilities = (SharedUtilities) ((ObjectToken) 
                sharedUtilitiesWrapper.getToken()).getValue();
    }        

    public SharedParameter sharedUtilitiesWrapper;

    protected SharedUtilities _sharedUtilities;

    public void removeProperties(PropertyHelper helper) throws IllegalActionException {
        Iterator propertyables = 
            helper.getPropertyables(NamedObj.class).iterator();
   
        while (propertyables.hasNext()) {
            NamedObj propertyable = (NamedObj) propertyables.next();
            _removePropertyAttributes(propertyable);
        }
        
        // Recursive case.
        Iterator subHelpers = helper._getSubHelpers().iterator();
        
        while (subHelpers.hasNext()) {
            PropertyHelper subHelper = (PropertyHelper) subHelpers.next();
            removeProperties(subHelper);
        }
    }
    
    public void removeProperties(CompositeEntity component) throws IllegalActionException {

        Iterator solvers = _sharedUtilities.getAllSolvers().iterator();

        while (solvers.hasNext()) {
            PropertySolver solver = (PropertySolver) solvers.next();
            
            PropertyHelper helper = solver.getHelper(component); 
            removeProperties(helper);
        }
        
        // Update the GUI.
        requestChange(new ChangeRequest(this, "Repaint the GUI.") {
                protected void _execute() throws Exception {}
        });        
    }

    private void _removePropertyAttributes(NamedObj namedObj) throws IllegalActionException {
        Iterator attributeIterator = 
            namedObj.attributeList(PropertyAttribute.class).iterator();

        Attribute attribute;
        while (attributeIterator.hasNext()) {
            attribute = (Attribute) attributeIterator.next();
            
            try {
                attribute.setContainer(null);
            } catch (NameDuplicationException e) {
                assert false;
            }
            //String moml = "<deleteProperty name=\"" + attribute.getName() + "\"/>";
            //namedObj.requestChange(new MoMLChangeRequest(this, namedObj, moml));
        }

        attribute = namedObj.getAttribute("_showInfo");
        if (attribute != null) {
            
            try {
                attribute.setContainer(null);
            } catch (NameDuplicationException e) {
                assert false;
            }
            //String moml = "<deleteProperty name=\"_showInfo\"/>";
            //namedObj.requestChange(new MoMLChangeRequest(this, namedObj, moml));
        }
        
        attribute = namedObj.getAttribute("_highlightColor");
        if (attribute != null) {
            
            try {
                attribute.setContainer(null);
            } catch (NameDuplicationException e) {
                assert false;
            }
            //String moml = "<deleteProperty name=\"" + attribute.getName() + "\"/>";
            //namedObj.requestChange(new MoMLChangeRequest(this, namedObj, moml));
        }
    }
}
