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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.lib.SetVariable;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.DoubleMatrixMath;
import ptolemy.math.SignalProcessing;

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
public class ParticleFilter extends TypedCompositeActor {
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

    /** A boolean parameter that when set to true, implements the so-called
     * bootstrap particle filter, where particles are resampled at each time step
     * If this parameter is false, particles are resampled only when the effective
     * sample size drops below 50% of the total number of particles
     */
    public Parameter bootstrap;

    /** Low-variance resampler */
    public Parameter lowVarianceSampler;

    /** Standard deviation of the measurement noise ( assuming  Gaussian measurement noise
     * at the moment)
     */
    public Parameter measurementCovariance;

    /**
     * The expression that specifies the PDF for the measurementNoise. use N(m,s) for
     * a Gaussian distribution with mean m and standard deviation s. unif (x,y) evaluates
     * to a uniform distribution in range [x,y]
     */
    public Parameter particleCount;

    public Parameter outputParticleCount;

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

    /** The output port that outputs the produced particles at each firing.
     */
    public TypedIOPort particleOutput;

    /** State estimate output. A record token with one field per state variable */
    public TypedIOPort stateEstimate;
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

    /** Boolean parameter to determine whether seeds are reset on each run.
     */
    public SharedParameter resetOnEachRun;

    /** The seed to be used for random token generation, to evaluate
     * probabilistic transitions between states.
     */
    public SharedParameter seed;

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

        if (attribute == outputParticleCount) {
            int proposed = ((IntToken) (outputParticleCount.getToken()))
                    .intValue();
            if (proposed > 0) {
                Noutput = proposed;
            }
        } else if (attribute == stateVariableNames) {
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
        } else if (attribute == particleCount) {
            int proposed = ((IntToken) (particleCount.getToken())).intValue();
            if (proposed > 0) {
                Nparticles = proposed;
                particles = new Particle[Nparticles];
            }
        } else if (attribute == measurementCovariance) {
            double[][] proposed = ((MatrixToken) measurementCovariance
                    .getToken()).doubleMatrix();
            _Sigma = proposed;
        } else if (attribute == bootstrap) {
            _doBootstrap = ((BooleanToken) bootstrap.getToken()).booleanValue();
        } else if (attribute == lowVarianceSampler) {
            _lowVarianceSampler = ((BooleanToken) lowVarianceSampler.getToken())
                    .booleanValue();
        } else if (attribute == resetOnEachRun) {
            _resetOnEachRun = ((BooleanToken) resetOnEachRun.getToken())
                    .booleanValue();
        } else if (attribute == seed) {
            long seedVal = ((LongToken) seed.getToken()).longValue();
            _seed = seedVal;
            _createRandomGenerator();
        } else {
            super.attributeChanged(attribute);
        }
        // If any parameter changes, then the next preinitialize()
        // will recreate the contents.
        _upToDate = false;

        if (attribute instanceof Parameter && attribute != t
                && attribute != stateVariableNames) {
            // If the attribute name matches an input port name,
            // do not reinitialize.
            TypedIOPort port = (TypedIOPort) getPort(attribute.getName());
            if (port == null || !port.isInput()) {
                // Change of any parameter triggers reinitialization.
                _requestInitialization();
            }
        }
    }

    /** Override the base class to first set the value of the
     *  parameter <i>t</i> to match current time, then to set
     *  the local parameters that mirror input values,
     *  and then to fire the contained actors.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Set the time variable.
        double currentTime = getDirector().getModelTime().getDoubleValue();
        t.setToken(new DoubleToken(currentTime));

        super.fire();
        // update measurement values
        Iterator k = _measurementParameters.keySet().iterator();
        while (k.hasNext()) {
            String mvName = (String) k.next();
            Token value = (_measurementParameters.get(mvName).getToken());
            _measurementValues.put(mvName, value);
        }
        for (int i = 0; i < _parameterInputs.size(); i++) {
            PortParameter s = (PortParameter) ParticleFilter.this
                    .getAttribute(_parameterInputs.get(i));
            s.update();
        }

        // The Sequential Monte Carlo algorithm
        try {
            if (_firstIteration) {
                _initializeParticles();
                _normalizeWeights();
                _sendStateEstimate();
                _generateOutputParticles();
                _firstIteration = false;

            } else {
                _propagate();
                _normalizeWeights();
                _sendStateEstimate();
                _generateOutputParticles();
                if (_doBootstrap) {
                    _resample();
                } else if (_getEffectiveSampleSize() < 0.5 * Nparticles) {
                    _resample();
                }
            }
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this,
                    "NameDuplicationException while initializing particles");
        }
    }

    /** Create the model inside from the parameter values.
     *  This method gets write access on the workspace.
     *  @exception IllegalActionException If there is no director,
     *   or if any contained actors throws it in its preinitialize() method.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (_upToDate) {
            super.preinitialize();
            return;
        }
        // Check parameters.
        _checkParameters();

        if (_resetOnEachRun || _random == null) {
            _createRandomGenerator();
        }

        ArrayToken stateNames = (ArrayToken) stateVariableNames.getToken();
        int n = stateNames.length(); // number of state variables
        int m = inputPortList().size(); // number of control inputs
        _stateSpaceSize = n;
        try {
            _workspace.getWriteAccess();
            removeAllEntities();
            removeAllRelations();

            _stateVariables = new String[n];
            _updateEquations = new HashMap<String, Expression>();
            _updateTrees = new HashMap<String, ASTPtRootNode>();
            _measurementEquations = new LinkedList<Expression>();

            for (int i = 0; i < n; i++) {
                _stateVariables[i] = ((StringToken) stateNames.getElement(i))
                        .stringValue().trim();
                // find the state update equation for the current state variable
                Expression e = new Expression(this, _stateVariables[i]
                        + "_update");
                //e.setPersistent(false);
                String updateEqnName = _stateVariables[i] + "_update";
                e.expression
                .setExpression(((Parameter) getAttribute(updateEqnName))
                        .getExpression());
                if (_stateVariables[i] == null) {
                    System.err.println("One state variable is null at index "
                            + i);
                } else {
                    _updateEquations.put(_stateVariables[i], e);
                    _updateTrees.put(_stateVariables[i], new PtParser()
                    .generateParseTree(_updateEquations
                            .get(_stateVariables[i]).expression
                            .getExpression()));
                }
            }
            // put an update tree for the process noise
            _updateTrees.put("processNoise", new PtParser()
            .generateParseTree(processNoise.getExpression()));
            // update tree for the prior distribution
            _updateTrees.put("priorDistribution",
                    new PtParser().generateParseTree(prior.getExpression()));

            // Inputs-make connections
            String[] inputs = new String[m];
            IORelation[] inputRelations = new IORelation[m];
            Iterator inputPorts = inputPortList().iterator();
            int measurementIndex = 0;
            int inputIndex = 0;
            _controlInputs = new HashMap<String, Double>();
            _parameterInputs = new LinkedList<String>();

            while (inputPorts.hasNext()) {
                IOPort p1 = (IOPort) inputPorts.next();
                inputs[inputIndex] = ((NamedObj) p1).getName();
                inputRelations[inputIndex] = new TypedIORelation(this,
                        "relation_" + inputs[inputIndex]);
                inputRelations[inputIndex].setPersistent(false);
                getPort(inputs[inputIndex]).link(inputRelations[inputIndex]);
                String inputName = inputs[inputIndex];
                if (inputName.endsWith("_m")) {
                    measurementIndex = inputIndex;
                    String eqnName = inputName.substring(0,
                            inputName.length() - 2);
                    Expression _measurementEquation = new Expression(this,
                            inputName + "_equation");
                    _measurementEquation.expression
                    .setExpression(((Parameter) getAttribute(eqnName))
                            .getExpression());
                    _measurementEquations.add(_measurementEquation);

                    _measurementCovariance = new Expression(this, inputName
                            + "_covariance");
                    _measurementCovariance.expression
                    .setExpression(((Parameter) getAttribute("measurementCovariance"))
                            .getExpression());

                    SetVariable zm = new SetVariable(this, "set" + inputName);
                    // add new parameter to the actor
                    Parameter measure1;
                    if (this.getAttribute(inputName) == null) {
                        measure1 = new Parameter(this, inputName);
                        measure1.setVisibility(Settable.EXPERT);
                    } else {
                        measure1 = (Parameter) this.getAttribute(inputName);
                    }
                    _measurementParameters.put(inputName, measure1);
                    zm.delayed.setExpression("false");
                    zm.variableName.setExpression(inputName);
                    zm.input.link(inputRelations[measurementIndex]);

                } else {
                    if (p1 instanceof ParameterPort) {
                        _parameterInputs.add(inputName);
                    } else {
                        _controlInputs.put(inputName, 0.0);
                    }
                }
                inputIndex++;
            }

            // Connect state feedback expressions.
            for (int i = 0; i < _stateVariables.length; i++) {

                for (int k = 0; k < m; k++) {
                    Parameter stateUpdateSpec = (Parameter) getAttribute(_stateVariables[i]
                            + "_update");
                    Set<String> freeIdentifiers = stateUpdateSpec
                            .getFreeIdentifiers();
                    // Create an output port only if the expression references the input.
                    if (freeIdentifiers.contains(inputs[k])) {
                        TypedIOPort port = new TypedIOPort(
                                _updateEquations.get(_stateVariables[i]),
                                inputs[k], true, false);
                        //port.setTypeEquals(BaseType.DOUBLE);
                        port.link(inputRelations[k]);
                    }
                }
            }

            _upToDate = true;
        } catch (NameDuplicationException ex) {
            // Should never happen.
            throw new InternalErrorException("Duplicated name when "
                    + "constructing the subsystem" + ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
        // Preinitialize the contained model.
        super.preinitialize();
    }

    @Override
    public void wrapup() throws IllegalActionException {
        _firstIteration = true;
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /**
     * Do a binary interval search for the key in array A. The bin index in which
     * key is found is returned.
     */
    private int _binarySearch(double[] A, double key, int imin, int imax) {
        if (imax < imin) {
            return -1;
        } else {
            int imid = imin + ((imax - imin) / 2);
            if (imid >= A.length - 1) {
                return -1;
            } else if (A[imid] <= key && A[imid + 1] > key) {
                return imid;
            } else if (A[imid] > key) {
                return _binarySearch(A, key, imin, imid - 1);
            } else if (A[imid] < key) {
                return _binarySearch(A, key, imid + 1, imax);
            } else {
                return imid;
            }
        }
    }

    /** Check the dimensions of all parameters and ports.
     *  @exception IllegalActionException If the dimensions are illegal.
     */
    private void _checkParameters() throws IllegalActionException {
        // Check state variable names.
        ArrayToken stateNames = (ArrayToken) stateVariableNames.getToken();
        int n = stateNames.length();

        if (n < 1) {
            throw new IllegalActionException(this, "There must be at "
                    + "least one state variable for the state space model.");
        }

        // Check if any of the state variable names is an empty string.
        for (int i = 0; i < n; i++) {
            String name = ((StringToken) stateNames.getElement(i))
                    .stringValue().trim();

            if (name.equals("")) {
                throw new IllegalActionException(this, "A state variable "
                        + "name should not be an empty string.");
            }

            // Check state equations.
            String equation = name + "_update";

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

    private void _createRandomGenerator() throws IllegalActionException {

        _seed = ((LongToken) seed.getToken()).longValue();
        if (_seed == 0L) {
            _seed = System.currentTimeMillis() + hashCode();
        } else {
            _seed = _seed + getFullName().hashCode();
        }
        _random = new Random(_seed);
    }

    /**
     * Generate output particles and send to the particleOutput port
     */
    private void _generateOutputParticles() throws IllegalActionException {
        Token[] tokens = new Token[_stateSpaceSize + 1];
        if (Noutput != Nparticles) {
            int[] indices = _subsampleIndices();
            Particle[] outputParticles = new Particle[Noutput];
            double sum = 0;
            for (int i = 0; i < Noutput; i++) {
                outputParticles[i] = new Particle(particles[indices[i]]);
                sum += outputParticles[i].getWeight();
            }
            for (int i = 0; i < Noutput; i++) {
                outputParticles[i].adjustWeight(sum);
            }

            for (int i = 0; i < Noutput; i++) {
                double[] l = outputParticles[i].getValue();
                for (int j = 0; j < _stateSpaceSize; j++) {
                    tokens[j] = new DoubleToken(l[j]);
                }
                tokens[_stateSpaceSize] = new DoubleToken(
                        outputParticles[i].getWeight());
                RecordToken r = new RecordToken(_particleLabels, tokens);
                particleOutput.send(0, r);
            }
        } else {
            for (int i = 0; i < Nparticles; i++) {
                double[] l = particles[i].getValue();
                for (int j = 0; j < _stateSpaceSize; j++) {
                    tokens[j] = new DoubleToken(l[j]);
                }
                tokens[_stateSpaceSize] = new DoubleToken(
                        particles[i].getWeight());
                RecordToken r = new RecordToken(_particleLabels, tokens);
                particleOutput.send(0, r);
            }
        }
    }

    private double _getEffectiveSampleSize() {
        double sum = 0;
        for (int i = 0; i < particles.length; i++) {
            if (!SignalProcessing.close(particles[i].getWeight(), 0)) {
                //System.out.println(particles[i].getWeight());
                sum += Math.pow(particles[i].getWeight(), 2);
            }
        }
        sum = 1.0 / sum;
        return sum;
    }

    /** Initialize the class. */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        StringToken[] empty = new StringToken[1];
        stateVariableNames = new Parameter(this, "stateVariableNames");
        empty[0] = new StringToken("");
        stateVariableNames.setToken(new ArrayToken(BaseType.STRING, empty));

        bootstrap = new Parameter(this, "bootstrap");
        bootstrap.setTypeEquals(BaseType.BOOLEAN);
        bootstrap.setExpression("true");

        lowVarianceSampler = new Parameter(this, "lowVarianceSampler");
        lowVarianceSampler.setTypeEquals(BaseType.BOOLEAN);
        lowVarianceSampler.setExpression("false");

        particleCount = new Parameter(this, "particleCount");
        particleCount.setExpression("1000");
        Nparticles = 1000;

        outputParticleCount = new Parameter(this, "outputParticleCount");
        outputParticleCount.setExpression("100");
        Noutput = 100;

        processNoise = new Parameter(this, "processNoise");
        processNoise
        .setExpression("multivariateGaussian({0.0,0.0},[1.0,0.4;0.4,1.2])");

        particleOutput = new TypedIOPort(this, "particleOutput", false, true);
        //particleOutput.setTypeEquals(BaseType.DOUBLE);
        //setClassName("org.ptolemy.machineLearning.ParticleFilter");
        particleOutput.setTypeEquals(RecordType.EMPTY_RECORD);

        seed = new SharedParameter(this, "seed");
        seed.setExpression("0L");
        seed.setTypeEquals(BaseType.LONG);
        seed.setVisibility(Settable.EXPERT);

        resetOnEachRun = new SharedParameter(this, "resetOnEachRun");
        resetOnEachRun.setExpression("false");
        resetOnEachRun.setVisibility(Settable.EXPERT);
        resetOnEachRun.setTypeEquals(BaseType.BOOLEAN);

        stateEstimate = new TypedIOPort(this, "stateEstimate", false, true);
        stateEstimate.setTypeEquals(RecordType.EMPTY_RECORD);

        prior = new Parameter(this, "prior");
        prior.setExpression("random()*200-100");

        t = new Parameter(this, "t");
        t.setTypeEquals(BaseType.DOUBLE);
        t.setVisibility(Settable.EXPERT);
        t.setExpression("0.0");

        measurementCovariance = new Parameter(this, "measurementCovariance");
        measurementCovariance.setExpression("[10.0,0.0;0.0,10.0]");

        _measurementParameters = new HashMap<String, Parameter>();
        _measurementValues = new HashMap<String, Token>();
        _measurementEquations = new LinkedList<Expression>();

        _firstIteration = true;
        particles = new Particle[Nparticles];

        _createRandomGenerator();

        _tokenMap = new HashMap<String, Token>();

        _parseTreeEvaluator = new ParseTreeEvaluator();
        _scope = new VariableScope();

        new DEDirector(this, "DEDirector").setPersistent(false);
        //((Parameter)this.getAttribute("_isOpaque")).setExpression("true");
        // icon
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-30\" " + "width=\"150\" height=\"60\" "
                + "style=\"fill:white\"/>\n" + "<text x=\"-45\" y=\"-10\" "
                + "style=\"font-size:14\">\n" + "x_{t+1}=f(x, u, t, w)"
                + "</text>\n" + "<text x=\"-45\" y=\"10\" "
                + "style=\"font-size:14\">\n" + "     y=g(x, u, t, v)"
                + "</text>\n" + "style=\"fill:blue\"/>\n" + "</svg>\n");
    }

    private void _initializeParticles() throws IllegalActionException,
    NameDuplicationException {
        // let prior distribution be N(0,1) for now.
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle(_stateSpaceSize);
            particles[i].sampleFromPrior();
            particles[i].assignWeight();
        }
    }

    private void _normalizeWeights() throws IllegalActionException,
    NameDuplicationException {
        // let prior distribution be N(0,1) for now.
        double sum = 0;
        for (int i = 0; i < particles.length; i++) {
            sum += particles[i].getWeight();
        }
        for (int i = 0; i < particles.length; i++) {
            particles[i].adjustWeight(sum);
        }
    }

    /** Propagate particles according to the state update equations
     */
    private void _propagate() throws IllegalActionException,
    NameDuplicationException {
        for (int i = 0; i < Nparticles; i++) {
            particles[i].setNextParticle();
        }
    }

    /** Set this composite actor to opaque and request for reinitialization
     *  from the director if there is one.
     */
    private void _requestInitialization() {
        // Request for initialization.
        Director dir = getExecutiveDirector();

        if (dir != null) {
            dir.requestInitialization(this);
        }
    }

    private void _resample() throws IllegalActionException {
        double randomValue;
        int intervalIndex;
        double[] cumulativeSums = new double[Nparticles + 1];
        Particle[] previousParticles = new Particle[Nparticles];
        cumulativeSums[0] = 0;
        for (int i = 0; i < Nparticles; i++) {
            double w = particles[i].getWeight();
            cumulativeSums[i + 1] = cumulativeSums[i] + w;
            previousParticles[i] = particles[i];
        }
        // If low-variance sampling has been selected, sample a random particle in [0,1/Nparticles]
        // and choose all other particles in reference to the first sample. Yields a low-variance
        // particle set.
        if (_lowVarianceSampler) {
            double baseValue = _random.nextDouble() * (1.0 / Nparticles);
            for (int i = 0; i < Nparticles; i++) {
                randomValue = baseValue + i * 1.0 / Nparticles;
                intervalIndex = _binarySearch(cumulativeSums, randomValue, 0,
                        Nparticles);
                //FIXME: check intervalIndex and remove the failure condition
                if (intervalIndex < 0 || intervalIndex > Nparticles - 1) {
                    System.out.println("Index does not exist!");
                } else {
                    particles[i] = new Particle(particles[i].getSize());
                    particles[i].setValue(previousParticles[intervalIndex]
                            .getValue());
                    // the weights are equal at a result of resampling
                    particles[i].setWeight(1.0 / Nparticles);
                }
            }

        } else {
            // will resample particles according to their weights
            // last entry of cumulative sums is the range of the random variable
            // resampling to set equal weights

            for (int i = 0; i < Nparticles; i++) {
                randomValue = _random.nextDouble() * cumulativeSums[Nparticles];
                intervalIndex = _binarySearch(cumulativeSums, randomValue, 0,
                        Nparticles);
                if (intervalIndex < 0 || intervalIndex > Nparticles - 1) {
                    System.out.println("Index does not exist!");
                } else {
                    particles[i] = new Particle(particles[i].getSize());
                    particles[i].setValue(previousParticles[intervalIndex]
                            .getValue());
                    // the weights are equal at a result of resampling
                    particles[i].setWeight(1.0 / Nparticles);
                }
            }
        }

    }

    /**
     * Generate state estimate from the current particle set and send to the stateEstimate output
     */
    private void _sendStateEstimate() throws IllegalActionException {
        Token[] stateTokens = new Token[_stateSpaceSize];
        double[] stateValues = new double[_stateSpaceSize];
        double weight = 0;
        for (int i = 0; i < Nparticles; i++) {
            Particle p = particles[i];
            double[] pVal = p.getValue();
            weight = p.getWeight();
            for (int j = 0; j < pVal.length; j++) {
                stateValues[j] += weight * pVal[j];
            }
        }
        for (int j = 0; j < _stateSpaceSize; j++) {
            DoubleToken sToken = new DoubleToken(stateValues[j]);
            stateTokens[j] = sToken;
        }
        stateEstimate.send(0, new RecordToken(_stateLabels, stateTokens));

    }

    private int[] _subsampleIndices() {
        int N = Noutput;
        int[] outputIndices = new int[N];
        // return a subsample of particles at the chosen indices
        double randomValue;
        int intervalIndex;
        double[] cumulativeSums = new double[Nparticles + 1];
        Particle[] previousParticles = new Particle[Nparticles];
        cumulativeSums[0] = 0;
        for (int i = 0; i < Nparticles; i++) {
            cumulativeSums[i + 1] = cumulativeSums[i]
                    + particles[i].getWeight();
            previousParticles[i] = particles[i];
        }
        // If low-variance sampling has been selected, sample a random particle in [0,1/Nparticles]
        // and choose all other particles in reference to the first sample. Yields a low-variance
        // particle set.
        if (_lowVarianceSampler) {
            double baseValue = _random.nextDouble() * (1.0 / Noutput);
            for (int i = 0; i < Noutput; i++) {
                randomValue = baseValue + i * 1.0 / Noutput;
                intervalIndex = _binarySearch(cumulativeSums, randomValue, 0,
                        Nparticles - 1);
                //FIXME: check intervalIndex and remove the failure condition
                if (intervalIndex < 0 || intervalIndex > Nparticles - 1) {
                    System.out.println("Index does not exist!");
                } else {
                    outputIndices[i] = intervalIndex;
                }
            }

        } else {
            // will resample particles according to their weights
            // last entry of cumulative sums is the range of the random variable
            // resampling to set equal weights
            for (int i = 0; i < Noutput; i++) {
                randomValue = _random.nextDouble() * cumulativeSums[Nparticles];
                intervalIndex = _binarySearch(cumulativeSums, randomValue, 0,
                        Nparticles - 1);
                if (intervalIndex < 0 || intervalIndex > Nparticles - 1) {
                    System.out.println("Index does not exist!");
                } else {
                    outputIndices[i] = intervalIndex;
                }
            }
        }

        return outputIndices;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    /**
     * Boolean that defines whether the bootstrap particle filter algorithm will be used
     */
    private boolean _doBootstrap;

    /** Boolean choice to use a low-variance sampler for sampling particles */
    private boolean _lowVarianceSampler;

    /** Flag indicating whether the contained model is up to date. */
    private boolean _upToDate;

    /** List that holds the measurement equation expression objects */
    private List<Expression> _measurementEquations;

    /** Internal particle representation that has memory of 1 in time */
    private Particle[] particles;

    /** Number of particles to be used by the particle filter estimators */
    private int Nparticles;

    /** Number of particles to be produced at the output port at each iteration */
    private int Noutput;

    //TODO: Add seed for random number generation.
    private Random _random;

    /** Public seed for random number generation */
    private long _seed;

    /** State-space size */
    private int _stateSpaceSize;

    /** Names of state variables */
    private String[] _stateVariables;

    /** State update equations, hashed by state variable name */
    private HashMap<String, Expression> _updateEquations;

    /** Values of control inputs, hashed by input name */
    private HashMap<String, Double> _controlInputs;

    /** Names of the PortParameter inputs */
    private List<String> _parameterInputs;

    private HashMap _tokenMap;

    private String[] _particleLabels;

    private Type[] _particleTypes;

    private boolean _resetOnEachRun;

    private String[] _stateLabels;

    private Type[] _stateTypes;

    private double[][] _Sigma;

    private ASTPtRootNode _parseTree;
    private HashMap<String, ASTPtRootNode> _updateTrees;
    private ParseTreeEvaluator _parseTreeEvaluator;
    private VariableScope _scope;
    private boolean _firstIteration;
    private HashMap<String, Parameter> _measurementParameters;
    // The values of the measurement inputs at the given iteration.
    private HashMap<String, Token> _measurementValues;
    private Expression _measurementCovariance;

    // set particle dimensions to be equal to the state space dimension
    private class Particle {
        public Particle(int size) {
            this._particleValue = new double[size];
        }

        public Particle(Particle p) {
            this._weight = p.getWeight();
            this._particleValue = new double[p.getSize()];
            double[] tempParticle = p.getValue();
            for (int i = 0; i < p.getSize(); i++) {
                this._particleValue[i] = tempParticle[i];
            }
        }

        public void assignWeight() throws IllegalActionException,
        NameDuplicationException {
            Token[] _result = new Token[_measurementEquations.size()];
            Token _particleCovariance;
            Parameter p;
            if (this.getSize() != _stateVariables.length) {
                throw new IllegalActionException(
                        "Particle dimensions must be equal to the state space dimension");
            } else {
                for (int i = 0; i < _stateVariables.length; i++) {
                    if ((ParticleFilter.this).getAttribute(_stateVariables[i]) == null) {
                        p = new Parameter(ParticleFilter.this,
                                _stateVariables[i]);
                        p.setVisibility(Settable.EXPERT);
                    } else {
                        p = (Parameter) (ParticleFilter.this)
                                .getAttribute(_stateVariables[i]);
                    }
                    p.setExpression(((Double) _particleValue[i]).toString());
                    _tokenMap.put(_stateVariables[i], new DoubleToken(
                            _particleValue[i]));
                }
                try {
                    PtParser parser = new PtParser();
                    Iterator i = _measurementEquations.iterator();
                    int ind = 0;
                    // get the measurements for all measurement ports
                    while (i.hasNext()) {
                        Expression measurementEquation = (Expression) i.next();
                        _parseTree = parser
                                .generateParseTree(measurementEquation.expression
                                        .getExpression());
                        _result[ind] = _parseTreeEvaluator.evaluateParseTree(
                                _parseTree, _scope);
                        if (_result[ind] == null) {
                            throw new IllegalActionException(
                                    "Expression yields a null result: "
                                            + measurementEquation.expression
                                            .getExpression());
                        }
                        ind++;
                    }

                    _parseTree = parser
                            .generateParseTree(_measurementCovariance.expression
                                    .getExpression());
                    _particleCovariance = _parseTreeEvaluator
                            .evaluateParseTree(_parseTree, _scope);
                } catch (Throwable throwable) {
                    // Chain exceptions to get the actor that threw the exception.
                    // Note that if evaluateParseTree does a divide by zero, we
                    // need to catch an ArithmeticException here.
                    throw new IllegalActionException("Expression invalid.");
                }

                // set particle weight

                Type t = _measurementEquations.get(0).output.getType();
                if (t.equals(BaseType.DOUBLE)) {
                    // one-dimensional measurements
                    _weight = 1;
                    for (int i = 0; i < _measurementEquations.size(); i++) {
                        String variableName = _measurementEquations.get(i)
                                .getName();
                        //lose the "_equation" postfix
                        variableName = variableName.substring(0,
                                variableName.length() - 9);
                        double _meanEstimate = ((DoubleToken) _result[i])
                                .doubleValue();

                        if (_measurementValues.containsKey(variableName)) {
                            double z_t = ((DoubleToken) _measurementValues
                                    .get(variableName)).doubleValue();
                            _weight *= 1
                                    / (Math.pow(2 * Math.PI, 0.5) * DoubleMatrixMath
                                            .determinant(_Sigma))
                                            * Math.exp(-Math
                                            .pow(z_t - _meanEstimate, 2)
                                                    / (2 * Math.pow(_Sigma[0][0], 2)));
                        }
                    }

                } else {
                    _weight = 1;
                    for (int i = 0; i < _measurementEquations.size(); i++) {
                        String variableName = _measurementEquations.get(i)
                                .getName();
                        //lose the "_equation" postfix
                        variableName = variableName.substring(0,
                                variableName.length() - 9);
                        if (_measurementValues.containsKey(variableName)) {
                            MatrixToken z_t = (MatrixToken) _measurementValues
                                    .get(variableName);
                            int k = z_t.getRowCount();
                            MatrixToken X = (DoubleMatrixToken) z_t
                                    .subtract(_result[i]);
                            MatrixToken Covariance = (DoubleMatrixToken) _particleCovariance;
                            //MatrixToken Cov = new DoubleMatrixToken(DoubleMatrixMath.inverse(Covariance.doubleMatrix()));
                            MatrixToken Xt = new DoubleMatrixToken(
                                    DoubleMatrixMath.transpose(X.doubleMatrix()));
                            double multiplier = Math.pow(2 * Math.PI, -0.5 * k)
                                    * Math.pow(DoubleMatrixMath
                                            .determinant(Covariance
                                                    .doubleMatrix()), -0.5);
                            Token exponent = Xt.multiply(Covariance);
                            exponent = exponent.multiply(X);
                            double value = ((DoubleMatrixToken) exponent)
                                    .getElementAt(0, 0);
                            _weight *= multiplier * Math.exp(-0.5 * value);
                        }
                    }
                }
            }
        }

        public boolean adjustWeight(double w) {
            // normalize weight
            if (w > 0.0) {
                _weight = _weight / w;
            } else {
                return false;
            }
            return true;
        }

        public double[] getValue() {
            double[] values = new double[this.getSize()];
            for (int i = 0; i < this.getSize(); i++) {
                values[i] = this._particleValue[i];
            }
            return values;
        }

        public int getSize() {
            return this._particleValue.length;
        }

        public void sampleFromPrior() throws IllegalActionException {
            _parseTree = _updateTrees.get("priorDistribution");
            Token priorSample = _parseTreeEvaluator.evaluateParseTree(
                    _parseTree, _scope);

            if (priorSample == null) {
                throw new IllegalActionException(
                        "Expression yields a null result: "
                                + prior.getExpression());
            }

            Type t = priorSample.getType();
            if (t.equals(BaseType.DOUBLE)) {
                // one dimensional
                if (this.getSize() != 1) {
                    throw new IllegalActionException(
                            "Prior distribution and state space dimensions must match.");
                }
                double value = ((DoubleToken) priorSample).doubleValue();
                _particleValue[0] = value;
            } else {
                Token[] vals = ((ArrayToken) priorSample).arrayValue();
                if (vals.length != this.getSize()) {
                    throw new IllegalActionException(
                            "Prior distribution and state space dimensions must match.");
                }
                for (int i = 0; i < this.getSize(); i++) {
                    double value = ((DoubleToken) vals[i]).doubleValue();
                    _particleValue[i] = value;
                }

            }
        }

        public void setNextParticle() throws NameDuplicationException,
        IllegalActionException {
            Token _result;
            //FIXME: the noise sample does not have to be an arrayToken
            Token processNoiseSample;
            double[] newParticle = new double[this.getSize()];
            for (int i = 0; i < _stateSpaceSize; i++) {
                // every component of the particle will be propagated according to its own update equation.
                //Parameter p = new Parameter(_updateEquations.get(_stateVariables[i]), _stateVariables[i]);
                Parameter p = (Parameter) (_updateEquations
                        .get(_stateVariables[i]))
                        .getAttribute(_stateVariables[i]);
                if (p != null) {
                    p.setExpression(((Double) _particleValue[i]).toString());
                } else {
                    p = new Parameter(_updateEquations.get(_stateVariables[i]),
                            _stateVariables[i]);
                    p.setExpression(((Double) _particleValue[i]).toString());
                }
                _tokenMap.put(_stateVariables[i], new DoubleToken(
                        _particleValue[i]));
                Iterator ci = _controlInputs.keySet().iterator();
                // set the control input values in scope
                while (ci.hasNext()) {
                    String controlVarName = (String) ci.next();
                    Parameter c = (Parameter) (_updateEquations
                            .get(_stateVariables[i]))
                            .getAttribute(controlVarName);
                    if (c != null) {
                        c.setExpression(_controlInputs.get(controlVarName)
                                .toString());
                    } else {
                        c = new Parameter(
                                _updateEquations.get(_stateVariables[i]),
                                controlVarName);
                        c.setExpression(_controlInputs.get(controlVarName)
                                .toString());
                    }
                    _tokenMap.put(controlVarName, new DoubleToken(
                            _controlInputs.get(controlVarName)));
                }
            }

            try {
                _parseTree = _updateTrees.get("processNoise");
                processNoiseSample = _parseTreeEvaluator.evaluateParseTree(
                        _parseTree, _scope);
            } catch (Throwable throwable) {
                // Chain exceptions to get the actor that threw the exception.
                // Note that if evaluateParseTree does a divide by zero, we
                // need to catch an ArithmeticException here.
                throw new IllegalActionException("Expression invalid.");
            }
            if (processNoiseSample == null) {
                throw new IllegalActionException(
                        "Expression yields a null result: "
                                + processNoise.getExpression());
            }

            for (int i = 0; i < _stateSpaceSize; i++) {
                try {
                    _parseTree = _updateTrees.get(_stateVariables[i]);
                    _result = _parseTreeEvaluator.evaluateParseTree(_parseTree,
                            _scope);
                } catch (Throwable throwable) {
                    // Chain exceptions to get the actor that threw the exception.
                    // Note that if evaluateParseTree does a divide by zero, we
                    // need to catch an ArithmeticException here.
                    throw new IllegalActionException("Expression invalid.");
                }

                if (_result == null) {
                    throw new IllegalActionException(
                            "Expression yields a null result: "
                                    + _updateEquations.get(_stateVariables[i]).expression
                                    .getExpression());
                }
                // set particle weight

                double _meanEstimate = ((DoubleToken) _result
                        .add(new DoubleToken(0.0))).doubleValue();
                //FIXME: what if the process noise sample is not an array token?
                double processNoiseForElement = ((DoubleToken) ((ArrayToken) processNoiseSample)
                        .getElement(i)).doubleValue();
                newParticle[i] = _meanEstimate + processNoiseForElement;
            }
            // set control inputs in range and also assigned state variable values to equal the current particle
            // value
            this.setValue(newParticle);
            this.assignWeight();

        }

        public void setValue(double[] l) throws IllegalActionException {

            if (l.length != this._particleValue.length) {
                throw new IllegalActionException(
                        "Cannot set a value with different size");
            }

            for (int i = 0; i < l.length; i++) {
                _particleValue[i] = l[i];
            }
        }

        public void setWeight(double weight) {
            _weight = weight;
        }

        public double getWeight() {
            return _weight;
        }

        /**
         * Value of the particle. Size is equal to _ssSize
         */
        private final double[] _particleValue;
        /**
         * Weight of the particle
         */
        private double _weight;
    }

    private class VariableScope extends ModelScope {
        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        @Override
        public Token get(String name) throws IllegalActionException {
            if (name.equals("time") || name.equals("t")) {
                return new DoubleToken(getDirector().getModelTime()
                        .getDoubleValue());
            }

            Token token = (DoubleToken) _tokenMap.get(name);

            if (token != null) {
                return token;
            }

            Variable result = getScopedVariable(null, ParticleFilter.this, name);

            if (result != null) {
                return result.getToken();
            }

            return null;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        @Override
        public Type getType(String name) throws IllegalActionException {
            if (name.equals("time") || name.equals("t")) {
                return BaseType.DOUBLE;
            } else if (name.equals("iteration")) {
                return BaseType.INT;
            }

            // Check the port names.
            TypedIOPort port = (TypedIOPort) getPort(name);

            if (port != null) {
                return port.getType();
            }

            Variable result = getScopedVariable(null, ParticleFilter.this, name);

            if (result != null) {
                return (Type) result.getTypeTerm().getValue();
            }
            return null;
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            if (name.equals("time")) {
                return new TypeConstant(BaseType.DOUBLE);
            } else if (name.equals("iteration")) {
                return new TypeConstant(BaseType.INT);
            }

            // Check the port names.
            TypedIOPort port = (TypedIOPort) getPort(name);

            if (port != null) {
                return port.getTypeTerm();
            }

            Variable result = getScopedVariable(null, ParticleFilter.this, name);

            if (result != null) {
                return result.getTypeTerm();
            }
            return null;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        @Override
        public Set identifierSet() {
            return getAllScopedVariableNames(null, ParticleFilter.this);
        }
    }

}
