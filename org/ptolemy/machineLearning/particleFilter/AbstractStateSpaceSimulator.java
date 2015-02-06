/* An actor that simulates a state dynamics.

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

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
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
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////

/**
 Abstract simulator base class 

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating Red (ilgea)

 */
public abstract class AbstractStateSpaceSimulator extends TypedCompositeActor {
    /** Construct the composite actor with a name and a container.
     *  This constructor creates the ports, parameters, and the icon.
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public AbstractStateSpaceSimulator(CompositeEntity container, String name)
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
    public AbstractStateSpaceSimulator(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////  

    /** State estimate output. A record token with one field per state variable */

    public TypedIOPort state; 

    /** An ordered array of initial state values */
    public Parameter initialState;

    /** The value of current time. This parameter is not visible in
     *  the expression screen except in expert mode. Its value initially
     *  is just 0.0, a double, but upon each firing, it is given a
     *  value equal to the current time as reported by the director.
     */
    public Parameter t;

    public TypedIOPort trigger;

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

        if (attribute == resetOnEachRun) {
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
        
        if (trigger.hasToken(0)) {
            double currentTime = getDirector().getModelTime().getDoubleValue();
            t.setToken(new DoubleToken(currentTime)); 
            super.fire();  
            for (int i = 0; i < _parameterInputs.size(); i++) {
                PortParameter s = (PortParameter) this
                        .getAttribute(_parameterInputs.get(i));
                s.update();
            }

            if (_firstIteration) {
                _initializeState(); 
                _firstIteration = false; 
            } else {
                try {
                    _propagate(); 
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }
            }

            _sendStateEstimate(); 

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

        _stateSpaceSize = _stateNames.length(); 
        _stateVariables = new String[_stateSpaceSize]; 

        if (_stateSpaceSize > 0) {
            // Set the output type according to the state variable 
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

                _stateLabels[i] = variableName;
                _stateTypes[i] = BaseType.DOUBLE; // preset to be double
            } 

            state.setTypeEquals(new RecordType(_stateLabels,
                    _stateTypes));
        } 

        try {
            _workspace.getWriteAccess();
            removeAllEntities();
            removeAllRelations();

            _setUpdateEquations();


            _parameterInputs = new LinkedList<String>();
            
            for (Object p : this.inputPortList()) {

                IOPort p1 = (IOPort) p;
                if (p1 instanceof ParameterPort) {
                        _parameterInputs.add(p1.getName());
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

    /** Labels of states */
    protected String[] _stateLabels;

    /** Types of each state dimension */
    protected Type[] _stateTypes;

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


    /** Initialize the class. */
    private void _init() throws IllegalActionException,
    NameDuplicationException { 

        trigger = new TypedIOPort (this, "trigger", true, false); 

        seed = new SharedParameter(this, "seed");
        seed.setExpression("0L");
        seed.setTypeEquals(BaseType.LONG);
        seed.setVisibility(Settable.EXPERT);

        resetOnEachRun = new SharedParameter(this, "resetOnEachRun");
        resetOnEachRun.setExpression("false");
        resetOnEachRun.setVisibility(Settable.EXPERT);
        resetOnEachRun.setTypeEquals(BaseType.BOOLEAN);

        state = new TypedIOPort(this, "state", false, true);
        state.setTypeEquals(RecordType.EMPTY_RECORD);

        initialState = new Parameter(this, "initialState");
        initialState.setTypeEquals(new ArrayType(BaseType.DOUBLE));



        t = new Parameter(this, "t");
        t.setTypeEquals(BaseType.DOUBLE);
        t.setVisibility(Settable.EXPERT);
        t.setExpression("0.0");


        //_parser = new PtParser(); 
        _updateEquations = new HashMap<>();
        _updateTrees = new HashMap<>();  

        _firstIteration = true; 

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

    private void _initializeState() throws IllegalActionException {
        // let prior distribution be N(0,1) for now.
        _currentState = new double[_stateSpaceSize];
        ArrayToken s0 = (ArrayToken)initialState.getToken();

        for (int i = 0; i < _stateSpaceSize; i++) {
            _currentState[i] = ((DoubleToken)s0.getElement(i)).doubleValue();
        }
    } 

    /** Propagate particles according to the state update equations
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    private void _propagate() throws IllegalActionException, NameDuplicationException {
        Token _result;
        //FIXME: the noise sample does not have to be an arrayToken

        Token processNoiseSample;
        double[] newState = new double[_stateSpaceSize];

        for (int i = 0; i < _stateSpaceSize; i++) {
            // every component of the particle will be propagated according to its own update equation.

            Expression updateExpression = _updateEquations
                    .get(_stateVariables[i]);

            Parameter p = (Parameter) updateExpression
                    .getAttribute(_stateVariables[i]);
            if (p != null) {
                p.setExpression(((Double) _currentState[i]).toString());
            } else {
                p = new Parameter(_updateEquations.get(_stateVariables[i]),
                        _stateVariables[i]);
                p.setExpression(((Double) _currentState[i]).toString());
            }
            _tokenMap.put(_stateVariables[i], new DoubleToken(
                    _currentState[i])); 
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
            newState[i] = _meanEstimate + processNoiseForElement;
        } 
        _currentState = newState;
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

    /**
     * Generate state estimate from the current particle set and send to the stateEstimate output
     */
    private void _sendStateEstimate() throws IllegalActionException {
        Token[] stateTokens = new Token[_stateSpaceSize]; 
        for (int j = 0; j < _stateSpaceSize; j++) {
            DoubleToken sToken = new DoubleToken(_currentState[j]);
            stateTokens[j] = sToken;
        }
        state.send(0, new RecordToken(_stateLabels, stateTokens)); 
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
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   //// 


    //TODO: Add seed for random number generation.
    private Random _random;

    private double[] _currentState;

    /** Public seed for random number generation */
    private long _seed;

    /** State-space size */
    private int _stateSpaceSize;

    /** Names of state variables */
    private String[] _stateVariables;

    /** State update equations, hashed by state variable name */
    private HashMap<String, Expression> _updateEquations; 

    /** Names of the PortParameter inputs */
    private List<String> _parameterInputs;

    private HashMap _tokenMap;

    private boolean _resetOnEachRun;
    //private PtParser _parser;
    private ASTPtRootNode _parseTree;
    private HashMap<String, ASTPtRootNode> _updateTrees;
    private ParseTreeEvaluator _parseTreeEvaluator;
    private VariableScope _scope;
    private boolean _firstIteration; 

    protected static final String STATE_VARIABLE_NAMES = "stateVariableNames";
    protected static final String PROCESS_NOISE = "processNoise"; 
    protected static final String UPDATE_POSTFIX = "_update";  


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

            Variable result = getScopedVariable(null, AbstractStateSpaceSimulator.this, name);

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

            Variable result = getScopedVariable(null, AbstractStateSpaceSimulator.this, name);

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

            Variable result = getScopedVariable(null, AbstractStateSpaceSimulator.this, name);

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
            return getAllScopedVariableNames(null, AbstractStateSpaceSimulator.this);
        }
    }

}
