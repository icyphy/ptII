/* HSMMGenerator.java

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.math.SignalProcessing;
 
/**
<p> An abstract class that implements a Bayesian network used to simulate
an Explicit-Duration Hidden Markov Model.  
 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public abstract class HSMMGenerator extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HSMMGenerator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        durationPriors = new PortParameter(this, "durationPriors");
        durationPriors.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        durationPriors.setExpression("{1.0,0.0}");
        new StringAttribute(durationPriors.getPort(), "_cardinal")
        .setExpression("SOUTH");
        new SingletonParameter(durationPriors.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        statePriors = new PortParameter(this, "statePriors");
        statePriors.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        statePriors.setExpression("{0.5,0.5}");
        new StringAttribute(statePriors.getPort(), "_cardinal")
            .setExpression("SOUTH");
        new SingletonParameter(statePriors.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);
        
        durationProbabilities = new PortParameter(this, "durationProbabilities");
        durationProbabilities.setTypeEquals(BaseType.DOUBLE_MATRIX);
        durationProbabilities.setExpression("[0,1.0;1.0,0]");
        new StringAttribute(durationProbabilities.getPort(), "_cardinal")
        .setExpression("SOUTH");
        new SingletonParameter(durationProbabilities.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        transitionMatrix = new PortParameter(this, "transitionMatrix");
        transitionMatrix.setTypeEquals(BaseType.DOUBLE_MATRIX);
        transitionMatrix.setExpression("[0.0,1.0;1.0,0.0]");
        new StringAttribute(transitionMatrix.getPort(), "_cardinal")
        .setExpression("SOUTH");
        new SingletonParameter(transitionMatrix.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(true);
        new SingletonParameter(trigger, "_showName")
        .setToken(BooleanToken.TRUE);
        
        observation = new TypedIOPort(this, "observation", false, true);
        observation.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        new SingletonParameter(observation, "_showName")
        .setToken(BooleanToken.TRUE);
        
        hiddenState = new TypedIOPort(this, "hiddenState", false, true);
        hiddenState.setTypeEquals(new ArrayType(BaseType.INT));
        new SingletonParameter(hiddenState, "_showName")
        .setToken(BooleanToken.TRUE);
        
        windowSize = new Parameter(this,"windowSize");
        windowSize.setTypeEquals((BaseType.INT));
        windowSize.setExpression("100");

        powerUpperBound = new PortParameter(this, "powerUpperBound");
        powerUpperBound.setTypeEquals(BaseType.DOUBLE);
        powerUpperBound.setExpression("100.0");
        new SingletonParameter(powerUpperBound.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The user-provided initial guess on the prior probability distribution.*/
    public PortParameter durationPriors;

    /** The user-provided initial guess on the prior probability distribution.*/
    public PortParameter durationProbabilities;

    /** Transition Probability matrix estimate. */
    public PortParameter transitionMatrix;
 
    /** Prior state distribution. */
    public PortParameter statePriors;

    /** Trigger generation. */
    public TypedIOPort trigger;

    /** Output port for observation array. */
    public TypedIOPort observation;

    /** Output port for corresponding state sequence. */
    public TypedIOPort hiddenState;

    /** Generation window size. */
    public Parameter windowSize; 

    /** Power limit on generation window. */
    public PortParameter powerUpperBound;

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _firstIteration = true;
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == durationPriors) {
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
        } else if (attribute == windowSize) {
            IntToken wx = ((IntToken) windowSize.getToken()); 
            _windowSize = wx.intValue(); 
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

    /**
     * Sample an observation value given current state.
     * @return Observation
     */
    protected abstract double[] _sampleObservation();

    /**
     * Sample a duration value given current state.
     * @return Duration
     */
    protected abstract int _sampleDurationForState();

    /** 
     * Sample next state given current state.
     * @return Next state index.
     * @throws IllegalActionException 
     */
    protected abstract int _propagateState() throws IllegalActionException; 
    
    /**
     * Sample state at this iteration from the state prior.
     * @return The hidden state at this iteration
     */
    protected abstract int _sampleHiddenStateFromPrior();
     
    /**
     * Sample duration value from prior duration distribution.
     * @return sampled duration value
     */
    protected abstract int _sampleDurationFromPrior(); 
    
    /** Duration priors: an nStates-by-nDurations matrix. */
    protected double[] _durationPriors;

    /** new duration distribution. */
    protected double[][] D_new = null;
    /** initial duration distribution. */
    protected double[][] _D0 = null;
    /** current duration distribution. */
    protected double[][] _D = null;
    /** maximum duration ( in time steps).  */
    protected double[] _x0;
     
    /** Cached transition matrix value. */
    protected double[][] _A;
    
    /** Boolean indicating the first iteration of the generator. */
    protected boolean _firstIteration = true;
    
    /** Cardinality of state space. */
    protected int _nStates;
    
    /** Cardinality of the support of duration distribution. */
    protected int _maxDuration;
    /** Duration variable: d(t). */
    protected int _dt;
    
    /** State variable: x(t). */
    protected int _xt;
    
    /** Generation window size. */
    protected int _windowSize;
    
    /** In the presence of window-based constraints, number of runs allowable
     * until valid trace found.
     */
    protected static int MAX_TRIALS = 100000;

}
