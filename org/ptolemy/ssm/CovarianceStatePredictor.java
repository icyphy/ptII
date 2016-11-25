/* 
 Copyright (c) 1998-2015 The Regents of the University of California.
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
package org.ptolemy.ssm;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.math.DoubleMatrixMath;

///////////////////////////////////////////////////////////////////
////

/**
 Implementation of StatePredictor witch uses acceleration control input.

 <p>
 This actor reads a single record(x, y, vx, vy, covariance) from the port <i>current_state</i> 
 and an array of vector(acc_x, acc_y) from the port "control_inputs".
 Output is an array of states(x, y, vx, vy, covariance) whose length is <i>prediction horizon</i>.
 Output states are calculated using a state space model given by
 <pre>
 X_{t+1} = f(X_t) = [P_{t+1}; V_{t+1}]
    P_{t+1} = P_t + V_t + 0.5*Acc_t
    V_{t+1} = V_t + Acc_t
 C_{t+1} = A*C_t*AT
 </pre>
 <p>where X is the state vector(P: position, V: velocity),  Acc is an acceleration input vector, 
 C is the covariance matrix of X, and A is the jacobian of transition function f(X).
 If the length of control_inputs is shorter than prediction horizon, 
 the last value of cotrol_inputs is used until the step of prediction horizon.
 </p>

 @author Shuhei Emoto
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (shuhei)
 @Pt.AcceptedRating 

 */
public class CovarianceStatePredictor extends TypedAtomicActor {
    /** Construct the composite actor with a name and a container.
     *  This constructor creates the ports, parameters, and the icon.
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public CovarianceStatePredictor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        /** Initialize the class. */

        // an array of control value for robots
        controlInput = new TypedIOPort(this, "control_input", true, false);
        controlInput.setTypeEquals(new ArrayType (new ArrayType(BaseType.DOUBLE)));

        // an array of state of robots
        currentState = new TypedIOPort(this, "current_state", true, false);
        ArrayToken names = new ArrayToken("{\"x\",\"y\",\"vx\",\"vy\",\"covariance\"}");
        _labels = new String[names.length()];
        _types = new Type[names.length()];
        for (int i = 0; i < names.length()-1; i++) {
            String stateName = ((StringToken) names.getElement(i)).stringValue();
            _labels[i] = stateName;
            _types[i] = BaseType.DOUBLE; // preset to be double
        }
        _labels[names.length()-1] =  ((StringToken) names.getElement(names.length()-1)).stringValue();
        _types[names.length()-1] = BaseType.DOUBLE_MATRIX; // preset to be double
        
        currentState.setTypeEquals(new RecordType(_labels, _types));

        // an array of predicted states of a robot
        predictedStates = new TypedIOPort(this, "predicted_states", false, true);
        predictedStates.setTypeEquals(new ArrayType(new RecordType(_labels, _types)));
         
        _timeHorizon = 1;
        timeHorizon = new PortParameter(this, "predictionHorizon");
        timeHorizon.setExpression("1");
        timeHorizon.setTypeEquals(BaseType.INT);
        new StringAttribute(timeHorizon.getPort(), "_cardinal")
        .setExpression("SOUTH");
        new Parameter(timeHorizon.getPort(), "_showName").setExpression("true");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * control input for robots.
     */
    public TypedIOPort controlInput;

    /**
     * current state of robots.
     */
    public TypedIOPort currentState;

    /**
     * predicted states of robots.
     */
    public TypedIOPort predictedStates;

    /** 
     * The time-horizon which defines the number of steps of prediction.
     */
    public PortParameter timeHorizon;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == timeHorizon) {
            _timeHorizon = ((IntToken) timeHorizon.getToken()).intValue();
        }
    }

    @Override
    public void fire() throws IllegalActionException {

        super.fire();
        timeHorizon.update();

        /// parsing of input
        if (controlInput.hasToken(0)) {
            ArrayToken arrayOfControl = ((ArrayToken) controlInput.get(0));
            ArrayToken uArray = (ArrayToken) arrayOfControl.getElement(0);
            _uValue = new double[arrayOfControl.length()][uArray.length()];
            for (int i = 0; i < _uValue.length; i++) {
                for (int it_u =0; it_u < _uValue[0].length; it_u++) {
                    uArray = (ArrayToken) arrayOfControl.getElement(i);
                    _uValue[i][it_u] = ((DoubleToken) uArray.getElement(it_u)).doubleValue();
                }
            }
        }
        if (currentState.hasToken(0)) {
            _currentState = new double[_labels.length];
            RecordToken incoming = (RecordToken) currentState.get(0);
            for (int it=0; it<_labels.length-1; it++) {
                _currentState[it] = ((DoubleToken) incoming.get(_labels[it])).doubleValue();
            }
            _currentCovariance = ((DoubleMatrixToken) incoming.get(_labels[_labels.length-1])).doubleMatrix();
        }

        _calcPredict();
        predictedStates.send(0, new ArrayToken(_predictedStates));
    }

    
    @Override
    public boolean prefire() throws IllegalActionException {
        super.prefire();
        
        if (!controlInput.hasToken(0) || !currentState.hasToken(0)) {
            return false;
        }
        return true;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double[][] _currentCovariance;
    private double[] _currentState;
    private String[] _labels;
    private RecordToken[] _predictedStates;
    private int  _timeHorizon;
    private Type[] _types;
    private double[][] _uValue;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _calcPredict() throws IllegalActionException {
        // predict the future state of each robots.
        _predictedStates = new RecordToken[_timeHorizon];
        int predict_step = 0;
        //First predicted states are calclated from current states.
        Token[] result_values = new Token[_labels.length]; //x, y, vx, vy, covariance
        result_values[0] = new DoubleToken(_currentState[0] + _currentState[2] + 0.5*_uValue[0][0]);
        result_values[1] = new DoubleToken(_currentState[1] + _currentState[3] + 0.5*_uValue[0][1]);
        result_values[2] = new DoubleToken(_currentState[2] + _uValue[0][0]);
        result_values[3] = new DoubleToken(_currentState[3] + _uValue[0][1]);
        ////////////////////////////////////
        ////Set transition matrix
        // (1 0 1 0)
        // (0 1 0 1)
        // (0 0 1 0)
        // (0 0 0 1)
        double[][] transitionMatrix = DoubleMatrixMath.identityMatrixDouble(4);
        transitionMatrix[0][2] = 1;
        transitionMatrix[1][3] = 1;
        double[][] tTransitionMatrix = DoubleMatrixMath.transpose(transitionMatrix);
        ////////////////////////////////////
        _currentCovariance = DoubleMatrixMath.multiply(_currentCovariance, tTransitionMatrix);
        _currentCovariance = DoubleMatrixMath.multiply(transitionMatrix, _currentCovariance);
        result_values[4] = new DoubleMatrixToken(_currentCovariance);
        _predictedStates[predict_step] = new RecordToken(_labels, result_values);
        
        for (predict_step = 1; predict_step < _timeHorizon; predict_step++) {
            int control_step = Math.min(_uValue.length-1, predict_step);
            //If array length of _uValue is shorter than TimeHorizon, the last _uValue is held until the last of prediction step.
            result_values[0] = new DoubleToken(((DoubleToken)result_values[0]).doubleValue() + ((DoubleToken)result_values[2]).doubleValue() + 0.5*_uValue[control_step][0]);
            result_values[1] = new DoubleToken(((DoubleToken)result_values[1]).doubleValue() + ((DoubleToken)result_values[3]).doubleValue() + 0.5*_uValue[control_step][1]);
            result_values[2] = new DoubleToken(((DoubleToken)result_values[2]).doubleValue() + _uValue[control_step][0]);
            result_values[3] = new DoubleToken(((DoubleToken)result_values[3]).doubleValue() + _uValue[control_step][1]);
            _currentCovariance = DoubleMatrixMath.multiply(_currentCovariance, tTransitionMatrix);
            _currentCovariance = DoubleMatrixMath.multiply(transitionMatrix, _currentCovariance);
            result_values[4] = new DoubleMatrixToken(_currentCovariance);
            _predictedStates[predict_step] = new RecordToken(_labels, result_values);
        }
    }
}
