/* Auditory Filterbank Temporal Envelope Extraction

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
////FFT

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
    public boolean postfire() throws IllegalActionException { 
        _envelopes = new double[_numChannels][_windowSize/(_fs/_fmod)];   
        _filterResult = new double[_numChannels][_windowSize]; 
        return super.postfire();
    }


    /**
     * Multithreaded FIR Gammatone filtering.
     * @param input
     * @return
     * @throws IllegalActionException 
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
        return Arrays.copyOfRange(res, 0, signal.length);
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
     * @throws IllegalActionException
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

    private int _fftChunks;

    private int _filterOrder;

    private int _fs;

    private int _fmod; 

    private int[] _centerFrequencies;

    private int _numChannels;

    private int _convolutionLength;

    private int _framePointer;

    private int _transferSize;

    private int _windowSize;

    private double[] _inArray;

    private double[][][] _convolutionChunks; //indexed by filter index, chunk index;

    private double[][] _impulseResponses;

    private double[][] _filterResult;

    private double[][] _envelopes; 

    private int[] _featureVector;

    private double[][] _envelopeFFT;

    private int _fftLength = 64;

    private Complex[][] _gammatoneImpulseResponseFFTs;

    // 256-tap low-passFIR filter with a transition band of 0.0018-0.0022
    // this corresponds to a cut-off frequency of 100 Hz for fs = 48 kHz.
    private final double[] env_fft = {0.00177,0.0017731,0.0017762,
            0.0017792,0.0017823,0.0017853,0.0017883,0.0017912,0.0017942,0.0017971,0.0018,0.0018029,0.0018058,0.0018086,0.0018114,0.0018142,0.001817,0.0018198,0.0018225,0.0018252,0.0018279,0.0018306,0.0018332,0.0018358,0.0018384,0.001841,0.0018436,0.0018461,0.0018486,0.0018511,0.0018535,0.001856,0.0018584,0.0018608,0.0018631,0.0018655,0.0018678,0.0018701,0.0018724,0.0018746,0.0018769,0.0018791,0.0018812,0.0018834,0.0018855,0.0018876,0.0018897,0.0018918,0.0018938,0.0018958,0.0018978,0.0018998,0.0019017,0.0019036,0.0019055,0.0019074,0.0019092,0.001911,0.0019128,0.0019146,0.0019163,0.001918,0.0019197,0.0019214,0.0019231,0.0019247,0.0019263,0.0019278,0.0019294,0.0019309,0.0019324,0.0019339,0.0019353,0.0019367,0.0019381,0.0019395,0.0019408,0.0019421,0.0019434,0.0019447,0.0019459,0.0019472,0.0019484,0.0019495,0.0019507,0.0019518,0.0019529,0.0019539,0.001955,0.001956,0.001957,0.0019579,0.0019589,0.0019598,0.0019607,0.0019615,0.0019623,0.0019632,0.0019639,0.0019647,0.0019654,0.0019661,0.0019668,0.0019674,0.0019681,0.0019687,0.0019692,0.0019698,0.0019703,0.0019708,0.0019713,0.0019717,0.0019721,0.0019725,0.0019729,0.0019732,0.0019735,0.0019738,0.0019741,0.0019743,0.0019745,0.0019747,0.0019749,0.001975,0.0019751,0.0019752,0.0019752,0.0019753,0.0019753,0.0019752,0.0019752,0.0019751,0.001975,0.0019749,0.0019747,0.0019745,0.0019743,0.0019741,0.0019738,0.0019735,0.0019732,0.0019729,0.0019725,0.0019721,0.0019717,0.0019713,0.0019708,0.0019703,0.0019698,0.0019692,0.0019687,0.0019681,0.0019674,0.0019668,0.0019661,0.0019654,0.0019647,0.0019639,0.0019632,0.0019623,0.0019615,0.0019607,0.0019598,0.0019589,0.0019579,0.001957,0.001956,0.001955,0.0019539,0.0019529,0.0019518,0.0019507,0.0019495,0.0019484,0.0019472,0.0019459,0.0019447,0.0019434,0.0019421,0.0019408,0.0019395,0.0019381,0.0019367,0.0019353,0.0019339,0.0019324,0.0019309,0.0019294,0.0019278,0.0019263,0.0019247,0.0019231,0.0019214,0.0019197,0.001918,0.0019163,0.0019146,0.0019128,0.001911,0.0019092,0.0019074,0.0019055,0.0019036,0.0019017,0.0018998,0.0018978,0.0018958,0.0018938,0.0018918,0.0018897,0.0018876,0.0018855,0.0018834,0.0018812,0.0018791,0.0018769,0.0018746,0.0018724,0.0018701,0.0018678,0.0018655,0.0018631,0.0018608,0.0018584,0.001856,0.0018535,0.0018511,0.0018486,0.0018461,0.0018436,0.001841,0.0018384,0.0018358,0.0018332,0.0018306,0.0018279,0.0018252,0.0018225,0.0018198,0.001817,0.0018142,0.0018114,0.0018086,0.0018058,0.0018029,0.0018,0.0017971,0.0017942,0.0017912,0.0017883,0.0017853,0.0017823,0.0017792,0.0017762,0.0017731,0.00177};


    private class GammatoneFilter implements Runnable
    { 
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
                _envelopes[filterIndex] = _decimateSignal(_firEnvelope(
                        _filterResult[filterIndex]), (int)Math.floor(_fs/_fmod)); 
            }
            synchronized (_envelopeFFT[filterIndex]) {
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

