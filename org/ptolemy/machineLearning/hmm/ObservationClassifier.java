/** An observation classifier

Copyright (c) 2013-2014 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ObservationClassifier

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


 <p>
 <b>References</b>
 <p>[1]
 Jordan, Michael I., et al. <i>An introduction to variational methods for graphical
 models</i>, Springer Netherlands, 1998.
 <p>[2]
 Bilmes, Jeff A. <i>A gentle tutorial of the EM algorithm and its application
 to parameter estimation for Gaussian mixture and hidden Markov models.</i>
 International Computer Science Institute 4.510 (1998): 126.

 @see org.ptolemy.machineLearning.hmm.ParameterEstimator
 @see org.ptolemy.machineLearning.hmm.HMMGaussianClassifier
 @see org.ptolemy.machineLearning.hmm.HMMMultinomialClassifier

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public abstract class ObservationClassifier extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ObservationClassifier(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        prior = new PortParameter(this, "priorDistribution");
        prior.setExpression("{0.5,0.5}");
        prior.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        StringAttribute cardinality = new StringAttribute(prior.getPort(),
                "_cardinal");
        cardinality.setExpression("SOUTH");

        transitionMatrix = new PortParameter(this, "transitionMatrix");
        transitionMatrix.setExpression("[0.5,0.5;0.5,0.5]");
        transitionMatrix.setTypeEquals(BaseType.DOUBLE_MATRIX);
        cardinality = new StringAttribute(transitionMatrix.getPort(),
                "_cardinal");
        cardinality.setExpression("SOUTH");

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.INT));

        likelihood = new TypedIOPort(this, "likelihood", false, true);
        likelihood.setTypeEquals(BaseType.DOUBLE);

        //_nStates = ((ArrayToken) meanToken).length();
        _nStates = ((ArrayToken) prior.getToken()).length();
        _transitionMatrixEstimate = new double[_nStates][_nStates];
        _priors = new double[_nStates];

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * An array that defines priors on initial states.
     */
    public PortParameter prior;

    /**
     * The transition probability matrix of the hidden markov chain.
     */
    public PortParameter transitionMatrix;

    /**
     * An array of state labels assigned to input symbols.
     */
    public TypedIOPort output;

    /**
     * An array of input symbols to be classified.
     */
    public TypedIOPort input;

    /**
     * Likelihood of the input stream given the parameterized HMM.
     */
    public TypedIOPort likelihood;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ObservationClassifier newObject = (ObservationClassifier) super
                .clone(workspace);
        newObject._priors = new double[_nStates];
        newObject._transitionMatrixEstimate = new double[_nStates][_nStates];
        return newObject;
    }

    /** Consume the inputs and produce the outputs of the FFT filter.
     *  @exception IllegalActionException If a runtime type error occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (input.hasToken(0)) {
            // Read input ports

            Token observationArray = input.get(0);
            _classificationLength = ((ArrayToken) observationArray).length();
            _observations = new double[_classificationLength];

            // Get Observation Values as doubles
            //FIXME: should the observations  allowed to be vectors of doubles, too?
            for (int i = 0; i < _classificationLength; i++) {
                _observations[i] = ((DoubleToken) ((ArrayToken) observationArray)
                        .getElement(i)).doubleValue();
            }
        } else {
            _observations = null;
        }

    }

    /**
     * Alpha-beta recursion
     * @param y input sequence
     * @param prior prior guess vectors
     * @param A transition probability matrix
     * @return An array of assigned labels to observations
     */

    protected final int[] classifyHMM(double[] y, double[] prior, double[][] A) {
        int nStates = prior.length;
        double[][] alphas = new double[y.length][nStates];
        double[][] gamma = new double[y.length][nStates];
        // do this with org.apache.commons.math3.distribution
        //later NormalDistribution gaussian = ...

        double[] alphaNormalizers = new double[y.length];
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

        //  Classification to clusters
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

        double logLikelihood = 0.0;
        for (int t = 0; t < _observations.length - 1; t++) {
            logLikelihood += emissionProbability(y[t], clusterAssignments[t]);
            logLikelihood += A[clusterAssignments[t]][clusterAssignments[t + 1]];
        }
        // add the emission probability at final time value
        logLikelihood += emissionProbability(y[_observations.length - 1],
                clusterAssignments[_observations.length - 1]);

        _likelihood = logLikelihood;

        return clusterAssignments;
    }

    /**
     * Classify the incoming symbols into hidden states
     * @param y input array
     * @param mu mean array containing current mean estimates for hidden states
     * @param sigma mean array containing current standard deviation estimates for hidden states
     * @param prior prior distribution estimates
     * @return labels of assigned states
     */
    public static final int[] gaussianClassifyMM(double[] y, double[] mu,
            double[] sigma, double[] prior) {
        int nStates = mu.length;
        // the soft assignments for the observations  given the parameter estimates
        double[][] tau = new double[y.length][nStates];
        // the classified states of observations
        int[] clusterAssignments = new int[y.length];

        for (int t = 0; t < y.length; t++) {
            for (int i = 0; i < nStates; i++) {
                tau[t][i] = prior[i] * gaussian(y[t], mu[i], sigma[i]);
                if (tau[t][i] > tau[t][clusterAssignments[t]]) {
                    clusterAssignments[t] = i;
                }
            }
        }
        return clusterAssignments;
    }

    /**
     * Compute the value of the Gaussian distribution
     * @param x value at which the Gaussian will be evaluated
     * @param mu Sean
     * @param sigma Standard Deviation
     * @return the value of the Gaussian density
     */
    private static final double gaussian(double x, double mu, double sigma) {

        return 1.0 / (Math.sqrt(2 * Math.PI) * sigma)
                * Math.exp(-0.5 * Math.pow((x - mu) / sigma, 2));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /**
     * Abstract class defining the emission probability computation of the
     * latent variable.
     */
    protected abstract double emissionProbability(double y, int hiddenState);

    /** length of the observation array to be classified. */
    protected int _classificationLength;

    /** sequence likelihood assigned to current input. */
    protected double _likelihood;

    /** observation array. */
    protected double[] _observations;

    /** number of hidden states. */
    protected int _nStates;

    /** transition matrix estimate for the markov chain model. */
    protected double[][] _transitionMatrixEstimate;

    /** prior hidden state distribution. */
    protected double[] _priors;

}
