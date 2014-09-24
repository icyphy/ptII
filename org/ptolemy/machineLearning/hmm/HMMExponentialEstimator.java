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

import java.util.Arrays;

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
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ExpectationMaximization

/**
<p>This actor implements a parameter estimator for Hidden Markov Models with exponential
emissions. The base class ParameterEstimator performs the parameter estimation and
the HMMGaussianEstimator class contains density-specific methods for the exponential PDF
calculations and produces the relevant estimates at its output ports.</p>
<p>
The output ports for an exponential HMM model is the <i>lambda</i>
vector that contains the rate estimates of the exponential distributions in each possible
emission category.
</p>
<p>
The user-defined parameters are initial guesses for the model parameters, given by
<i>lambda0</i>, the rate vector guess, <i>prior</i>, the prior state distribution guess and
<i>A0</i>, the transition matrix guess. <i>iterations</i> is the number of EM iterations
allowed until convergence. If, during iteration, the conditional log-likelihood of the observed
sequence given the parameter estimates converges to a value within <i>likelihoodThreshold</i>,
the parameter estimation stops iterating and delivers the parameter estimates.

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public class HMMExponentialEstimator extends ParameterEstimator {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HMMExponentialEstimator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        lambdaGuess = new Parameter(this, "lambdaGuess");
        lambdaGuess.setExpression("{1.0, 4.0}");
        lambdaGuess.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        _lambda0 = new double[_nStates];
        _lambda = new double[_nStates];

        // output parameters
        lambda = new TypedIOPort(this, "lambda", false, true);
        lambda.setTypeEquals(new ArrayType(BaseType.DOUBLE));
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == lambdaGuess) {
            _lambda0 = new double[_nStates];
            _lambda = new double[_nStates];
            for (int i = 0; i < _nStates; i++) {
                _lambda0[i] = ((DoubleToken) ((ArrayToken) lambdaGuess
                        .getToken()).getElement(i)).doubleValue();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * Rate parameter of the exponential distribution
     */
    public TypedIOPort lambda;

    /**
     * Rate parameter guess for the exponential distribution
     */
    public Parameter lambdaGuess;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HMMExponentialEstimator newObject = (HMMExponentialEstimator) super
                .clone(workspace);
        newObject._lambda = new double[_nStates];
        newObject._lambda0 = new double[_nStates];
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if ((_nStates != _transitionMatrix.length)
                || (_nStates != _priors.length) || (_nStates != _lambda.length)) {
            throw new IllegalActionException(this,
                    "Parameter guess vectors can not have different lengths.");
        }

        _EMParameterEstimation();

        Token[] mTokens = new Token[_nStates];
        Token[] pTokens = new Token[_nStates];
        for (int i = 0; i < _nStates; i++) {
            // lambda estimate is the reciprocal of the mean estimate
            _lambda[i] = Math.pow(m_new[i], -1.0);
            mTokens[i] = new DoubleToken(_lambda[i]);
            pTokens[i] = new DoubleToken(prior_new[i]);
        }
        // broadcast best-effort parameter estimates
        lambda.send(0, new ArrayToken(mTokens));
        transitionMatrix.send(0, new DoubleMatrixToken(A_new));

    }

    @Override
    protected double emissionProbability(double y, int hiddenState) {
        double m = _lambda[hiddenState];
        return m * Math.exp(-m * y);
    }

    @Override
    protected boolean _checkForConvergence(int iterations) {

        // check for null estimates
        if ((m_new[0] != m_new[0]) || (A_new[0] != A_new[0])
                || (prior_new[0] != prior_new[0])) {
            // if no convergence in 10 iterations, issue warning message.
            if ((iterations >= _nIterations - 1)) {
                // return the guess parameters
                for (int i = 0; i < _nStates; i++) {
                    m_new[i] = Math.pow(_lambda0[i], -1.0); // reset to initial lambda guesses
                }
                A_new = _A0; // reset to initial guess
                prior_new = _priors; // reset to input priors
                System.out
                        .println("Expectation Maximization failed to converge");
                return false;
            } else if (_randomize) {
                // randomize means
                double minO = _observations[0];
                double maxO = _observations[0];
                for (int t = 0; t < _observations.length; t++) {
                    if (_observations[t] < minO) {
                        minO = _observations[t];
                    }
                    if (_observations[t] > maxO) {
                        maxO = _observations[t];
                    }
                }
                double L = maxO - minO;
                // make new random guess
                for (int i = 0; i < _nStates; i++) {
                    m_new[i] = L / _nStates * Math.random() + L * i / _nStates
                            + minO;
                    for (int j = 0; j < _nStates; j++) {
                        //A_new[i][j] = 1.0/nStates;
                    }
                }
                A_new = _A0;
                // sort arrays
                Arrays.sort(m_new);
                prior_new = _priors;
            }
        }
        return true;
    }

    @Override
    protected void _initializeEMParameters() {
        // set the initial values of parameters
        _lambda = _lambda0;
        _transitionMatrix = _A0;
        _priorIn = _priors;

        A_new = new double[_nStates][_nStates];
        m_new = new double[_nStates];
        prior_new = new double[_nStates];
    }

    @Override
    protected void _iterateEM() {
        newEstimates = HMMAlphaBetaRecursion(_observations, _transitionMatrix,
                _priorIn, 0);
        m_new = (double[]) newEstimates.get("mu_hat");
        A_new = (double[][]) newEstimates.get("A_hat");
        prior_new = (double[]) newEstimates.get("pi_hat");
        likelihood = (Double) (newEstimates.get("likelihood"));
    }

    @Override
    protected void _updateEstimates() {

        _transitionMatrix = A_new;
        for (int i = 0; i < _nStates; i++) {
            // lambda estimate is the inverse mean estimate
            _lambda[i] = Math.pow(m_new[i], -1.0);
        }
        _priorIn = _priors; // set to the original priors

    }

    private double[] _lambda;
    private double[] _lambda0;

    // EM Specific Parameters
    private double[][] A_new;
    private double[] m_new;
    private double[] prior_new;
}
