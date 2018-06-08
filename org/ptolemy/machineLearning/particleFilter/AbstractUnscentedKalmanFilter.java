/*
 Copyright (c) 1998-2018 The Regents of the University of California.
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
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
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
import ptolemy.math.DoubleMatrixMath;

///////////////////////////////////////////////////////////////////
////

/**
 Abstract Unscented Kalman Filter Base Class.
 @see org.ptolemy.machineLearning.particleFilter.ParticleFilter

 @author Shuhei Emoto
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Red (shuhei)
 @Pt.AcceptedRating Red

 */
public abstract class AbstractUnscentedKalmanFilter
        extends TypedCompositeActor {
    /** Construct the composite actor with a name and a container.
     *  This constructor creates the ports, parameters, and the icon.
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public AbstractUnscentedKalmanFilter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _init();
    }

    /** Construct a UKF in the specified
     *  workspace with no container and an empty string as a name. You
     *  can then change the name with setName(). If the workspace
     *  argument is null, then use the default workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public AbstractUnscentedKalmanFilter(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port that outputs the sigma-points of distribution of state variable at each firing.
     */
    public TypedIOPort sigmaPointOutput;

    /** State estimate output. A record token with one field per state variable. */
    public TypedIOPort stateEstimate;

    /** The value of current time. This parameter is not visible in
     *  the expression screen except in expert mode. Its value initially
     *  is just 0.0, a double, but upon each firing, it is given a
     *  value equal to the current time as reported by the director.
     */
    public Parameter t;

    /** A matrix value used in initialization of sigma points.   */
    public Parameter priorCovariance;

    /** An array value used in initialization of state variables. */
    public Parameter priorMeanState;

    /** a matrix value which determine process noise.  */
    public Parameter processNoiseCovariance;

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

        super.attributeChanged(attribute);
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
            PortParameter s = (PortParameter) AbstractUnscentedKalmanFilter.this
                    .getAttribute(_parameterInputs.get(i));
            s.update();
        }

        // The Sequential Unscented Kalman Filter algorithm
        try {
            if (_firstIteration) {
                _initializeStateVariables();
                _firstIteration = false;
            } else {
                _predictStateVariables();
            }
            _correctStateVariables();

            _sendStateEstimate();
            _generateOutputSigmaPoints();
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

        _stateSpaceSize = _stateNames.length();
        _stateVariables = new String[_stateSpaceSize];
        _NSigmaPoints = _stateSpaceSize * 2 + 1;

        if (_stateSpaceSize > 0) {
            // Set the output type according to the state variables
            _sigmaPointLabels = new String[_stateSpaceSize + 1];
            _sigmaPointTypes = new Type[_stateSpaceSize + 1];
            _stateLabels = new String[_stateSpaceSize];
            _stateTypes = new Type[_stateSpaceSize];
            Token[] nameTokens = _stateNames.arrayValue();
            for (int i = 0; i < _stateSpaceSize; i++) {
                String variableName = ((StringToken) nameTokens[i])
                        .stringValue();

                try {
                    // add a hidden parameter for each state variable name
                    Parameter varPar = (Parameter) this
                            .getAttribute(variableName);
                    if (varPar == null) {
                        varPar = new Parameter(this, variableName);
                        varPar.setTypeEquals(BaseType.DOUBLE);
                        varPar.setExpression("0.0");
                    }
                    varPar.setVisibility(Settable.NONE);
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }

                _sigmaPointLabels[i] = variableName;
                _sigmaPointTypes[i] = BaseType.DOUBLE; // preset to be double

                _stateLabels[i] = variableName;
                _stateTypes[i] = BaseType.DOUBLE; // preset to be double
            }
            _sigmaPointLabels[nameTokens.length] = "weight";
            _sigmaPointTypes[nameTokens.length] = BaseType.DOUBLE;

            sigmaPointOutput.setTypeEquals(
                    new RecordType(_sigmaPointLabels, _sigmaPointTypes));
            stateEstimate
                    .setTypeEquals(new RecordType(_stateLabels, _stateTypes));
        }

        try {
            _workspace.getWriteAccess();
            removeAllEntities();
            removeAllRelations();

            _setUpdateEquations();

            int inputIndex = 0;
            // Inputs-make connections
            int m = inputPortList().size(); // number of inputs
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
                    Parameter stateUpdateSpec = getUserDefinedParameter(
                            _stateVariables[i] + UPDATE_POSTFIX);
                    Set<String> freeIdentifiers = stateUpdateSpec
                            .getFreeIdentifiers();
                    // Create an output port only if the expression references the input.
                    if (freeIdentifiers.contains(inputs[k])) {
                        TypedIOPort port = new TypedIOPort(
                                _updateEquations.get(_stateVariables[i]),
                                inputs[k], true, false);

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

    protected abstract InputType getInputType(String inputName);

    @Override
    public void wrapup() throws IllegalActionException {
        _firstIteration = true;
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check the dimensions of all parameters and ports.
     *  @exception IllegalActionException If the dimensions are illegal.
     */
    protected abstract void _checkParameters() throws IllegalActionException;

    /**
     * Return the expression for a user-defined parameter.
     * @param parameterName Name of parameter
     * @return parameter expression
     * @exception IllegalActionException
     */
    protected String getUserDefinedParameterExpression(String parameterName)
            throws IllegalActionException {
        Parameter param = getUserDefinedParameter(parameterName);
        if (param != null) {
            return param.getExpression();
        } else {
            throw new IllegalActionException(
                    "Parameter " + parameterName + " value is null.");
        }
    }

    /**
     * Return the Parameter that is part of a state space model.
     * @param parameterName Name of parameter
     * @return Parameter object
     * @exception IllegalActionException
     */
    protected abstract Parameter getUserDefinedParameter(String parameterName)
            throws IllegalActionException;

    protected String getMeasurementParameterExpression(String fullName)
            throws IllegalActionException {
        Parameter param = getMeasurementParameter(fullName);
        if (param != null) {
            return param.getExpression();
        } else {
            throw new IllegalActionException(
                    "Parameter " + fullName + " value is null.");
        }
    }

    protected abstract Parameter getMeasurementParameter(String fullName)
            throws IllegalActionException;

    protected abstract Parameter getNoiseParameter(String inputName)
            throws IllegalActionException;

    /** Flag indicating whether the contained model is up to date. */
    protected boolean _upToDate;

    /** Cached State variable names. */
    protected ArrayToken _stateNames;

    /** Array of input Relations. */
    protected IORelation[] _inputRelations;

    /** Labels of sigma points, that contains state names and a weight label. */
    protected String[] _sigmaPointLabels;

    /** Types of each sigma point dimension. */
    protected Type[] _sigmaPointTypes;

    /** Labels of states. */
    protected String[] _stateLabels;

    /** Types of each state dimension. */
    protected Type[] _stateTypes;

    /** Measurement covariance matrix. */
    protected double[][] _Sigma;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Generate output particles and send to the particleOutput port
     */
    private void _generateOutputSigmaPoints() throws IllegalActionException {
        Token[] tokens = new Token[_stateSpaceSize + 1];
        for (int i = 0; i < _NSigmaPoints; i++) {
            double[] l = sigmaPoints[i].getValue();
            for (int j = 0; j < _stateSpaceSize; j++) {
                tokens[j] = new DoubleToken(l[j]);
            }
            tokens[_stateSpaceSize] = new DoubleToken(
                    sigmaPoints[i].getWeight());
            RecordToken r = new RecordToken(_sigmaPointLabels, tokens);
            sigmaPointOutput.send(0, r);
        }
    }

    /** Initialize the class. */
    private void _init()
            throws IllegalActionException, NameDuplicationException {

        sigmaPointOutput = new TypedIOPort(this, "sigmaPointOutput", false,
                true);
        sigmaPointOutput.setTypeEquals(RecordType.EMPTY_RECORD);

        stateEstimate = new TypedIOPort(this, "stateEstimate", false, true);
        stateEstimate.setTypeEquals(RecordType.EMPTY_RECORD);

        t = new Parameter(this, "t");
        t.setTypeEquals(BaseType.DOUBLE);
        t.setVisibility(Settable.EXPERT);
        t.setExpression("0.0");

        priorCovariance = new Parameter(this, "priorCovariance");
        priorCovariance.setTypeEquals(BaseType.DOUBLE_MATRIX);

        priorMeanState = new Parameter(this, "priorMeanState");
        priorMeanState.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        processNoiseCovariance = new Parameter(this, "processNoiseCovariance");
        processNoiseCovariance.setTypeEquals(BaseType.DOUBLE_MATRIX);

        _measurementParameters = new HashMap<String, Parameter>();
        _measurementValues = new HashMap<String, Token>();
        _parser = new PtParser();
        _measurementTypes = new HashMap<>();
        _updateEquations = new HashMap<>();
        _updateTrees = new HashMap<>();
        _measurementEquations = new HashMap<>();
        _noiseEquations = new HashMap<>();

        _firstIteration = true;

        _tokenMap = new HashMap<String, Token>();

        _parseTreeEvaluator = new ParseTreeEvaluator();
        _scope = new VariableScope();

        Director d = new DEDirector(this, "DEDirector");
        d.setPersistent(false);
        this.setDirector(d);

        //((Parameter)this.getAttribute("_isOpaque")).setExpression("true");
        // icon
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-50\" y=\"-30\" "
                + "width=\"150\" height=\"60\" " + "style=\"fill:white\"/>\n"
                + "<text x=\"-45\" y=\"-10\" " + "style=\"font-size:14\">\n"
                + "x_{t+1}=f(x, u, t, w)" + "</text>\n"
                + "<text x=\"-45\" y=\"10\" " + "style=\"font-size:14\">\n"
                + "     y=g(x, u, t, v)" + "</text>\n"
                + "style=\"fill:blue\"/>\n" + "</svg>\n");
    }

    /** Clear the input matrix to zero */
    private void clearMatrix(double[][] matrix_in) {
        for (int row = 0; row < matrix_in.length; row++) {
            for (int col = 0; col < matrix_in[0].length; col++) {
                matrix_in[row][col] = 0;
            }
        }
    }

    /** clear the input vector to zero */
    private void clearVector(double[] vector_in) {
        for (int row = 0; row < vector_in.length; row++) {
            vector_in[row] = 0;
        }
    }

    /**
     * TODO: This function should be defined in DoubleMatrixMath.
     * Return a matrix that is the decomposition of input matrix.
     * Input matrix A is decomposed into the matrix product of L x Lt.
     * @param A input matrix which is decomposed.
     * @param L output matrix (Lower triangular matrix)
     */
    private void choleskyDecomposition(double[][] A, double[][] L) {
        double tolerance = 1.0E-20;
        clearMatrix(L);
        ///////////////////////////////////////////////////////////
        //Implementation of modified cholesky decomposition
        double[] d = new double[A.length]; //singular values
        if (A[0][0] == 0) {
            L[0][0] = 1;
            d[0] = 0;
        } else {
            L[0][0] = A[0][0];
            d[0] = 1.0 / L[0][0];
        }
        for (int i = 1; i < A.length; ++i) {
            for (int j = 0; j <= i; ++j) {
                double lld = A[i][j];
                for (int k = 0; k < j; ++k) {
                    lld -= L[i][k] * L[j][k] * d[k];
                }
                L[i][j] = lld;
            }
            if (L[i][i] > tolerance) {
                d[i] = 1.0 / L[i][i];
            } else {
                d[i] = 0;
            }
        }
        ///////////////////////////////////////////////////////////
        for (int col = 0; col < L[0].length; col++) {
            double scale = d[col];
            if (scale > 0) {
                scale = Math.sqrt(scale);
            }
            for (int row = 0; row < L.length; row++) {
                L[row][col] *= scale;
            }
        }
    }

    private void _setSigmaPoints(SigmaPoint[] points, double[] mean_state,
            double[][] covariance_matrix) throws IllegalActionException {
        double[][] sqrt_covariance_matrix = new double[covariance_matrix.length][covariance_matrix[0].length];
        choleskyDecomposition(covariance_matrix, sqrt_covariance_matrix);
        points[0].setValue(mean_state); //first point is mean state
        double weight = Math.sqrt(_kai + _stateSpaceSize);
        for (int i = 1; i < points.length; i += 2) { //other points are defined by covariance
            int col_id = (i - 1) / 2;
            for (int p_it = 0; p_it < _stateSpaceSize; p_it++) {
                points[i]._x_hat[p_it] = points[0]._x_hat[p_it]
                        + weight * sqrt_covariance_matrix[p_it][col_id];
                points[i + 1]._x_hat[p_it] = points[0]._x_hat[p_it]
                        - weight * sqrt_covariance_matrix[p_it][col_id];
            }
        }
    }

    private void _initializeStateVariables() throws IllegalActionException {
        ////////////////////////////////////////////////////
        // Initialization of prior covariance, state, and process noise settings.
        _priorCovariance = new double[_stateSpaceSize][_stateSpaceSize];
        currentCovariance = new double[_stateSpaceSize][_stateSpaceSize];
        for (int row = 0; row < _stateSpaceSize; row++) {
            for (int col = 0; col < _stateSpaceSize; col++) {
                _priorCovariance[row][col] = ((DoubleMatrixToken) priorCovariance
                        .getToken()).doubleMatrix()[row][col];
                currentCovariance[row][col] = _priorCovariance[row][col];
            }
        }
        _priorMeanStates = new double[_stateSpaceSize];
        currentStates = new double[_stateSpaceSize];
        for (int row = 0; row < _stateSpaceSize; row++) {
            _priorMeanStates[row] = ((DoubleToken) ((ArrayToken) priorMeanState
                    .getToken()).getElement(row)).doubleValue();
            currentStates[row] = _priorMeanStates[row];
        }
        _processNoiseCovariance = new double[_stateSpaceSize][_stateSpaceSize];
        for (int row = 0; row < _stateSpaceSize; row++) {
            for (int col = 0; col < _stateSpaceSize; col++) {
                _processNoiseCovariance[row][col] = ((DoubleMatrixToken) processNoiseCovariance
                        .getToken()).doubleMatrix()[row][col];
            }
        }
        ////////////////////////////////////////////////////

        // Create sigma-points
        sigmaPoints = new SigmaPoint[_NSigmaPoints];
        for (int i = 0; i < sigmaPoints.length; i++) {
            sigmaPoints[i] = new SigmaPoint(_stateSpaceSize, i);
        }
        _setSigmaPoints(sigmaPoints, _priorMeanStates, _priorCovariance);
    }

    /** Propagate sigma points according to the state update equations
     */
    private void _predictStateVariables()
            throws IllegalActionException, NameDuplicationException {
        for (int i = 0; i < _NSigmaPoints; i++) {
            sigmaPoints[i].setNextParticle();
        }

        //calculate the mean state and covariance from propagated sigma points.
        clearVector(currentStates);
        for (int i = 0; i < _NSigmaPoints; i++) {
            for (int row = 0; row < _stateSpaceSize; row++) {
                currentStates[row] += sigmaPoints[i]._weight
                        * sigmaPoints[i]._x_hat[row];
            }
        }
        clearMatrix(currentCovariance);
        double[] diff_xi = new double[_stateSpaceSize];
        double[] w_diff_xi = new double[_stateSpaceSize];
        for (int i = 0; i < _NSigmaPoints; i++) {
            for (int row = 0; row < _stateSpaceSize; row++) {
                diff_xi[row] = (sigmaPoints[i]._x_hat[row]
                        - currentStates[row]);
                w_diff_xi[row] = sigmaPoints[i]._weight * diff_xi[row];
            } //x_diff_i = sigmaPoint_i - meanState
              // Covariance is sum of weight*(x_diff_i * (x_diff_i)T)
            for (int row = 0; row < _stateSpaceSize; row++) {
                for (int col = 0; col < _stateSpaceSize; col++) {
                    currentCovariance[row][col] += (w_diff_xi[row]
                            * diff_xi[col]);
                }
            }
        }
        // add process noise
        for (int row = 0; row < _stateSpaceSize; row++) {
            for (int col = 0; col < _stateSpaceSize; col++) {
                currentCovariance[row][col] += _processNoiseCovariance[row][col];
            }
        }
        // Recalculate sigma-points
        _setSigmaPoints(sigmaPoints, currentStates, currentCovariance);
    }

    //    private void printVector(double[] array, String label) {
    //        System.out.print(label);
    //        for (int col=0; col<array.length; col++) {
    //                System.out.print(array[col]+ " ");
    //        }
    //        System.out.println();
    //    }
    //    private void printMatrix(double[][] matrix, String label) {
    //        System.out.println(label);
    //        for (int row=0; row<matrix.length; row++) {
    //            for (int col=0; col<matrix[0].length; col++) {
    //                System.out.print(matrix[row][col]+ " ");
    //            }
    //            System.out.println();
    //        }
    //    }

    /** Calculate sigma-points in measurement space and correct the mean state and covariance. */
    private void _correctStateVariables()
            throws IllegalActionException, NameDuplicationException {

        // Calculate sigma-points in measurement space
        for (int i = 0; i < sigmaPoints.length; i++) {
            sigmaPoints[i].calcMeasurement();
        }

        // Mean state in measurement space
        double[] Y_hat = new double[sigmaPoints[0]._y_hat.length];
        clearVector(Y_hat);
        for (int i = 0; i < _NSigmaPoints; i++) {
            for (int row = 0; row < Y_hat.length; row++) {
                Y_hat[row] += sigmaPoints[i]._weight
                        * sigmaPoints[i]._y_hat[row];
            }
        }

        // Covariance of mean state in measurement space (added measurement noise)
        double[][] cov_innovation = new double[Y_hat.length][Y_hat.length];
        for (int row = 0; row < Y_hat.length; row++) {
            for (int col = 0; col < Y_hat.length; col++) {
                cov_innovation[row][col] = sigmaPoints[0]._measurementNoiseAll[row][col];
            }
        }
        // Covariance between state-space and measurement-space
        double[][] cov_X_Y = new double[_stateSpaceSize][Y_hat.length];
        clearMatrix(cov_X_Y);

        /// calculate cov_innovation and cov_X_Y
        double[] diff_yi = new double[Y_hat.length];
        double[] w_diff_yi = new double[Y_hat.length];
        double[] diff_xi = new double[_stateSpaceSize];
        for (int i = 0; i < _NSigmaPoints; i++) {
            for (int row = 0; row < Y_hat.length; row++) {
                diff_yi[row] = sigmaPoints[i]._y_hat[row] - Y_hat[row]; //diff_yi = sigmaPoint_i - Y_hat
                w_diff_yi[row] = sigmaPoints[i]._weight * diff_yi[row]; //w_diff_yi = weight*(sigmaPoint_i - Y_hat)
            }
            // Covariance = SUM(weight * (diff_yi * (diff_yi)T)) + measurementNoise
            for (int row = 0; row < Y_hat.length; row++) {
                for (int col = 0; col < Y_hat.length; col++) {
                    cov_innovation[row][col] += w_diff_yi[row] * diff_yi[col];
                }
            }
            for (int row = 0; row < _stateSpaceSize; row++) {
                diff_xi[row] = sigmaPoints[i]._x_hat[row] - currentStates[row]; //diff_xi = (sigmaPoint_i - meanState);
            }
            // Covariance is SUM(weight * (diff_xi x (diff_yi)T))
            for (int row = 0; row < _stateSpaceSize; row++) {
                for (int col = 0; col < Y_hat.length; col++) {
                    cov_X_Y[row][col] += diff_xi[row] * w_diff_yi[col];
                }
            }
        }

        // calculate Kalman gain
        double[][] inv_innovation = DoubleMatrixMath.inverse(cov_innovation);
        double[][] K_gain = DoubleMatrixMath.multiply(cov_X_Y, inv_innovation);

        // calculate correction
        double[][] diff_y_yh = new double[Y_hat.length][1];
        for (int row = 0; row < Y_hat.length; row++) {
            diff_y_yh[row][0] = Y_hat[row] - sigmaPoints[0]._y_actual[row];
        }

        double[][] state_correction = DoubleMatrixMath.multiply(K_gain,
                diff_y_yh);

        double[][] trans_cov_X_Y = DoubleMatrixMath.transpose(cov_X_Y);
        double[][] cov_correction = DoubleMatrixMath.multiply(K_gain,
                trans_cov_X_Y);

        // correct state and covariance
        for (int row = 0; row < _stateSpaceSize; row++) {
            currentStates[row] -= state_correction[row][0];
            for (int col = 0; col < _stateSpaceSize; col++) {
                currentCovariance[row][col] -= cov_correction[row][col];
            }
        }
        // Recalculate sigma-points
        _setSigmaPoints(sigmaPoints, currentStates, currentCovariance);
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
            stateTokens[j] = new DoubleToken(currentStates[j]);
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
        _measurementEquations.put(inputName, measurementEquation);

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
            Expression e = new Expression(this,
                    _stateVariables[i] + UPDATE_POSTFIX);
            //e.setPersistent(false);
            String updateEqnName = _stateVariables[i] + UPDATE_POSTFIX;
            e.expression.setExpression(
                    getUserDefinedParameterExpression(updateEqnName));
            if (_stateVariables[i] == null) {
                System.err.println("One state variable is null at index " + i);
            } else {
                _updateEquations.put(_stateVariables[i], e);
                _updateTrees.put(_stateVariables[i],
                        new PtParser().generateParseTree(_updateEquations
                                .get(_stateVariables[i]).expression
                                        .getExpression()));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    /** List that holds the measurement equation expression objects */
    private HashMap<String, Expression> _measurementEquations;

    /** List that holds the measurement equation expression objects */
    private HashMap<String, Expression> _noiseEquations;

    /** Internal particle representation that has memory of 1 in time */
    private SigmaPoint[] sigmaPoints;

    /** Number of sigma points to be used by the uncsented kalman filter estimator */
    private int _NSigmaPoints;

    /** weight of sigma points */
    private double _kai = 0.0;

    /** prior covariance matrix */
    private double[][] _priorCovariance;

    /** prior mean state values */
    private double[] _priorMeanStates;

    /** process noise covariance matrix */
    private double[][] _processNoiseCovariance;

    /** current mean state values */
    private double[] currentStates;

    /** current covariance of mean state values */
    private double[][] currentCovariance;;

    /** State-space size */
    private int _stateSpaceSize;

    /** Names of state variables */
    private String[] _stateVariables;

    /** State update equations, hashed by state variable name */
    private HashMap<String, Expression> _updateEquations;

    /** Names of the PortParameter inputs */
    private List<String> _parameterInputs;

    private HashMap _tokenMap;

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

    protected static final String STATE_VARIABLE_NAMES = "stateVariableNames";
    protected static final String MEASUREMENT_NOISE = "noiseCovariance";
    protected static final String UPDATE_POSTFIX = "_update";
    protected static final String MEASUREMENT_POSTFIX = "_m";

    protected enum InputType {
        MEASUREMENT_INPUT, CONTROL_INPUT
    };

    // set particle dimensions to be equal to the state space dimension
    private class SigmaPoint {
        public SigmaPoint(int size, int id) {
            this._x_hat = new double[size];
            assignWeight(id);
        }

        /**
         * Assign a weight to the sigma point.
         */
        private void assignWeight(int id) {
            if (id == 0) {
                this._weight = _kai / (_stateSpaceSize + _kai);
            } else {
                this._weight = 1 / (2 * (_stateSpaceSize + _kai));
            }
        }

        public void calcMeasurement()
                throws IllegalActionException, NameDuplicationException {
            Parameter p;
            if (this.getSize() != _stateVariables.length) {
                throw new IllegalActionException(
                        "Particle dimensions must be equal to the state space dimension");
            } else {
                for (int i = 0; i < _stateVariables.length; i++) {
                    if ((AbstractUnscentedKalmanFilter.this)
                            .getUserDefinedParameter(
                                    _stateVariables[i]) == null) {
                        p = new Parameter(AbstractUnscentedKalmanFilter.this,
                                _stateVariables[i]);
                        p.setVisibility(Settable.EXPERT);
                    } else {
                        p = (AbstractUnscentedKalmanFilter.this)
                                .getUserDefinedParameter(_stateVariables[i]);
                    }
                    p.setExpression(((Double) _x_hat[i]).toString());
                    _tokenMap.put(_stateVariables[i],
                            new DoubleToken(_x_hat[i]));
                }

                // evaluate all equations given the particle value
                Token[] predictedValues = new Token[_measurementEquations
                        .size()];
                Token[] measurementNoiseList = new Token[_measurementEquations
                        .size()];
                Token[] actualMeasurementList = new Token[_measurementEquations
                        .size()];
                Type[] inputTypeList = new Type[_measurementEquations.size()];
                int[] inputDimensions = new int[_measurementEquations.size()];
                int count = 0;
                for (String inputName : _measurementEquations.keySet()) {

                    Expression measurementEquation = _measurementEquations
                            .get(inputName);
                    _parseTree = _parser.generateParseTree(
                            measurementEquation.expression.getExpression());
                    predictedValues[count] = _parseTreeEvaluator
                            .evaluateParseTree(_parseTree, _scope);
                    if (predictedValues[count] == null) {
                        throw new IllegalActionException(
                                "Expression yields a null result: "
                                        + measurementEquation.expression
                                                .getExpression());
                    }

                    Expression noiseEq = _noiseEquations.get(inputName);
                    _parseTree = _parser.generateParseTree(
                            noiseEq.expression.getExpression());
                    measurementNoiseList[count] = _parseTreeEvaluator
                            .evaluateParseTree(_parseTree, _scope);

                    // TODO: Do not do this for every particle!
                    inputTypeList[count] = _measurementTypes.get(inputName);
                    if (inputTypeList[count].equals(BaseType.UNKNOWN)) {
                        inputTypeList[count] = predictedValues[count].getType();
                        _measurementTypes.put(inputName, inputTypeList[count]);
                    }
                    if (inputTypeList[count].equals(BaseType.DOUBLE)) {
                        inputDimensions[count] = 1;
                    } else {
                        inputDimensions[count] = ((DoubleMatrixToken) predictedValues[count])
                                .getRowCount();
                    }
                    actualMeasurementList[count] = _measurementValues
                            .get(inputName);

                    count++;
                }

                // concatenate all values into single vector.
                int measurementDim = 0;
                for (int i = 0; i < predictedValues.length; i++) {
                    measurementDim += inputDimensions[i];
                }
                _y_hat = new double[measurementDim];
                _y_actual = new double[measurementDim];
                int it = 0;
                for (int i = 0; i < predictedValues.length; i++) {
                    if (inputTypeList[i].equals(BaseType.DOUBLE)) {
                        _y_hat[it] = ((DoubleToken) predictedValues[i])
                                .doubleValue();
                        _y_actual[it] = ((DoubleToken) actualMeasurementList[i])
                                .doubleValue();
                    } else {
                        double[][] matrix = ((DoubleMatrixToken) predictedValues[i])
                                .doubleMatrix();
                        double[][] mea_matrix = ((DoubleMatrixToken) actualMeasurementList[i])
                                .doubleMatrix();
                        for (int row = 0; row < matrix.length; row++) {
                            _y_hat[it + row] = matrix[row][0];
                            _y_actual[it + row] = mea_matrix[row][0];
                        }
                    }
                    it += inputDimensions[i];
                }

                // concatenate all noise settings into single matrix
                _measurementNoiseAll = new double[measurementDim][measurementDim];
                for (int row = 0; row < measurementDim; row++) {
                    for (int col = 0; col < measurementDim; col++) {
                        _measurementNoiseAll[row][col] = 0;
                    }
                }
                int write_row = 0;
                int write_col = 0;
                for (int i = 0; i < measurementNoiseList.length; i++) {
                    if (inputTypeList[i].equals(BaseType.DOUBLE)) {
                        _measurementNoiseAll[write_row][write_col] = ((DoubleToken) measurementNoiseList[i])
                                .doubleValue();
                    } else {
                        double[][] matrix = ((DoubleMatrixToken) measurementNoiseList[i])
                                .doubleMatrix();
                        for (int row = 0; row < matrix.length; row++) {
                            for (int col = 0; col < matrix[0].length; col++) {
                                _measurementNoiseAll[write_row + row][write_col
                                        + col] = matrix[row][col];
                            }
                        }
                    }
                    write_row += inputDimensions[i];
                    write_col += inputDimensions[i];
                }
            }
        }

        public double[] getValue() {
            double[] values = new double[this.getSize()];
            for (int i = 0; i < this.getSize(); i++) {
                values[i] = this._x_hat[i];
            }
            return values;
        }

        public int getSize() {
            return this._x_hat.length;
        }

        public void setNextParticle()
                throws NameDuplicationException, IllegalActionException {

            Token _result;
            for (int i = 0; i < _stateSpaceSize; i++) {
                // every component of the particle will be propagated according to its own update equation.

                Expression updateExpression = _updateEquations
                        .get(_stateVariables[i]);

                Parameter p = (Parameter) updateExpression
                        .getAttribute(_stateVariables[i]);
                if (p != null) {
                    p.setExpression(((Double) _x_hat[i]).toString());
                } else {
                    p = new Parameter(_updateEquations.get(_stateVariables[i]),
                            _stateVariables[i]);
                    p.setExpression(((Double) _x_hat[i]).toString());
                }
                _tokenMap.put(_stateVariables[i], new DoubleToken(_x_hat[i]));
                // set the control input values in scope
            }

            double[] newState = new double[this.getSize()];
            for (int i = 0; i < _stateSpaceSize; i++) {
                _parseTree = _updateTrees.get(_stateVariables[i]);
                _result = _parseTreeEvaluator.evaluateParseTree(_parseTree,
                        _scope);

                if (_result == null) {
                    throw new IllegalActionException(
                            "Expression yields a null result: "
                                    + _updateEquations
                                            .get(_stateVariables[i]).expression
                                                    .getExpression());
                }
                newState[i] = ((DoubleToken) _result.add(new DoubleToken(0.0)))
                        .doubleValue();
            }
            this.setValue(newState);
        }

        public void setValue(double[] l) throws IllegalActionException {

            if (l.length != this._x_hat.length) {
                throw new IllegalActionException(
                        "Cannot set a value with different size");
            }

            for (int i = 0; i < l.length; i++) {
                _x_hat[i] = l[i];
            }
        }

        public double getWeight() {
            return _weight;
        }

        /**
         * Value of the sigma-point. Size is equal to _ssSize
         */
        private double[] _x_hat;
        /**
         * Weight of the sigma-point
         */
        private double _weight;

        /**
         * Value of the sigma-point in measurement space
         */
        private double[] _y_hat;

        /**
         * Measurement noise setting for all measurement inputs
         */
        private double[][] _measurementNoiseAll;

        /**
         * Actual measurement
         */
        private double[] _y_actual;
    }

    private class VariableScope extends ModelScope {
        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        @Override
        public Token get(String name) throws IllegalActionException {
            if (name.equals("time") || name.equals("t")) {
                return new DoubleToken(
                        getDirector().getModelTime().getDoubleValue());
            }

            Token token = (Token) _tokenMap.get(name);

            if (token != null) {
                return token;
            }

            Variable result = getScopedVariable(null,
                    AbstractUnscentedKalmanFilter.this, name);

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

            Variable result = getScopedVariable(null,
                    AbstractUnscentedKalmanFilter.this, name);

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

            Variable result = getScopedVariable(null,
                    AbstractUnscentedKalmanFilter.this, name);

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
            return getAllScopedVariableNames(null,
                    AbstractUnscentedKalmanFilter.this);
        }
    }
}
