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
 Abstract Particle Filter Base Class.
 @see org.ptolemy.machineLearning.particleFilter.ParticleFilter

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating Red (ilgea)

 */
public abstract class AbstractParticleFilter extends TypedCompositeActor {
    /** Construct the composite actor with a name and a container.
     *  This constructor creates the ports, parameters, and the icon.
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public AbstractParticleFilter(CompositeEntity container, String name)
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
    public AbstractParticleFilter(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A boolean parameter that when set to true, implements the so-called
     * bootstrap particle filter, where particles are resampled at each time step
     * If this parameter is false, particles are resampled only when the effective
     * sample size drops below 50% of the total number of particles.
     */
    public Parameter bootstrap;

    /** Low-variance resampler. */
    public Parameter lowVarianceSampler; 
    /**
     * Number of internal particles used to estimate the posterior distribution.
     */
    public Parameter particleCount;

    /**
     * Number of output particles.
     */
    public Parameter outputParticleCount;


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

        if (attribute == outputParticleCount) {
            int proposed = ((IntToken) (outputParticleCount.getToken()))
                    .intValue();
            if (proposed > 0) {
                Noutput = proposed;
            }
        } else if (attribute == particleCount) {
            int proposed = ((IntToken) (particleCount.getToken())).intValue();
            if (proposed > 0) {
                Nparticles = proposed;
                particles = new Particle[Nparticles];
            }
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
        double currentTime = getDirector().getModelTime().getDoubleValue();
        t.setToken(new DoubleToken(currentTime));

        super.fire();
        // update measurement values

        for (String mp : _measurementParameters.keySet()) {
            Token value = _measurementParameters.get(mp).getToken();
            _measurementValues.put(mp, value);
            _tokenMap.put(mp, value);
        }

        for (int i = 0; i < _parameterInputs.size(); i++) {
            PortParameter s = (PortParameter) AbstractParticleFilter.this
                    .getAttribute(_parameterInputs.get(i));
            s.update();
        }

        // The Sequential Monte Carlo algorithm
        try {
            if (_firstIteration) {
                _initializeParticles(); 
                _firstIteration = false;

            } else {
                _propagate(); 
            }

            _normalizeWeights();
            _sendStateEstimate();
            _generateOutputParticles();
            if (_doBootstrap) {
                _resample();
            } else if (_getEffectiveSampleSize() < 0.5 * Nparticles) {
                _resample();
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

        // Make sure all necessary parameters are provided.
        _checkParameters();

        if (_resetOnEachRun || _random == null) {
            _createRandomGenerator();
        }

        int m = inputPortList().size(); // number of inputs
        _stateSpaceSize = _stateNames.length(); 
        _stateVariables = new String[_stateSpaceSize]; 

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

            particleOutput.setTypeEquals(new RecordType(
                    _particleLabels, _particleTypes));
            stateEstimate.setTypeEquals(new RecordType(_stateLabels,
                    _stateTypes));
        } 

        try {
            _workspace.getWriteAccess();
            removeAllEntities();
            removeAllRelations();

            _setUpdateEquations();

            int inputIndex = 0;
            // Inputs-make connections
            String[] inputs = new String[m];
            _inputRelations = new IORelation[m];  
            _parameterInputs = new LinkedList<String>();

            // get all the inputs and classify according to them
            // being measurements or control inputs.
            for (Object p : this.inputPortList()) {

                IOPort p1 = (IOPort) p;
                inputs[inputIndex] = ((NamedObj) p1).getName();

                _inputRelations[inputIndex] = new TypedIORelation(this,
                        "relation_" + inputs[inputIndex]);

                _inputRelations[inputIndex].setPersistent(false);

                getPort(inputs[inputIndex]).link(_inputRelations[inputIndex]);

                String inputName = inputs[inputIndex];
                if (getInputType(inputName) == InputType.MEASUREMENT_INPUT) {
                    _setMeasurementEquations(inputName);
                } else if (getInputType(inputName) == InputType.CONTROL_INPUT) {
                    if (p1 instanceof ParameterPort) {
                        _parameterInputs.add(inputName);
                    } else {
                        //FIXME _controlInputs.put(inputName, 0.0);
                    }
                }
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
                zm.input.link(_inputRelations[inputIndex]);
                inputIndex++;
                // FIXME: different noise for all inputs 
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


    /** 
     * Get input type by name reference
     * @param inputName The name of the input
     * @return an InputType object indicating the type of input
     */
    protected abstract InputType getInputType(String inputName);

    @Override
    public void wrapup() throws IllegalActionException {
        _firstIteration = true;
        super.wrapup();
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

    /**
     * Get measurement parameter expression.
     * @param fullName Name of the measurement Parameter
     * @return A string expression 
     * @throws IllegalActionException
     */
    protected String getMeasurementParameterExpression(String fullName)  throws IllegalActionException {
        Parameter param = getMeasurementParameter(fullName);
        if (param != null) {
            return param.getExpression();
        } else {
            throw new IllegalActionException("Parameter " 
                    + fullName + " value is null.");
        }
    }


    /** 
     * Returns a parameter value corresponding to a measurement.
     * @param fullName Name of measurement parameter
     * @return a Parameter object that by name reference corresponds to a
     * specific measurement.
     * @throws IllegalActionException
     */
    protected abstract Parameter getMeasurementParameter(String fullName)
            throws IllegalActionException;

    /**
     * Returns a parameter value corresponding to a noise distribution.
     * @param inputName Name of noise parameter
     * @return a Parameter object that by name reference corresponds to a
     * specific noise distribution
     * @throws IllegalActionException
     */
    protected abstract Parameter getNoiseParameter(String inputName) throws IllegalActionException;

    /** Flag indicating whether the contained model is up to date. */
    protected boolean _upToDate;

    /** Cached State variable names. */
    protected ArrayToken _stateNames;

    /** Array of input Relations. */
    protected IORelation[] _inputRelations;

    /** Labels of particles, that contains state names and a weight label. */
    protected String[] _particleLabels;

    /** Types of each particle dimension. */
    protected Type[] _particleTypes;

    /** Labels of states. */
    protected String[] _stateLabels;

    /** Types of each state dimension. */
    protected Type[] _stateTypes;

    /** Measurement covariance matrix. */
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


        t = new Parameter(this, "t");
        t.setTypeEquals(BaseType.DOUBLE);
        t.setVisibility(Settable.EXPERT);
        t.setExpression("0.0");


        _measurementParameters = new HashMap<String, Parameter>();
        _measurementValues = new HashMap<String, Token>(); 
        _parser = new PtParser();
        _measurementTypes = new HashMap<>();
        _updateEquations = new HashMap<>();
        _updateTrees = new HashMap<>();
        _measurementEquations = new HashMap<>();
        _noiseEquations = new HashMap<>();

        _firstIteration = true;
        particles = new Particle[Nparticles];

        _createRandomGenerator();

        _tokenMap = new HashMap<String, Token>();

        _parseTreeEvaluator = new ParseTreeEvaluator();
        _scope = new VariableScope();
        
        Director d = new DEDirector(this, "DEDirector");
        d.setPersistent(false);
        this.setDirector(d);

        
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
            cumulativeSums[i + 1] = cumulativeSums[i] + particles[i].getWeight();
            previousParticles[i] = particles[i];
        }
        
        if (cumulativeSums[Nparticles ] < 1.0) {
            double wt = 1.0/Nparticles;
            for (Particle p: particles ) {
                p.setWeight(wt);
            }
            cumulativeSums = new double[Nparticles + 1];
            for (int i = 0; i < Nparticles; i++) { 
                cumulativeSums[i + 1] = cumulativeSums[i] + particles[i].getWeight();
                previousParticles[i] = particles[i];
            }
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

    private void _setMeasurementEquations(String inputName) 
            throws IllegalActionException, NameDuplicationException {
        Expression measurementEquation = new Expression(this,
                inputName + "_equation");
        // FIXME: Write a method that returns expected parameter name
        // here, since we expect different names for the Decorated actors
        // and the others.
        measurementEquation.expression
        .setExpression(getMeasurementParameterExpression(inputName));
        _measurementEquations.put(inputName,measurementEquation);

        Expression measurementNoise = new Expression(this,
                inputName + "_noise");
        measurementNoise.expression
        .setExpression(getNoiseParameter(inputName).getExpression()); 
        _noiseEquations.put(inputName, measurementNoise);
        _measurementTypes.put(inputName, measurementEquation.output.getType()); 
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

    /**
     * Return a subsample of particles at the chosen indices. Used for low-variance
     * resampling.
     * @return an array of particles.
     */
    private int[] _subsampleIndices() {
        int N = Noutput;
        int[] outputIndices = new int[N]; 
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
                intervalIndex = Algorithms._binaryIntervalSearch(cumulativeSums, randomValue, 0,
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
                intervalIndex = Algorithms._binaryIntervalSearch(cumulativeSums, randomValue, 0,
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
    /** List that holds the measurement equation expression objects */
    private HashMap<String,Expression> _measurementEquations;

    /** List that holds the measurement equation expression objects */
    private HashMap<String,Expression> _noiseEquations;

    /** Internal particle representation that has memory of 1 in time */
    private Particle[] particles;

    /** Number of particles to be used by the particle filter estimators. */
    private int Nparticles;

    /** Number of particles to be produced at the output port at each iteration. */
    private int Noutput;

    /** Random number generator. */
    private Random _random;

    /** Public seed for random number generation. */
    private long _seed;

    /** State-space size. */
    private int _stateSpaceSize;

    /** Names of state variables. */
    private String[] _stateVariables;

    /** State update equations, hashed by state variable name. */
    private HashMap<String, Expression> _updateEquations; 

    /** Names of the PortParameter inputs. */
    private List<String> _parameterInputs;

    private HashMap _tokenMap;

    private boolean _resetOnEachRun;
    private PtParser _parser;
    private ASTPtRootNode _parseTree;
    private HashMap<String, ASTPtRootNode> _updateTrees;
    private ParseTreeEvaluator _parseTreeEvaluator;
    private VariableScope _scope;
    private boolean _firstIteration;
    private HashMap<String, Parameter> _measurementParameters;
    // The values of the measurement inputs at the given iteration.
    private HashMap<String, Token> _measurementValues;
    private HashMap<String, Type> _measurementTypes;



    /** State Variable names identifier. */
    protected static final String STATE_VARIABLE_NAMES = "stateVariableNames";
    /** Process Noise identifier. */
    protected static final String PROCESS_NOISE = "processNoise";
    /** Noise covariance identifier. */
    protected static final String MEASUREMENT_NOISE = "noiseCovariance"; 
    /** Update variable postfix. */
    protected static final String UPDATE_POSTFIX = "_update";
    /** Measurement variable postfix. */
    protected static final String MEASUREMENT_POSTFIX = "_m";
    /** Prior distribution identifier. */
    protected static final String PRIOR_NAME = "prior";

    /**
     * Type of user-added input
     * @author ilgea
     *
     */
    protected enum InputType {
        /**
         * Measurement
         */
        MEASUREMENT_INPUT,
        /** 
         * Control
         */
        CONTROL_INPUT
    };

    /**
     * A particle object definition.
     * @author ilgea
     *
     */
    private class Particle {
        /**
         * Create a particle with given size
         * @param size Particle dimension
         */
        public Particle(int size) {
            this._particleValue = new double[size];
        }

        /**
         * Copy constructor for Particle object
         * @param p A Particle object
         */
        public Particle(Particle p) {
            this._weight = p.getWeight();
            this._particleValue = new double[p.getSize()];
            double[] tempParticle = p.getValue();
            for (int i = 0; i < p.getSize(); i++) {
                this._particleValue[i] = tempParticle[i];
            }
        }

        /**
         * Assign a weight to the particle that is proportional to its likelihood
         * according to all i.i.d. measurements made available to the particle filter.
         * @throws IllegalActionException
         * @throws NameDuplicationException
         */
        public void assignWeight() throws IllegalActionException,
        NameDuplicationException {

            Parameter p;
            if (this.getSize() != _stateVariables.length) {
                throw new IllegalActionException(
                        "Particle dimensions must be equal to the state space dimension");
            } else {
                for (int i = 0; i < _stateVariables.length; i++) {
                    if ((AbstractParticleFilter.this)
                            .getUserDefinedParameter(_stateVariables[i]) == null) {
                        p = new Parameter(AbstractParticleFilter.this,
                                _stateVariables[i]);
                        p.setVisibility(Settable.EXPERT);
                    } else {
                        p = (Parameter) (AbstractParticleFilter.this)
                                .getUserDefinedParameter(_stateVariables[i]);
                    }
                    p.setExpression(((Double) _particleValue[i]).toString());
                    _tokenMap.put(_stateVariables[i], new DoubleToken(
                            _particleValue[i]));
                }

                // initialize weight
                _weight = 1.0;
                // evaluate all equations given the particle value
                for (String inputName: _measurementEquations.keySet()) {
                    Expression measurementEquation = _measurementEquations.get(inputName);
                    _parseTree = _parser
                            .generateParseTree(measurementEquation.expression
                                    .getExpression());
                    Token e = _parseTreeEvaluator.evaluateParseTree(
                            _parseTree, _scope); 

                    Expression noiseEq = _noiseEquations.get(inputName);
                    _parseTree = _parser
                            .generateParseTree(noiseEq.expression
                                    .getExpression()); 
                    Token n = _parseTreeEvaluator.evaluateParseTree(
                            _parseTree, _scope);

                    if (e == null) {
                        throw new IllegalActionException(
                                "Expression yields a null result: "
                                        + measurementEquation.expression
                                        .getExpression());
                    }
                    // TODO: Do not do this for every particle!
                    Type t = _measurementTypes.get(inputName);    
                    if (t.equals(BaseType.UNKNOWN)) {
                        t = e.getType();
                        _measurementTypes.put(inputName, t);
                    }
                    if (_measurementValues.containsKey(inputName)) {
                        if (t.equals(BaseType.DOUBLE)) {
                            // one-dimensional measurements
                            double zt = ((DoubleToken)_measurementValues
                                    .get(inputName))
                                    .doubleValue();
                            double _mean = ((DoubleToken) e).doubleValue();
                            double _sigma = ((DoubleToken) n).doubleValue();

                            _weight *= 1.0 / (Math.sqrt(2 * Math.PI) * _sigma*_sigma)
                                    * Math.exp(-Math
                                            .pow(zt - _mean, 2)
                                            / (2 * Math.pow(_sigma, 2))); 

                        } else {
                            MatrixToken zt = (MatrixToken) _measurementValues
                                    .get(inputName);
                            int k = zt.getRowCount();
                            MatrixToken X = (DoubleMatrixToken) zt
                                    .subtract(e);
                            MatrixToken covariance = (DoubleMatrixToken) n;
                            MatrixToken invCov = new DoubleMatrixToken(DoubleMatrixMath.inverse(covariance.doubleMatrix()));
                            MatrixToken Xt = new DoubleMatrixToken(
                                    DoubleMatrixMath.transpose(X.doubleMatrix()));
                            double multiplier = Math.pow(2 * Math.PI, -0.5 * k)
                                    * Math.pow(DoubleMatrixMath
                                            .determinant(covariance
                                                    .doubleMatrix()), -0.5);
                            Token exponent = Xt.multiply(invCov);
                            exponent = exponent.multiply(X);
                            double value = ((DoubleMatrixToken) exponent)
                                    .getElementAt(0, 0);
                            _weight *= multiplier * Math.exp(-0.5 * value);
                        }
                    }

                } 
            }
        }

        /**
         * Scale existing particle weight by the scaling factor.
         * @param w The scaling factor.
         * @return A boolean indicating the status of operation.
         */
        public boolean adjustWeight(double w) {
            // normalize weight
            if (w > 0.0) {
                _weight = _weight / w;
            } else {
                return false;
            }
            return true;
        }

        /**
         * Return the value of a particle.
         * @return A double array corresponding to the Particle value.
         */
        public double[] getValue() {
            double[] values = new double[this.getSize()];
            for (int i = 0; i < this.getSize(); i++) {
                values[i] = this._particleValue[i];
            }
            return values;
        }

        /**
         * Return the dimension of the particle.
         * @return particle dimension
         */
        public int getSize() {
            return this._particleValue.length;
        }

        /**
         * Assign a value to the particle, given the prior distribution.
         * @throws IllegalActionException
         */
        public void sampleFromPrior() throws IllegalActionException {
            _parseTree = _updateTrees.get(PRIOR_NAME);
            Token priorSample = _parseTreeEvaluator.evaluateParseTree(
                    _parseTree, _scope);

            if (priorSample == null) {
                throw new IllegalActionException(
                        "Expression priorDistribution yields a null result: ");
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

        /**
         * Propagate the particle value, with respect to the state-space model
         * defined by the particle filter problem.
         * @throws NameDuplicationException
         * @throws IllegalActionException
         */
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
            }

            _parseTree = _updateTrees.get(PROCESS_NOISE);
            processNoiseSample = _parseTreeEvaluator.evaluateParseTree(
                    _parseTree, _scope); 
            if (processNoiseSample == null) {
                throw new IllegalActionException(
                        "Expression processNoise yields a null result.");
            }

            for (int i = 0; i < _stateSpaceSize; i++) { 
                _parseTree = _updateTrees.get(_stateVariables[i]);
                _result = _parseTreeEvaluator.evaluateParseTree(_parseTree,
                        _scope); 

                if (_result == null) {
                    throw new IllegalActionException(
                            "Expression yields a null result: "
                                    + _updateEquations.get(_stateVariables[i]).expression
                                    .getExpression());
                } 
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

        /**
         * Set the particle to have a desired value.
         * @param l A desired particle value.
         * @throws IllegalActionException
         */
        public void setValue(double[] l) throws IllegalActionException {

            if (l.length != this._particleValue.length) {
                throw new IllegalActionException(
                        "Cannot set a value with different size");
            }

            for (int i = 0; i < l.length; i++) {
                _particleValue[i] = l[i];
            }
        }

        /**
         * Set the particle value to a desired weight.
         * @param weight The particle weight.
         */
        public void setWeight(double weight) {
            _weight = weight;
        }

        /**
         * Get particle weight.
         * @return Particle weight
         */
        public double getWeight() {
            return _weight;
        }

        /**
         * Value of the particle. Size is equal to _ssSize.
         */
        private final double[] _particleValue;
        /**
         * Weight of the particle.
         */
        private double _weight;
    }

    /**
     * A local variable scope class.
     * @author ilgea
     *
     */
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

            Token token = (Token)_tokenMap.get(name);

            if (token != null) {
                return token;
            }

            Variable result = getScopedVariable(null, AbstractParticleFilter.this, name);

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

            Variable result = getScopedVariable(null, AbstractParticleFilter.this, name);

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

            Variable result = getScopedVariable(null, AbstractParticleFilter.this, name);

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
            return getAllScopedVariableNames(null, AbstractParticleFilter.this);
        }
    }

}
