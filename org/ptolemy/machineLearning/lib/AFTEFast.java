/* Auditory Filterbank Temporal Envelope Extraction

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
package org.ptolemy.machineLearning.lib;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ArrayToken;
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
import ptolemy.math.Complex;
import ptolemy.math.ComplexArrayMath;
import ptolemy.math.SignalProcessing;

///////////////////////////////////////////////////////////////////
////FFT

/**
This actor calculates the Auditory Filterbank Temporal Envelope (AFTE) 
features of a given audio signal. 

@author Ilge Akkaya
@version $Id$
@since Ptolemy II 11.0
@Pt.ProposedRating  
@Pt.AcceptedRating  
@see  
 */
public class AFTEFast extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AFTEFast(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        output.setTypeEquals(new ArrayType(new ArrayType(BaseType.DOUBLE)));

        minFrequency = new Parameter(this, "minFrequency");
        minFrequency.setExpression("25");
        minFrequency.setTypeEquals(BaseType.INT);

        maxFrequency = new Parameter(this, "maxFrequency");
        maxFrequency.setExpression("1000");
        maxFrequency.setTypeEquals(BaseType.INT);

        numberOfChannels = new Parameter(this, "numberOfChannels");
        numberOfChannels.setExpression("18");
        numberOfChannels.setTypeEquals(BaseType.INT);

        filterOrder = new Parameter(this, "filterOrder");
        filterOrder.setExpression("4");
        filterOrder.setTypeEquals(BaseType.INT);

        fs = new Parameter(this, "fs");
        fs.setExpression("48000");
        fs.setTypeEquals(BaseType.INT);

        fmodspec = new Parameter(this, "fmodspec");
        fmodspec.setExpression("3000");
        fmodspec.setTypeEquals(BaseType.INT); 

        windowSize = new Parameter(this, "transferSize");
        windowSize.setExpression("16000");
        windowSize.setTypeEquals(BaseType.INT);

        nOverlap  = new Parameter(this, "nOverlap");
        nOverlap.setExpression("3200");
        nOverlap.setTypeEquals(BaseType.INT);

        //input_tokenConsumptionRate.setExpression("transferSize");
        //output_tokenProductionRate.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** 
     * Maximum center frequency in the filterbank
     */ 
    public Parameter maxFrequency;

    /** 
     * Minimum center frequency in the filterbank
     */ 
    public Parameter minFrequency;

    /** 
     * Number of channels of the filterbank output
     */ 
    public Parameter numberOfChannels;

    /**
     * Order of the gammatone filters
     */
    public Parameter filterOrder;

    /** 
     * Input sampling frequency
     */ 
    public Parameter fs;

    /** 
     * Mod spec sampling frequency
     */ 
    public Parameter fmodspec;


    /**
     * Transfer size.
     * 
     */
    public Parameter windowSize;

    /**
     * Number of samples of overlap between adjoining sections.
     */

    public Parameter nOverlap;
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Ensure that the order parameter is positive and recompute the
     *  size of internal buffers.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fs) {
            // Get the size of the FFT transform
            _fs = ((IntToken) fs.getToken()).intValue();

            if (_fs <= 0) {
                throw new IllegalActionException(this, "Sampling frequency was "
                        + _fs + " but must be greater than zero.");
            } 
        } else if (attribute == minFrequency) {
            _fMin = ((IntToken) minFrequency.getToken()).intValue();
        } else if (attribute == maxFrequency) {
            _fMax = ((IntToken) maxFrequency.getToken()).intValue();
        } else if (attribute == numberOfChannels) {
            _numChannels = ((IntToken) numberOfChannels.getToken()).intValue();
        } else if (attribute == fs) {
            _fs = ((IntToken) fs.getToken()).intValue(); 
        } else if (attribute == fmodspec) {

            int fmod = ((IntToken) fmodspec.getToken()).intValue(); 
            if (_fs % fmod != 0) {
                throw new IllegalActionException(this, "Sampling frequency is not an "
                        + "integer multiple of the mod-spec frequency");
            } 
            _fmod = fmod;
        } else if (attribute == filterOrder) {
            _filterOrder = ((IntToken) filterOrder.getToken()).intValue();
            if (_filterOrder <= 0) {
                throw new IllegalActionException(this, "Filter order was "
                        + _filterOrder + " but must be greater than zero.");
            }
        } else if (attribute == windowSize) {
            _windowSize = ((IntToken) windowSize.getToken()).intValue();
        } else if (attribute == nOverlap) {
            int ts = ((IntToken) nOverlap.getToken()).intValue();
            if (_windowSize % ts != 0) {
                throw new IllegalActionException(this, "Window size has to be an"
                        + " integer multiple of nOverlap");
            }
            _transferSize = ts;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Initialize the actor.
     * Sets up impulse responses for gammatone filters 
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize(); 
        _setupGammatoneFilterbank(); 
        _envelopes = new double[_numChannels][_transferSize/(_fs/_fmod)];   
        _filterResult = new double[_numChannels][_transferSize]; 
        _buffering = true;
    }
    /** Consume the inputs and produce the outputs of the FFT filter.
     *  @exception IllegalActionException If a runtime type error occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (_buffering) {
            _inArray = new double[_windowSize];   
            // place first received chunks into _inArray so that we can perform sliding window 
            // convolutions on the first complete array
            Token inTokenArray = input.get(0);
            for (int j = 0; j < _transferSize; j++) {
                _inArray[j+_framePointer*_transferSize] = ((DoubleToken)((ArrayToken) inTokenArray).
                        getElement(j)).doubleValue();
            }
            _framePointer ++; 
        } else {
            // just remove #_nOverlap tokens and update. 
            Token inTokenArray = input.get(0);
            double[] incomingArray = new double[_transferSize];
            for (int i = 0; i < _transferSize; i++) {
                incomingArray[i] = ((DoubleToken)((ArrayToken) inTokenArray).
                        getElement(i)).doubleValue();
            }
            System.arraycopy(_inArray, _transferSize, _inArray, 0, (_windowSize-_transferSize));
            System.arraycopy(incomingArray, 0, _inArray, _transferSize, _transferSize);   
        }
        
        if ( _framePointer >= _windowSize / _transferSize) {
            _buffering = false;
            _framePointer = 0;
        }
        
        if (_buffering) {
            return;
        }

        // multithreaded gammatone filtering and subsampling
        _filterInput();  

        Token[] subsampledEnvelopes = new Token[_numChannels];
        for (int i = 0; i < _numChannels; i++) {
            Token[] envelopei = new Token[_envelopes[i].length];
            for (int j =0 ; j < _envelopes[i].length; j++) {
                envelopei[j] = new DoubleToken(_envelopes[i][j]);
            }
            subsampledEnvelopes[i] = new ArrayToken(envelopei);
        }

        output.send(0, new ArrayToken(subsampledEnvelopes));


    }

    @Override
    public boolean postfire() throws IllegalActionException { 
        _envelopes = new double[_numChannels][_inArray.length/(_fs/_fmod)];   
        _filterResult = new double[_numChannels][_inArray.length]; 
        return super.postfire();
    }


    /**
     * Multithreaded FIR Gammatone filtering 
     * @param input
     * @return
     * @throws IllegalActionException 
     */
    private void _filterInput() throws IllegalActionException {

        ExecutorService executor = Executors.newFixedThreadPool(_numChannels);

        for (int i = 0; i < _numChannels; i++) { 
            executor.execute(new GammatoneFilter(i)); 
        } 

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            throw new IllegalActionException(this, "Could not shut down threadpool in 1 second.");
        } 
    }

    private double[] _hilbertEnvelope(double[] signal) {

        /** x[n] ---DTFT----> X(w)
           u[n] = hilbertTransform(x[n]) ---- DTFT ---> -2*i*sgn[w]*X(w) where sgn is the sign function
         */
        Complex[] fftOut = SignalProcessing.FFTComplexOut(signal); 
        int fftLength = fftOut.length;
        for (int i = 0 ; i < fftOut.length; i++) {
            Complex old = fftOut[i];
            if (i > fftLength/2) {
                fftOut[i] = new Complex(0,0);
            } else { 
                fftOut[i] = new Complex(-2*old.imag, 2*old.real);
            }
        }

        double [] hilbertEnvelope = ComplexArrayMath.
                magnitude(SignalProcessing.IFFT(fftOut));

        return hilbertEnvelope; // need to truncate at some point. 
    }


    private double[] _decimateSignal( 
            double[] timeSignal, 
            int factor) { 
        double[] lowpasstime = SignalProcessing.downsample(timeSignal, factor);
        return lowpasstime; 

    }
    private void _setupGammatoneFilterbank() {
        // compute filterbank center frequencies

        double[] erbLimits = {_hz2erb(_fMin), _hz2erb(_fMax)};

        double[] allFrequencies = new double[_numChannels];

        double freqSpacing = (erbLimits[1] - erbLimits[0])/(_numChannels-1);
        // linearly spaced center frequencies in ERB scale correspond to log-spacing in Hz scale.
        for (int i = 0 ; i < _numChannels; i++) {
            double freqInErb = erbLimits[0] + freqSpacing*i;
            allFrequencies[i] = _erb2hz(freqInErb);
        }



        int a = (int)(0.128*_fs); // minimum 128 ms window
        int nextPowerOf2 = SignalProcessing.order(a);
        int filterLength = (int) Math.pow(2,nextPowerOf2); 
        double tpt = 2*Math.PI/_fs;

        _impulseResponses = new double[_numChannels][filterLength];
        for (int i = 0; i < _numChannels; i++) {
            double cfreq = allFrequencies[i];
            double b = 1.019*24.7*(4.37*cfreq/1000+1); // bandwidth 
            double gain = Math.pow((1.019*b*tpt),_filterOrder)/6; 
            for (int j =0; j < filterLength; j++) {
                double tstep = j*0.1/_fs;
                _impulseResponses[i][j] = gain*Math.pow(_fs,3)*Math.pow(tstep,_filterOrder-1)
                        *Math.exp(-2*Math.PI*b*tstep)*Math.cos(2*Math.PI*cfreq*tstep);
            } 
        }
    }
    /**
     * Convert to Equivalent Rectangular Bandwidth (ERB) scale from Hz
     * @param frequencyInHz Frequency in Hz
     * @return frequency in ERB scale
     */
    private static double _hz2erb(double frequencyInHz) {
        return 21.4*Math.log10(4.37e-3*frequencyInHz+1);
    }

    private static double _erb2hz(double freqInERB) {
        return (Math.pow(10,freqInERB/21.4)-1)/4.37e-3; 
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _buffering;

    private int _filterOrder;

    private int _fs;

    private int _fmod; 

    private int _fMin;

    private int _fMax;

    private int _numChannels;

    private int _frameRate;

    private int _framePointer;

    private int _transferSize;

    private int _windowSize;

    private double[] _inArray;

    private double[][] _impulseResponses;

    private double[][] _filterResult;

    private double[][] _envelopes; 

    private class GammatoneFilter implements Runnable
    { 
        private final int index;
        GammatoneFilter( int index) {
            this.index = index; 
        } 
        @Override
        public void run()
        { 
            double [] h = _impulseResponses[index];
            double [] convResult = SignalProcessing.convolve(_inArray,h);  
            int N = _inArray.length;
            synchronized (_filterResult) {
                _filterResult[index] =  Arrays.copyOfRange(convResult, 0, N);
            }
            synchronized (_envelopes) {
                _envelopes[index] = _decimateSignal(_hilbertEnvelope(
                        Arrays.copyOfRange(convResult, 0, N)), 
                        (int)Math.floor(_fs/_fmod)); 
            }
        }
    }

}

