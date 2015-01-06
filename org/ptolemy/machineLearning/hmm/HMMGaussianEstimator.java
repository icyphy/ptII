/* Parameter Estimation for Graphical Models.

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

import org.ptolemy.machineLearning.Algorithms;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ExpectationMaximization

/**
<p>This actor implements a parameter estimator for Hidden Markov Models with Gaussian
Emissions. The base class ParameterEstimator performs the parameter estimation and
the HMMGaussianEstimator class contains density-specific methods for Gaussian emission
calculations and produces the relevant estimates at its output ports.</p>
<p>
The output ports for a Gaussian HMM model are the <i>mean</i> and the <i>standardDeviation</i>
vectors of the possible hidden states in addition to the HMM parameters independent
from the emission density: <i>transitionMatrix</i> .
T
he <i>mean</i>  is a double array output containing the mean estimates and
<i>sigma</i> is a double array output containing standard deviation estimates of
each mixture component. If the <i>modelType</i> is HMM, then an additional output,
<i>transitionMatrix</i> is provided, which is an estimate of the transition matrix
governing the Markovian process representing the hidden state evolution.
If the <i>modelType</i> is MM, this port outputs a double array with the prior
probability estimates of the mixture components.
</p>
<p>
The user-defined parameters are initial guesses for the model parameters, given by
<i>m0</i>, the mean vector guess, <i>s0</i>, the standard deviation vector guess,
<i>prior</i>, the prior state distribution guess, <i>A0</i>, the transition
matrix guess ( only for HMM). <i>iterations</i> is the number of EM iterations
allowed until convergence.
If, during iteration, the conditional log-likelihood of the observed
sequence given the parameter estimates converges to a value within <i>likelihoodThreshold</i>,
the parameter estimation stops iterating and delivers the parameter estimates.

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public class HMMGaussianEstimator extends ParameterEstimator {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HMMGaussianEstimator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        mean = new TypedIOPort(this, "mean", false, true);

        standardDeviation = new TypedIOPort(this, "standardDeviation", false,
                true);

        meanVectorGuess = new Parameter(this, "meanVectorGuess");
        meanVectorGuess.setExpression("{0.0, 4.0}");

        standardDeviationGuess = new Parameter(this, "standardDeviationGuess");
        standardDeviationGuess.setExpression("{1.0, 1.0}");

    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == meanVectorGuess) {
            int nS = ((ArrayToken) meanVectorGuess.getToken()).length();
            if (((ArrayToken) meanVectorGuess.getToken()).getElementType().equals(BaseType.DOUBLE)) {
                _obsDimension = 1;
                _mu0 = new double[nS][1];
                for (int i = 0; i < nS; i++) {
                    _mu0[i][0] = ((DoubleToken) ((ArrayToken) meanVectorGuess
                            .getToken()).getElement(i)).doubleValue(); 
                } 
            } else {
                _obsDimension = ((ArrayToken)((ArrayToken) meanVectorGuess.getToken()).getElement(0)).length();
                _mu0 = new double[nS][_obsDimension]; 
                for (int i = 0; i < nS; i++) {
                    for (int j = 0; j < _obsDimension; j++) {
                        _mu0[i][j] = ((DoubleToken)((ArrayToken) ((ArrayToken) meanVectorGuess
                                .getToken()).getElement(i)).getElement(j)).doubleValue();
                    }
                }
            }
            if (_obsDimension > 1) {
                mean.setTypeEquals(new ArrayType(new ArrayType(BaseType.DOUBLE)));
                standardDeviation.setTypeEquals(new ArrayType(BaseType.DOUBLE_MATRIX));
            } else {
                mean.setTypeEquals(new ArrayType(BaseType.DOUBLE));
                standardDeviation.setTypeEquals(new ArrayType(BaseType.DOUBLE));
            }
        } else if (attribute == standardDeviationGuess) {
            int nS = ((ArrayToken) standardDeviationGuess.getToken()).length();
            if (((ArrayToken) standardDeviationGuess.getToken()).getElementType().equals(BaseType.DOUBLE)) {
                _obsDimension = 1;
                _sigma0 = new double[nS][1][1];
                for (int i = 0; i < nS; i++) {
                    _sigma0[i][0][0] = ((DoubleToken) ((ArrayToken) standardDeviationGuess
                            .getToken()).getElement(i)).doubleValue(); 
                } 
            } else {
                _obsDimension = ((DoubleMatrixToken)((ArrayToken) standardDeviationGuess.getToken()).getElement(0)).getColumnCount();
                _sigma0 = new double[nS][_obsDimension][_obsDimension]; 
                for (int i = 0; i < nS; i++) { 
                    _sigma0[i] = ((DoubleMatrixToken) ((ArrayToken) standardDeviationGuess
                            .getToken()).getElement(i)).doubleMatrix(); 
                }
            } 
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * Mean parameter array for the Gaussian distribution.
     */
    public TypedIOPort mean;

    /**
     * Standard deviation parameter array for the Gaussian distribution.
     */
    public TypedIOPort standardDeviation;

    /**
     * The initial guess mean parameter array for the Gaussian distribution.
     */
    public Parameter meanVectorGuess;

    /**
     * The initial guess standard deviation parameter array for the Gaussian distribution.
     */
    public Parameter standardDeviationGuess;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HMMGaussianEstimator newObject = (HMMGaussianEstimator) super
                .clone(workspace);
        newObject._sigma0 = null;
        newObject._mu0 = null;
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if ((_nStates != _sigma0.length)
                || (_nStates != _transitionMatrix.length)
                || (_nStates != _priors.length) || (_nStates != _mu0.length)) {
            throw new IllegalActionException(this,
                    "Parameter guess vectors must have equal lengths.");
        }

        _EMParameterEstimation();
        System.out.println(likelihood);
        Token[] mTokens = new Token[_nStates];
        Token[] sTokens = new Token[_nStates];
        Token[] pTokens = new Token[_nStates];
         

        for (int i = 0; i < _nStates; i++) {
            if (_obsDimension > 1) {
                Token[] meani = new Token[_obsDimension];
                for (int j = 0; j < _obsDimension; j++) {
                    meani[j] = new DoubleToken(m_new[i][j]);
                }
                mTokens[i] = new ArrayToken(meani);
                sTokens[i] = new DoubleMatrixToken(s_new[i]);
            } else {
                mTokens[i] = new DoubleToken(m_new[i][0]);
                sTokens[i] = new DoubleToken(Math.sqrt(s_new[i][0][0]));
            }
            
            pTokens[i] = new DoubleToken(prior_new[i]);
        }
        mean.send(0, new ArrayToken(mTokens));
        standardDeviation.send(0, new ArrayToken(sTokens));
        transitionMatrix.send(0, new DoubleMatrixToken(A_new));
        priorEstimates.send(0, new ArrayToken(pTokens));
        // broadcast best-effort parameter estimates

    }

    @Override
    protected double emissionProbability(double[] y, int hiddenState) 
            throws IllegalActionException {

        double[][] s = _sigma[hiddenState];
        double[] m = _mu[hiddenState];
        return Algorithms.mvnpdf(y, m, s);
    }

    @Override
    protected boolean _checkForConvergence(int iterations) { 
        if (Double.isNaN(m_new[0][0]) || Double.isNaN(s_new[0][0][0])
                || Double.isNaN(A_new[0][0]) || Double.isNaN(prior_new[0])) {
            // if no convergence in 10 iterations, issue warning message.
            if ((iterations >= _nIterations - 1)) {
                // return the guess parameters
                m_new = _mu0;
                s_new = _sigma0;
                A_new = _A0;
                prior_new = _priors;
                System.out.println("Failed");
                throw new InternalErrorException("EM did not converge in " + iterations + " iterations.");
               
            } else if (_randomize) {
                
                for (int j = 0 ; j < _obsDimension; j++) {
                    // randomize means
                    double minO = _observations[0][j];
                    double maxO = _observations[0][j];
                    for (int t = 0; t < _observations.length; t++) {
                        if (_observations[t][j] < minO) {
                            minO = _observations[t][j];
                        }
                        if (_observations[t][j] > maxO) {
                            maxO = _observations[t][j];
                        }
                    }
                    double L = maxO - minO;
                    // make new random guess
                    for (int i = 0; i < _nStates; i++) {
                        m_new[i][j] = L / _nStates * Math.random() + L * i / _nStates
                                + minO;
                        s_new[i][j][j] = Math.abs((maxO - minO) * Math.random())
                                / _nStates;
                    }
                }
                A_new = _A0;
                // sort arrays
                ///Arrays.sort(m_new);
                prior_new = _priors;
            }
        }
        return true;
    }

    @Override
    protected void _initializeEMParameters() {

        // set the initial values of parameters
        _sigma = _sigma0;
        _mu = _mu0;
        _transitionMatrix = _A0;
        _priorIn = _priors;

        A_new = new double[_nStates][_nStates];
        m_new = new double[_nStates][_obsDimension];
        s_new = new double[_nStates][_obsDimension][_obsDimension];
        prior_new = new double[_nStates];
    }

    @Override
    protected void _iterateEM() throws IllegalActionException {

        newEstimates = HMMAlphaBetaRecursion(_observations, _transitionMatrix,
                _priorIn, 0);
        m_new = (double[][]) newEstimates.get("mu_hat");
        s_new = (double[][][]) newEstimates.get("s_hat");
        A_new = (double[][]) newEstimates.get("A_hat");
        prior_new = (double[]) newEstimates.get("pi_hat");
        likelihood = (Double) (newEstimates.get("likelihood"));
    }

    @Override
    protected void _updateEstimates() {
        _transitionMatrix = A_new;
        _sigma = s_new;
        _mu = m_new;
        _priorIn = _priors; // set to the original priors
    }

    /** Mean estimates. */
    private double[][] _mu = null;
    /** Mean guess. */
    private double[][] _mu0 = null;
    /**  Standard deviation estimates. */
    private double[][][] _sigma = null;
    /**  Standard deviation guess. */
    private double[][][] _sigma0 = null;

    /**  Updated Transition probability matrix estimate. */
    private double[][] A_new = null;
    /**  Updated mean estimate. */
    private double[][] m_new = null;
    /**  Updated standard deviation estimate. */
    private double[][][] s_new = null;
    /** Updated prior belief. */
    private double[] prior_new = null;

}
