/* Auditory Filterbank Temporal Envelope Calculator and Feature Exractor

 Copyright (c) 2015 The Regents of the University of California.
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
////AFTEFast

/**
 * This actor calculates the Auditory Filterbank Temporal Envelope (AFTE)
 * features of a given audio signal.
 *
 * @author Ilge Akkaya
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating
 * @Pt.AcceptedRating
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

        centerFrequencies = new Parameter(this, "centerFrequencies");
        centerFrequencies.setTypeEquals(new ArrayType(BaseType.INT));
        centerFrequencies.setExpression("{3220, 5099, 8001}");


        filterOrder = new Parameter(this, "filterOrder");
        filterOrder.setExpression("4");
        filterOrder.setTypeEquals(BaseType.INT);

        fftCoefficientsToOutput= new Parameter(this, "fftCoefficientsToOutput");
        fftCoefficientsToOutput.setTypeEquals(new ArrayType(BaseType.INT));
        fftCoefficientsToOutput.setExpression("{0,4,5,6,7,8,9,10,11,12,13,14}");

        fftLength = new Parameter(this, "fftLength");
        fftLength.setExpression("64");
        fftLength.setTypeEquals(BaseType.INT);

        fs = new Parameter(this, "fs");
        fs.setExpression("48000");
        fs.setTypeEquals(BaseType.INT);

        fmodspec = new Parameter(this, "fmodspec");
        fmodspec.setExpression("100");
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
     * Maximum center frequency in the filterbank.
     */
    public Parameter centerFrequencies;

    /**
     * Order of the gammatone filters.
     */
    public Parameter filterOrder;

    /**
     * Input sampling frequency.
     */
    public Parameter fs;

    /**
     * Indices of FFTCoefficients to output, normalized by dc.
     */
    public Parameter fftCoefficientsToOutput;

    public Parameter fftLength;
    /**
     * Mod spec sampling frequency.
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
        } else if (attribute == centerFrequencies) {
            Token[] cfs = ((ArrayToken)centerFrequencies.getToken()).arrayValue();

            _centerFrequencies = new int[cfs.length];

            for (int i = 0; i< cfs.length; i++) {
                _centerFrequencies[i] = ((IntToken)cfs[i]).intValue();
            }
            _numChannels = cfs.length;
        }  else if (attribute == fs) {
            _fs = ((IntToken) fs.getToken()).intValue();
        } else if (attribute == fftLength) {
            _fftLength = ((IntToken) fftLength.getToken()).intValue();
        } else if (attribute == fftCoefficientsToOutput) {
            Token[] indices = ((ArrayToken) fftCoefficientsToOutput.getToken()).arrayValue();
            _featureVector = new int[indices.length];
            for (int i = 0; i < indices.length; i++) {
                _featureVector[i] = ((IntToken)indices[i]).intValue();
            }
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
        _fftChunks = _windowSize / _transferSize;
        _convolutionLength = _transferSize + _impulseResponses[0].length - 1;

        _inArray = new double[_windowSize];
        _loadFilterFFTs();
        _convolutionChunks = new double[_numChannels][_fftChunks][_convolutionLength];
        _envelopeFFT = new double[_numChannels][_fftLength];
    }
    /** Consume the inputs and produce the outputs of the FFT filter.
     *  @exception IllegalActionException If a runtime type error occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _inArray = new double[_transferSize];
        Token inTokenArray = input.get(0);
        for (int j = 0; j < _transferSize; j++) {
            _inArray[j] = ((DoubleToken)((ArrayToken) inTokenArray).
                    getElement(j)).doubleValue();
        }

        // do the fft conv for the received chunk
        doBlockFFT(_framePointer);

        _framePointer++;

        // the following will be done for all frames after the bufering is complete.
        if (_framePointer >= _fftChunks) {

            // multithreaded gammatone filtering and subsampling
            multithreadedFeatureExtraction();

            _framePointer = _fftChunks-1;

            Token[] envFFTs = new Token[_numChannels];
            for (int i = 0; i < _numChannels; i++) {
                Token[] ffti = new Token[_featureVector.length];
                double DC = _envelopeFFT[i][0];
                for (int j =0 ; j < _featureVector.length; j++) {
                    int index = _featureVector[j];
                    if (index == 0) {
                        ffti[j] = new DoubleToken(_envelopeFFT[i][index]);
                    } else {
                        ffti[j] = new DoubleToken(_envelopeFFT[i][index]/DC);
                    }

                }
                envFFTs[i] = new ArrayToken(ffti);
            }

            output.send(0, new ArrayToken(envFFTs));
        }
    }

    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _framePointer = 0;
    }

    /**
     * Multithreaded FIR Gammatone filtering.
     * @param input
     * @return
     * @exception IllegalActionException
     */
    private void multithreadedFeatureExtraction() throws IllegalActionException {

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

    private double[] _firEnvelope(double[] signal) {
        double[] rectified = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            rectified[i] = Math.abs(signal[i]);
        }
        double[] res = SignalProcessing.convolve(rectified, env_fft);
        return Arrays.copyOfRange(res, env_fft.length/2, signal.length+env_fft.length/2);
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

        // removing zero padding.
        return Arrays.copyOfRange(hilbertEnvelope,0,signal.length);
    }



    private double[] _decimateSignal(
            double[] timeSignal,
            int factor) {
        double[] lowpasstime = SignalProcessing.downsample(timeSignal, factor);
        return lowpasstime;

    }
    private void _setupGammatoneFilterbank() {
        // compute filterbank center frequencies

        int a = (int)(0.128*_fs); // minimum 128 ms window
        int nextPowerOf2 = SignalProcessing.order(a);
        int filterLength = (int) Math.pow(2,nextPowerOf2);
        double tpt = 2*Math.PI/_fs;

        _impulseResponses = new double[_numChannels][filterLength];
        for (int i = 0; i < _numChannels; i++) {
            double cfreq = _centerFrequencies[i];
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
     * Overlap and output when frame ready to go.
     * TODO: Don't need to do this triple sum every time we will output.
     * We will already have summed up the previous 5, so substract the first channle
     * and add in the new one at the end.
     */
    private void overlapAndAdd(int filterIndex) {
        synchronized (_convolutionChunks[filterIndex]) {
            _filterResult[filterIndex] = new double[_windowSize];
            for ( int j = 0; j<_fftChunks; j++) {
                int maxIndex = Math.min (_windowSize - j*_transferSize, _convolutionLength );
                for (int k = 0 ; k < maxIndex ; k++) {
                    _filterResult[filterIndex][j*_transferSize + k] += _convolutionChunks[filterIndex][j][k];
                }
            }
        }
    }
    private void shiftChunks(int filterIndex) {

        synchronized (_convolutionChunks[filterIndex]) {
            for ( int j=1; j<_fftChunks; j++) {
                _convolutionChunks[filterIndex][j-1] = _convolutionChunks[filterIndex][j];
            }
        }
    }

    /**
<<<<<<< .mine
     * Do FFTs on all channels for this block and save to the array
     * @param startIndex Chunk Index
     * @exception IllegalActionException
=======
     * Convert to Equivalent Rectangular Bandwidth (ERB) scale from Hz.
     * @param frequencyInHz Frequency in Hz
     * @return frequency in ERB scale
>>>>>>> .r73083
     */
    private void doBlockFFT(int startIndex) throws IllegalActionException {
        // can be done in parallel for multiple filters
        ExecutorService executor = Executors.newFixedThreadPool(_numChannels);
        for (int i = 0; i < _numChannels; i++) {
            executor.execute(new FFTWorker(i, startIndex));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            throw new IllegalActionException(this, "Could not shut down threadpool in 1 second.");
        }
    }

    private double[] blockFFT(double[] chunk, int filterIndex) {
        int fftOrder = SignalProcessing.order(SignalProcessing.nextPowerOfTwo(_convolutionLength));
        Complex[] inputFFT = SignalProcessing.FFTComplexOut(chunk, fftOrder);
        Complex[] result = ComplexArrayMath.multiply(inputFFT, _gammatoneImpulseResponseFFTs[filterIndex]);
        return Arrays.copyOfRange(SignalProcessing.IFFTRealOut(result),0,_convolutionLength);
        // place convolution result into the appropriate segment of result.
    }
    /**
     * Compute the FFTs of the impulse responses and save.
     * Do this once in initialization.
     */
    private void _loadFilterFFTs() {
        _gammatoneImpulseResponseFFTs = new Complex[_numChannels]
                [SignalProcessing.nextPowerOfTwo(_convolutionLength)];
        for (int i=0; i < _numChannels; i++) {
            _gammatoneImpulseResponseFFTs[i] =
                    SignalProcessing.FFTComplexOut(_impulseResponses[i], SignalProcessing.order(_convolutionLength));
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Number of FFT chunks per window that are used for overlap-and-add convolution.
     */
    private int _fftChunks;

    /**
     * Gammatone filter order.
     */
    private int _filterOrder;

    /**
     * Sampling frequency.
     */
    private int _fs;

    /**
     * Modulation band sampling frequency.
     */
    private int _fmod;

    /**
     * Gammatone filterbank center frequencies.
     */
    private int[] _centerFrequencies;

    /**
     * Number of channels in the Gammatone Filterbank.
     */
    private int _numChannels;

    /**
     * Convolution length of gammatone filtering.
     */
    private int _convolutionLength;

    /**
     * Convolution chunk frame index.
     */
    private int _framePointer;

    /**
     * Input transfer size.
     */
    private int _transferSize;

    /**
     * Processing window size ( a window consists of incoming frame padded with buffered past input tokens).
     */
    private int _windowSize;

    /**
     * Incoming frame tokens.
     */
    private double[] _inArray;

    /**
     * Overlap-and-add storage.
     */
    private double[][][] _convolutionChunks; //indexed by filter index, chunk index;

    /**
     * Gammatone filterbank impulse responses.
     */
    private double[][] _impulseResponses;

    /**
     * Result of gammatone filtering.
     */
    private double[][] _filterResult;

    /**
     * Modulation band envelopes of Gammatone filter outputs.
     */
    private double[][] _envelopes;

    /**
     * FFT indices to be included at the output feature vector per frame.
     */
    private int[] _featureVector;

    /**
     * FFTs of the envelopes.
     */
    private double[][] _envelopeFFT;

    /**
     * FFT Length.
     */
    private int _fftLength = 64;

    /**
     * Gammatone impulse response FFTs for overlap-and-add convolution.
     */
    private Complex[][] _gammatoneImpulseResponseFFTs;

    /** 256-tap low-pass FIR filter with a transition band around 0.001
    this corresponds to a cut-off frequency of 50 Hz for fs = 48 kHz. */
    private final double[] env_fft = {0.0009237,0.00092406,0.00092442,0.00092477,0.00092512,0.00092547,0.00092582,0.00092616,0.0009265,0.00092684,0.00092717,0.0009275,0.00092783,0.00092816,0.00092848,0.0009288,0.00092912,0.00092944,0.00092975,0.00093006,0.00093037,0.00093067,0.00093097,0.00093127,0.00093157,0.00093186,0.00093215,0.00093244,0.00093273,0.00093301,0.00093329,0.00093356,0.00093384,0.00093411,0.00093438,0.00093464,0.00093491,0.00093517,0.00093542,0.00093568,0.00093593,0.00093618,0.00093642,0.00093667,0.00093691,0.00093714,0.00093738,0.00093761,0.00093784,0.00093807,0.00093829,0.00093851,0.00093873,0.00093894,0.00093916,0.00093937,0.00093957,0.00093978,0.00093998,0.00094017,0.00094037,0.00094056,0.00094075,0.00094094,0.00094112,0.0009413,0.00094148,0.00094166,0.00094183,0.000942,0.00094217,0.00094233,0.00094249,0.00094265,0.00094281,0.00094296,0.00094311,0.00094326,0.0009434,0.00094354,0.00094368,0.00094382,0.00094395,0.00094408,0.00094421,0.00094433,0.00094445,0.00094457,0.00094469,0.0009448,0.00094491,0.00094502,0.00094512,0.00094522,0.00094532,0.00094542,0.00094551,0.0009456,0.00094569,0.00094577,0.00094585,0.00094593,0.00094601,0.00094608,0.00094615,0.00094622,0.00094628,0.00094634,0.0009464,0.00094646,0.00094651,0.00094656,0.00094661,0.00094665,0.00094669,0.00094673,0.00094677,0.0009468,0.00094683,0.00094686,0.00094688,0.00094691,0.00094692,0.00094694,0.00094695,0.00094696,0.00094697,0.00094697,0.00094698,0.00094697,0.00094697,0.00094696,0.00094695,0.00094694,0.00094692,0.00094691,0.00094688,0.00094686,0.00094683,0.0009468,0.00094677,0.00094673,0.00094669,0.00094665,0.00094661,0.00094656,0.00094651,0.00094646,0.0009464,0.00094634,0.00094628,0.00094622,0.00094615,0.00094608,0.00094601,0.00094593,0.00094585,0.00094577,0.00094569,0.0009456,0.00094551,0.00094542,0.00094532,0.00094522,0.00094512,0.00094502,0.00094491,0.0009448,0.00094469,0.00094457,0.00094445,0.00094433,0.00094421,0.00094408,0.00094395,0.00094382,0.00094368,0.00094354,0.0009434,0.00094326,0.00094311,0.00094296,0.00094281,0.00094265,0.00094249,0.00094233,0.00094217,0.000942,0.00094183,0.00094166,0.00094148,0.0009413,0.00094112,0.00094094,0.00094075,0.00094056,0.00094037,0.00094017,0.00093998,0.00093978,0.00093957,0.00093937,0.00093916,0.00093894,0.00093873,0.00093851,0.00093829,0.00093807,0.00093784,0.00093761,0.00093738,0.00093714,0.00093691,0.00093667,0.00093642,0.00093618,0.00093593,0.00093568,0.00093542,0.00093517,0.00093491,0.00093464,0.00093438,0.00093411,0.00093384,0.00093356,0.00093329,0.00093301,0.00093273,0.00093244,0.00093215,0.00093186,0.00093157,0.00093127,0.00093097,0.00093067,0.00093037,0.00093006,0.00092975,0.00092944,0.00092912,0.0009288,0.00092848,0.00092816,0.00092783,0.0009275,0.00092717,0.00092684,0.0009265,0.00092616,0.00092582,0.00092547,0.00092512,0.00092477,0.00092442,0.00092406,0.0009237};

    /**
     * Private class that implements GammatoneFiltering threads. These are done in parallel
     * for a given input frame.
     * @author ilgea
     *
     */
    private class GammatoneFilter implements Runnable
    {
        /** The index of the filter in the Gammatone filterbank. */
        private final int filterIndex;
        GammatoneFilter( int index) {
            this.filterIndex = index;
        }
        @Override
        public void run()
        {
            overlapAndAdd(filterIndex);
            shiftChunks(filterIndex);
            synchronized (_envelopes[filterIndex]) {
                // extract hilbert envelope of signal
                // low-pass filter to 50 Hz ( done after Hilbert transform to avoid ringing artifacts)
                // decimate signal down to fs=100 Hz
                _envelopes[filterIndex] = _decimateSignal(
                        _firEnvelope(
                                _hilbertEnvelope(
                        _filterResult[filterIndex])), (int)Math.floor(_fs/_fmod));
            }
            synchronized (_envelopeFFT[filterIndex]) {
                // obtain FFT of the decimated signal and return magnitude.
                _envelopeFFT[filterIndex] = ComplexArrayMath.magnitude(
                        SignalProcessing.FFTComplexOut(_envelopes[filterIndex],
                                SignalProcessing.order(_fftLength)));

            }
        }
    }

    private class FFTWorker implements Runnable
    {
        private final int filterIndex;
        private final int blockIndex;
        FFTWorker( int filterIndex, int blockIndex) {
            this.filterIndex = filterIndex;
            this.blockIndex = blockIndex;
        }
        @Override
        public void run()
        {
            synchronized ( _convolutionChunks[filterIndex][blockIndex]) {
                _convolutionChunks[filterIndex][blockIndex] =
                        blockFFT(_inArray, filterIndex);
            }
        }
    }

}

