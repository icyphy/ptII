/* Implementation of StatePredictor which uses control inputs.

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
package org.ptolemy.ssm;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.DoubleMatrixMath;

///////////////////////////////////////////////////////////////////
////

/**
 Implementation of StatePredictor witch uses acceleration control input.

 <p>StatePredictor runs on a state space model given by
 <pre>
 X_{t+1} = f(X_t, U_t, t)
 </pre>
 where X is the state vector, U is the input vector. 
 This actor reads a single record(x, y, vx, vy) from the port "current_state" 
 and an array of vector(acc_x, acc_y) from the port "control_inputs".
 Output is an array of state(x, y, vx, vy) whose length is "prediction horizon".
 If the length of control_inputs is shorter than prediction horizon, 
 the last value of cotrol_inputs is used until the step of prediction horizon.

 @author Shuhei Emoto
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (shuhei)
 @Pt.AcceptedRating Red (shuhei)

 */
public class StatePredictorWithAccControl extends TypedAtomicActor {
    /** Construct the composite actor with a name and a container.
     *  This constructor creates the ports, parameters, and the icon.
     * @param container The container.
     * @param name The name.
     * @exception NameDuplicationException If another entity already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public StatePredictorWithAccControl(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        /** Initialize the class. */

        // an array of control value for robots
        controlInput = new TypedIOPort(this, "control_input", true, false);
        controlInput.setTypeEquals(new ArrayType (new ArrayType(BaseType.DOUBLE)));

        // an array of state of robots
        currentState = new TypedIOPort(this, "current_state", true, false);
        //FIXME: Labels of robot's state should be defined by StateSpaceModel
        ArrayToken names = new ArrayToken("{\"x\",\"y\",\"vx\",\"vy\"}");
        String stateName;
        _labels = new String[names.length()];
        _types = new Type[names.length()];
        for (int i = 0; i < names.length(); i++) {
            stateName = ((StringToken) names.getElement(i)).stringValue();
            _labels[i] = stateName;
            _types[i] = BaseType.DOUBLE; // preset to be double
        }
        currentState.setTypeEquals(new RecordType(_labels, _types));

        // an array of predicted states of a robot
        predictedStates = new TypedIOPort(this, "predicted_states", false, true);
        predictedStates.setTypeEquals(new ArrayType(new RecordType(_labels, _types)));

        // an array of jacobian of states.
        jacobianOfStates = new TypedIOPort(this, "jacobianOfStates", false, true);
        jacobianOfStates.setTypeEquals(new ArrayType(BaseType.DOUBLE_MATRIX));
         
        _timeHorizon = 1;
        timeHorizon = new Parameter(this, "prediction horizon");
        timeHorizon.setExpression("1");
        timeHorizon.setTypeEquals(BaseType.INT);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    /** 
     * The time-horizon which defines the number of steps of prediction.
     */
    public Parameter timeHorizon;

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
     * jacobian of predicted states (dX/dU).
     * this port output array of matrix {dX1/dU, dX2/dU, ..., dXn/dU} 
     * where Xn means predicted states of n-Step later and U means control input (matrix [U1, U2, ..., Un]).
     */
    public TypedIOPort jacobianOfStates;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
   @Override
    public void fire() throws IllegalActionException {

        super.fire();

        /// parsing of input
        if (controlInput.hasToken(0)) {
            ArrayToken arrayOfControl = ((ArrayToken) controlInput.get(0));
            ArrayToken uArray = (ArrayToken) arrayOfControl.getElement(0);
            _uValue = new double[arrayOfControl.length()][uArray.length()];
            for (int i = 0; i < _uValue.length; i++) {
                for(int it_u =0; it_u < _uValue[0].length; it_u++) {
                    uArray = (ArrayToken) arrayOfControl.getElement(i);
                    _uValue[i][it_u] = ((DoubleToken) uArray.getElement(it_u)).doubleValue();
                }
            }
        }

        if (currentState.hasToken(0)) {
            _currentState = new double[_labels.length];
            RecordToken incoming = (RecordToken) currentState.get(0);
            for(int it=0; it<_labels.length; it++) {
                _currentState[it] = ((DoubleToken) incoming.get(_labels[it])).doubleValue();
            }
        }

        calcPredict();
        predictedStates.send(0, new ArrayToken(_predictedStates));
        DoubleMatrixToken[] jacobianResult = new DoubleMatrixToken[_jacobianOfStates.length];
        for(int i=0; i<jacobianResult.length; i++) {
            jacobianResult[i] = new DoubleMatrixToken(_jacobianOfStates[i]);
        }
        jacobianOfStates.send(0, new ArrayToken(jacobianResult));
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == timeHorizon) {
            _timeHorizon = ((IntToken) timeHorizon.getToken()).intValue();
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int  _timeHorizon;
    private String[] _labels;
    private Type[] _types;
    private double[] _currentState;
    private double[][] _uValue;
    private RecordToken[] _predictedStates;
    private double[][][] _jacobianOfStates;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void calcPredict() throws IllegalActionException {
        // predict the future state of each robots.
        _predictedStates = new RecordToken[_timeHorizon];
        //dimension of the jacobian is Dim of state(x,y,vx, vy) X Num of Input (accx1, accy1, accx2, accy2, ....)
        _jacobianOfStates = new double[_timeHorizon][4][_uValue.length*_uValue[0].length];
        int predict_step = 0;
        //First predicted states are calclated from current states.
        DoubleToken[] result_values = new DoubleToken[_labels.length];
        result_values[0] = new DoubleToken(_currentState[0] + _currentState[2] + 0.5*_uValue[0][0]);
        result_values[1] = new DoubleToken(_currentState[1] + _currentState[3] + 0.5*_uValue[0][1]);
        result_values[2] = new DoubleToken(_currentState[2] + _uValue[0][0]);
        result_values[3] = new DoubleToken(_currentState[3] + _uValue[0][1]);
        for(int it_val=4; it_val<result_values.length; it_val++) {
            result_values[it_val] = new DoubleToken(_currentState[it_val]);
        }
        _predictedStates[predict_step] = new RecordToken(_labels, result_values);
        ////////////////////////////////////
        ////Set matrix
        // (0.5   0 0 0 ....0 )
        // (0   0.5 0 0 ....0 )
        // (1     0 0 0 ....0 )
        // (0     1 0 0 ....0 )
        for(int row=0; row<_jacobianOfStates[0].length; row++) {
            for(int col=0; col<_jacobianOfStates[0][0].length; col++) {
                _jacobianOfStates[predict_step][row][col] = 0;
            }
        }
        _jacobianOfStates[predict_step][0][0] = 0.5;
        _jacobianOfStates[predict_step][1][1] = 0.5;
        _jacobianOfStates[predict_step][2][0] = 1;
        _jacobianOfStates[predict_step][3][1] = 1;
        ////////////////////////////////////
        
        for (predict_step = 1; predict_step < _timeHorizon; predict_step++) {
            int control_step = Math.min(_uValue.length-1, predict_step);
            //If array length of _uValue is shorter than TimeHorizon, the last _uValue is held until the last of prediction step.
            result_values[0] = new DoubleToken(result_values[0].doubleValue() + result_values[2].doubleValue() + 0.5*_uValue[control_step][0]);
            result_values[1] = new DoubleToken(result_values[1].doubleValue() + result_values[3].doubleValue() + 0.5*_uValue[control_step][1]);
            result_values[2] = new DoubleToken(result_values[2].doubleValue() + _uValue[control_step][0]);
            result_values[3] = new DoubleToken(result_values[3].doubleValue() + _uValue[control_step][1]);
            for(int it_val=4; it_val<result_values.length; it_val++) {
                result_values[it_val] = new DoubleToken(result_values[it_val].doubleValue());
            }
            _predictedStates[predict_step] = new RecordToken(_labels, result_values);
            ////////////////////////////////////
            ////Set matrix dUm/dU  (Um: control_input of control_step M).
            // When the dimension of control_step is 2, dUm/dU is
            // (0 0 0 0 1 0 0 0 ... 0 )
            // (0 0 0 0 0 1 0 0 ... 0 )
            double[][] dUm_dU = new double[2][_uValue.length*_uValue[0].length];
            for(int row=0; row<dUm_dU.length; row++) {
                for(int col=0; col<dUm_dU[0].length; col++) {
                    dUm_dU[row][col] = 0;
                }
                dUm_dU[row][control_step*2+row] = 1;
            }
            //// Set Matrix dXn/dXn-1, dXn/dUm (Xn: states of predict_step N);
            // dXn/dXn-1 = 
            // (1 0 1 0)
            // (0 1 0 1)
            // (0 0 1 0)
            // (0 0 0 1)
            double[][] dXn_dXn_1 = DoubleMatrixMath.identityMatrixDouble(4);
            dXn_dXn_1[0][2] = 1;
            dXn_dXn_1[1][3] = 1;
            // dXn/dUm = 
            // (0.5  0)
            // (0  0.5)
            // (1    0)
            // (0    1)
            double[][] dXn_dUm = new double[4][2];
            dXn_dUm[0][0] = 0.5;
            dXn_dUm[1][1] = 0.5;
            dXn_dUm[2][0] = 1;
            dXn_dUm[3][1] = 1;
            //// calculate dXn_dU = dXn/dXn-1*(dXn-1/dU) + dXn/dUm*(dUm/dU)
            double[][] dXn_dU_via_Xn_1 = DoubleMatrixMath.multiply(dXn_dXn_1, _jacobianOfStates[predict_step-1]);
            double[][] dXn_dU_via_Um = DoubleMatrixMath.multiply(dXn_dUm, dUm_dU);
            for(int row=0; row<_jacobianOfStates[0].length; row++) {
                for(int col=0; col<_jacobianOfStates[0][0].length; col++) {
                    _jacobianOfStates[predict_step][row][col] = dXn_dU_via_Xn_1[row][col] + dXn_dU_via_Um[row][col];
                }
            }
            ////result matrix should be below. (When predict_step = 1).
            // (2.5   0 1.5   0 0.5     0 0 0 ....0 ) 
            // (0   2.5   0 1.5   0   0.5 0 0 ....0 )
            // (1     0   1   0   1     0 0 0 ....0 ) 
            // (0     1   0   1   0     1 0 0 ....0 )
            ////////////////////////////////////
        }
    }
}
