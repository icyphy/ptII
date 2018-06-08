/* Mutual-information approximation.

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

import java.util.LinkedList;
import java.util.List;

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
import ptolemy.kernel.util.Workspace;
import ptolemy.math.DoubleArrayMath;
import ptolemy.math.DoubleMatrixMath;

/**
 * Calculate mutual information.
 *
 * <p> This class calculates mutual information between the inference
 * represented by particles and the observation using a zeroth-order
 * Gaussian approximation to the entropy expression that is
 * approximated over a subset of particles. See references for further
 * details on the theory.
 *
 * <b>References</b>
 * <p>[1]
 * B. Charrow, V. Kumar, and N. Michael <i>Approximate Representations for Multi-Robot
 * Control Policies that Maximize Mutual Information</i>, In Proc. Robotics: Science and Systems Conference (RSS), 2013.
 * @see org.ptolemy.machineLearning.particleFilter.ParticleFilter
 * @see com.cureos.numerics
 *
 * @author Shuhei Emoto
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (shuhei)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ParticleMutualInformation extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ParticleMutualInformation(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct an actor in the given workspace.
     *  @param workspace the workspace in which to construct the actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ParticleMutualInformation(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * The computed mutual information between particle sets.
     */
    public TypedIOPort output;

    /**
     * Particles input that accepts an array of record tokens. One
     * field of the record must be labeled as "weight".  Other fields
     * will be resolved to state variables.
     */
    public TypedIOPort particles;

    /**
     * The locations of the pursue robots that are producing the
     * particle estimate.
     */
    public TypedIOPort locations;

    /**
     * Jacobian of the mutual information.
     */
    public TypedIOPort jacobianOfMutualInformation;

    /**
     * Index of the robot which is optimizing location.  Jacobian of
     * mutual information will be dMI/dRob_i, where Rob_i is the
     * location of robotID.  If robotID &lt; 0, Jacobian will be
     * dMI/dRob_all.
     */
    public Parameter robotID;

    //    /**
    //     * Index list of the robots which are optimizing location.
    //     * Jacobian of mutual information will be [0 ... dMI/dRob_i ... 0],
    //     * where i is a part of robotIDList.
    //     */
    //    public Parameter robotIdList;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == robotID) {
            _robotId = ((IntToken) robotID.getToken()).intValue();
        }
    }

    @Override
    public void fire() throws IllegalActionException {

        super.fire();

        if (particles.hasToken(0)) {
            ArrayToken incoming = (ArrayToken) particles.get(0);
            N = incoming.length();
            if (_firstStep) {
                _px = new double[N];
                _py = new double[N];
                _weights = new double[N];
                _firstStep = false;
            }

            for (int i = 0; i < N; i++) {
                RecordToken token = (RecordToken) incoming.getElement(i);

                //To be modified: Definition of _labels is fixed in _init() methods.
                //But it should be modified by StateSpaceModel.
                for (int k = 0; k < _labels.length; k++) {
                    if (_labels[k].equals("weight")) {
                        _weights[i] = ((DoubleToken) token.get(_labels[k]))
                                .doubleValue();
                    } else if (_labels[k].equals("x")) {
                        _px[i] = ((DoubleToken) token.get(_labels[k]))
                                .doubleValue();
                    } else if (_labels[k].equals("y")) {
                        _py[i] = ((DoubleToken) token.get(_labels[k]))
                                .doubleValue();
                    }
                }
            }

            double wsum = 0;
            for (int i = 0; i < N; i++) {
                wsum += _weights[i];
            }
            for (int i = 0; i < N; i++) {
                _weights[i] /= wsum;
            }
        }

        if (locations.hasToken(0)) {
            ArrayToken robotLocations = (ArrayToken) locations.get(0);
            _nRobots = robotLocations.length();
            _robotLocations.clear();
            for (int i = 0; i < _nRobots; i++) {
                RecordToken robotLocation = (RecordToken) robotLocations
                        .getElement(i);
                _robotLocations.add(robotLocation);
            }
            _robotPos = new double[_nRobots * 2];
            for (int j = 0; j < _nRobots; j++) {
                RecordToken robotJ = _robotLocations.get(j);
                _robotPos[j * 2] = ((DoubleToken) robotJ.get("x"))
                        .doubleValue();
                _robotPos[j * 2 + 1] = ((DoubleToken) robotJ.get("y"))
                        .doubleValue();
            }
        } else {
            _nRobots = 0;
            return;
        }
        output.send(0, new DoubleToken(_Hz()));
        jacobianOfMutualInformation.send(0, new DoubleMatrixToken(_jacobian));

        /////////////////////////////////////
        // check result
        /////////////////////////////////////
        //        double mutualInformation = Hz();
        //        double[][] tempJacobian = DoubleMatrixMath.multiply(_jacobian, 1.0);
        //        double[] tempRobotPos  = DoubleArrayMath.allocCopy(_robotPos);
        //        double[] change_val = new double[_nRobots*2];
        //        for (int count=0; count<1; count++) {
        //            for (int i=0; i<_nRobots*2; i++) {
        //                change_val[i] = Math.random() - 0.5;
        //            }
        //            //change current states
        //            for (int i=0; i<_robotPos.length; i++) {
        //                _robotPos[i] = tempRobotPos[i] + change_val[i];
        //            }
        //            //compute MI
        //            double newMI = Hz();
        //            // verify
        //            double[] dMI = DoubleMatrixMath.multiply(change_val, tempJacobian);
        //            double approximatedMI = mutualInformation + dMI[0];
        //
        //            //print out
        //            System.out.println("MI = "+mutualInformation + ", newMI = " + newMI + ", appNewMI = " + approximatedMI);
        //            System.out.println("jacobian = ");
        //            printMatrix(tempJacobian);
        //            System.out.println("dx = ");
        //            printVector(change_val);
        //            double verify = Math.abs(newMI-approximatedMI);
        //            double norm = DoubleArrayMath.dotProduct(change_val, change_val);
        //            verify /= Math.abs(newMI-mutualInformation);
        //            System.out.println("error prop = " + verify);
        //            System.out.println();
        //        }
        //
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        super.prefire();
        if (particles.hasToken(0) && locations.hasToken(0)) {
            return true;
        }
        return false;
    }

    @Override
    public void wrapup() throws IllegalActionException {
        // TODO Auto-generated method stub
        super.wrapup();
        _firstStep = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addMatrix(double[][] dist, double[][] val) {
        for (int row = 0; row < dist.length; row++) {
            for (int col = 0; col < dist[0].length; col++) {
                dist[row][col] += val[row][col];
            }
        }
    }

    /**
     * Compute the mutual information between particle sets and measurements.
     * This function also calculate a jacobian matrix of the mutual information
     * @return - calculated mutual information
     */
    private double _Hz() {
        // zeroth order approximation of the measurement entropy.
        double[][] Sigma = DoubleMatrixMath.identity(_nRobots);
        for (int i = 0; i < Sigma.length; i++) {
            for (int j = 0; j < Sigma[0].length; j++) {
                Sigma[i][j] *= _covariance;
            }
        }
        double[][] gaussianMeans = new double[N][_nRobots];
        int numOfVariable = _nRobots * 2; //number of variables
        // If _robotId >= 0, we assume only the location of _robotId is variable.
        if (_robotId >= 0) {
            numOfVariable = 2;
        }
        double[][][] dMeansDpos = new double[N][_nRobots][numOfVariable];
        for (int i = 0; i < N; i++) {
            //To be modified: This equation must refer to the measurement equation of sensors
            //defined by StateSpaceModel.
            // gmi = { |Rob0 - Pi|, ..., |Robj - Pi|, ... , |RobM - Pi|}
            for (int j = 0; j < _nRobots; j++) {
                gaussianMeans[i][j] = Math
                        .sqrt(Math.pow(_robotPos[j * 2] - _px[i], 2)
                                + Math.pow(_robotPos[j * 2 + 1] - _py[i], 2));
            }
        }
        for (int i = 0; i < N; i++) {
            // dgmi/dRob = [(1/gmi_0)*(x0 - Pxi), (1/gmi_0)*(y0 - Pyi), 0, 0,       ...;
            //              ...
            //              0,  0, ..., (1/gmi_j)*(xj - Pxi), (1/gmi_j)*(yj - Pyi), ...;
            //              ...
            //              0,  0, ...,                            (1/gmi_M)*(yM - Pyi);
            //
            for (int j = 0; j < _nRobots; j++) {
                if (_robotId < 0) {
                    dMeansDpos[i][j][j * 2] = (_robotPos[j * 2] - _px[i])
                            / gaussianMeans[i][j];
                    dMeansDpos[i][j][j * 2
                            + 1] = (_robotPos[j * 2 + 1] - _py[i])
                                    / gaussianMeans[i][j];
                } else if (_robotId == j) {
                    dMeansDpos[i][j][0] = (_robotPos[j * 2] - _px[i])
                            / gaussianMeans[i][j];
                    dMeansDpos[i][j][1] = (_robotPos[j * 2 + 1] - _py[i])
                            / gaussianMeans[i][j];
                }
            }
            //            System.out.println("Mean = ");
            //            printVector(gaussianMeans[i]);
            //            System.out.println("dMean = ");
            //            printMatrix(dMeansDx[i]);
        }
        // Prepare Inverse and Determinant of Sigma before Integration
        // so that Hz will be calculated faster.
        double[][] invSigma = DoubleMatrixMath.inverse(Sigma);
        double detSigma = DoubleMatrixMath.determinant(Sigma);
        detSigma = Math.sqrt(1.0
                / (Math.pow(Math.PI * 2, gaussianMeans[0].length) * detSigma));

        double[] logSums = new double[N];
        // logSum_i = w0*mvnpdf(gmi, gm0) + ... + wj*mvnpdf(gmi, gmj) + ... + wN*mvnpdf(gmi, gmN)
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                double mvnpdf_ij = _mvnpdf(gaussianMeans[i], gaussianMeans[j],
                        Sigma, invSigma, detSigma);
                //integrate mutual information
                logSums[i] += _weights[j] * mvnpdf_ij;
            }
        }
        // d_logSum/d_gm
        // = [SUM(wj*d_mvnpdf(gm0, gmj)/dgm0), w1*d_mvnpdf(gm0, gm1)/dgm1, ..., wN*d_mvnpdf(gm0, gmN)/dgmN;
        //    w0*d_mvnpdf(gm1, gm0)/d_gm0, SUM(wj*d_mvnpdf(gm1, gmj)/dgm1), ..., ...                      ;
        //     ....
        //    W0*d_mvnpdf(gmN, gm0)/d_gm0, ...                                SUM(wj*d_mvnpdf(gmN, gmj)/dgmN)]
        double[][][][] dlogSum_dgm = new double[N][N][1][gaussianMeans[0].length];
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                if (row == col) {
                    continue;
                }
                double[][] dmvnpdf_dgm = _calcJacobianOfmvnpdf(
                        gaussianMeans[row], gaussianMeans[col], Sigma, invSigma,
                        detSigma);
                double[][] weightedMatrix = DoubleMatrixMath
                        .multiply(dmvnpdf_dgm, _weights[col]);
                // d_mvnpdf(gmi, gmj)/d_gmj = - d_mvnpdf(gmi, gmj)/d_gmi
                for (int r = 0; r < dmvnpdf_dgm.length; r++) {
                    for (int c = 0; c < dmvnpdf_dgm[0].length; c++) {
                        dlogSum_dgm[row][col][r][c] = -weightedMatrix[r][c];
                    }
                }
                // calculation of diagonal ( SUM(wj*d_mvnpdf(gm_row, gmj)/dgm_row) )
                _addMatrix(dlogSum_dgm[row][row], weightedMatrix);
            }
        }

        double Hz = 0;
        // Hz = - w0*log(logSum0) - w1*log(logSum1) - ... - wN*log(logSumN)
        for (int k = 0; k < N; k++) {
            Hz -= _weights[k] * Math.log(logSums[k]);
        }

        double[][] dHz_dL = new double[1][N];
        // d_Hz/d_logsum = [-w0/logSum0, -w1/logSum1, ..., -wN/logSumN]
        for (int col = 0; col < N; col++) {
            dHz_dL[0][col] = -_weights[col] / logSums[col];
        }

        // compute jacobian of mutual information
        // J = dHz/dRob = d_Hz/d_logSum * d_logSum/d_gm * d_gm/d_rob
        double[][] d_logSum_d_rob = new double[N][numOfVariable];
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                //                System.out.println("dlogSum_dgm" + row + "_" + col + " = ");
                //                printMatrix(dlogSum_dgm[row][col]);
                double[][] tempMatrix = DoubleMatrixMath
                        .multiply(dlogSum_dgm[row][col], dMeansDpos[col]);
                //                System.out.println("tempMatrix = ");
                //                printMatrix(tempMatrix);
                for (int i = 0; i < numOfVariable; i++) {
                    d_logSum_d_rob[row][i] += tempMatrix[0][i]; //tempMatrix is 1 x numOfVariable.
                }
            }
        }
        _jacobian = DoubleMatrixMath.multiply(dHz_dL, d_logSum_d_rob);
        return Hz;
    }

    private void _init()
            throws IllegalActionException, NameDuplicationException {
        particles = new TypedIOPort(this, "particles", true, false);
        //To be modified: Parameter names are hard-coded. It should be obtained using StateSpaceModel.
        ArrayToken names = new ArrayToken("{\"x\",\"y\"}"); //{"x","y"}
        String stateName;
        _labels = new String[names.length() + 1];
        _types = new Type[names.length() + 1];
        for (int i = 0; i < names.length(); i++) {
            stateName = ((StringToken) names.getElement(i)).stringValue();
            _labels[i] = stateName;
            _types[i] = BaseType.DOUBLE;
        }
        _labels[names.length()] = "weight";
        _types[names.length()] = BaseType.DOUBLE;
        particles.setTypeEquals(new ArrayType(new RecordType(_labels, _types)));
        _px = new double[0];
        _py = new double[0];
        _weights = new double[0];

        // an array of robot locations
        locations = new TypedIOPort(this, "locations", true, false);

        _robotLocations = new LinkedList<RecordToken>();

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        jacobianOfMutualInformation = new TypedIOPort(this, "jacobianOfMI",
                false, true);
        jacobianOfMutualInformation.setTypeEquals(BaseType.DOUBLE_MATRIX);

        _firstStep = true;

        _covariance = 2.0; //This parameter should be defined by StateSpaceModel.

        _robotId = -1;
        robotID = new Parameter(this, "robot id");
        robotID.setExpression("-1");
        robotID.setTypeEquals(BaseType.INT);
        robotID.setVisibility(Settable.EXPERT);
    }

    /**
     * Compute the multivariate PDF value at x
     * @param x Value at which the PDF will be calculated
     * @param mu Mean vector
     * @param Sigma Covariance matrix
     * @param invSigma Inverse Covariance Matrix
     * @param detSigma Determinant of covariance matrix
     * @return the value of the PDF computed at x
     */
    private double _mvnpdf(double[] x, double[] mu, double[][] Sigma,
            double[][] invSigma, double detSigma) {
        double multiplier = Math
                .sqrt(1.0 / (Math.pow(Math.PI * 2, x.length) * detSigma));
        double[] x_mu = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            x_mu[i] = x[i] - mu[i];
        }
        double exponent = DoubleArrayMath
                .dotProduct(DoubleMatrixMath.multiply(x_mu, invSigma), x_mu);

        return multiplier * Math.exp(-0.5 * exponent);

    }

    /**
     * Compute jacobian matrix of the multivariate PDF (d_mvnPDF/d_x)
     * Note that jacobian d_mvnPDF/d_mu = - d_mvnPDF/d_x.
     * @param x Value at which the PDF will be calculated
     * @param mu Mean vector
     * @param Sigma Covariance matrix
     * @param invSigma Inverse Covariance Matrix
     * @param detSigma Determinant of covariance matrix
     * @return jacobian matrix of the PDF computed at x
     */
    private double[][] _calcJacobianOfmvnpdf(double[] x, double[] mu,
            double[][] Sigma, double[][] invSigma, double detSigma) {
        double multiplier = Math
                .sqrt(1.0 / (Math.pow(Math.PI * 2, x.length) * detSigma));
        double[] x_mu = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            x_mu[i] = x[i] - mu[i];
        }
        double exponent = DoubleArrayMath
                .dotProduct(DoubleMatrixMath.multiply(x_mu, invSigma), x_mu);
        double mvnpdfVal = multiplier * Math.exp(-0.5 * exponent);

        // d[multiplier * exp(-0.5 * exponent)]/dx
        // = multiplier * exp(-0.5 * exponent) * d[-0.5*exponent]/dx
        // = multiplier * exp(-0.5 * exponent) * -0.5 * [2*x_mu*invSigma] * d[x_mu]/dx
        // = mvnpdfVal * -1.0 *[x_mu*invSigma]*I   /// (d[x_mu]/dx is identity matrix)
        double[][] jacobianOfmvnpdf = new double[1][x.length];
        double[] dExponentDx = DoubleMatrixMath.multiply(invSigma, x_mu);
        for (int i = 0; i < dExponentDx.length; i++) {
            jacobianOfmvnpdf[0][i] = mvnpdfVal * -1.0 * dExponentDx[i];
        }
        return jacobianOfmvnpdf;
    }

    private double[] _weights;
    private double[] _px;
    private double[] _py;
    private double[] _robotPos;
    private double[][] _jacobian;
    private List<RecordToken> _robotLocations;
    private double _covariance;
    private boolean _firstStep;
    private int _nRobots;
    private int _robotId;
    private String[] _labels;
    private Type[] _types;
    private int N;

}
