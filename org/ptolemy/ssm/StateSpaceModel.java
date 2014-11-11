package org.ptolemy.ssm; 

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
            // create a hidden parameter that corresponds to the specified state variable, if not already present
            ArrayToken names = (ArrayToken) stateVariableNames.getToken();
            String stateName = ((StringToken) names.getElement(0))
                    .stringValue();
            if (stateName.length() > 0) {
                // Set the output type according to the state variables

                try {
                    for (int i = 0; i < names.length(); i++) {
                        stateName = ((StringToken) names.getElement(i))
                                .stringValue();
                        if (this.getAttribute(stateName) == null
                                && stateName.length() != 0) {
                            Parameter y = new Parameter(this, stateName); 
                            y.setExpression("0.0");
                            y.setVisibility(Settable.EXPERT); 
                            sendParameterEvent(DecoratorEvent.ADDED_PARAMETER, y);
                        } 
                        if (this.getAttribute(stateName+"_update") == null) {
                            Parameter yUpdate = new Parameter(this, stateName+"_update");
                            yUpdate.setExpression(stateName); 
                            sendParameterEvent(DecoratorEvent.ADDED_PARAMETER, yUpdate);

                        }
                    } 

                } catch (NameDuplicationException e) {
                    // should not happen
                    throw new InternalErrorException("Duplicate field in " + this.getName());
                }
            }
        }  
        super.attributeChanged(attribute); 
    } 
    
    /** Standard deviation of the measurement noise ( assuming  Gaussian measurement noise
     * at the moment)
     */
    public Parameter measurementCovariance;

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
        StringToken[] empty = new StringToken[1];
        stateVariableNames = new Parameter(this, "stateVariableNames");
        empty[0] = new StringToken("x");
        stateVariableNames.setToken(new ArrayToken(BaseType.STRING, empty));

        processNoise = new Parameter(this, "processNoise");
        processNoise
        .setExpression("multivariateGaussian({0.0,0.0},[1.0,0.4;0.4,1.2])");

        measurementCovariance = new Parameter(this, "measurementCovariance");
        measurementCovariance.setExpression("[10.0,0.0;0.0,10.0]");  

        prior = new Parameter(this, "prior");
        prior.setExpression("random()*200-100");

        t = new Parameter(this, "t");
        t.setTypeEquals(BaseType.DOUBLE);
        t.setVisibility(Settable.EXPERT);
        t.setExpression("0.0");    
    }  
}
