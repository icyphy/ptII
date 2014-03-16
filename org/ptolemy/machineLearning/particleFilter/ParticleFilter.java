/* Discrete-Event Particle Filter Implementation.

 Copyright (c) 1998-2013 The Regents of the University of California.
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
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.lib.SetVariable;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.BooleanToken;
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
 <li> For each input in <i>U</i>, create an input port with an arbitrary name.
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

 </ul>


 The preinitialize() method of this actor is based on the ptolemy.domain.ct.lib.DifferentialSystem
 actor by Jie Liu.

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.1
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating Red (ilgea)
 @see ptolemy.domains.continuous.lib.Integrator
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
    public ParticleFilter(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                           parameters                        ////

    /** A boolean parameter that when set to true, implements the so-called
     * bootstrap particle filter, where particles are resampled at each time step
     * If this parameter is false, particles are resampled only when the effective
     * sample size drops below 50% of the total number of particles
     */
    public Parameter bootstrap;

    /** Low-variance resampler **/
    public Parameter lowVarianceSampler;

    /** Standard deviation of the measurement noise ( assuming  Gaussian measurement noise
     * at the moment)
     */
    public Parameter measurementCovariance;



    /**
     * The expression that specifies the PDF for the measurementNoise. use N(m,s) for
     * a Gaussian distribution with mean m and standard deviation s. unif(x,y) evaluates
     * to a uniform distribution in range [x,y] 
     */
    public Parameter particleCount;

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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** If the argument is any parameter other than <i>stateVariableNames</i>
     *  <i>t</i>, or any parameter matching an input port,
     *  then request reinitialization.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the numerator and the
     *   denominator matrix is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute instanceof Parameter && attribute != t
                && attribute != stateVariableNames) {
            // If the attribute name matches an input port name,
            // do not reinitialize.
            TypedIOPort port = (TypedIOPort) getPort(attribute.getName());
            if (port == null || !port.isInput()) {
                // Change of any parameter triggers reinitialization.
                _requestInitialization();
            }
        }else if(attribute == stateVariableNames){
            // create a hidden parameter that corresponds to the specified state variable, if not already present
            ArrayToken names = (ArrayToken)stateVariableNames.getToken();
            try{
                for(int i = 0; i < names.length(); i++){
                    String stateName = ((StringToken)names.getElement(i)).stringValue();
                    if(this.getAttribute(stateName) == null && stateName.length()!= 0){
                        Parameter y = new Parameter(this, stateName);
                        y.setExpression("0.0");
                        y.setVisibility(Settable.EXPERT);
                    }
                }
            }catch(NameDuplicationException e){
                // should not happen
                System.err.println("Duplicate field in " + this.getName());
            }
        }

        if(attribute == particleCount){
            int proposed = ((IntToken)(particleCount.getToken())).intValue();
            if(proposed > 0){
                Nparticles = proposed;
                particles = new Particle[Nparticles];
            }
        }else if(attribute == measurementCovariance){
            double[][] proposed = ((MatrixToken) measurementCovariance.getToken()).doubleMatrix();
            _Sigma = proposed;
        }else if(attribute == bootstrap)
        {
            _doBootstrap = ((BooleanToken) bootstrap.getToken()).booleanValue();
        }else if(attribute == lowVarianceSampler){
            _lowVarianceSampler = ((BooleanToken) lowVarianceSampler.getToken()).booleanValue();
        }

        else{
            super.attributeChanged(attribute);
        }
        // If any parameter changes, then the next preinitialize()
        // will recreate the contents.
        _upToDate = false;
    }


    /** Override the base class to first set the value of the
     *  parameter <i>t</i> to match current time, then to set
     *  the local parameters that mirror input values,
     *  and then to fire the contained actors.
     */
    public void fire() throws IllegalActionException {
        // Set the time variable.
        double currentTime = getDirector().getModelTime().getDoubleValue();
        t.setToken(new DoubleToken(currentTime));

        super.fire();
        // update measurement values
        Iterator k = _measurementParameters.keySet().iterator();
        while(k.hasNext()){
            String mvName = (String)k.next();
            Token value = (((Parameter)_measurementParameters.
                    get(mvName)).getToken());
            _measurementValues.put(mvName, value);
        }
        // The Sequential Monte Carlo algorithm
        try{
            if(_firstIteration){
                _initializeParticles();
                _normalizeWeights();
                _resample();
                _firstIteration = false;
            }else{
                _propagate();

                // create tokens for generated particles
                String[] labels = new String[_stateSpaceSize+1];
                for(int j = 0; j< _stateSpaceSize; j++){
                    labels[j] =_stateVariables[j];
                }
                labels[_stateSpaceSize] = "weight";
                Token[]  tokens = new Token[_stateSpaceSize+1];
                for(int i=0; i < Nparticles; i++){
                    LinkedList l = (LinkedList)particles[i].getValue();
                    for(int j = 0; j< _stateSpaceSize; j++){
                        tokens[j] = new DoubleToken((Double)l.get(j));
                    }
                    tokens[_stateSpaceSize] = new DoubleToken(particles[i].getWeight());
                    RecordToken r = new RecordToken(labels,tokens);
                    particleOutput.send(0, r);
                }


                _normalizeWeights();
                if(_doBootstrap){
                    _resample();
                }else if(_getEffectiveSampleSize() < 0.5*Nparticles){
                    _resample();
                }

            }
        }catch(NameDuplicationException e){
            System.out.println("!!!!");
            //FIXME
        }
    }

    /** Create the model inside from the parameter values.
     *  This method gets write access on the workspace.
     *  @exception IllegalActionException If there is no director,
     *   or if any contained actors throws it in its preinitialize() method.
     */
    public void preinitialize() throws IllegalActionException {
        if (_upToDate) {
            super.preinitialize();
            return;
        }
        // Check parameters.
        _checkParameters();

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
            _updateTrees = new HashMap<String,ASTPtRootNode>();

            for (int i = 0; i < n; i++) {
                _stateVariables[i] = ((StringToken) stateNames.getElement(i))
                        .stringValue().trim();
                // find the state update equation for the current state variable
                Expression e = new Expression(this, _stateVariables[i] + "_update");
                //e.setPersistent(false);
                String updateEqnName = _stateVariables[i]
                        + "_update";
                e.expression
                .setExpression(((Parameter) getAttribute(updateEqnName)).getExpression()); 
                if(_stateVariables[i] == null){
                    System.err.println("One state variable is null at index " + i);
                }else{
                    _updateEquations.put(_stateVariables[i], e);
                    _updateTrees.put(_stateVariables[i], new PtParser().generateParseTree(_updateEquations.get(_stateVariables[i]).expression
                            .getExpression()));
                }
            }
            // put an update tree for the process noise
            _updateTrees.put("processNoise", new PtParser().generateParseTree(processNoise
                    .getExpression()));
            // update tree for the prior distribution
            _updateTrees.put("priorDistribution", new PtParser().generateParseTree(prior
                    .getExpression()));

            // Inputs-make connections
            String[] inputs = new String[m];
            IORelation[] inputRelations = new IORelation[m];
            Iterator inputPorts = inputPortList().iterator();
            int measurementIndex = 0;
            int inputIndex = 0;
            _controlInputs = new HashMap<String,Double>();

            while (inputPorts.hasNext()) {
                inputs[inputIndex] = ((NamedObj) inputPorts.next()).getName();
                inputRelations[inputIndex] = new TypedIORelation(this,
                        "relation_" + inputs[inputIndex]);
                inputRelations[inputIndex].setPersistent(false);
                getPort(inputs[inputIndex]).link(inputRelations[inputIndex]);
                String inputName = inputs[inputIndex];
                if(inputName.endsWith("_m")){
                    _measurementVariable = inputName;
                    measurementIndex = inputIndex;
                    String eqnName = inputName.substring(0, inputName.length()-2);
                    _measurementEquation = new Expression(this, inputName+"_equation");
                    //_measurementEquation.output.setTypeEquals(BaseType.DOUBLE);
                    _measurementEquation.expression
                    .setExpression(((Parameter) getAttribute(eqnName)).getExpression());

                    _measurementCovariance = new Expression(this, inputName+"_covariance");
                    _measurementCovariance.expression
                    .setExpression(((Parameter) getAttribute("measurementCovariance")).getExpression());


                    SetVariable zm = new SetVariable(this, "set"+inputName);
                    // add new parameter to the actor
                    Parameter measure1;
                    if(this.getAttribute(inputName) == null){
                        measure1 = new Parameter(this,inputName);
                    }else{
                        measure1 = (Parameter)this.getAttribute(inputName);
                    }
                    _measurementParameters.put(inputName, measure1);
                    zm.delayed.setExpression("false");
                    zm.variableName.setExpression(inputName);
                    zm.input.link(inputRelations[measurementIndex]);

                }else{
                    _controlInputs.put(inputName,0.0);
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
                        TypedIOPort port = new TypedIOPort(_updateEquations.get(_stateVariables[i]),
                                inputs[k], true, false);
                        port.setTypeEquals(BaseType.DOUBLE);
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

    public void wrapup() throws IllegalActionException{
        _firstIteration = true;
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
                throw new IllegalActionException(this, "Please add a "
                        + "parameter with name \"" + equation
                        + "\" that gives the state update expression for state " + name + ".");
            }
        }
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

        processNoise = new Parameter(this, "processNoise");
        processNoise.setExpression("multivariateGaussian({0.0,0.0},[1.0,0.4;0.4,1.2])");

        particleOutput = new TypedIOPort(this, "particleOutput", false, true );
        //particleOutput.setTypeEquals(BaseType.DOUBLE);
        //setClassName("org.ptolemy.machineLearning.ParticleFilter");
        particleOutput.setTypeEquals(RecordType.EMPTY_RECORD);

        prior = new Parameter(this, "prior");
        prior.setExpression("random()*200-100");

        t = new Parameter(this, "t");
        t.setTypeEquals(BaseType.DOUBLE);
        t.setVisibility(Settable.EXPERT);
        t.setExpression("0.0");

        measurementCovariance = new Parameter(this, "measurementCovariance");
        measurementCovariance.setExpression("[10.0,0.0;0.0,10.0]");

        _measurementParameters = new HashMap<String,Parameter>();
        _measurementValues = new HashMap<String, Token>();

        _firstIteration = true;
        particles = new Particle[Nparticles];

        _tokenMap = new HashMap<String, Token>();
        _random = new Random(0);

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

    private void _initializeParticles() throws IllegalActionException,NameDuplicationException{
        // let prior distribution be N(0,1) for now.
        for(int i = 0; i < particles.length; i++){
            particles[i] = new Particle(_stateSpaceSize);
            particles[i].sampleFromPrior();
            particles[i].assignWeight(_measurementEquation, _stateVariables);
        }
    }

    private void _normalizeWeights() throws IllegalActionException,NameDuplicationException{
        // let prior distribution be N(0,1) for now.
        double sum =0;
        for(int i = 0; i < particles.length; i++){
            sum += particles[i].getWeight();
        }
        for(int i = 0; i < particles.length; i++){
            particles[i].adjustWeight(sum);
        }

    }
    private double _getEffectiveSampleSize(){
        double sum = 0;
        for(int i = 0; i < particles.length; i++){
            if(!SignalProcessing.close(particles[i].getWeight(), 0)){
                //System.out.println(particles[i].getWeight());
                sum += Math.pow(particles[i].getWeight(),2);
            }
        }
        sum = 1.0/sum;
        return sum;
    }
    private void _resample(){
        double randomValue;
        int intervalIndex;
        double[] cumulativeSums = new double[Nparticles+1];
        Particle[] previousParticles = new Particle[Nparticles];
        cumulativeSums[0]=0;
        for(int i = 0; i < Nparticles; i++){
            cumulativeSums[i+1] = cumulativeSums[i] + particles[i].getWeight();
            previousParticles[i] = particles[i];
        }
        // If low-variance sampling has been selected, sample a random particle in [0,1/Nparticles] 
        // and choose all other particles in reference to the first sample. Yields a low-variance 
        // particle set.
        if(_lowVarianceSampler){
            double baseValue = _random.nextDouble()*(1.0/Nparticles);
            for(int i = 0; i < Nparticles; i++){
                randomValue = baseValue + i*1.0/Nparticles;
                intervalIndex = binarySearch(cumulativeSums, randomValue, 0, Nparticles);
                //FIXME: check intervalIndex and remove the failure condition
                if(intervalIndex < 0 || intervalIndex > Nparticles-1){
                    System.out.println("Index does not exist!");
                }else{
                    particles[i] = new Particle(particles[i].getSize());
                    particles[i].setValue((LinkedList)previousParticles[intervalIndex].getValue());
                    // the weights are equal at a result of resampling
                    particles[i].setWeight(1.0/Nparticles);
                }
            }

        }else{
            // will resample particles according to their weights
            // last entry of cumulative sums is the range of the random variable
            // resampling to set equal weights
            
            for(int i = 0; i < Nparticles; i++){
                randomValue = _random.nextDouble()*cumulativeSums[Nparticles];
                intervalIndex = binarySearch(cumulativeSums, randomValue, 0, Nparticles);
                if(intervalIndex < 0 || intervalIndex > Nparticles-1){
                    System.out.println("Index does not exist!");
                }else{
                    particles[i] = new Particle(particles[i].getSize());
                    particles[i].setValue((LinkedList)previousParticles[intervalIndex].getValue());
                    // the weights are equal at a result of resampling
                    particles[i].setWeight(1.0/Nparticles);
                }
            }
        }

    }
    private void _propagate() throws IllegalActionException, NameDuplicationException{
        for(int i = 0; i < Nparticles; i++){
            particles[i].setNextParticle();
        }
    }
    private int binarySearch(double[] A, double key, int imin, int imax){
        while(imin <= imax){
            int imid = imin + ((imax-imin)/2);
            // the second condition is to ensure to return the bin where the random number resides.
            if(A[imid] == key || (A[imid]<=key && A[imid+1] > key)){
                //found at mid
                return imid;
            }else if(A[imid] > key){
                return binarySearch(A, key, imin, imid-1);
            }else{
                return binarySearch(A, key, imid+1, imax);
            }
        }
        return -1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private boolean _doBootstrap = true;

    private boolean _lowVarianceSampler = false;
    /** Flag indicating whether the contained model is up to date. */
    private boolean _upToDate = false;

    private Expression _measurementEquation;

    private Particle[] particles;

    private int Nparticles;

    private Random _random; 

    private int _stateSpaceSize;

    private String[] _stateVariables;

    private HashMap<String, Expression> _updateEquations;

    private HashMap<String,Double> _controlInputs;

    private HashMap _tokenMap;

    private double[][] _Sigma;
    // set particle dimensions to be equal to the state space dimension
    private class Particle{
        public Particle(int size){
            _particleValue = new LinkedList<Double>();
            _ssSize = size;
        }
        public void sampleFromPrior() throws IllegalActionException{
            _parseTree = _updateTrees.get("priorDistribution");
            Token priorSample = _parseTreeEvaluator.evaluateParseTree(_parseTree, _scope);

            if (priorSample == null) {
                throw new IllegalActionException(
                        "Expression yields a null result: "
                                + prior.getExpression());
            }

            Type t = priorSample.getType();
            if(t.equals(BaseType.DOUBLE)){
                // one dimensional 
                if(this.getSize()>1){
                    throw new IllegalActionException(
                            "Prior distribution and state space dimensions must match.");
                }
                double value = ((DoubleToken)priorSample).doubleValue();
                _particleValue.add(Double.valueOf(value));
            }else{
                Token[] vals = ((ArrayToken)priorSample).arrayValue();
                if(vals.length!=_ssSize){
                    throw new IllegalActionException(
                            "Prior distribution and state space dimensions must match.");
                }
                for(int i = 0; i < _ssSize; i++){
                    double value = ((DoubleToken)vals[i]).doubleValue();
                    _particleValue.add(Double.valueOf(value));
                }

            }
        }
        public int getSize(){
            return _ssSize;
        }
        public void setValue(LinkedList<Double> l){
            _particleValue = new LinkedList<Double>();

            for(int i = 0; i < l.size(); i++){
                _particleValue.add(l.get(i));
            }
        }
        public void setWeight(double weight){
            _weight = weight;
        }
        public List<Double> getValue(){
            return _particleValue;
        }
        public void assignWeight(Expression measurementEquation, String[] _stateVariables)
                throws IllegalActionException, NameDuplicationException{
            Token _particle;
            Token _particleCovariance;
            Parameter p;
            if(this.getSize() != _stateVariables.length){
                throw new IllegalActionException("Particle dimensions must be equal to the state space dimension");
            }else{
                for(int i = 0; i < _stateVariables.length; i++){
                    if((ParticleFilter.this).getAttribute(_stateVariables[i]) == null){
                        p = new Parameter(ParticleFilter.this,_stateVariables[i]);
                    }else{
                        p = (Parameter)(ParticleFilter.this).getAttribute(_stateVariables[i]);
                    }
                    //set the parameters to have the particle values
                    p.setExpression(_particleValue.get(i).toString());
                    p.setVisibility(Settable.EXPERT);
                    _tokenMap.put(_stateVariables[i], new DoubleToken(_particleValue.get(i).doubleValue()));

                }

                try {
                    // Note: this code parallels code in the OutputTypeFunction class
                    // below.
                    //if (_parseTree == null) {
                    // Note that the parser is NOT retained, since in most
                    // cases the expression doesn't change, and the parser
                    // requires a large amount of memory.

                    PtParser parser = new PtParser();
                    _parseTree = parser.generateParseTree(measurementEquation.expression
                            .getExpression());
                    _particle = _parseTreeEvaluator.evaluateParseTree(_parseTree, _scope);
                    _parseTree = parser.generateParseTree(_measurementCovariance.expression
                            .getExpression());
                    _particleCovariance = _parseTreeEvaluator.evaluateParseTree(_parseTree,_scope);
                } catch (Throwable throwable) {
                    // Chain exceptions to get the actor that threw the exception.
                    // Note that if evaluateParseTree does a divide by zero, we
                    // need to catch an ArithmeticException here.
                    throw new IllegalActionException(
                            "Expression invalid.");
                }

                if (_particle == null) {
                    throw new IllegalActionException(
                            "Expression yields a null result: "
                                    + measurementEquation.expression.getExpression());
                }
                // set particle weight


                Type t =_measurementEquation.output.getType();
                if(t.equals(BaseType.DOUBLE)){
                    // one-dimensional measurement
                    double _particleValue = ((DoubleToken)_particle).doubleValue();
                    double _meanEstimate = ((DoubleToken)_measurementValues.get(_measurementVariable)).doubleValue();
                    _weight = 1/(Math.pow(2*Math.PI,0.5) * DoubleMatrixMath.determinant(_Sigma))*Math.exp(-Math.pow(_particleValue
                            - _meanEstimate,2)/(2*Math.pow(_Sigma[0][0],2)));
                }else{
                    // >1 dimensional measurement that returns an array type
                    MatrixToken z_t = (MatrixToken)_measurementValues.get(_measurementVariable);
                    // get measurement components.
                    int k = z_t.getRowCount();

                    MatrixToken X = (DoubleMatrixToken) _particle.subtract((Token)z_t);
                    MatrixToken Covariance = (DoubleMatrixToken)_particleCovariance;
                    MatrixToken invCov = new DoubleMatrixToken(DoubleMatrixMath.inverse(Covariance.doubleMatrix()));
                    MatrixToken Xt = new DoubleMatrixToken(DoubleMatrixMath.transpose(X.doubleMatrix()));
                    double multiplier = Math.pow(2*Math.PI,-0.5*k)*Math.pow(DoubleMatrixMath.determinant(Covariance.doubleMatrix()),-0.5);
                    Token exponent = Xt.multiply(invCov);
                    exponent = exponent.multiply(X);
                    double value = ((DoubleMatrixToken)exponent).getElementAt(0, 0);
                    _weight = multiplier*Math.exp(-0.5*value);

                }
            }
        }
        public boolean adjustWeight(double w){
            // normalize weight
            if(w > 0.0){
                _weight = _weight/w;
            }else{
                return false;
            }
            return true;
        }
        public void setNextParticle() throws NameDuplicationException, IllegalActionException{
            Token _result;
            //FIXME: the noise sample does not have to be an arrayToken
            Token processNoiseSample;
            LinkedList newParticle = new LinkedList();
            for(int i = 0; i < _stateSpaceSize; i++){
                // every component of the particle will be propagated according to its own update equation.
                //Parameter p = new Parameter(_updateEquations.get(_stateVariables[i]), _stateVariables[i]);
                Parameter p = (Parameter)(_updateEquations.get(_stateVariables[i])).getAttribute(_stateVariables[i]);
                if( p != null){
                    p.setExpression(_particleValue.get(i).toString());
                }else{
                    p = new Parameter(_updateEquations.get(_stateVariables[i]), _stateVariables[i]);
                    p.setExpression(_particleValue.get(i).toString());
                }
                _tokenMap.put(_stateVariables[i], new DoubleToken((double)_particleValue.get(i).doubleValue()));
                Iterator ci = _controlInputs.keySet().iterator();
                // set the control input values in scope
                while(ci.hasNext()){
                    String controlVarName = (String)ci.next();
                    Parameter c = (Parameter)(_updateEquations.get(_stateVariables[i])).getAttribute(controlVarName);
                    if(c != null){
                        c.setExpression(_controlInputs.get(controlVarName).toString()); 
                    }else{
                        c = new Parameter(_updateEquations.get(_stateVariables[i]), controlVarName );
                        c.setExpression(_controlInputs.get(controlVarName).toString()); 
                    }
                    _tokenMap.put(controlVarName, new DoubleToken(_controlInputs.get(controlVarName)));
                }
            }

            try {
                _parseTree = _updateTrees.get("processNoise");
                processNoiseSample = _parseTreeEvaluator.evaluateParseTree(_parseTree, _scope);
            } catch (Throwable throwable) {
                // Chain exceptions to get the actor that threw the exception.
                // Note that if evaluateParseTree does a divide by zero, we
                // need to catch an ArithmeticException here.
                throw new IllegalActionException(
                        "Expression invalid.");
            }
            if (processNoiseSample == null) {
                throw new IllegalActionException(
                        "Expression yields a null result: "
                                + processNoise.getExpression());
            }

            for(int i = 0; i < _stateSpaceSize; i++){
                try {
                    _parseTree = _updateTrees.get(_stateVariables[i]);
                    _result = _parseTreeEvaluator.evaluateParseTree(_parseTree, _scope);
                } catch (Throwable throwable) {
                    // Chain exceptions to get the actor that threw the exception.
                    // Note that if evaluateParseTree does a divide by zero, we
                    // need to catch an ArithmeticException here.
                    throw new IllegalActionException(
                            "Expression invalid.");
                }

                if (_result == null) {
                    throw new IllegalActionException(
                            "Expression yields a null result: "
                                    + _updateEquations.get(_stateVariables[i]).expression.getExpression());
                }
                // set particle weight

                double _meanEstimate = ((DoubleToken)_result.add(new DoubleToken(0.0))).doubleValue();
                //FIXME: what if the process noise sample is not an array token?
                double processNoiseForElement = ((DoubleToken)((ArrayToken)processNoiseSample).getElement(i)).doubleValue();
                newParticle.add(_meanEstimate + processNoiseForElement);
            }
            // set control inputs in range and also assigned state variable values to equal the current particle
            // value
            this.setValue(newParticle);

            this.assignWeight(_measurementEquation, _stateVariables);

        }
        public double getWeight(){
            return _weight;
        }
        private List<Double> _particleValue;
        private int _ssSize;
        private double _weight;
    }


    private class VariableScope extends ModelScope {
        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Token get(String name) throws IllegalActionException {
            if (name.equals("time") || name.equals("t")) {
                return new DoubleToken(getDirector().getModelTime()
                        .getDoubleValue());
            } 

            Token token = (DoubleToken)_tokenMap.get(name);

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
        public Set identifierSet() {
            return getAllScopedVariableNames(null, ParticleFilter.this);
        }
    }

    private ASTPtRootNode _parseTree;
    private HashMap<String, ASTPtRootNode> _updateTrees;
    private ParseTreeEvaluator _parseTreeEvaluator;
    private VariableScope _scope;
    private boolean _firstIteration;
    private HashMap<String, Parameter> _measurementParameters;
    // The values of the measurement inputs at the given iteration. 
    private HashMap<String, Token> _measurementValues;
    private Expression _measurementCovariance;
    // the name of the measurement variable. ( only one?)
    private String _measurementVariable;


}