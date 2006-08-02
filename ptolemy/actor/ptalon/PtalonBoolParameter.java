package ptolemy.actor.ptalon;

import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

/**
 * @author acataldo
 * @see PtalonActor
 *
 */
/**
 * @author acataldo
 *
 */
public class PtalonBoolParameter extends Parameter {

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PtalonBoolParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setTypeEquals(BaseType.BOOLEAN);
        _hasValue = false;
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * @return True if this this parameter's value has been set.
     */
    public boolean hasValue() {
        return _hasValue;
    }
    
    
    /**
     * Set the expression and flag that the value has been set for this
     * parameter.
     * 
     * @param expr The expression to set.
     */
    public void setExpression(String expr) {
        super.setExpression(expr);
        _hasValue = true;
        setVisibility(Settable.NOT_EDITABLE);
    }
    
    /**
     * Set the token and flag that the value has been set for this
     * parameter.
     * 
     * @param token The token to set.
     * @throws IllegalActionException If the superclass throws one.
     */
    public void setToken(Token token) throws IllegalActionException {
        super.setToken(token);
        _hasValue = true;
        setVisibility(Settable.NOT_EDITABLE);
   }
    
    /**
     * Set the token and flag that the value has been set for this
     * parameter.
     * 
     * @param expression The expression for this token
     * @throws IllegalActionException If the superclass throws one.
     */
    public void setToken(String expression) throws IllegalActionException {
        super.setToken(expression);
        _hasValue = true;
        setVisibility(Settable.NOT_EDITABLE);
    }
    
    
    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////
    
    /**
     * True if this parameter has a value.
     */
    private boolean _hasValue;
    
}
