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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.math.SignalProcessing;

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
public class HSMMGeneratorMultinomialEmissions extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HSMMGeneratorMultinomialEmissions(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        powerUpperBound = new PortParameter(this,"powerUpperBound"); 
        powerLowerBound = new PortParameter(this,"powerLowerBound"); 
        durationPriors = new PortParameter(this, "durationPriors");
        durationPriors.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        durationPriors.setExpression("{1.0,0.0}");
        StringAttribute cardinality = new StringAttribute(
                durationPriors.getPort(), "_cardinal");
        cardinality.setExpression("SOUTH");
        
        nCategories = new Parameter(this,"nCategories");
        nCategories.setExpression("{3,3}");

        statePriors = new PortParameter(this, "statePriors");
        statePriors.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        statePriors.setExpression("{0.5,0.5}");
        cardinality = new StringAttribute(statePriors.getPort(), "_cardinal");
        cardinality.setExpression("SOUTH");

        durationProbabilities = new PortParameter(this, "durationProbabilities");
        durationProbabilities.setTypeEquals(BaseType.DOUBLE_MATRIX);
        durationProbabilities.setExpression("[0,1.0;1.0,0]");
        cardinality = new StringAttribute(durationProbabilities.getPort(),
                "_cardinal");
        cardinality.setExpression("SOUTH");

        transitionMatrix = new PortParameter(this, "transitionMatrix");
        transitionMatrix.setTypeEquals(BaseType.DOUBLE_MATRIX);
        transitionMatrix.setExpression("[0.0,1.0;1.0,0.0]");
        cardinality = new StringAttribute(transitionMatrix.getPort(),
                "_cardinal");
        cardinality.setExpression("SOUTH");


        multinomialEstimates = new PortParameter(this, "multinomialEstimates");
        multinomialEstimates.setTypeEquals(BaseType.DOUBLE_MATRIX);  
        multinomialEstimates.setExpression("[0.5,0.5,0.0;0.0,0.0,1.0]");

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(true);

        observation = new TypedIOPort(this, "observation", false, true);
        observation.setTypeEquals(new ArrayType(new ArrayType(BaseType.DOUBLE)));

        hiddenState = new TypedIOPort(this, "hiddenState", false, true);
        hiddenState.setTypeEquals(new ArrayType(BaseType.INT));
        
        windowSize = new PortParameter(this, "windowSize");
        windowSize.setTypeEquals(BaseType.INT);
        windowSize.setExpression("100");
         

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  //// 
    
    /* The user-provided initial guess on the prior probability distribution*/
    public PortParameter durationPriors;

    /* The user-provided initial guess on the prior probability distribution*/
    public PortParameter durationProbabilities;

    /* Transition Probability Matrix */
    public PortParameter transitionMatrix;
    /**
     * Number of categories in the multinomial distribution
     */
    public Parameter nCategories; 
    
    
    public PortParameter statePriors;

    public TypedIOPort trigger;

    public TypedIOPort observation;

    public TypedIOPort hiddenState;
    
    public PortParameter windowSize;


    public PortParameter powerUpperBound;
    public PortParameter powerLowerBound;
    /**
     * A matrix that has a row for each state and a column for each category 
     * in the learned multinomial distribution. if the observations are multidimensional,
     * the categories for each dimension are concatenated into a single vector.
     */
    public PortParameter multinomialEstimates;

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _firstIteration = true;
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == windowSize) {

            _windowSize = ((IntToken)windowSize.getToken()).intValue();
        } else if (attribute == multinomialEstimates) {
           _B = ((DoubleMatrixToken)multinomialEstimates.getToken()).doubleMatrix();
           _nStates = _B.length;
                   
        } else if (attribute == nCategories) {
            Token[] cat = ((ArrayToken) nCategories.getToken()).arrayValue();
            if (cat.length <= 0) {
                throw new IllegalActionException(this,
                        "Number of categories must be positive");
            } else {
                _nCategories = new int[cat.length];  
                for (int i = 0 ; i <cat.length ; i++) {
                    _nCategories[i] = ((IntToken)cat[i]).intValue();
                }
            }
        }  else if (attribute == durationPriors) {
            ArrayToken durationPriorsToken = ((ArrayToken) durationPriors
                    .getToken());
            int maxDuration = durationPriorsToken.length();
            _durationPriors = new double[maxDuration];
            double checksum = 0.0;
            for (int i = 0; i < maxDuration; i++) {
                _durationPriors[i] = ((DoubleToken) durationPriorsToken
                        .getElement(i)).doubleValue();
                checksum += _durationPriors[i];
            }
            if (!SignalProcessing.close(checksum, 1.0)) {
                throw new IllegalActionException(this,
                        "Duration Priors must sum to one. Currently:"
                                + checksum);
            }
        } else if (attribute == durationProbabilities) {
            DoubleMatrixToken durationsToken = ((DoubleMatrixToken) durationProbabilities
                    .getToken());
            int maxDuration = durationsToken.getColumnCount();
            int nStates = durationsToken.getRowCount();
            _D = new double[nStates][maxDuration];
            double[] checkSums = new double[nStates];
            for (int i = 0; i < maxDuration; i++) {
                for (int j = 0; j < nStates; j++) {
                    _D[j][i] = durationsToken.getElementAt(j, i);
                    checkSums[j] += _D[j][i];
                }
            }
            for (int i = 0; i < nStates; i++) {
                if (!SignalProcessing.close(checkSums[i], 1.0)) {
                    throw new IllegalActionException(this,
                            "Duration density of a state ( i.e., each row"
                                    + "of " + durationProbabilities.getName()
                                    + " must sum to one.");
                }
            }
        } else if (attribute == transitionMatrix) {
            DoubleMatrixToken transitionMatrixToken = ((DoubleMatrixToken) transitionMatrix
                    .getToken());
            int m = transitionMatrixToken.getRowCount();
            int n = transitionMatrixToken.getColumnCount();
            _A = new double[m][n];
            double[] checkSums = new double[m];
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    _A[i][j] = transitionMatrixToken.getElementAt(i, j);
                    checkSums[i] += _A[i][j];
                }
            }
            for (int i = 0; i < m; i++) {
                if (!SignalProcessing.close(checkSums[i], 1.0)) {
                    throw new IllegalActionException(this,
                            "Transition Probabilities originating from each state ( i.e., each row"
                                    + "of " + transitionMatrix.getName()
                                    + " must sum to one.");
                }
            }
        } else if (attribute == statePriors) {
            ArrayToken statePriorsToken = ((ArrayToken) statePriors.getToken());
            int nStates = statePriorsToken.length();
            _x0 = new double[nStates];
            for (int i = 0; i < nStates; i++) {
                _x0[i] = ((DoubleToken) statePriorsToken.getElement(i))
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
        statePriors.update();
        powerUpperBound.update();
        powerLowerBound.update();
        multinomialEstimates.update();
        _maxDuration = ((DoubleMatrixToken)durationProbabilities.getToken()).getColumnCount();
        
        if (trigger.hasToken(0)) {
            _windowSize = ((IntToken)trigger.get(0)).intValue();

            Token[] outputObservations = new ArrayToken[_windowSize];
            
            int nStates = ((ArrayToken) statePriors.getToken()).length();  
             
            _nStates = nStates; 
            int _obsDimension = _nCategories.length;
            // start generating values 
            boolean validSequenceFound = false;
            int trials = 0;
            double[][] ys = new double[_windowSize][_obsDimension];
            int [] xs = new int [_windowSize]; 
            while (!validSequenceFound && trials < MAX_TRIALS) { 
                double totalPower = 0.0;
                for (int i = 0; i < _windowSize; i ++ ) {  
                    if (_firstIteration) {
                        // sample hidden state from prior
                        _xt = _sampleHiddenStateFromPrior();
                        _dt = _sampleDurationFromPrior();
                        System.out.println("Duration = " + _dt);
                        _firstIteration = false;
                    }
                    if (_dt < 1) {
                        // if the remaining time at the current state is 1 or less, there needs to be a stata transition.
                        _xt = _propagateState();
                        _dt = _sampleDurationForState();
                    } else {
                        // _xt doesn't change, decrement _dt.
                        _dt--;
                    }
                    double[] yt = _sampleObservation();
                    Token[] yArray = new Token[yt.length];
                    for (int x= 0; x < yArray.length; x++) {
                        yArray[x] = new DoubleToken(yt[x]);
                        totalPower += yt[x];
                    }
                    //System.out.println("y[0] = " + yt[0]);
                    outputObservations[i] = new ArrayToken(yArray);
                        
                    //cumulativePower += yt;
                    xs[i] = _xt;
                    ys[i] = yt;
                }
                if (totalPower <= ((DoubleToken)powerUpperBound.getToken()).doubleValue() &&
                        totalPower >= ((DoubleToken)powerLowerBound.getToken()).doubleValue()) {
                    validSequenceFound = true;
                    break;
                } else {
                    System.out.println("Window Size = " +  _windowSize + " Total Power = " + totalPower);
                    trials++;
                }
            }
            System.out.println(trials + " sequences rejected until sequence found");
            if (validSequenceFound) { 
                Token[] states = new IntToken[_windowSize];
                for (int i = 0; i < _windowSize; i ++) { 
                    states[i] = new IntToken(xs[i]);
                }
                observation.send(0, new ArrayToken(outputObservations));
                hiddenState.send(0, new ArrayToken(states));
            }
        }
    }

    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _firstIteration = true;
    }
    protected double[] _sampleObservation() {
        int obsDimension = _nCategories.length;
        double[] result = new double[obsDimension];
         for (int i = 0; i < obsDimension; i++) {
             result[i] = _sampleSingleDimension(i);  
         } 
         return result;
    }
    private double _sampleSingleDimension(int dim) {
        //FIXME
        double[] cumSums = new double[_nCategories[dim]+1];
        int startIndex = 0;
        for (int i = 0; i < dim; i++) {
            startIndex += _nCategories[i];
        } 
        for (int i = 0; i < _nCategories[dim]; i++) {
            cumSums[i + 1] = cumSums[i] + _B[_xt][startIndex + i];
        }
     // generate a random value ( in theory, within 0 and 1,
        // since this is a probability, however, should be
        // normalized to avoid any numerical errors
        double randomValue = Math.random() * cumSums[cumSums.length-1];
        int bin = Algorithms._binaryIntervalSearch(cumSums, randomValue, 0,
                cumSums.length-1);
        return bin;
    }

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
        return bin;
    }

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

    /**
     * Sample state at this iteration from the state prior.
     * @return The hidden state at this iteration
     */
    protected int _sampleHiddenStateFromPrior() {
        System.out.println("Sampling from prior. "
                + "NumCategories:" + _nCategories.length +
                "\nNumStates:" + _nStates);
        
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

    /* Duration priors - nStates x nDurations*/
    protected double[] _durationPriors;

    /* new duration distribution */
    protected double[][] _D = null;  
    /* maximum duration ( in time steps)c  */
    protected double[] _x0; 
    protected double[][] _A;
    protected double[][] _B;
    protected boolean _firstIteration = true;
    protected int _nStates;
    protected int _maxDuration;
    // Remaining time at the state
    protected int _dt;
    // Current state variable
    protected int _xt;
    protected int _windowSize;
    /**
     * Number of categories
     */
    protected int[] _nCategories;

    protected static int MAX_TRIALS = 10000;

}
