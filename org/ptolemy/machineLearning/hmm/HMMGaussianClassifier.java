/* A sequence classifier for Gaussian emission HMMs

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
package org.ptolemy.machineLearning.hmm;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.DoubleMatrixMath;

/**
<p>This actor performs Maximum-Likelihood classification of the partially-observed
Bayesian Network models. ClassifyObservations is designed to work with <i>
ExpectationMaximization</i>, which provides the Maximum-Likelihood model parameters
from which the observations are assumed to be drawn. The output is an integer array
of labels, representing the maximum-likelihood hidden state sequence of the given
model.

<p>
The user provides a set of parameter estimates as inputs to the model, and
The <i>mean</i>  is a double array input containing the mean estimate and
<i>sigma</i> is a double array input containing standard deviation estimate of
each mixture component. If the <i>modelType</i> is HMM, then an additional input,
<i>transitionMatrix</i> is provided, which is an estimate of the transition matrix
governing the Markovian process representing the hidden state evolution. The <i>prior
</i> input is an estimate of the prior state distribution.

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public class HMMGaussianClassifier extends ObservationClassifier {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HMMGaussianClassifier(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        mean = new PortParameter(this, "mean");
        mean.setExpression("{0.0,3.0}");
        StringAttribute cardinality = new StringAttribute(mean.getPort(),
                "_cardinal");
        cardinality.setExpression("SOUTH");

        standardDeviation = new PortParameter(this, "standardDeviation");
        standardDeviation.setExpression("{10E-3,50E-3}"); 
        cardinality = new StringAttribute(standardDeviation.getPort(),
                "_cardinal");
        cardinality.setExpression("SOUTH");

        //_nStates = ((ArrayToken) meanToken).length();
        _nStates = ((ArrayToken) mean.getToken()).length(); 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * Mean parameter array for the Gaussian distribution.
     */
    public PortParameter mean;
    /**
     * Standard deviation parameter array for the Gaussian distribution.
     */
    public PortParameter standardDeviation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HMMGaussianClassifier newObject = (HMMGaussianClassifier) super
                .clone(workspace);
        newObject._mu = null;
        newObject._sigma = null;
        return newObject;
    }

    /** Consume the inputs and produce the outputs of the FFT filter.
     *  @exception IllegalActionException If a runtime type error occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        mean.update();
        standardDeviation.update();
        transitionMatrix.update();
        prior.update();

        // update array values and lengths

        _nStates = ((ArrayToken) mean.getToken()).length();
        _priors = new double[_nStates];
        _transitionMatrixEstimate = new double[_nStates][_nStates];
        for (int i = 0; i < _nStates; i++) { 
            _priors[i] = ((DoubleToken) ((ArrayToken) prior.getToken())
                    .getElement(i)).doubleValue();
            for (int j = 0; j < _nStates; j++) {
                _transitionMatrixEstimate[i][j] = ((DoubleToken) ((MatrixToken) transitionMatrix
                        .getToken()).getElementAsToken(i, j)).doubleValue();
            }
        }

        int obsDim = 1;
        if (((ArrayToken) mean.getToken()).getElementType().isCompatible(BaseType.DOUBLE)) {
            _mu = new double[_nStates][obsDim];
            _sigma = new double[_nStates][obsDim][obsDim];
            for (int i = 0; i < _nStates; i++) {
                _sigma[i][0][0] = ((DoubleToken) ((ArrayToken) standardDeviation
                        .getToken()).getElement(i)).doubleValue(); 
                _mu[i][0] = ((DoubleToken) ((ArrayToken) mean.getToken())
                        .getElement(i)).doubleValue();
            }
        } else {
            obsDim = ((ArrayToken)((ArrayToken) mean.getToken()).getElement(0)).length();
            _mu = new double[_nStates][obsDim];
            _sigma = new double[_nStates][obsDim][obsDim];
            for (int i = 0; i < _nStates; i++) {
                _sigma[i] = ((DoubleMatrixToken) ((ArrayToken) standardDeviation
                        .getToken()).getElement(i)).doubleMatrix();  
                for (int j = 0; j < obsDim; j++) { 
                    _mu[i][j] = ((DoubleToken)((ArrayToken) ((ArrayToken) mean.getToken())
                            .getElement(i)).getElement(j)).doubleValue();
                }
            } 
        }
        if ((_nStates != _sigma.length)
                || (_nStates != _transitionMatrixEstimate.length)) {
            throw new IllegalActionException(this,
                    "Parameter guess vectors need to have the same length.");
        }
        if (_observations != null) {
            int[] classifyStates = new int[_observations.length];

            classifyStates = classifyHMM(_observations, _priors,
                    _transitionMatrixEstimate);

            IntToken[] _outTokenArray = new IntToken[classifyStates.length];
            for (int i = 0; i < classifyStates.length; i++) {
                _outTokenArray[i] = new IntToken(classifyStates[i]);
            }

            output.broadcast(new ArrayToken(BaseType.INT, _outTokenArray));
            likelihood.send(0, new DoubleToken(_likelihood));
        } 
    }

    @Override
    protected double emissionProbability(double[] y, int hiddenState) throws IllegalActionException 
    {

        double[][] s = _sigma[hiddenState];
        double[] m = _mu[hiddenState];

        double[] xt = new double[y.length];
        Token[] xmat = new Token[y.length]; 
        for (int i = 0; i < y.length; i ++) {
            xt[i] = y[i] - m[i];
            xmat[i] = new DoubleToken(xt[i]); 
        }
        MatrixToken X = MatrixToken.arrayToMatrix(xmat, y.length, 1);
        DoubleMatrixToken Covariance;
        try {
            Covariance = new DoubleMatrixToken( DoubleMatrixMath.inverse(s));
        } catch (IllegalArgumentException e) {
            return 0.0;
        }
        MatrixToken Xtranspose = MatrixToken.arrayToMatrix(xmat, 1, y.length); 
        Token exponent = Xtranspose.multiply(Covariance);
        exponent = exponent.multiply(X); 

        double value = ((DoubleMatrixToken) exponent)
                .getElementAt(0, 0);
        double result = ( 1.0 / (Math.sqrt(2 * Math.PI) * DoubleMatrixMath.determinant(s))
                * Math.exp(-0.5 * value)); 

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The mean array. */
    private double[][] _mu;

    /** Standard deviations array. */
    private double[][][] _sigma;
}
