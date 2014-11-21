package org.ptolemy.ssm; 

import java.util.ArrayList;
import java.util.List;

import org.ptolemy.ssm.MirrorDecoratorListener.DecoratorEvent;

import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

public class StateSpaceModel extends MirrorDecorator {

    /** Construct a StateSpaceModel with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public StateSpaceModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        _init();
    }

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        StateSpaceModel newObject = (StateSpaceModel) super
                .clone(workspace);
        newObject._cachedStateVariableNames = null;
        return newObject;
    }
    /** Construct a StateSpaceModel in the given workspace.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace. 
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public StateSpaceModel(Workspace workspace) 
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    } 

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == stateVariableNames) { 

            sendParameterEvent(DecoratorEvent.CHANGED_PARAMETER, stateVariableNames);
            // create a hidden parameter that corresponds to the specified state variable, if not already present
            ArrayToken names = (ArrayToken) stateVariableNames.getToken();
            String stateName = ((StringToken) names.getElement(0))
                    .stringValue(); 
            List<String> temp = new ArrayList<>();

                    if (stateName.length() > 0) {
                        // Set the output type according to the state variables 
                        try {
                            // create missing parameters for the newly added state variables.
                            for (int i = 0; i < names.length(); i++) { 
                                stateName = ((StringToken) names.getElement(i))
                                        .stringValue();
                                temp.add(stateName);
                                // check if this state name already existed before
                                if (!_cachedStateVariableNames.contains(stateName)) { 
                                    Parameter y = (Parameter) this.getAttribute(stateName);
                                    if ( y == null
                                            && stateName.length() != 0) {
                                        y = new Parameter(this, stateName); 
                                        y.setExpression("0.0"); 
                                        sendParameterEvent(DecoratorEvent.ADDED_PARAMETER, y);
                                    } 
                                    y.setVisibility(Settable.NONE); 
                                    if (this.getAttribute(stateName+"_update") == null) {
                                        Parameter yUpdate = new Parameter(this, stateName+"_update");
                                        yUpdate.setExpression(stateName); 
                                        sendParameterEvent(DecoratorEvent.ADDED_PARAMETER, yUpdate); 
                                    }
                                    _cachedStateVariableNames.add(stateName);
                                }
                            }
                            // remove parameters corresponding to obsolete state variables.
                            for (String old : _cachedStateVariableNames) {
                                if (! temp.contains(old)) {
                                    Parameter yUpdate = (Parameter) this.getAttribute(old+"_update"); 
                                    sendParameterEvent(DecoratorEvent.REMOVED_PARAMETER,yUpdate); 
                                    if (yUpdate != null) {
                                        yUpdate.setContainer(null);
                                    }
                                    Parameter y = (Parameter) this.getAttribute(old);
                                    sendParameterEvent(DecoratorEvent.REMOVED_PARAMETER,y); 
                                    if (y != null) {
                                        y.setContainer(null);
                                    } 
                                    _cachedStateVariableNames.remove(old);
                                }
                            }
                        } catch (NameDuplicationException e) {
                            // should not happen
                            throw new InternalErrorException("Duplicate field in " + this.getName());
                        }
                    }
        } else {
            // FIXME: If the attribute is changed in the SSM, this needs to be propagated to the
            // container StateSpaceActor b/c we likely would like to change the expressions accordingly
            super.attributeChanged(attribute);
        }
    } 


    /** An expression for the prior distribution from which the samples are drawn.
     */
    public Parameter prior;

    /** The process noise. If the system contains multiple state variables, the process noise
     * should be an expression that returns an ArrayToken. See multivariateGaussian for one such function.
     */
    public Parameter processNoise;

    /** An expression for a prior distribution from which the initial particles are sampled
     */
    public Parameter priorDistribution;

    /** The names of the state variables, in an array of strings.
     *  The default is an ArrayToken of an empty String.
     */
    public Parameter stateVariableNames;

    /** The value of current time. This parameter is not visible in
     *  the expression screen except in expert mode. Its value initially
     *  is just 0.0, a double, but upon each firing, it is given a
     *  value equal to the current time as reported by the director.
     */
    public Parameter t;

    /** Initialize the class. */
    private void _init() throws IllegalActionException,
    NameDuplicationException { 
        stateVariableNames = new Parameter(this, "stateVariableNames"); 
        stateVariableNames.setExpression("{\"x\",\"y\"}");


        prior = new Parameter(this, "prior");
        prior.setExpression("{random()*200-100,random()*200-100}");
        processNoise = new Parameter(this, "processNoise");
        processNoise
        .setExpression("multivariateGaussian({0.0,0.0},[1.0,0.4;0.4,1.2])"); 


        t = new Parameter(this, "t");
        t.setTypeEquals(BaseType.DOUBLE);
        t.setVisibility(Settable.EXPERT);
        t.setExpression("0.0");    

        _cachedStateVariableNames = new ArrayList<>();
    }  

    private List<String> _cachedStateVariableNames;
}
