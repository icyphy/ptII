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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.ptolemy.machineLearning.Algorithms;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken; 
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
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
import ptolemy.math.SignalProcessing;

///////////////////////////////////////////////////////////////////
////

/**
 Abstract Predictor Base Class.
 @see org.ptolemy.machineLearning.particleFilter.ParticleFilter

 @author Ilge Akkaya
 @version $Id: ParticleFilter.java 70534 2014-11-11 21:56:08Z ilgea $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating Red (ilgea)

 */
public abstract class AbstractPredictor extends TypedCompositeActor {
    /** Construct the composite actor with a name and a container.
     *  This constructor creates the ports, parameters, and the icon.
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public AbstractPredictor(CompositeEntity container, String name)
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
    public AbstractPredictor(Workspace workspace) throws IllegalActionException,
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

    public TypedIOPort particleInput;

    public Parameter predictionStep;


    /** The output port that outputs the produced particles at each firing.
     */
    public TypedIOPort particleOutput;

    /** State estimate output. A record token with one field per state variable */

    public TypedIOPort stateEstimate; 

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

        if (attribute == predictionStep) {
            _Nstep = ((IntToken)predictionStep.getToken()).intValue();
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

        if (attribute instanceof Parameter && attribute != t) {
            // If the attribute name matches an input port name,
            // do not reinitialize.
            TypedIOPort port = (TypedIOPort) getPort(attribute.getName());

            if (port == null || !port.isInput()) { 
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

        if (particleInput.hasToken(0)) {
            double currentTime = getDirector().getModelTime().getDoubleValue();
            t.setToken(new DoubleToken(currentTime)); 

            try {

                _initializeParticles();

                super.fire(); 

                for (int i = 0; i < _Nstep ; i++) {
                    _propagate();
                    _normalizeWeights(); 
                    if (_doBootstrap) {
                        _resample();
                    } else if (_getEffectiveSampleSize() < 0.5 * Nparticles) {
                        _resample();
                    }
                }

                _sendStateEstimate();
                _generateOutputParticles();


            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this,
                        "NameDuplicationException while initializing particles");
            }
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

        // Make sure all necessary parameters are provided.
        _checkParameters();

        if (_resetOnEachRun || _random == null) {
            _createRandomGenerator();
        }

        int m = inputPortList().size(); // number of control inputs
        _stateSpaceSize = _stateNames.length(); 
        if (_stateSpaceSize > 0) {
            // Set the output type according to the state variables
            _particleLabels = new String[_stateSpaceSize + 1];
            _particleTypes = new Type[_stateSpaceSize + 1];
            _stateLabels = new String[_stateSpaceSize];
            _stateTypes = new Type[_stateSpaceSize];
            Token[] nameTokens = ((ArrayToken)_stateNames).arrayValue();
            for (int i = 0; i < _stateSpaceSize; i++) {

                String variableName = ((StringToken)nameTokens[i]).stringValue();

                try {
                    // add a hidden parameter for each state variable name
                    Parameter varPar = (Parameter) this.getAttribute(variableName);
                    if ( varPar == null) {
                        varPar = new Parameter(this, variableName);
                        varPar.setTypeEquals(BaseType.DOUBLE);
                        varPar.setExpression("0.0");
                    } 
                    ((Parameter)varPar).setVisibility(Settable.NONE); 
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }

                _particleLabels[i] = variableName;
                _particleTypes[i] = BaseType.DOUBLE; // preset to be double

                _stateLabels[i] = variableName;
                _stateTypes[i] = BaseType.DOUBLE; // preset to be double
            }
            _particleLabels[nameTokens.length] = "weight";
            _particleTypes[nameTokens.length] = BaseType.DOUBLE;

            particleOutput.setTypeEquals(new ArrayType(new RecordType(
                    _particleLabels, _particleTypes)));
            stateEstimate.setTypeEquals(new RecordType(_stateLabels,
                    _stateTypes));
        }



        try {
            _workspace.getWriteAccess();
            removeAllEntities();
            removeAllRelations();

            _stateVariables = new String[_stateSpaceSize];

            _updateEquations = new HashMap<>();
            _updateTrees = new HashMap<>(); 

            _setUpdateEquations();

            // Inputs-make connections
            String[] inputs = new String[m];
            _inputRelations = new IORelation[m]; 
            int inputIndex = 0;
            _controlInputs = new HashMap<String, Double>();
            _parameterInputs = new LinkedList<String>();

            for (Object p : this.inputPortList()) {

                IOPort p1 = (IOPort) p;
                if (!p1.getName().equals("particleInput")) {
                    inputs[inputIndex] = ((NamedObj) p1).getName();
                    _inputRelations[inputIndex] = new TypedIORelation(this,
                            "relation_" + inputs[inputIndex]);
                    _inputRelations[inputIndex].setPersistent(false);
                    getPort(inputs[inputIndex]).link(_inputRelations[inputIndex]);
                    String inputName = inputs[inputIndex];

                    if (getUserDefinedParameter(MEASUREMENT_NOISE) != null) {
                        _Sigma = ((DoubleMatrixToken)
                                getUserDefinedParameter(MEASUREMENT_NOISE).getToken()).doubleMatrix();
                    }

                    if (inputName.endsWith(MEASUREMENT_POSTFIX)) {
                        // do nothing _setMeasurementEquations(inputName, inputIndex);
                    } else {
                        if (p1 instanceof ParameterPort) {
                            _parameterInputs.add(inputName);
                        } else {
                            _controlInputs.put(inputName, 0.0);
                        }
                    }
                    inputIndex++;
                }
            }

            // Connect state feedback expressions.
            for (int i = 0; i < _stateVariables.length; i++) { 
                for (int k = 0; k < m; k++) {
                    Parameter stateUpdateSpec = 
                            (Parameter) getUserDefinedParameter(_stateVariables[i]
                                    + UPDATE_POSTFIX);
                    Set<String> freeIdentifiers = stateUpdateSpec
                            .getFreeIdentifiers();
                    // Create an output port only if the expression references the input.
                    if (freeIdentifiers.contains(inputs[k])) {
                        TypedIOPort port = new TypedIOPort(
                                _updateEquations.get(_stateVariables[i]),
                                inputs[k], 
                                true, 
                                false);

                        port.link(_inputRelations[k]);
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


    //////////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    /** Check the dimensions of all parameters and ports.
     *  @exception IllegalActionException If the dimensions are illegal.
     */
    protected abstract void _checkParameters() throws IllegalActionException; 

    /**
     * Return the expression for a user-defined parameter.
     * @param parameterName Name of parameter
     * @return parameter expression
     * @throws IllegalActionException
     */ 
    protected String getUserDefinedParameterExpression(String parameterName) 
            throws IllegalActionException {
        Parameter param = getUserDefinedParameter(parameterName);
        if (param != null) {
            return param.getExpression();
        } else {
            throw new IllegalActionException("Parameter " 
                    + parameterName + " value is null.");
        }
    }
    /**
     * Return the Parameter that is part of a state space model.
     * @param parameterName Name of parameter
     * @return Parameter object
     * @throws IllegalActionException
     */
    protected abstract Parameter getUserDefinedParameter(String parameterName) 
            throws IllegalActionException;


    /** Flag indicating whether the contained model is up to date. */
    protected boolean _upToDate;

    /** Cached State variable names */
    protected ArrayToken _stateNames;

    /** Array of input Relations */
    protected IORelation[] _inputRelations;

    /** Labels of particles, that contains state names and a weight label */
    protected String[] _particleLabels;

    /** Types of each particle dimension */
    protected Type[] _particleTypes;

    /** Labels of states */
    protected String[] _stateLabels;

    /** Types of each state dimension */
    protected Type[] _stateTypes;

    /** Measurement covariance matrix */
    protected double[][] _Sigma;

    //////////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
        Token[] particleTokens = new Token[Nparticles];
        Token[] tokens = new Token[_stateSpaceSize + 1];

        for (int i = 0; i < Nparticles; i++) {
            double[] l = particles[i].getValue();
            for (int j = 0; j < _stateSpaceSize; j++) {
                tokens[j] = new DoubleToken(l[j]);
            }
            tokens[_stateSpaceSize] = new DoubleToken(
                    particles[i].getWeight());
            RecordToken r = new RecordToken(_particleLabels, tokens);
            particleTokens[i] = r;
        }

        particleOutput.send(0, new ArrayToken(particleTokens));
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

        bootstrap = new Parameter(this, "bootstrap");
        bootstrap.setTypeEquals(BaseType.BOOLEAN);
        bootstrap.setExpression("true");

        lowVarianceSampler = new Parameter(this, "lowVarianceSampler");
        lowVarianceSampler.setTypeEquals(BaseType.BOOLEAN);
        lowVarianceSampler.setExpression("false"); 

        particleOutput = new TypedIOPort(this, "particleOutput", false, true);
        //particleOutput.setTypeEquals(BaseType.DOUBLE);
        //setClassName("org.ptolemy.machineLearning.ParticleFilter");
        particleOutput.setTypeEquals(new ArrayType(RecordType.EMPTY_RECORD));

        particleInput = new TypedIOPort(this, "particleInput", true, false);

        predictionStep = new Parameter(this, "predictionStep");
        predictionStep.setExpression("1");
        predictionStep.setTypeEquals(BaseType.INT);
        _Nstep = 1;

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


        t = new Parameter(this, "t");
        t.setTypeEquals(BaseType.DOUBLE);
        t.setVisibility(Settable.EXPERT);
        t.setExpression("0.0");

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

    /**
     * Read input token and initialize particles accordingly
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    private void _initializeParticles() throws IllegalActionException,
    NameDuplicationException {

        // get input particles and set length of array.
        ArrayToken particleArray = (ArrayToken) particleInput.get(0);
        Nparticles = particleArray.length(); 
        particles = new Particle[Nparticles];
        // let prior distribution be N(0,1) for now.
        RecordToken t = (RecordToken) particleArray.getElement(0);
        // FIXME: Should type check here
        double[] value = new double[t.labelSet().size()-1];
        for (int i = 0; i < particles.length; i++) {
            t = (RecordToken) particleArray.getElement(i);
            for (int k = 0; k < _stateVariables.length; k++) {
                value[k] = ((DoubleToken)t.get(_stateVariables[k])).doubleValue();
            } 
            particles[i] = new Particle(value.length);
            particles[i].setValue(value);
            particles[i].setWeight(((DoubleToken)t.get("weight")).doubleValue());
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
                intervalIndex = Algorithms._binaryIntervalSearch(cumulativeSums, randomValue, 0,
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
                intervalIndex = Algorithms._binaryIntervalSearch(cumulativeSums, randomValue, 0,
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

    private void _setUpdateEquations() 
            throws NameDuplicationException, IllegalActionException {
        for (int i = 0; i < _stateSpaceSize; i++) {
            _stateVariables[i] = ((StringToken) _stateNames.getElement(i))
                    .stringValue().trim();
            // find the state update equation for the current state variable
            Expression e = new Expression(this, _stateVariables[i]
                    + UPDATE_POSTFIX);
            //e.setPersistent(false);
            String updateEqnName = _stateVariables[i] + UPDATE_POSTFIX;
            e.expression
            .setExpression(getUserDefinedParameterExpression(updateEqnName));
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
        _updateTrees.put(PROCESS_NOISE, new PtParser()
        .generateParseTree(getUserDefinedParameterExpression(PROCESS_NOISE)));
        // update tree for the prior distribution
        _updateTrees.put(PRIOR_NAME,
                new PtParser()
        .generateParseTree(getUserDefinedParameterExpression(PRIOR_NAME)));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    /**
     * Boolean that defines whether the bootstrap particle filter algorithm will be used
     */
    private boolean _doBootstrap;

    /** Boolean choice to use a low-variance sampler for sampling particles */
    private boolean _lowVarianceSampler;  

    /** Internal particle representation that has memory of 1 in time */
    private Particle[] particles;

    /** Number of particles to be used by the particle filter estimators */
    private int Nparticles; 

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

    private boolean _resetOnEachRun;
    private ASTPtRootNode _parseTree;
    private HashMap<String, ASTPtRootNode> _updateTrees;
    private ParseTreeEvaluator _parseTreeEvaluator;
    private VariableScope _scope; 
    private int _Nstep;
 

    protected static final String STATE_VARIABLE_NAMES = "stateVariableNames";
    protected static final String PROCESS_NOISE = "processNoise";
    protected static final String MEASUREMENT_NOISE = "measurementCovariance"; 
    protected static final String UPDATE_POSTFIX = "_update";
    protected static final String MEASUREMENT_POSTFIX = "_m";
    protected static final String PRIOR_NAME = "prior";

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


        public void setNextParticle() throws NameDuplicationException,
        IllegalActionException {

            Token _result;
            //FIXME: the noise sample does not have to be an arrayToken

            Token processNoiseSample;
            double[] newParticle = new double[this.getSize()];

            for (int i = 0; i < _stateSpaceSize; i++) {
                // every component of the particle will be propagated according to its own update equation.

                Expression updateExpression = _updateEquations
                        .get(_stateVariables[i]);

                Parameter p = (Parameter) updateExpression
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
                // set the control input values in scope
                for (String controlVarName : _controlInputs.keySet()) {

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
                _parseTree = _updateTrees.get(PROCESS_NOISE);
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
                        "Expression processNoise yields a null result.");
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
        public void assignWeight() {
            this._weight = 1.0 / Nparticles;
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

            Variable result = getScopedVariable(null, AbstractPredictor.this, name);

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

            Variable result = getScopedVariable(null, AbstractPredictor.this, name);

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

            Variable result = getScopedVariable(null, AbstractPredictor.this, name);

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
            return getAllScopedVariableNames(null, AbstractPredictor.this);
        }
    }

}
