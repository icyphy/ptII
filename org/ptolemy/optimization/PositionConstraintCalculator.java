/*
 Copyright (c) 2014-2018 The Regents of the University of California.
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

package org.ptolemy.optimization;

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
import ptolemy.kernel.util.Settable;
import ptolemy.math.DoubleArrayMath;
import ptolemy.math.DoubleMatrixMath;

/**
The class for calculation of constraints of Swarm-Robots.

This class computes following constraint functions used in an optimization class.
The number of functions is N X H, where N is the number of reference trajectories and H is prediction time horizon.
N and H are obtained from the length of "referenceTrajectories" and "trajectory", respectively.
The output array consists of {f0_0, f0_1, ... , f0_t, f1_0, ... , f1_t, ... ,fi_t}.
If i is "robotID", constraint functions is
fi_t = - (Px_t - Pi_t) * CovP^-1 * (Px_t - Pi_t) + 1.0 &gt; 0,
where Px_t is a position of robot at time t obtained from "trajectory",
Pi_t is a position of the robot predicted in previous control step obtained from "referenceTrajectories",
and CovP is the covariance of Pi at time t.
If i is not "robotID", constraint functions is
fi_t = g(Pi) - sqrt(CovR) = |Px_t - Pi_t|^2 - D^2 - sqrt(CovR) &gt; 0,
CovR = (dg/dPi) * CovPi * (dg/dPi)T,
where D is "DistanceLimit" parameter and Pi_t is a position of other robot or obstacle obtained from "referenceTrajectories".
This class outputs not only the results of constraints function, but also gradients of fi_t.

@author Shuhei Emoto
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (shuhei)
@Pt.AcceptedRating

 */

public class PositionConstraintCalculator extends TypedAtomicActor {
    public PositionConstraintCalculator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // an array of robot locations
        trajectory = new TypedIOPort(this, "trajectory", true, false);
        {
            ArrayToken namesOfTrajectory = new ArrayToken("{\"x\",\"y\"}");
            String[] labelsOfTrajectory = new String[namesOfTrajectory
                    .length()];
            Type[] typesOfTrajectory = new Type[namesOfTrajectory.length()];
            for (int i = 0; i < namesOfTrajectory.length(); i++) {
                labelsOfTrajectory[i] = ((StringToken) namesOfTrajectory
                        .getElement(i)).stringValue();
                typesOfTrajectory[i] = BaseType.DOUBLE; // preset to be double
            }
            trajectory.setTypeEquals(new ArrayType(
                    new RecordType(labelsOfTrajectory, typesOfTrajectory)));
        }
        // an array of jacobian of robot locations
        jacobianOfTrajectory = new TypedIOPort(this, "jacobianOftrajectory",
                true, false);
        jacobianOfTrajectory
                .setTypeEquals(new ArrayType(BaseType.DOUBLE_MATRIX));

        //estimated position of target
        referenceTrajectories = new TypedIOPort(this, "referenceTrajectories",
                true, false);
        {
            ArrayToken namesOfRS = new ArrayToken(
                    "{\"x\",\"y\",\"covariance\"}");
            String[] labelsOfRS = new String[namesOfRS.length()];
            Type[] typesOfRS = new Type[namesOfRS.length()];
            for (int i = 0; i < namesOfRS.length(); i++) {
                labelsOfRS[i] = ((StringToken) namesOfRS.getElement(i))
                        .stringValue();
            }
            typesOfRS[0] = BaseType.DOUBLE; // x : preset to be double
            typesOfRS[1] = BaseType.DOUBLE; // y : preset to be double
            typesOfRS[2] = BaseType.DOUBLE_MATRIX; //covariance : preset to be double matrix
            referenceTrajectories.setTypeEquals(new ArrayType(
                    new ArrayType(new RecordType(labelsOfRS, typesOfRS))));
        }

        //instantiate output ports.
        positionConstraints = new TypedIOPort(this, "positionConstraints",
                false, true);
        positionConstraints.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        gradientOfConstraints = new TypedIOPort(this, "gradientOfConstraints",
                false, true);
        gradientOfConstraints
                .setTypeEquals(new ArrayType(BaseType.DOUBLE_MATRIX));

        _distanceLimit = 1;
        DistanceLimit = new Parameter(this, "distance limit");
        DistanceLimit.setExpression("1.0");
        DistanceLimit.setTypeEquals(BaseType.DOUBLE);

        _scalefactorOfStdDeviation = 0.0; //scale factor for standard deviation of constraints
        ScalefactorOfVariance = new Parameter(this,
                "scale factor for robustness");
        ScalefactorOfVariance.setExpression("0.0");
        ScalefactorOfVariance.setTypeEquals(BaseType.DOUBLE);
        ScalefactorOfVariance.setVisibility(Settable.EXPERT);

        //the index of the robot in referenceTrajectories array.
        _robotId = -1;
        RobotId = new Parameter(this, "robot id");
        RobotId.setExpression("-1");
        RobotId.setTypeEquals(BaseType.INT);
        RobotId.setVisibility(Settable.EXPERT);
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == DistanceLimit) {
            _distanceLimit = ((DoubleToken) DistanceLimit.getToken())
                    .doubleValue();
        } else if (attribute == ScalefactorOfVariance) {
            _scalefactorOfStdDeviation = ((DoubleToken) ScalefactorOfVariance
                    .getToken()).doubleValue();
        } else if (attribute == RobotId) {
            _robotId = ((IntToken) RobotId.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    public void fire() throws IllegalActionException {

        super.fire();

        /// parsing input
        if (trajectory.hasToken(0)) {
            ArrayToken robArray = ((ArrayToken) trajectory.get(0));
            _robotX = new double[robArray.length()];
            _robotY = new double[robArray.length()];
            for (int i = 0; i < robArray.length(); i++) {
                RecordToken robotState = (RecordToken) robArray.getElement(i);
                _robotX[i] = ((DoubleToken) robotState.get("x")).doubleValue();
                _robotY[i] = ((DoubleToken) robotState.get("y")).doubleValue();
            }
        }

        // Copy input tokens to _objectX, _objectY, and _objectCovariances.
        // If an input array is shorter than _robotX,
        // the last value of the input token is copied.
        if (referenceTrajectories.hasToken(0)) {
            ArrayToken objArrays = ((ArrayToken) referenceTrajectories.get(0));
            ArrayToken objArray = (ArrayToken) objArrays.getElement(0);
            _objectNum = objArrays.length();
            _objectX = new double[_robotX.length * _objectNum];
            _objectY = new double[_robotX.length * _objectNum];
            _objectCovariances = new double[_robotX.length * _objectNum][2][2];
            for (int j = 0; j < objArrays.length(); j++) {
                objArray = (ArrayToken) objArrays.getElement(j);
                for (int i = 0; i < _robotX.length; i++) {
                    RecordToken objectState = (RecordToken) objArray
                            .getElement(Math.min(i, objArray.length() - 1));
                    _objectX[i
                            + j * _robotX.length] = ((DoubleToken) objectState
                                    .get("x")).doubleValue();
                    _objectY[i
                            + j * _robotX.length] = ((DoubleToken) objectState
                                    .get("y")).doubleValue();
                    double[][] matrix = ((DoubleMatrixToken) objectState
                            .get("covariance")).doubleMatrix();
                    for (int row = 0; row < 2; row++) {
                        for (int col = 0; col < 2; col++) {
                            _objectCovariances[i + j
                                    * _robotX.length][row][col] = matrix[row][col];
                        }
                    }
                }
            }
        }

        if (jacobianOfTrajectory.hasToken(0)) {
            ArrayToken jacobiArray = ((ArrayToken) jacobianOfTrajectory.get(0));
            DoubleMatrixToken firstOne = (DoubleMatrixToken) jacobiArray
                    .getElement(0);
            _jacobianOfTrajectory = new double[jacobiArray.length()][2][firstOne
                    .getColumnCount()];
            for (int i = 0; i < jacobiArray.length(); i++) {
                DoubleMatrixToken jacobian = (DoubleMatrixToken) jacobiArray
                        .getElement(i);
                double[][] matrix = jacobian.doubleMatrix();
                for (int row = 0; row < _jacobianOfTrajectory[0].length; row++) {
                    for (int col = 0; col < _jacobianOfTrajectory[0][0].length; col++) {
                        _jacobianOfTrajectory[i][row][col] = matrix[row][col];
                    }
                }
            }
        }

        //compute all constraint functions.
        funcConstraints();

        positionConstraints.send(0, new ArrayToken(_positionConstraints));
        gradientOfConstraints.send(0, new ArrayToken(_gradientOfConstraints));
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        super.prefire();

        if ((trajectory.hasToken(0)) && (referenceTrajectories.hasToken(0))
                && (jacobianOfTrajectory.hasToken(0))) {
            return true;
        } else {
            return false;
        }
    }

    public TypedIOPort referenceTrajectories;
    public TypedIOPort trajectory;
    public TypedIOPort positionConstraints;
    public TypedIOPort gradientOfConstraints;
    public TypedIOPort jacobianOfTrajectory;

    public Parameter DistanceLimit;
    public Parameter ScalefactorOfVariance;
    public Parameter RobotId;

    // code for computing position constraints.
    private void funcConstraints() throws IllegalActionException {
        int predictTimeHorizon = _robotX.length;
        int numOfConstraints = _objectX.length;
        _positionConstraints = new DoubleToken[numOfConstraints];
        _gradientOfConstraints = new DoubleMatrixToken[numOfConstraints];
        for (int objIt = 0; objIt < _objectNum; objIt++) {
            for (int k = 0; k < predictTimeHorizon; k++) {
                int indexOfObject = k + objIt * predictTimeHorizon;
                double[] dP = new double[2];
                dP[0] = _robotX[k] - _objectX[indexOfObject];
                dP[1] = _robotY[k] - _objectY[indexOfObject];
                if (_robotId == objIt) {
                    //constraint function is fi_t = - dpT * CovP^-1 * dp + sigmaScale > 0
                    double[][] inverseCov = DoubleMatrixMath
                            .inverse(_objectCovariances[indexOfObject]); //inverse of covariance
                    double[] invC_dP = DoubleMatrixMath.multiply(dP,
                            inverseCov);
                    double sigmaScale = Math.max(_scalefactorOfStdDeviation,
                            1.0);
                    double fi = sigmaScale
                            - DoubleArrayMath.dotProduct(dP, invC_dP);
                    double[] jacobianOfFi = DoubleArrayMath.multiply(invC_dP,
                            -2.0); // Jacobian is -2 * dP * C^-1 * dPx/dx

                    _positionConstraints[indexOfObject] = new DoubleToken(fi);
                    double[][] gradient = new double[1][2];
                    for (int i = 0; i < 2; i++) {
                        gradient[0][i] = jacobianOfFi[i];
                    }
                    _gradientOfConstraints[indexOfObject] = new DoubleMatrixToken(
                            DoubleMatrixMath.multiply(gradient,
                                    _jacobianOfTrajectory[k]));
                } else {
                    // Constraint function is
                    // fi_t = g(Pi) - sqrt(CovR) = |Px_t - Pi_t|^2 - D^2 - sqrt(CovR) > 0,
                    // CovR = (dg/dPi) * CovPi * (dg/dPi)T
                    double gXk = dP[0] * dP[0] + dP[1] * dP[1]
                            - _distanceLimit * _distanceLimit;
                    double[] jacobianOfGXk = DoubleArrayMath.multiply(dP, -2.0); // Jacobian of g(Pi) = dg/dPi
                    // calculate J x Covariance
                    double[] jacobian_covariance = DoubleMatrixMath.multiply(
                            _objectCovariances[indexOfObject], jacobianOfGXk);
                    // caluculate J x C x JT
                    double covarianceOfGXk = DoubleArrayMath
                            .dotProduct(jacobian_covariance, jacobianOfGXk);
                    double fi = gXk - _scalefactorOfStdDeviation
                            * Math.sqrt(covarianceOfGXk);

                    //calculate dfi/dPx
                    /// dg/drob = 2*dP
                    double[] jacobianOfG_Rob = DoubleArrayMath.multiply(dP,
                            2.0);
                    /// d(-sqrt(CovR))/drob = - factor/sqrt(cov) * J*C*dJ/dx
                    double invSqrtCov = -_scalefactorOfStdDeviation
                            / Math.sqrt(covarianceOfGXk);
                    double[] jacobianOfCov_Rob = new double[2];
                    jacobianOfCov_Rob[0] = invSqrtCov
                            * (jacobian_covariance[0] * -2.0); // Note that dJ/dx = (-2, 0)
                    jacobianOfCov_Rob[1] = invSqrtCov
                            * (jacobian_covariance[1] * -2.0); // Note that dJ/dy = (0, -2)

                    //dfi/dPx = dg/drob + d(-sqrt(CovR))/drob
                    double[] jacobianOfFi = DoubleArrayMath.add(jacobianOfG_Rob,
                            jacobianOfCov_Rob);

                    _positionConstraints[indexOfObject] = new DoubleToken(fi);

                    double[][] gradient = new double[1][2];
                    for (int i = 0; i < 2; i++) {
                        gradient[0][i] = jacobianOfFi[i];
                    }
                    _gradientOfConstraints[indexOfObject] = new DoubleMatrixToken(
                            DoubleMatrixMath.multiply(gradient,
                                    _jacobianOfTrajectory[k]));
                }
            }
        }
    }

    private double _distanceLimit;
    private double _scalefactorOfStdDeviation;
    private int _robotId;
    private int _objectNum;
    private double[] _objectX;
    private double[] _objectY;
    private double[] _robotX;
    private double[] _robotY;
    private double[][][] _objectCovariances;
    private double[][][] _jacobianOfTrajectory;
    private DoubleToken[] _positionConstraints;
    private DoubleMatrixToken[] _gradientOfConstraints;
}
