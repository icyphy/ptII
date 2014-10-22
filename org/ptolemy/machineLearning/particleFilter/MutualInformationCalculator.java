/* Mutual-information approximation.

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


import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
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
Black-box optimizer that uses JCobyla as the solver

<p> Performs mutual information-based optimization using a zeroth-order Gaussian approximation
to the entropy expression that is approximated over a subset of particles. See references for
further details on the theory.

<b>References</b>
 <p>[1]
B. Charrow, V. Kumar, and N. Michael <i>Approximate Representations for Multi-Robot
  Control Policies that Maximize Mutual Information</i>, In Proc. Robotics: Science and Systems Conference (RSS), 2013.
@see org.ptolemy.machineLearning.particleFilter.ParticleFilter
@see com.cureos.numerics

@author Ilge Akkaya, Shuhei Emoto
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
 */
public class MutualInformationCalculator extends TypedAtomicActor {

    /**
     * Constructs a MutualInformationCalculator object.
     *
     * @param container  a CompositeEntity object
     * @param name       entity name
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public MutualInformationCalculator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /**
     * Constructs a MutualInformationCalculator object.
     *
     * @param workspace The workspace
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public MutualInformationCalculator(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == timeHorizon) {
            int th = ((IntToken) timeHorizon.getToken()).intValue();
            if (th > 0) {
            } else {
                throw new IllegalActionException(this,
                        "Time horizon must be a positive integer");
            }
        } else if (attribute == optIndex) {
            _optIndex = ((IntToken) optIndex.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    public void fire() throws IllegalActionException {

        super.fire();

        if (particles.hasToken(0)) {
            ArrayToken incoming = (ArrayToken) particles.get(0);
            N = incoming.length();
            _weights = new double[N];
            if (_px.length == 0) {
                _px = new double[N];
                _py = new double[N];
                _weights = new double[N];
            }

            for (int i = 0; i < N; i++) {
                RecordToken token = (RecordToken) incoming.getElement(i);

                for (int k = 0; k < _labels.length; k++) {
                    if (_labels[k].equals("weight")) {
                        _weights[i] = ((DoubleToken) token.get(_labels[k])).doubleValue();
                    }
                }
                _px[i] = ((DoubleToken) token.get(_labels[0])).doubleValue(); //particleValue.get(0);
                _py[i] = ((DoubleToken) token.get(_labels[1])).doubleValue(); //particleValue.get(1);
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
        }
        if (xValue.hasToken(0)) {
            ArrayToken xArray = ((ArrayToken) xValue.get(0));
            _xValue = new double[xArray.length()];
            for (int i = 0; i < xArray.length(); i++) {
                _xValue[i] = ((DoubleToken) xArray.getElement(i)).doubleValue();
            }
        }

        output.send(0, new DoubleToken(Hz(_xValue)));

    }

    /**
     * The computed mutual information between particle sets.
     */
    public TypedIOPort output;
    /**
     * Particles input that accepts an array of record tokens. One field of the record must be labeled as "weight".
     * Other fields will be resolved to state variables.
     */
    public TypedIOPort particles;
    /**
     * The locations of the pursue robots that are producing the particle estimate.
     */
    public TypedIOPort locations;

    /**
     * Measurement noise covariance.
     */
    public Parameter covariance;

    /**
     * Time horizon over which the mutual information will be calculated.
     */
    public Parameter timeHorizon;

    /**
     * The index of the robot, over which the mutual information is being optimized.
     */
    public Parameter optIndex;

    /**
     * The control input that determines the update in robot position.
     */
    public TypedIOPort xValue;

    // code for computing the mutual information between particle sets and measurements


    private double Hz(double[] x) {
        // zeroth order approximation of the measurement entropy.
        double[][] Sigma = DoubleMatrixMath.identity(_nRobots);
        for (int i = 0; i < Sigma.length; i++) {
            for (int j = 0; j < Sigma[0].length; j++) {
                Sigma[i][j] *= _covariance;
            }
        }

        double[] robotX = new double[_nRobots];
        double[] robotY = new double[_nRobots];
        for (int j = 0; j < _nRobots; j++) {
            RecordToken robotJ = _robotLocations.get(j);
            if (_optIndex < 0) {
                robotX[j] = ((DoubleToken) robotJ.get("x")).doubleValue()
                        + x[2 * j];
                robotY[j] = ((DoubleToken) robotJ.get("y")).doubleValue()
                        + x[2 * j + 1];
            } else if (j == _optIndex) {
                robotX[j] = ((DoubleToken) robotJ.get("x")).doubleValue()
                        + x[0];
                robotY[j] = ((DoubleToken) robotJ.get("y")).doubleValue()
                        + x[1];
            } else {
                robotX[j] = ((DoubleToken) robotJ.get("x")).doubleValue();
                robotY[j] = ((DoubleToken) robotJ.get("y")).doubleValue();
            }
        }
        double[][] gaussianMeans = new double[N][_nRobots];

        for (int i = 0; i < N; i++) {

            if (_optIndex < 0) {
                for (int j = 0; j < _nRobots; j++) {
                    gaussianMeans[i][j] = Math.sqrt(Math.pow(
                            _px[i] - robotX[j], 2)
                            + Math.pow(_py[i] - robotY[j], 2));
                }
            } else {
                // optimize over single robot
                for (int j = 0; j < _nRobots; j++) {
                    gaussianMeans[i][j] = Math.sqrt(Math.pow(
                            _px[i] - robotX[j], 2)
                            + Math.pow(_py[i] - robotY[j], 2));
                }
            }
        }
        double Hz = 0;
        // Prepare Inverse and Determinant of Sigma before Integration
        // so that Hz will be calculated faster.
        double[][] invSigma = DoubleMatrixMath.inverse(Sigma);
        double detSigma = DoubleMatrixMath.determinant(Sigma);
        detSigma = Math.sqrt(1.0 / (Math.pow(Math.PI * 2,
                gaussianMeans[0].length) * detSigma));
        double[] logSums = new double[N];
        for (int k = 0; k < N; k++) {
            logSums[k] = 0;
        }
        for (int k = 0; k < N; k++) {
            //            logSum = 0;
            for (int j = k; j < N; j++) {
                double log_kj = _mvnpdf(gaussianMeans[k], gaussianMeans[j],
                        Sigma, invSigma, detSigma);
                logSums[k] += _weights[j] * log_kj;
                if (j != k) {
                    logSums[j] += _weights[k] * log_kj;
                }
            }
            Hz += _weights[k] * Math.log(logSums[k]);
        }

        return -Hz;
    }

    private void _init() throws IllegalActionException, NameDuplicationException {
        optIndex = new Parameter(this, "optIndex");
        optIndex.setExpression("-1");
        optIndex.setTypeEquals(BaseType.INT);
        _optIndex = -1;

        timeHorizon = new Parameter(this, "timeHorizon");
        timeHorizon.setExpression("1");
        timeHorizon.setTypeEquals(BaseType.INT);
        xValue = new TypedIOPort(this, "xValue", true, false);
        xValue.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        particles = new TypedIOPort(this, "particles", true, false);
        ArrayToken names = new ArrayToken("{\"x\",\"y\"}"); //
        String stateName;
        _labels = new String[names.length() + 1];
        _types = new Type[names.length() + 1];
        for (int i = 0; i < names.length(); i++) {
            stateName = ((StringToken) names.getElement(i)).stringValue();
            if (this.getAttribute(stateName) == null
                    && stateName.length() != 0) {
                Parameter y = new Parameter(this, stateName);
                y.setExpression("0.0");
                y.setVisibility(Settable.EXPERT);
            }
            _labels[i] = stateName;
            _types[i] = BaseType.DOUBLE;
        }
        _labels[names.length()] = "weight";
        _types[names.length()] = BaseType.DOUBLE;
        particles.setTypeEquals(new ArrayType(new RecordType(_labels,
                _types)));
        _px = new double[0];
        _py = new double[0];
        _weights = new double[0];

        // an array of robot locations
        locations = new TypedIOPort(this, "locations", true, false);

        _robotLocations = new LinkedList<RecordToken>();

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        _covariance = 2.0;
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
        // int k = x.length;
        // double multiplier = Math.sqrt(1.0/(Math.pow(Math.PI*2, k)*detSigma));
        double multiplier = detSigma;
        double[] x_mu = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            x_mu[i] = x[i] - mu[i];
        }
        double exponent = DoubleArrayMath.dotProduct(
                DoubleMatrixMath.multiply(x_mu, invSigma), x_mu);

        return multiplier * Math.exp(-0.5 * exponent);

    }

    private double[] _weights;
    private double[] _px;
    private double[] _py;
    private List<RecordToken> _robotLocations;
    private double _covariance;
    private int _optIndex;
    private int _nRobots;
    private double[] _xValue;
    private String[] _labels;
    private Type[] _types;
    private int N;

}
