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

import java.util.HashMap;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.SignalProcessing;

///////////////////////////////////////////////////////////////////
////ExpectationMaximization

/**
<p>This actor implements the Expectation-Maximization(EM) algorithm for
parameter estimation in graphical stochastic models. Two types of fundamental
types of Bayesian Network models: <i>Mixture Model(MM)</i> and <i>Hidden Markov
Model(HMM)</i> are supported. The input is an array of observations of arbitrary
length and the outputs are the parameter estimates for the chosen model.</p>
<p>
The output ports reflect the parameter estimates of Gaussian MM or HMM. The Mixture
Model is parameterized by <i>M</i> states, each distributed according to a distribution
specified by the <i>emissionDistribution</i> parameter. Currently, the actor supports
Gaussian emissions.
The <i>mean</i>  is a double array output containing the mean estimates and
<i>sigma</i> is a double array output containing standard deviation estimates of
each mixture component. If the <i>modelType</i> is HMM, then an additional output,
<i>transitionMatrix</i> is provided, which is an estimate of the transition matrix
governing the Markovian process representing the hidden state evolution.
If the <i>modelType</i> is MM, this port outputs a double array with the prior
probability estimates of the mixture components.
</p>
<p>
<i>iterations</i> is the maximum number of EM iterations until the log-likelihood
P(observations | model parameters) remain within <i>likelihoodThreshold</i> neighborhood
of the previous likelihood estimate. The default likelihood threshold is set to 1E-4 and
for precise applications, may be set to a lower positive value.


The actor iterates over the parameter estimates using the EM algorithm. If, at any
point, the estimates become NaN, the user is notified that the algorithm did not
converge and is given the option to randomize initial guesses to reiterate.


 <p>
 <b>References</b>
 <p>[1]
 Jordan, Michael I., et al. <i>An introduction to variational methods for graphical
 models</i>, Springer Netherlands, 1998.
 <p>[2]
 Bilmes, Jeff A. <i>A gentle tutorial of the EM algorithm and its application
 to parameter estimation for Gaussian mixture and hidden Markov models.</i>
 International Computer Science Institute 4.510 (1998): 126.

@see org.ptolemy.machineLearning.hmm.ObservationClassifier
@see org.ptolemy.machineLearning.hmm.HMMGaussianEstimator


 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public abstract class ParameterEstimator extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ParameterEstimator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        transitionMatrix = new TypedIOPort(this, "transitionMatrix", false,
                true);
        transitionMatrix.setTypeEquals(BaseType.DOUBLE_MATRIX);

        priorEstimates = new TypedIOPort(this, "priorEstimates", false, true);
        priorEstimates.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        StringAttribute cardinality = new StringAttribute(priorEstimates,
                "_cardinal");
        cardinality.setExpression("SOUTH");

        randomizeGuessVectors = new Parameter(this, "randomizeGuessVectors");
        randomizeGuessVectors.setTypeEquals(BaseType.BOOLEAN);
        randomizeGuessVectors.setExpression("false");

        likelihoodThreshold = new Parameter(this, "likelihoodThreshold");
        likelihoodThreshold.setExpression("1E-4");
        likelihoodThreshold.setTypeEquals(BaseType.DOUBLE);

        maxIterations = new Parameter(this, "maxIterations");
        maxIterations.setExpression("10");
        maxIterations.setTypeEquals(BaseType.INT);

        A0 = new Parameter(this, "A0");
        A0.setExpression("[0.5, 0.5; 0.5, 0.5]");
        A0.setTypeEquals(BaseType.DOUBLE_MATRIX);
        A0.setDisplayName("Transition Probability Matrix");

        priorDistribution = new Parameter(this, "priorDistribution");
        priorDistribution.setExpression("{0.5,0.5}");
        priorDistribution.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        nStates = new Parameter(this, "nStates");
        nStates.setExpression("2");
        nStates.setTypeEquals(BaseType.INT);
        nStates.setDisplayName("numberOfStates");

        _initializeArrays();

    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == A0) {
            int nRow = ((MatrixToken) A0.getToken()).getRowCount();
            int nCol = ((MatrixToken) A0.getToken()).getColumnCount();
            if (nRow != nCol) {
                throw new IllegalActionException(this,
                        "Transition Probability Matrix must be a square matrix.");
            } else {
                _transitionMatrix = new double[nRow][nCol];
                _A0 = new double[nRow][nCol];
                for (int i = 0; i < nRow; i++) {
                    for (int j = 0; j < nCol; j++) {
                        _transitionMatrix[i][j] = ((DoubleToken) ((MatrixToken) A0
                                .getToken()).getElementAsToken(i, j))
                                .doubleValue();
                        if (_transitionMatrix[i][j] < 0.0) {
                            throw new IllegalActionException(this,
                                    "Transition probabilities cannot be negative.");
                        }
                    }
                }
                _A0 = _transitionMatrix;
            }
        } else if (attribute == priorDistribution) {
            int nS = ((ArrayToken) priorDistribution.getToken()).length();
            double[] tempPriors = new double[nS];
            double sum = 0.0;

            for (int i = 0; i < nS; i++) {
                tempPriors[i] = ((DoubleToken) ((ArrayToken) priorDistribution
                        .getToken()).getElement(i)).doubleValue();
                if (tempPriors[i] < 0.0) {
                    throw new IllegalActionException(this,
                            "Priors must be non-negative.");
                }
                sum += tempPriors[i];
            }
            // check if priors is a valid probability vector.
            if (!SignalProcessing.close(sum, 1.0)) {
                throw new IllegalActionException(this, "Priors sum to " + sum
                        + " . The sum must be equal to 1.0.");
            } else {
                _priors = tempPriors;
            }
        } else if (attribute == maxIterations) {
            if (((IntToken) maxIterations.getToken()).intValue() <= 0) {
                throw new IllegalActionException(this,
                        "Number of iterations must be greater than zero.");
            } else {
                _nIterations = ((IntToken) maxIterations.getToken()).intValue();
            }
        } else if (attribute == likelihoodThreshold) {
            double threshold = ((DoubleToken) likelihoodThreshold.getToken())
                    .doubleValue();
            if (threshold > 0.0) {
                _likelihoodThreshold = threshold;
            } else {
                throw new IllegalActionException(this,
                        "Likelihood threshold must be positive.");
            }
        } else if (attribute == nStates) {

            int nS = ((IntToken) nStates.getToken()).intValue();
            if (nS > 0) {
                _nStates = nS;
            } else {
                throw new IllegalActionException(this,
                        "Number of states must be a positive integer");
            }

        } else if (attribute == randomizeGuessVectors) {
            boolean randomize = ((BooleanToken) randomizeGuessVectors
                    .getToken()).booleanValue();
            _randomize = randomize;
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The user-provided initial guess of the transition probability matrix*/
    public Parameter A0;

    /** The user-provided threshold on the minimum desired improvement on likelihood per iteration*/
    public Parameter likelihoodThreshold;

    /** The user-provided maximum number of allowed iterations of the Alpha-Beta Recursion*/
    public Parameter maxIterations;

    /** Number of states of the HMM*/
    public Parameter nStates;

    /** Boolean that determines whether or not to randomize input guess vectors */
    public Parameter randomizeGuessVectors;

    /** The user-provided initial guess on the prior probability distribution*/
    public Parameter priorDistribution;

    /** The input port that provides the sample observations*/
    public TypedIOPort input;

    /** The vector estimate for the prior distribution on the set of states*/
    public TypedIOPort priorEstimates;

    /** The transition matrix estimate obtained by iterating over the observation set*/
    public TypedIOPort transitionMatrix;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ParameterEstimator newObject = (ParameterEstimator) super
                .clone(workspace);
        newObject._likelihood = 0.0;
        newObject._transitionMatrix = new double[_nStates][_nStates];
        newObject._A0 = new double[_nStates][_nStates];
        newObject._priors = new double[_nStates];
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {

        super.fire();
        Token observationArray = input.get(0);
        // observation length is inferred from the input array length.
        int _observationLength = ((ArrayToken) observationArray).length();
        _observations = new double[_observationLength];
        if (_observationLength <= 0) {
            throw new IllegalActionException(this,
                    "Observation sequence length " + _observationLength
                            + " but must be greater than zero.");
        }
        // Get Observation Values as doubles
        //FIXME: should the observations  allowed to be vectors too, for multivariate distributions?
        for (int i = 0; i < _observationLength; i++) {
            _observations[i] = ((DoubleToken) ((ArrayToken) observationArray)
                    .getElement(i)).doubleValue();
        }
    }

    /**
     * Expectation-Maximization
     * @return
     * @throws IllegalActionException
     */
    protected boolean _EMParameterEstimation() throws IllegalActionException {

        boolean success = false;
        _initializeEMParameters();

        for (int iterations = 0; iterations < _nIterations; iterations++) {

            _iterateEM();
            success = _checkForConvergence(iterations);
            // randomization not allowed and convergence was not achieved
            if (!_randomize && !success) {
                break;
            }
            _updateEstimates();

            // check convergence within likelihoodThreshold
            if (Math.abs(likelihood - _likelihood) < _likelihoodThreshold) {
                break;
            } else {
                _likelihood = likelihood;
            }
        }

        return success;
    }

    // the function that computes the emission probability. Implemented by the child class.
    protected abstract double emissionProbability(double y, int hiddenState);

    protected void _initializeArrays() throws IllegalActionException {

        //_observations = new double[_observationLength];
        // infer the number of states from the mean array
        _likelihood = 0.0;
        _nStates = ((IntToken) nStates.getToken()).intValue();
        _transitionMatrix = new double[_nStates][_nStates];
        _A0 = new double[_nStates][_nStates];
        _priors = new double[_nStates];
    }

    protected abstract void _initializeEMParameters();

    protected abstract void _iterateEM();

    protected abstract boolean _checkForConvergence(int i) throws IllegalActionException;

    protected abstract void _updateEstimates();

    /** Java implementation of the Baum-Welch algorithm (Alpha-Beta Recursion) for parameter estimation
     * and cluster assignment. This method uses normalized alpha values for computing the conditional
     * probabilities of input sequences, to ensure numerical stability. SEt nCategories to zero for
     * continuous distribution types
     * @param y input observation stream
     * @param A transition probability matrix guess
     * @param prior prior state distribution guess
     * @param nCategories number of categories in the multinomial distribution, where applies
     * @return
     */ 
    protected HashMap HMMAlphaBetaRecursion(double[] y, double[][] A,
            double[] prior, int nCategories)

    {
        boolean multinomial = (nCategories > 0) ? true : false;
        int nStates = _nStates;
        int nObservations = y.length;

        double[][] alphas = new double[nObservations][nStates];
        double[][] gamma = new double[nObservations][nStates];
        double[][][] xi = new double[nObservations - 1][nStates][nStates];

        double[][] A_hat = new double[nStates][nStates];
        double[] mu_hat = new double[nStates];
        double[] s_hat = new double[nStates];
        double[] pi_hat = new double[nStates];
        double[][] eta_hat = new double[nStates][nCategories];

        double[] alphaNormalizers = new double[nObservations];
        double alphaSum = 0;
        for (int t = 0; t < y.length; t++) {
            alphaSum = 0;
            for (int i = 0; i < nStates; i++) {
                alphas[t][i] = 0;
                if (t == 0) {
                    alphas[t][i] = prior[i] * emissionProbability(y[t], i);
                } else {
                    for (int qt = 0; qt < nStates; qt++) {
                        alphas[t][i] += A[qt][i] * emissionProbability(y[t], i)
                                * alphas[t - 1][qt];
                    }
                }
                alphaSum += alphas[t][i];
            }
            // alpha normalization
            for (int i = 0; i < nStates; i++) {
                alphas[t][i] /= alphaSum;
                alphaNormalizers[t] = alphaSum;
            }
        }
        for (int t = y.length - 1; t >= 0; t--) {
            for (int qt = 0; qt < nStates; qt++) {
                if (t == y.length - 1) {
                    gamma[t][qt] = alphas[t][qt];
                } else {
                    gamma[t][qt] = 0;
                    for (int qtp = 0; qtp < nStates; qtp++) {
                        double alphasum = 0;
                        for (int j = 0; j < nStates; j++) {
                            alphasum += alphas[t][j] * A[j][qtp];
                        }
                        gamma[t][qt] += (alphas[t][qt] * A[qt][qtp] * gamma[t + 1][qtp])
                                / alphasum;
                    }
                }
            }
        }

        //next, calculate the xis ( for the transition matrix)
        for (int next = 0; next < nStates; next++) {
            for (int now = 0; now < nStates; now++) {
                for (int t = 0; t < (y.length - 1); t++) {
                    // gamma t or t+1? alphas t or t+1?
                    if (alphas[t + 1][next] == 0) {
                        xi[t][now][next] = 0;
                    } else {
                        xi[t][now][next] = alphas[t][now]
                                * emissionProbability(y[t + 1], next)
                                * gamma[t + 1][next] * A[now][next]
                                / alphas[t + 1][next]; // MJ Eqn (11.45)
                    }
                    A_hat[now][next] += xi[t][now][next];
                }
            }
            mu_hat[next] = 0;
            s_hat[next] = 0;
        }
        // Normalize A
        double[] rowsum = new double[nStates];
        double[] gammasum = new double[nStates];
        for (int i = 0; i < nStates; i++) {
            rowsum[i] = 0;
            for (int j = 0; j < nStates; j++) {
                rowsum[i] += A_hat[i][j];
            }
            for (int j = 0; j < nStates; j++) {
                A_hat[i][j] /= rowsum[i];
            }
            gammasum[i] = 0.0;
        }
        for (int j = 0; j < nStates; j++) {
            gammasum[j] = 0.0;
            for (int t = 0; t < y.length; t++) {
                gammasum[j] += gamma[t][j];
                mu_hat[j] += gamma[t][j] * y[t];
            }
            mu_hat[j] = mu_hat[j] / gammasum[j];
            for (int t = 0; t < y.length; t++) {
                s_hat[j] += (gamma[t][j] * Math.pow((y[t] - mu_hat[j]), 2));
            }
            s_hat[j] = Math.sqrt(s_hat[j] / gammasum[j]);
            // prior probabilities updated
            pi_hat[j] = gamma[0][j];
        }
        // labels for the multinomial setting
        if (multinomial) {
            for (int i = 0; i < nStates; i++) {
                for (int j = 0; j < nCategories; j++) {
                    for (int t = 0; t < y.length; t++) {
                        eta_hat[i][j] += gamma[t][i] * ((y[t] == j) ? 1 : 0);
                    }
                    eta_hat[i][j] /= gammasum[i]; //normalize for gammas
                }
            }
        }
        // do hidden state sequence estimation to compute the log-likelihood, given the current
        // parameter estimates
        int[] clusterAssignments = new int[y.length];
        for (int t = 0; t < y.length; t++) {
            int maxState = 0;
            for (int j = 1; j < nStates; j++) {
                if (gamma[t][j] > gamma[t][maxState]) {
                    maxState = j;
                }
            }
            clusterAssignments[t] = maxState;
        }
        // compute the log-likelihood P(Y|\theta), where \theta is the set of parameter estimates
        // for the HMM.

        double logLikelihood = 0.0;
        for (int t = 0; t < _observations.length - 1; t++) {
            logLikelihood += emissionProbability(y[t], clusterAssignments[t]);
            logLikelihood += A_hat[clusterAssignments[t]][clusterAssignments[t + 1]];
        }
        // add the emission probability at final time value
        logLikelihood += emissionProbability(y[_observations.length - 1],
                clusterAssignments[_observations.length - 1]);

        HashMap estimates = new HashMap();

        estimates.put("mu_hat", mu_hat);
        estimates.put("s_hat", s_hat);
        estimates.put("gamma", gamma); //this will not be needed for most of the distributions.
        estimates.put("A_hat", A_hat);
        estimates.put("pi_hat", pi_hat);
        estimates.put("eta_hat", eta_hat);
        estimates.put("likelihood", logLikelihood);
        return estimates;
    }

    protected HashMap HMMAlphaBetaRecursionNonNormalized(double[] y,
            double[][] A, double[] prior, int unused) {
        int nStates = _nStates;
        int nObservations = y.length;

        int nCategories = 3; // FIXME

        double[][] alphas = new double[nObservations][nStates];
        double[][] betas = new double[nObservations][nStates];
        double[] Py = new double[nObservations];
        double[][] gamma = new double[nObservations][nStates];
        double[][][] xi = new double[nObservations - 1][nStates][nStates];

        double[][] A_hat = new double[nStates][nStates];
        double[] mu_hat = new double[nStates];
        double[] s_hat = new double[nStates];
        double[] pi_hat = new double[nStates];
        double[][] eta_hat = new double[nStates][nCategories];

        for (int t = 0; t < y.length; t++) {
            for (int i = 0; i < nStates; i++) {
                alphas[t][i] = 0;
                if (t == 0) {
                    alphas[t][i] = prior[i] * emissionProbability(y[t], i);
                } else {
                    for (int qt = 0; qt < nStates; qt++) {
                        alphas[t][i] += A[qt][i] * emissionProbability(y[t], i)
                                * alphas[t - 1][qt];
                    }
                }
            }
        }

        for (int t = y.length - 1; t >= 0; t--) {
            // initialize py at time t
            Py[t] = 0;
            for (int i = 0; i < nStates; i++) {
                gamma[t][i] = 0.0;
                betas[t][i] = 0.0;
                if (t == y.length - 1) {
                    betas[t][i] = 1;
                } else {
                    // reverse-time recursion  (do this recursively later)
                    for (int qtp = 0; qtp < nStates; qtp++) {
                        betas[t][i] += A[i][qtp]
                                * emissionProbability(y[t + 1], qtp)
                                * betas[t + 1][qtp];
                    }
                }
                Py[t] += alphas[t][i] * betas[t][i];
            }
            for (int i = 0; i < nStates; i++) {
                gamma[t][i] += alphas[t][i] * betas[t][i] / Py[t];
            }
        }

        //next, calculate the xis ( for the transition matrix)
        for (int next = 0; next < nStates; next++) {
            for (int now = 0; now < nStates; now++) {
                for (int t = 0; t < (y.length - 1); t++) {
                    // gamma t or t+1? alphas t or t+1?
                    if (alphas[t + 1][next] == 0) {
                        xi[t][now][next] = 0;
                    } else {
                        xi[t][now][next] = alphas[t][now]
                                * emissionProbability(y[t + 1], next)
                                * gamma[t + 1][next] * A[now][next]
                                / alphas[t + 1][next];
                    }
                    A_hat[now][next] += xi[t][now][next];
                }

            }
            mu_hat[next] = 0;
            s_hat[next] = 0;
        }
        // Normalize A
        double[] rowsum = new double[nStates];
        double[] gammasum = new double[nStates];
        for (int i = 0; i < nStates; i++) {

            rowsum[i] = 0;
            for (int j = 0; j < nStates; j++) {
                rowsum[i] += A_hat[i][j];
            }
            for (int j = 0; j < nStates; j++) {
                A_hat[i][j] /= rowsum[i];
            }
            gammasum[i] = 0.0;
        }

        for (int j = 0; j < nStates; j++) {
            gammasum[j] = 0.0;
            for (int t = 0; t < y.length; t++) {
                gammasum[j] += gamma[t][j];
                mu_hat[j] += gamma[t][j] * y[t];
            }
            mu_hat[j] = mu_hat[j] / gammasum[j];

            for (int t = 0; t < y.length; t++) {
                s_hat[j] += (gamma[t][j] * Math.pow((y[t] - mu_hat[j]), 2));
            }
            s_hat[j] = Math.sqrt(s_hat[j] / gammasum[j]);
            // prior probabilities updated
            pi_hat[j] = gamma[0][j];
        }

        for (int i = 0; i < nStates; i++) {
            for (int j = 0; j < nCategories; j++) {
                for (int t = 0; t < y.length; t++) {
                    eta_hat[i][j] += gamma[t][i] * ((y[t] == j) ? 1 : 0);
                }
                eta_hat[i][j] /= gammasum[i]; //normalize for gammas
            }
        }

        // do hidden state sequence estimation to compute the log-likelihood, given the current
        // parameter estimates
        int[] clusterAssignments = new int[y.length];
        for (int t = 0; t < y.length; t++) {
            int maxState = 0;
            for (int j = 1; j < nStates; j++) {
                if (gamma[t][j] > gamma[t][maxState]) {
                    maxState = j;
                }
            }
            clusterAssignments[t] = maxState;
        }
        // compute the log-likelihood P(Y|\theta), where \theta is the set of parameter estimates
        // for the HMM.

        double logLikelihood = 0.0;

        for (int t = 0; t < _observations.length - 1; t++) {
            logLikelihood += emissionProbability(y[t], clusterAssignments[t]);
            logLikelihood += A_hat[clusterAssignments[t]][clusterAssignments[t + 1]];
        }
        // add the emission probability at final time value
        logLikelihood += emissionProbability(y[_observations.length - 1],
                clusterAssignments[_observations.length - 1]);
        // display the log-likelihood at this iteration
        //System.out.println(logLikelihood);

        HashMap estimates = new HashMap();

        estimates.put("mu_hat", mu_hat);
        estimates.put("s_hat", s_hat);
        estimates.put("gamma", gamma); //this will not be needed for most of the distributions.
        estimates.put("A_hat", A_hat);
        estimates.put("pi_hat", pi_hat);
        estimates.put("eta_hat", eta_hat);
        estimates.put("likelihood", logLikelihood);

        return estimates;

    }

    /* User-defined initial guess array for the state transition matrix*/
    protected double[][] _A0;

    /* likelihood value of the observations given the current estimates L(x1,....xT | \theta_p)*/
    protected double _likelihood;

    protected double _likelihoodThreshold;

    /* User-defined number of iterations of the alpha-beta recursion*/
    protected int _nIterations;

    /* Number of hidden states in the model*/
    protected int _nStates;

    /* Observation array*/
    protected double[] _observations;

    /* Prior distribution on hidden states*/
    protected double[] _priors;

    /* The prior estimates used in the EM iterations*/
    protected double[] _priorIn;

    /* randomize the initial guess vectors or not*/
    protected boolean _randomize;
    /* Initial guess array for the state transition matrix for the Alpha-Beta Recursion*/
    protected double[][] _transitionMatrix;

    protected HashMap newEstimates;

    protected double likelihood;

}
