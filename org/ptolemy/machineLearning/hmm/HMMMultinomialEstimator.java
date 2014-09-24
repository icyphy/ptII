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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ExpectationMaximization

/**
<p>This actor implements a parameter estimator for Hidden Markov Models with Multinomial
Emissions. The base class ParameterEstimator performs the parameter estimation and
the HMMGaussianEstimator class contains density-specific methods for Multinomial emission
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
public class HMMMultinomialEstimator extends ParameterEstimator {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HMMMultinomialEstimator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        emissionEstimates = new TypedIOPort(this, "emissionEstimates", false,
                true);
        emissionEstimates.setTypeEquals(BaseType.DOUBLE_MATRIX);

        observationProbabilities = new Parameter(this,
                "observationProbabilities");
        observationProbabilities.setExpression("[0.6,0.3,0.1;0.1,0.4,0.5]");
        observationProbabilities.setTypeEquals(BaseType.DOUBLE_MATRIX);

        nCategories = new Parameter(this, "nCategories");
        nCategories.setExpression("3");
        nCategories.setTypeEquals(BaseType.INT);
        _nCategories = 3;

        _B = new double[_nStates][_nCategories];
        _B0 = new double[_nStates][_nCategories];
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == observationProbabilities) {

            int nCat = ((MatrixToken) observationProbabilities.getToken())
                    .getColumnCount();
            _nStates = ((IntToken) nStates.getToken()).intValue();
            _B0 = new double[_nStates][nCat];
            for (int i = 0; i < _nStates; i++) {
                for (int j = 0; j < nCat; j++) {
                    _B0[i][j] = ((DoubleToken) ((MatrixToken) observationProbabilities
                            .getToken()).getElementAsToken(i, j)).doubleValue();
                }
            }

        } else if (attribute == nCategories) {
            int cat = ((IntToken) nCategories.getToken()).intValue();
            if (cat <= 0) {
                throw new IllegalActionException(this,
                        "Number of categories must be positive");
            } else {
                _nCategories = cat;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /*
     * An output that defines a probability mass estimate of the multinomial
     * observation probabilities 
     */
    public TypedIOPort emissionEstimates;

    /*
     * An input guess array that defines a probability mass, defining the multinomial
     * observation probabilities 
     */
    public Parameter observationProbabilities;

    public Parameter nCategories;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HMMMultinomialEstimator newObject = (HMMMultinomialEstimator) super
                .clone(workspace);
        newObject._B = new double[_nStates][_nStates];
        newObject._B0 = new double[_nStates][_nStates];
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if ((_nStates != _transitionMatrix.length)
                || (_nStates != _priors.length)
                || (_nCategories != _B0[0].length)) {
            throw new IllegalActionException(this,
                    "Parameter guess vectors cannot have different lengths.");
        }

        _EMParameterEstimation();

        Token[] pTokens = new Token[_nStates];
        for (int i = 0; i < _nStates; i++) {
            pTokens[i] = new DoubleToken(prior_new[i]);
        }
        transitionMatrix.send(0, new DoubleMatrixToken(A_new));
        emissionEstimates.send(0, new DoubleMatrixToken(B_new));
        priorEstimates.send(0, new ArrayToken(pTokens));
    }

    @Override
    public boolean postfire() throws IllegalActionException {
        _likelihood = 0.0;
        return true;
    }

    @Override
    protected boolean _checkForConvergence(int iterations) {
        return true;
    }

    @Override
    protected void _initializeEMParameters() {
        _transitionMatrix = _A0;
        _B = _B0;
        _priorIn = _priors;
        A_new = new double[_nStates][_nStates];
        B_new = new double[_nStates][_nCategories];
        prior_new = new double[_nStates];
    }

    @Override
    protected void _iterateEM() {
        newEstimates = HMMAlphaBetaRecursion(_observations, _transitionMatrix,
                _priorIn, _nCategories);
        B_new = (double[][]) newEstimates.get("eta_hat");
        A_new = (double[][]) newEstimates.get("A_hat");
        prior_new = (double[]) newEstimates.get("pi_hat");
        likelihood = (Double) (newEstimates.get("likelihood"));
    }

    @Override
    protected void _updateEstimates() {
        _transitionMatrix = A_new;
        _B = B_new;
        _priorIn = prior_new;
    }

    @Override
    protected double emissionProbability(double y, int hiddenState) {
        return _B[hiddenState][(int) y];
    }

    // emission distributions Bij = P(Yt=j | qt = i)
    private double[][] _B;
    private double[][] _B0;
    private int _nCategories;

    private double[][] A_new;
    private double[][] B_new;
    private double[] prior_new;
}
