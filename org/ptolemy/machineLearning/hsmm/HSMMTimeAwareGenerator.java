/**Time aware EDHMM generator.

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


import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.ptolemy.machineLearning.Algorithms;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DateToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
///////////////////////////////////////////////////////////////////
////HSMMTimeAwareGenerator.java
import ptolemy.kernel.util.Workspace;

/**
<p>This actor implements an Explicit-Duration Hidden-Markov Model (EDHMM) and executes this
model to generates traces. This subclass assumes that the state transitions of the EDHMM
are characterized by a set of transition matrices, each active for a certain time interval.
By default, the actor expects to receive a set of hourly specifications for the transition
matrix, and a sampling period with which to produce observations. 
</p> 


@author Ilge Akkaya
@version $Id$ 
@since Ptolemy II 10.0
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
 */
public class HSMMTimeAwareGenerator extends HSMMGeneratorMultinomialEmissions {
    /**
     * Constructs a HSMMTimeAwareGenerator object.
     *
     * @param container  a CompositeEntity object
     * @param name       a String ...
     * @throws NameDuplicationException ...
     * @throws IllegalActionException ...
     */
    public HSMMTimeAwareGenerator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name); 
        At = new PortParameter(this,"At");
        At.setExpression("{[1.0]}");
        samplingPeriod = new Parameter(this, "samplingPeriod");
        samplingPeriod.setExpression("6"); 
    }


    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == At) {
            ArrayToken spec = ((ArrayToken)At.getToken());
            _At = new double[spec.length()][][];
            for (int i =0 ; i < spec.length(); i++) {
                double[][] A = ((DoubleMatrixToken)spec.getElement(i)).doubleMatrix();
                _At[i] = A;
            }
        } else if (attribute == samplingPeriod) {
            _Tsampling = ((IntToken)samplingPeriod.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** An array of transition matrices. **/
    public PortParameter At;

    /** Sampling period in seconds. */
    public Parameter samplingPeriod;

    
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HSMMTimeAwareGenerator newObject = (HSMMTimeAwareGenerator) super
                .clone(workspace);
        newObject._At = null;
        newObject._ta = null;
        
        return newObject;
    }
    
    @Override
    public void fire() throws IllegalActionException { 
 
        durationPriors.update();
        durationProbabilities.update();
        transitionMatrix.update(); 
        statePriors.update();
        powerUpperBound.update();
        powerLowerBound.update();
        multinomialEstimates.update();
        At.update();
        _maxDuration = ((DoubleMatrixToken)durationProbabilities.getToken()).getColumnCount();
 
        if (trigger.hasToken(0)) {
            long ts0 = ((IntToken) trigger.get(0)).intValue();  
            _ta = new TimedAutomaton(new DateToken(ts0,
                    DateToken.PRECISION_SECOND,tz),
                    _Tsampling);

            Token[] outputObservations = new ArrayToken[_windowSize];

            int nStates = ((ArrayToken) statePriors.getToken()).length();  

            _nStates = nStates; 
            int _obsDimension = _nCategories.length;
            // start generating values 
            boolean validSequenceFound = false;
            int trials = 0;
            double[][] ys = new double[_windowSize][_obsDimension];
            int [] xs = new int [_windowSize]; 

            while (!validSequenceFound && trials < MAX_TRIALS && !_stopRequested) { 
                double totalPower = 0.0;
                // set to true if we're forcing an inner loop to 
                // end because a deadlock has been detected
                boolean _failFast = false; 

                for (int i = 0; i < _windowSize; i ++ ) {  

                    if (_firstIteration) {
                        // sample hidden state from prior
                        _xt = _sampleHiddenStateFromPrior();
                        _dt = _sampleDurationFromPrior(); 
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
                    yt = _sampleObservation();   

                    Token[] yArray = new Token[yt.length];
                    for (int x= 0; x < yArray.length; x++) {
                        yArray[x] = new DoubleToken(yt[x]);
                        totalPower += yt[x];  
                    } 
                    if (totalPower >= ((DoubleToken)powerUpperBound.getToken()).doubleValue()) {
                        _failFast = true;
                        continue;
                    }
                    outputObservations[i] = new ArrayToken(yArray);

                    xs[i] = _xt;
                    ys[i] = yt;
                    _ta.advanceTime();
                }

                if (totalPower <= ((DoubleToken)powerUpperBound.getToken()).doubleValue() 
                        && totalPower >= ((DoubleToken)powerLowerBound.getToken()).doubleValue() 
                        && !_failFast) {
                    validSequenceFound = true;
                    break;
                } else { 
                    trials++;
                }
            }

            if (validSequenceFound) {  
                Token[] states = new IntToken[_windowSize];
                for (int i = 0; i < _windowSize; i ++) { 
                    states[i] = new IntToken(xs[i]);
                }
                System.out.println( (trials) + " samples rejected.");
                observation.send(0, new ArrayToken(outputObservations));
                hiddenState.send(0, new ArrayToken(states)); 
            } else {
                // Send out all zeros.
                Token[] states = new IntToken[_windowSize];
                for (int i = 0; i < _windowSize; i ++) { 
                    states[i] = new IntToken(0);
                    Token[] yArray = new Token[_nCategories.length];
                    for (int x= 0; x < _nCategories.length; x++) {
                        yArray[x] = new DoubleToken(0.0); 
                    }  
                    outputObservations[i] = new ArrayToken(yArray);
                }
                observation.send(0, new ArrayToken(outputObservations));
                hiddenState.send(0, new ArrayToken(states)); 
            }
        }
    }

    @Override
    protected int _propagateState() throws IllegalActionException {
        // use a time-dependent transition probability matrix.
        if (_ta.getState() > (_At.length-1) ) {
            throw new IllegalActionException(this, "The timed automaton is set "
                    + "to produce state " + _ta.getState() + " but At unknown for this state.");
        }
        _A = _At[_ta.getState()];
        double[] cumSums = new double[_nStates + 1];
        for (int i = 0; i < _nStates; i++) {
            cumSums[i + 1] = cumSums[i] + _A[_xt][i];
        } 
        double randomValue = Math.random() * cumSums[_nStates];
        int bin = Algorithms._binaryIntervalSearch(cumSums, randomValue, 0,
                _nStates);
        return bin;
    } 

    /**
     * A simple timed automaton that runs synchronously with the generator to
     * keep track of the timeline on which symbols are being generated.
     * @author ilgea
     *
     */
    private static class TimedAutomaton {
        private TimedAutomaton(DateToken timestamp, long step) {
            this._step = step;
            this._currentTime = timestamp;
        }
        private int getState() {
            return _currentTime.getHour();
        }
        private void advanceTime() throws IllegalActionException {
            _currentTime = new DateToken(this._currentTime.getTimeInMilliseconds()/1000 
                    + _step,DateToken.PRECISION_SECOND,tz);
        }  
        /* Time step of the automaton. */
        private final long _step;
        /* Current time variable of the automaton. */
        private DateToken _currentTime;
    } 

    /** sampling period in seconds. */
    private int _Tsampling;
    
    /** an array of time-dependent transition matrices. */
    private double[][][] _At;
    
    /** A timed automaton that is synchronously composed with the EDHMM. */
    private TimedAutomaton _ta; 

    /** Default time zone. */
    private static TimeZone tz = new SimpleTimeZone(0,"GMT");
} 

