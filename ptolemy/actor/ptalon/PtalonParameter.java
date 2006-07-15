package ptolemy.actor.ptalon;

import java.lang.reflect.Method;
import java.util.ArrayList;

import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * This helper class is an extension of the StringParameter
 * class.  It is used to distinguish ordinary parameters
 * from Ptalon's parameters, which are higher-order paramters.
 * These parameters require special handling in PtalonActor,
 * because one may depend on another.  The PtalonActor is
 * designed to be conservative, doing nothing with these parameters
 * until all are assigned values.  The values in these parameter
 * should refer to other actors.  Action for these parameters
 * is given special treatment in the attributeChanged method of
 * PtalonActor.
 * @author acataldo
 * @see PtalonActor
 *
 */
/**
 * @author acataldo
 *
 */
public class PtalonParameter extends StringParameter implements PtalonObject {

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
    public PtalonParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _hasValue = false;
        _methods = new ArrayList<Method>();
        _parameters = new ArrayList<ArrayList<PtalonObject>>();
        _actorName = "";
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * Add a method to the list of methods for this parameter
     * and pass the list of ptalon objects upon which this method
     * depends.  The objects must be given in the order that 
     * the method requires them to be in when passed as actual
     * parameters.
     * @param method The method to add.
     * @param parameters The corresponding Ptalon parameters.
     */
    public void addMethod(Method method, ArrayList<PtalonObject> parameters) {
        _methods.add(method);
        _parameters.add(parameters);
    }
    
    /**
     * @return The generated actor name for this parameter.
     */
    public String getActorName() {
        return _actorName;
    }
    
    /**
     * @return True if this this parameter's value has been set.
     */
    public boolean hasValue() {
        return _hasValue;
    }
    
    /**
     * Execute the list of added methods in the order
     * they were added.
     * @param actor The actor on which to execute the methods.
     * @throws IllegalActionException If there is any problem
     * trying to execute the method.
     */
    public void executeMethods(PtalonActor actor) throws IllegalActionException {
        try {
            ArrayList<PtalonObject> list;
            PtalonObject[] params;
            ;
            for (int i = 0; i < _methods.size(); i++) {
                list = _parameters.get(i);
                params = new PtalonObject[list.size()]; 
                params =  list.toArray(params);
                _methods.get(i).invoke(actor, new Object[] {params});
            }
        } catch(Exception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }
    }
    
    /**
     * @return True if this paramter has methods and all
     * necessary parameters have values.
     */
    public boolean methodsAreReady() {
        if (_methods.size() == 0) {
            return false;
        }
        ArrayList<PtalonObject> params;
        PtalonParameter param;
        for (int i = 0; i < _methods.size(); i++) {
            try {
                params = _parameters.get(i);
            } catch(IndexOutOfBoundsException e) {
                return false;
            }
            for (int j = 0; j < params.size(); j++) {
                if (!(params.get(i) instanceof PtalonParameter)) {
                    param = (PtalonParameter) params.get(i);
                    if (!(param.hasValue())) {
                        return false;
                    }
                }
            }
        }
        return hasValue();
    }
    
    
    /**
     * Set the actor name corresponding to this
     * parameter's value.  This will be generated
     * during runtime.
     * @param name The generated name.
     */
    public void setActorName(String name) {
        _actorName = name;
    }
    
    /**
     * Set this true if this value of this paramter has been set.
     * @param status The status of this parameter.
     */
    public void setHasValue(boolean status) {
        _hasValue = status;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /**
     * A string to store an actor name generated for this actor.
     */
    private String _actorName;
    
    /**
     * True if this parameter has a value.
     */
    private boolean _hasValue;
    
    /**
     * The list of methods to call when this parameter
     * and all parameters it depends on are ready.
     */
    private ArrayList<Method> _methods;
    
    /**
     * The paramters that must be ready in order to call
     * the methods associated with this parameter.
     */
    private ArrayList<ArrayList<PtalonObject>> _parameters;
}
