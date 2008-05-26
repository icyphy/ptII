package ptolemy.data.properties.token;

import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.ModelAnalyzer;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.PropertySolver;
import ptolemy.data.properties.gui.PropertySolverGUIFactory;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

public class PropertyCombineSolver extends PropertySolver {
    
    public PropertyCombineSolver(NamedObj container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _propertyName = new StringParameter(this, "propertyName");
        _propertyName.setExpression("combinedValueToken");
        _propertyName.setVisibility(Settable.NOT_EDITABLE);
        
        _propertyExpression = new StringParameter(this, "propertyExpression");
        _propertyExpression.setExpression("");
        TextStyle style = new TextStyle(_propertyExpression, "_style");
        style.height.setExpression("10");
        style.width.setExpression("80");
/*
        _propertyEmptyString = new StringParameter(this, "propertyEmptyString");
        _propertyEmptyString.setExpression("");
*/        
        _inputPorts = new Parameter(this, "inputPorts");
        _inputPorts.setTypeEquals(BaseType.BOOLEAN);
        _inputPorts.setExpression("true");
        
        _outputPorts = new Parameter(this, "outputPorts");
        _outputPorts.setTypeEquals(BaseType.BOOLEAN);
        _outputPorts.setExpression("true");
         
        _unconnectedPorts = new Parameter(this, "ignore unconnected Ports");
        _unconnectedPorts.setTypeEquals(BaseType.BOOLEAN);
        _unconnectedPorts.setExpression("true");
/*
        _atomicActors = new Parameter(this, "atomicActors");
        _atomicActors.setTypeEquals(BaseType.BOOLEAN);
        _atomicActors.setExpression("true");
 
        _compositeActors = new Parameter(this, "compositeActors");
        _compositeActors.setTypeEquals(BaseType.BOOLEAN);
        _compositeActors.setExpression("false");
*/ 
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"115\" height=\"40\" "
                + "style=\"fill:red\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nResolve Property.</text></svg>");

        new PropertySolverGUIFactory(
                this, "_portValueSolverGUIFactory");

    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
   
    /**
     * Returns the helper that contains property information for
     * the given component.
     * @param component The given component
     * @return The associated property constraint helper.
     */    
    public PropertyHelper getHelper(Object object) throws IllegalActionException {
        return _getHelper(object);
    }

    protected void _resolveProperties(ModelAnalyzer analyzer) 
        throws KernelException {
        
        super._resolveProperties(analyzer);
        
        PropertyCombineCompositeHelper topLevelHelper = 
            (PropertyCombineCompositeHelper) _getHelper(toplevel());
        
        topLevelHelper.reinitialize();

        topLevelHelper.determineProperty();
     
    }

    public String getUseCaseName() {
        return _propertyName.getExpression();        
    }
    
    public String getExtendedUseCaseName() {
        return "token::" + getUseCaseName();
    }
/*        
    public String getPropertyEmptyString() {
        return _propertyEmptyString.getExpression();        
    }
*/
    //FIXME: only use method from base class?
    public Property getProperty(Object object) {
        if (object instanceof PortParameter) {
            return super.getProperty(((PortParameter)object).getPort());
        }
        return super.getProperty(object);
    }

    protected StringParameter _propertyName;
    protected StringParameter _propertyExpression;
    protected StringParameter _propertyEmptyString;
    protected Parameter _inputPorts;
    protected Parameter _outputPorts;
    protected Parameter _atomicActors;
    protected Parameter _compositeActors;
    protected Parameter _unconnectedPorts;
    
    public String getPropertyExpression() {
        return _propertyExpression.getExpression();        
    }
    
    public Boolean getInputPorts() {
        return (_inputPorts.getExpression().equalsIgnoreCase("true")) ? true : false;        
    }

    public Boolean getOutputPorts() {
        return (_outputPorts.getExpression().equalsIgnoreCase("true")) ? true : false;        
    }

    public Boolean getUnconnectedPorts() {
        return (_unconnectedPorts.getExpression().equalsIgnoreCase("true")) ? true : false;        
    }
    
    public void putToken(Object object, Token token) {
        _tokenMap.put(object, token);
    }

    public Token getToken(Object object) {
        return (Token)_tokenMap.get(object);
    }

    /*
    public Boolean getAtomicActors() {
        return (_atomicActors.getExpression().equalsIgnoreCase("true")) ? true : false;        
    }

    public Boolean getCompositeActors() {
        return (_compositeActors.getExpression().equalsIgnoreCase("true")) ? true : false;        
    }
*/
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    private Map<Object, Token> _tokenMap = new HashMap<Object, Token>();

}
