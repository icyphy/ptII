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
package org.ptolemy.machineLearning.hsmm;

import java.util.stream.IntStream;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
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
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ExpectationMaximization

/**
<p>This actor implements a parameter estimator for a Hidden Semi-Markov Model with Gaussian
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
public class HSMMMultinomialEstimator extends HSMMParameterEstimator {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HSMMMultinomialEstimator(CompositeEntity container, String name)
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
        nCategories.setExpression("{3}");
        nCategories.setTypeEquals(new ArrayType(BaseType.INT));
        _nCategories = new int[1];
        _nCategories[0]= 3;
        _etaDimension = IntStream.of(_nCategories).sum();

        _B = new double[_nStates][_etaDimension];
        _B0 = new double[_nStates][_etaDimension]; 
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == observationDimension) {
            _obsDimension = ((IntToken)observationDimension.getToken()).intValue();
        } if (attribute == observationProbabilities) {

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
            Token[] cat = ((ArrayToken) nCategories.getToken()).arrayValue();
            if (cat.length <= 0) {
                throw new IllegalActionException(this,
                        "Number of categories must be positive");
            } else {
                _nCategories = new int[cat.length];
                int total = 0;
                for ( int i = 0 ; i < cat.length; i++) {
                    _nCategories[i] = ((IntToken)cat[i]).intValue();
                    total += _nCategories[i];
                }
                // necessary for the HMM recursion to follow.
                _etaDimension = total;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * An output that defines a probability mass estimate of the multinomial
     * observation probabilities
     */
    public TypedIOPort emissionEstimates;

    /**
     * An input guess array that defines a probability mass, defining the multinomial
     * observation probabilities
     */
    public Parameter observationProbabilities;

    /**
     * Number of categories in the multinomial distribution
     */
    public Parameter nCategories;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HSMMMultinomialEstimator newObject = (HSMMMultinomialEstimator) super
                .clone(workspace);
        newObject._B = null;
        newObject._B0 = null;
        newObject.B_new = null;
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire(); 
        if ( _EMParameterEstimation() == true) { 
            //System.out.println("Final Likelihood: " +likelihood);
            int _nObservations = _observations.length; 
            Token[] pTokens = new Token[_nStates];
            Token[] cTokens = new Token[_nObservations];
            Token[] dTokens = new Token[_maxDuration];
            Token[] lTokens = new Token[_likelihoodHistory.size()];

            for (int i = 0; i < _nStates; i++) { 
                pTokens[i] = new DoubleToken(prior_new[i]);
            }
            for (int i = 0; i < _maxDuration; i++) {
                dTokens[i] = new DoubleToken(dPrior_new[i]);
            }

            for (int i = 0; i < _nObservations; i++) {
                cTokens[i] = new IntToken(clusters[i]);
            }

            for (int i = 0; i < lTokens.length; i++) {
                lTokens[i] = new DoubleToken(_likelihoodHistory.get(i));
            }
            _likelihoodHistory.clear();

            emissionEstimates.send(0, new DoubleMatrixToken(_B)); 
            transitionMatrix.send(0, new DoubleMatrixToken(A_new));
            priorEstimates.send(0, new ArrayToken(pTokens));
            durationEstimates.send(0, new DoubleMatrixToken(D_new));
            clusterAssignments.send(0, new ArrayToken(cTokens));
            durationPriorEstimates.send(0, new ArrayToken(dTokens)); 
        } else { 
            System.err.println("EM Algorithm did not converge!");
        } 
    }

    protected double emissionProbability(double[] y, int hiddenState) {


        double probability = _B[hiddenState][(int)y[0]];

        // retrieving the joint probability of all observations being equal
        // to the observed y. Note that _B[hiddenState] is a vector that contains
        // categorical probability belief for ALL dimensions of y in a concatenated format
        // For instance, if y is a 2-D observation and _nCategories = {M1, M2}, 
        // _B[hiddenState] will be a vector of length M1+M2. 
        int categoryIndex = 0;
        for (int i = 1; i < y.length; i ++) {
            categoryIndex += _nCategories[i-1];
            probability *= _B[hiddenState][(int)y[i] + categoryIndex];
        }

        return probability;
    }

    @Override
    protected boolean _checkForConvergence(int iterations) {

        boolean nanDetected = false;
        for (int i = 0; i < B_new.length; i++) {
            if (Double.isNaN(B_new[0][0])) {
                nanDetected = true;
                break;
            }
        }
        if (nanDetected) { 
            if (Double.isNaN(D_new[0][0])) {
                D_new = _D0;
            }

            // if no convergence in 10 iterations, issue warning message.
            if ((iterations >= _nIterations - 1)) {
                // return the guess parameters

                A_new = _A0;
                prior_new = _priors;
                _D = _D0;
                _durationPriors = _dPriors0;
                System.out
                .println("Expectation Maximization failed to converge");
                return false;
            } else if (_randomize) { 
            }
        }
        return true;
    }

    @Override
    protected void _initializeEMParameters() { 
        _transitionMatrix = _A0;
        _priorIn = _priors; 
        _B = _B0;
        B_new = new double[_nStates][_etaDimension];
        A_new = new double[_nStates][_nStates]; 
        prior_new = new double[_nStates];
        D_new = new double[_nStates][_maxDuration];
        _D = _D0;
    }

    @Override
    protected void _iterateEM() { 
        newEstimates = HSMMAlphaBetaRecursion(_observations, _transitionMatrix,
                _priorIn, _nCategories);  
        B_new = (double[][]) newEstimates.get("eta_hat");  
        A_new = (double[][]) newEstimates.get("A_hat");
        prior_new = (double[]) newEstimates.get("pi_hat");
        dPrior_new = (double[]) newEstimates.get("pi_d_hat");
        likelihood = (Double) (newEstimates.get("likelihood"));
        D_new = (double[][]) newEstimates.get("D_hat");
        clusters = (int[]) newEstimates.get("clusterAssignments"); 
    }

    @Override
    protected void _updateEstimates() {
        _transitionMatrix = A_new; 
        _priorIn = prior_new; // set to the original priors
        _D = D_new;
        _B = B_new;
        _durationPriors = dPrior_new; 
    }



    /**
     * Prior durations
     */
    private double[] dPrior_new; 
    /**
     * Inferred cluster assignments
     */
    protected int[] clusters;

    /**
     *  Emission distributions Bij = P(Yt=j | qt = i)
     */
    private double[][] _B;
    /**
     * Initial guess of Emission distribution matrix
     */
    private double[][] _B0;
     

    /**
     * Number of categories
     */
    private int[] _nCategories;

    /*
     * Updated transition probability matrix
     */
    private double[][] A_new;

    /**
     * Updated emission probability matrix
     */
    private double[][] B_new;

    /**
     * Updated state prior belief
     */
    private double[] prior_new;

    @Override
    protected double durationProbability(int y, int hiddenState) {
        // TODO Auto-generated method stub
        if (y >= _maxDuration) {
            return 0.0;
        } else {
            return _D[hiddenState][y];
        }
    }


}
