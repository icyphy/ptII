package org.ptolemy.machineLearning.hsmm;


import org.ptolemy.machineLearning.Algorithms;

import ptolemy.actor.TypedIOPort;
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

public class HSMMTimeAwareGenerator extends HSMMGeneratorMultinomialEmissions {

    public HSMMTimeAwareGenerator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        timestamp = new TypedIOPort(this, "timestamp", true, false);
        At = new PortParameter(this,"specArray");
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
    /** Timestamp at which the first sample should be generated */
    public TypedIOPort timestamp;
    
    /** An array of transition matrices **/
    public PortParameter At;
    
    /** Sampling period in seconds */
    public Parameter samplingPeriod;

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

        if (trigger.hasToken(0) && timestamp.hasToken(0)) {
            _windowSize = ((IntToken)trigger.get(0)).intValue();
            long ts0 = ((IntToken) timestamp.get(0)).intValue(); 
            System.out.println("Timestamp = " + ts0);
            _ta = new TimedAutomaton(new DateToken(ts0,
                    DateToken.PRECISION_SECOND), _Tsampling);

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
             // set to true if we're forcing an inner loop to end because we're in a deadlock.
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
                            
                    //System.out.println("Time spec satisfied at time stamp " +_ta.getCurrentTime());
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
                System.out.println("Sending out observations after " + trials + " trials.");
                observation.send(0, new ArrayToken(outputObservations));
                hiddenState.send(0, new ArrayToken(states)); 
            } else {
                System.out.println("No feasible sample sequence found.");
            }
        }
    }
    
    @Override
    protected int _propagateState() {
        // use a time-dependent transition probability matrix.
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
    
    private class TimedAutomaton {
        private TimedAutomaton(DateToken timestamp, long step) {
            this._step = step;
            this._currentTime = timestamp;
        }
        private int getState() {
            return _currentTime.getHour();
        }
        private void advanceTime() throws IllegalActionException {
            _currentTime = new DateToken(this._currentTime.getTimeInMilliseconds()/1000 
                    + _step,DateToken.PRECISION_SECOND);
        }  
        private final long _step;
        private DateToken _currentTime;
    } 
    
    private int _Tsampling;
    // time-dependent transition matrix.
    private double[][][] _At;
   private TimedAutomaton _ta;
} 

