/* Discrete-Event Particle Filter Implementation.

 Copyright (c) 1998-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package org.ptolemy.machineLearning.particleFilter;

import ptolemy.data.ArrayToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////

/**
 A Particle Filter Implementation

 <p>The particle filter runs on a state space model given by
 <pre>
 X_{t+1} = f(X_t, U_t, t)
 Y_{t} = g(X_t, U_t, t)
 X(0) = X0
 </pre>
 where X is the state vector, U is the input vector, Y is the observation
 vector, and t is the model time. To use this actor :
 <ul>

 <li> For each control input in <i>U</i>, create an input port with an arbitrary name.
 This actor will automatically create a parameter with the same name as the
 input port. That parameter will have its value set during execution to match
 the value of the input.

 <li> Fill in the <i>stateVariableNames</i> parameter, which is
 an array of strings, with the names of the state variables in <i>X</i>.
 These names can be arbitrary, since you will refer them to
 by name rather than by the symbol <i>X</i>.

 <li> Specify an update function (part of <i>f</i> above) for each
 state variable by creating a parameter named <i>name</i>_update, where
 <i>name</i> is the name of the state variable. The value of this
 parameter should be an expression giving the rate of change of
 this state variable as a function of any of the state variables,
 any input, any other actor parameter, and (possibly), the variable
 <i>t</i>, representing current time.

 <li> For each measurement input, create an input port with name <i>measurementName</i>_m,
 where <i>measurementName</i> is an arbitrary measurement name. Add a parameter to the actor
 named <i>measurementName</i>, which is an expression describing the measurement's
 correspondence to the state space. Namely, the measurement equation should be a function
 of <i>stateVariableNames</i>, <i>U</i> and <i>t</i>.

 <li> Fill in the measurement covariance parameter, that should be a square double matrix with
 dimension equal to the number of measurement equations defined. In case the measurements are
 independent, the matrix should be a scaled identity

 <li> Fill in the processNoise parameter, which should be a function that samples from the
 (possibly multivariate) distribution the state transition process noise is distributed according
 to. The return type should be an array of size equal to the state-space size

 <li> Specify the prior distribution as a random function from which the particles will be sampled.
 For instance, use the the random() function to draw uniform random variables in [0,1] or use
 multivariateGaussian() or gaussian() for Gaussian priors.The return type should be an array of size
 equal to the state-space size

 <li> It is important to note how multiple measurement inputs are interpreted by the actor.
 This implementation interprets multiple measurement inputs to be conditionally
 independent given the hidden state. This allows the likelihood (weight) of each particle at time
 step t to be computed as a product of its likelihood with respect to each measurement at that time.

<li> For additional parameters that are time varying, add arbitrarily many PortParameters to the actor
and refer to the port parameter by port name within measurement and/or update equations.
 </ul>


 The preinitialize() method of this actor is based on the ptolemy.domain.ct.lib.DifferentialSystem
 actor by Jie Liu.

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating Red (ilgea)

 */
public class ParticleFilter extends AbstractParticleFilter {
    /** Construct the composite actor with a name and a container.
     *  This constructor creates the ports, parameters, and the icon.
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public ParticleFilter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _init();
    }

    /** Construct a PF in the specified
     *  workspace with no container and an empty string as a name. You
     *  can then change the name with setName(). If the workspace
     *  argument is null, then use the default workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public ParticleFilter(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////


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

    /** The names of the state variables, in an array of strings.
     *  The default is an ArrayToken of an empty String.
     */
    public Parameter stateVariableNames;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** If the argument is any parameter other than <i>stateVariableNames</i>
     *  <i>t</i>, or any parameter matching an input port,
     *  then request reinitialization.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the numerator and the
     *   denominator matrix is not a row vector.
     */
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
                _particleLabels = new String[names.length() + 1];
                _particleTypes = new Type[names.length() + 1];
                _stateLabels = new String[names.length()];
                _stateTypes = new Type[names.length()];
                try {
                    for (int i = 0; i < names.length(); i++) {
                        stateName = ((StringToken) names.getElement(i))
                                .stringValue();
                        if (this.getAttribute(stateName) == null
                                && stateName.length() != 0) {
                            Parameter y = new Parameter(this, stateName);
                            y.setExpression("0.0");
                            y.setVisibility(Settable.EXPERT);
                        }
                        _particleLabels[i] = stateName;
                        _particleTypes[i] = BaseType.DOUBLE; // preset to be double

                        _stateLabels[i] = stateName;
                        _stateTypes[i] = BaseType.DOUBLE; // preset to be double
                    }
                    _particleLabels[names.length()] = "weight";
                    _particleTypes[names.length()] = BaseType.DOUBLE;

                    particleOutput.setTypeEquals(new RecordType(
                            _particleLabels, _particleTypes));
                    stateEstimate.setTypeEquals(new RecordType(_stateLabels,
                            _stateTypes));

                } catch (NameDuplicationException e) {
                    // should not happen
                    System.err.println("Duplicate field in " + this.getName());
                }
            }
        } else if (attribute == measurementCovariance) {
            double[][] proposed = ((MatrixToken) measurementCovariance
                    .getToken()).doubleMatrix();
            _Sigma = proposed;
        } else {
            super.attributeChanged(attribute);
        }
    }



    //////////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    /** Check the dimensions of all parameters and ports.
     *  @exception IllegalActionException If the dimensions are illegal.
     */
    @Override
    protected void _checkParameters() throws IllegalActionException {
        // Check state variable names.
        _stateNames = (ArrayToken) stateVariableNames.getToken();
        int n = _stateNames.length();

        if (n < 1) {
            throw new IllegalActionException(this, "There must be at "
                    + "least one state variable for the state space model.");
        }

        // Check if any of the state variable names is an empty string.
        for (int i = 0; i < n; i++) {
            String name = ((StringToken) _stateNames.getElement(i))
                    .stringValue().trim();

            if (name.equals("")) {
                throw new IllegalActionException(this, "A state variable "
                        + "name should not be an empty string.");
            }

            // Check state equations.
            String equation = name + UPDATE_POSTFIX;

            if (getAttribute(equation) == null) {
                throw new IllegalActionException(
                        this,
                        "Please add a "
                                + "parameter with name \""
                                + equation
                                + "\" that gives the state update expression for state "
                                + name + ".");
            }
        }
    }
  
    /**
     * Return the Parameter that is part of a state space model.
     * @param parameterName Name of parameter
     * @return Parameter object
     * @throws IllegalActionException
     */
    @Override
    protected Parameter getUserDefinedParameter(String parameterName) 
            throws IllegalActionException {
        Attribute attr = this.getAttribute(parameterName); 
        if (attr != null) {
            return ((Parameter)attr);
        } else {
            throw new IllegalActionException("Missing Parameter named: " + parameterName);
        } 
    } 
    

    /** Initialize the class. */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        StringToken[] empty = new StringToken[1];
        stateVariableNames = new Parameter(this, "stateVariableNames");
        empty[0] = new StringToken("");
        stateVariableNames.setToken(new ArrayToken(BaseType.STRING, empty)); 

        processNoise = new Parameter(this, "processNoise");
        processNoise
        .setExpression("multivariateGaussian({0.0,0.0},[1.0,0.4;0.4,1.2])");

        prior = new Parameter(this, "prior");
        prior.setExpression("random()*200-100"); 

        measurementCovariance = new Parameter(this, "measurementCovariance");
        measurementCovariance.setExpression("[10.0,0.0;0.0,10.0]"); 
    } 
}
