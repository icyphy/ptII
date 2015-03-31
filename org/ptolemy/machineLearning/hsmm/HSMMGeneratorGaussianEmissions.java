/* Parameter Estimation for Explicit-Duration Hidden Markov Models.

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

import org.ptolemy.machineLearning.Algorithms;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
////ExpectationMaximization

/**
<p> This actor implements the Expectation-Maximization(EM) algorithm for
parameter estimation in a family of graphical stochastic models, known as
the Hidden Semi-Markov Model family.
<p> In addition to estimating the parameters of a hidden markov model,


 <p>
 <b>References</b>
 <p>[1]
 Dewar, M.; Wiggins, C.; Wood, F., <i>Inference in Hidden Markov Models with Explicit
 State Duration Distributions</i>, Signal Processing Letters, IEEE , vol.19, no.4, pp.235,238, April 2012

@see org.ptolemy.machineLearning.hmm.ParameterEstimator


 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public class HSMMGeneratorGaussianEmissions extends HSMMGenerator {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HSMMGeneratorGaussianEmissions(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
  

        mean = new PortParameter(this, "mean");
        mean.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        mean.setExpression("{0.0,100.0}");
        new StringAttribute(mean.getPort(), "_cardinal")
            .setExpression("SOUTH");
        new SingletonParameter(mean.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        covariance = new PortParameter(this, "covariance");
        covariance.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        covariance.setExpression("{10.0,10.0}");
        new StringAttribute(covariance.getPort(), "_cardinal")
            .setExpression("SOUTH");
        new SingletonParameter(covariance.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
 
    /** Mean array. */
    public PortParameter mean;

    /** Variance array. */
    public PortParameter covariance;
  

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == mean) {
            ArrayToken meanToken = ((ArrayToken) mean.getToken());
            int nStates = meanToken.length();
            _mean = new double[nStates];

            for (int i = 0; i < nStates; i++) {
                _mean[i] = ((DoubleToken) meanToken.getElement(i))
                        .doubleValue();
            }
        } else if (attribute == covariance) {
            ArrayToken sigmaToken = ((ArrayToken) covariance.getToken());
            int nStates = sigmaToken.length();
            _covariance = new double[nStates];
            for (int i = 0; i < nStates; i++) {
                _covariance[i] = ((DoubleToken) sigmaToken.getElement(i))
                        .doubleValue();
            }
        } else {
            super.attributeChanged(attribute);
        } 
    }

    @Override
    public void fire() throws IllegalActionException {

        super.fire();
        durationPriors.update();
        durationProbabilities.update();
        transitionMatrix.update();
        mean.update();
        covariance.update();
        statePriors.update();
        powerUpperBound.update();
        
        if (trigger.hasToken(0)) {
            trigger.get(0); 
            int nStates = ((ArrayToken) statePriors.getToken()).length();
            ArrayToken meanToken = ((ArrayToken) mean.getToken());
            ArrayToken sigmaToken = ((ArrayToken) covariance.getToken());
            ArrayToken statePriorsToken = ((ArrayToken) statePriors.getToken());
            ArrayToken durationPriorsToken = ((ArrayToken) durationPriors
                    .getToken());
            DoubleMatrixToken transitionMatrixToken = ((DoubleMatrixToken) transitionMatrix
                    .getToken());
            DoubleMatrixToken durationsToken = ((DoubleMatrixToken) durationProbabilities
                    .getToken());
            int maxDuration = durationPriorsToken.length();

            if (nStates != meanToken.length() || nStates != sigmaToken.length()
                    || nStates != statePriorsToken.length()
                    || nStates != transitionMatrixToken.getRowCount()
                    || nStates != transitionMatrixToken.getColumnCount()) {
                throw new IllegalActionException(this,
                        "Parameters must have consistent dimension");
            }
            _nStates = nStates;
            if (maxDuration != durationsToken.getColumnCount()) {
                throw new IllegalActionException(this,
                        "Duration distribution and duration priors "
                                + "must be defined over the same time extent.");
            }
            _maxDuration = maxDuration;
            // start generating values 
            boolean validSequenceFound = false;
            int trials = 0;
            double [] ys = new double[_windowSize];
            int [] xs = new int [_windowSize];
            while (!validSequenceFound && trials < MAX_TRIALS) {
                double cumulativePower = 0.0;
                for (int i = 0; i < _windowSize; i ++ ) {  
                    if (_firstIteration) {
                        // sample hidden state from prior
                        _xt = _sampleHiddenStateFromPrior();
                        _dt = _sampleDurationFromPrior();
                        _firstIteration = false;
                       
                    }
                    if (_dt <= 1) {
                        // if the remaining time at the current state is 1 or less, there needs to be a stata transition.
                        _xt = _propagateState();
                        _dt = _sampleDurationForState();
                    } else {
                        // _xt doesn't change, decrement _dt.
                        _dt--;
                    }
                    double[] yt = _sampleObservation();
                    cumulativePower += yt[0];
                    xs[i] = _xt;
                    ys[i] = yt[0];
                }
                if (cumulativePower <= ((DoubleToken)powerUpperBound.getToken()).doubleValue()) {
                    validSequenceFound = true;
                    break;
                } else {
                    trials++;
                }
            }
            System.out.println(trials + " sequences rejected until sequence found");
            if (validSequenceFound) {
                Token[] outputObservations = new DoubleToken[_windowSize];
                Token[] states = new IntToken[_windowSize];
                for (int i = 0; i < _windowSize; i ++) {
                    outputObservations[i] = new DoubleToken(ys[i]);
                    states[i] = new IntToken(xs[i]);
                }
                observation.send(0, new ArrayToken(outputObservations));
                hiddenState.send(0, new ArrayToken(states));
            }
        }
    }

    @Override
    protected double[] _sampleObservation() {
        // generate y_t ~ p(y_t | x_t). In this class, y_t is Gaussian, whose mean and variance is a function of _xt.
        double mu = _mean[_xt];
        double s = _covariance[_xt];

        DoubleToken yt = UtilityFunctions.gaussian(mu, s);
        return new double[]{yt.doubleValue()}; 
    }

    @Override
    protected int _sampleDurationForState() {
        double[] cumSums = new double[_maxDuration + 1];
        for (int i = 0; i < _maxDuration; i++) {
            cumSums[i + 1] = cumSums[i] + _D[_xt][i];
        }
        // generate a random value ( in theory, within 0 and 1,
        // since this is a probability, however, should be
        // normalized to avoid any numerical errors
        double randomValue = Math.random() * cumSums[_maxDuration];
        int bin = Algorithms._binaryIntervalSearch(cumSums, randomValue, 0,
                _maxDuration);
        return bin + 1;
    }

    @Override
    protected int _propagateState() {
        double[] cumSums = new double[_nStates + 1];
        for (int i = 0; i < _nStates; i++) {
            cumSums[i + 1] = cumSums[i] + _A[_xt][i];
        }
        // generate a random value ( in theory, within 0 and 1,
        // since this is a probability, however, should be
        // normalized to avoid any numerical errors
        double randomValue = Math.random() * cumSums[_nStates];
        int bin = Algorithms._binaryIntervalSearch(cumSums, randomValue, 0,
                _nStates);
        return bin;
    }

    @Override
    protected int _sampleHiddenStateFromPrior() {
        // calculate cumulative sums and sample from the CDF
        double[] cumSums = new double[_nStates + 1];
        for (int i = 0; i < _nStates; i++) {
            cumSums[i + 1] = cumSums[i] + _x0[i];
        }
        // generate a random value ( in theory, within 0 and 1,
        // since this is a probability, however, should be
        // normalized to avoid any numerical errors
        double randomValue = Math.random() * cumSums[_nStates];
        int bin = Algorithms._binaryIntervalSearch(cumSums, randomValue, 0,
                _nStates);
        return bin;
    }

    @Override
    protected int _sampleDurationFromPrior() {
        double[] cumSums = new double[_maxDuration + 1];
        for (int i = 0; i < _maxDuration; i++) {
            cumSums[i + 1] = cumSums[i] + _durationPriors[i];
        }
        // generate a random value ( in theory, within 0 and 1,
        // since this is a probability, however, should be
        // normalized to avoid any numerical errors
        double randomValue = Math.random() * cumSums[_maxDuration];
        int bin = Algorithms._binaryIntervalSearch(cumSums, randomValue, 0,
                _maxDuration);
        return bin + 1;
    }
 
    /** Mean vector. */
    private double[] _mean;
    
    /** Covariance vector. */
    private double[] _covariance; 

}
