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
import ptolemy.data.BooleanToken;
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
import ptolemy.math.DoubleArrayMath;
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
public class AFTEDiscriminant extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AFTEDiscriminant(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        output.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        centerFrequencies = new Parameter(this, "centerFrequencies");
        centerFrequencies.setTypeEquals(new ArrayType(BaseType.INT));
        centerFrequencies.setExpression("{2000}");

        outputFeatureIndices = new Parameter(this,"outputFeatureIndices");
        outputFeatureIndices.setExpression("{0}");
        outputFeatureIndices.setTypeEquals(new ArrayType(BaseType.INT));

        normalize = new Parameter(this,"normalize");
        normalize.setTypeEquals(BaseType.BOOLEAN);
        normalize.setExpression("true");

        normalizeByDc = new Parameter(this,"normalizeByDc");
        normalizeByDc.setTypeEquals(BaseType.BOOLEAN);
        normalizeByDc.setExpression("true");

        useEnergyBands = new Parameter(this,"useEnergyBands");
        useEnergyBands.setTypeEquals(BaseType.BOOLEAN);
        useEnergyBands.setExpression("false");

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


        _fftLength = 64;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * The array of filterbank center frequencies.
     */
    public Parameter centerFrequencies;

    /**
     * FFT Feature indices to be output.
     */
    public Parameter outputFeatureIndices;

    /**
     * Boolean option to specify whether the output feature array should be normalized to have an L-2 norm of 1.
     */
    public Parameter normalize;

    /**
     * Boolean option to specify whether the selected output features should be normalized by the DC term. If set
     * to true together with normalize, this option is applied first.
     */
    public Parameter normalizeByDc;

    /**
     * The output features become log-sum of energies within certain FFT bands. Currently, the bin width is set t
     * 4 FFT samples per bin, and the FFT length is set to 64. Only computed for the nonnegative frequencies and DC term
     * is output as a separate feature. In the current setting, 9 features per channel is output.
     */
    public Parameter useEnergyBands;
    /**
     * Input sampling frequency.
     */
    public Parameter fs;

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
        if (attribute == useEnergyBands) {
            _energyFeatures = ((BooleanToken) useEnergyBands.getToken()).booleanValue();
        } else if (attribute == fs) {
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
        } else if (attribute == outputFeatureIndices) {
            Token[] indices = ((ArrayToken)outputFeatureIndices.getToken()).arrayValue();

            _featureIndices = new int[indices.length];

            for (int i = 0; i< indices.length; i++) {
                _featureIndices[i] = ((IntToken)indices[i]).intValue();
            }
        } else if (attribute == fs) {
            _fs = ((IntToken) fs.getToken()).intValue();
        } else if (attribute == fmodspec) {
            _fmod = ((IntToken) fmodspec.getToken()).intValue();
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
        _initializeGammatoneFilterbankTaps();
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
            boolean normalizeDC = ((BooleanToken)normalizeByDc.getToken()).booleanValue();
            if (_energyFeatures) {
              //right now, using a feature for DC and binds of width, 6.25Hz from there on
               double [] flatFeatures = new double[_featuresPerChannel*_numChannels];
               int index = 0;
               for (int i = 0 ; i < _numChannels;i++) {
                   double DC = _envelopeFFT[i][0];
                   flatFeatures[index++] = Math.log(Math.pow(DC, 2));
                   int k = 1;
                   for ( int feat=0; feat < (_featuresPerChannel-1); feat++) {
                       double sum = 0.0;
                       for (int f = 0; f < (_fftLength/2.0)/(_featuresPerChannel-1); f++) {
                           sum += Math.pow(_envelopeFFT[i][k++],2);
                       }
                       flatFeatures[index++] = Math.log(sum);
                   }
               }
               Token[] features = new Token[_featureIndices.length];
               for (int i = 0; i < features.length; i++) {
                   features[i] = new DoubleToken(flatFeatures[_featureIndices[i]]);
               }
               output.send(0, new ArrayToken(features));
            } else {

                double[] flatFeatures = new double[33*_numChannels];
                int index = 0;
                for (int i = 0 ; i < _numChannels;i++) {
                    double DC = _envelopeFFT[i][0];
                    for ( int f=0; f < 33; f++) {
                        if (f ==0) {
                            flatFeatures[index++] = _envelopeFFT[i][f];
                        } else {
                            flatFeatures[index++] = normalizeDC ? _envelopeFFT[i][f]/DC : _envelopeFFT[i][f];
                        }
                    }
                }
                if ( ((BooleanToken)normalize.getToken()).booleanValue()) {
                    // normalize each channel in itself
                    for (int i = 0 ; i < _numChannels;i++) {
                        double[] normalized = DoubleArrayMath.normalize(Arrays.copyOfRange(flatFeatures, i*33, (i+1)*33));
                        for (int j = 0; j < 33 ; j ++) {
                            flatFeatures[i*33+j] = normalized[j];
                        }
                    }
                }
                Token[] features = new Token[_featureIndices.length];
                for (int i = 0; i < features.length; i++) {
                    features[i] = new DoubleToken(flatFeatures[_featureIndices[i]]);
                }
                output.send(0, new ArrayToken(features));
            }
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

    //TODO: Think about speeding this up.
    private double[] _firEnvelope(double[] signal) {
        int convLength = signal.length + env_fir.length -1;
        Complex[] signalFFT = SignalProcessing.FFTComplexOut(signal,SignalProcessing.order(convLength));
        double[] result = SignalProcessing.IFFTRealOut(ComplexArrayMath.multiply(env_fft, signalFFT));
        return Arrays.copyOfRange(result, env_fir.length/2, signal.length+env_fir.length/2);

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
    private void _initializeGammatoneFilterbankTaps() {
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
                double tstep = j*1.0/_fs;
                _impulseResponses[i][j] = gain*Math.pow(_fs,3)*Math.pow(tstep,_filterOrder-1)
                        *Math.exp(-2*Math.PI*b*tstep)*Math.cos(2*Math.PI*cfreq*tstep);
            }
        }

        int convLength = _windowSize + env_fir.length -1;
        env_fft = SignalProcessing.FFTComplexOut(env_fir,SignalProcessing.order(convLength));
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

    private int[] _featureIndices;
    /**
     * Number of FFT chunks per window that are used for overlap-and-add convolution.
     */
    private int _fftChunks;

    /**
     * Gammatone filter order.
     */
    private int _filterOrder = 4;

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

    private boolean _energyFeatures;

    /**
     * Input transfer size.
     */
    private int _transferSize;

    private int _featuresPerChannel = 9;

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
     * FFTs of the envelopes.
     */
    private double[][] _envelopeFFT;

    /**
     * FFT Length.
     */
    private int _fftLength;

    /**
     * Gammatone impulse response FFTs for overlap-and-add convolution.
     */
    private Complex[][] _gammatoneImpulseResponseFFTs;

    /** 1896-tap low-pass FIR filter with a transition band around 0.001
    this corresponds to a cut-off frequency of 50 Hz for fs = 48 kHz. */
    private final double[] env_fir = {0.00087222,3.048e-05,3.0621e-05,3.0713e-05,3.0843e-05,3.0924e-05,3.1042e-05,3.1115e-05,3.1226e-05,3.1293e-05,3.1402e-05,3.1471e-05,3.1586e-05,3.1665e-05,3.1797e-05,3.1901e-05,3.2065e-05,3.2213e-05,3.2433e-05,3.2652e-05,3.1467e-05,3.2223e-05,3.2281e-05,3.2297e-05,3.2346e-05,3.2356e-05,3.2398e-05,3.2403e-05,3.2442e-05,3.2444e-05,3.248e-05,3.2482e-05,3.2514e-05,3.2511e-05,3.2533e-05,3.2513e-05,3.2503e-05,3.2432e-05,3.2344e-05,3.2159e-05,3.2462e-05,3.2266e-05,3.2228e-05,3.216e-05,3.2112e-05,3.2037e-05,3.198e-05,3.1896e-05,3.1829e-05,3.1736e-05,3.1655e-05,3.1546e-05,3.1446e-05,3.1316e-05,3.119e-05,3.1037e-05,3.0894e-05,3.074e-05,3.0627e-05,3.0563e-05,3.027e-05,3.0141e-05,2.9972e-05,2.9787e-05,2.9605e-05,2.9406e-05,2.9209e-05,2.8995e-05,2.878e-05,2.8547e-05,2.8311e-05,2.8061e-05,2.7807e-05,2.7543e-05,2.7279e-05,2.7012e-05,2.6748e-05,2.6474e-05,2.6176e-05,2.5803e-05,2.5562e-05,2.5227e-05,2.4901e-05,2.4568e-05,2.4224e-05,2.3872e-05,2.3509e-05,2.3138e-05,2.2756e-05,2.2369e-05,2.1972e-05,2.1572e-05,2.1163e-05,2.0751e-05,2.0327e-05,1.9894e-05,1.944e-05,1.8975e-05,1.8502e-05,1.8084e-05,1.7564e-05,1.7085e-05,1.6582e-05,1.6075e-05,1.5552e-05,1.5026e-05,1.4484e-05,1.3941e-05,1.3383e-05,1.2824e-05,1.2249e-05,1.1671e-05,1.1075e-05,1.0472e-05,9.851e-06,9.2265e-06,8.5915e-06,7.963e-06,7.3199e-06,6.6225e-06,5.9651e-06,5.2782e-06,4.5792e-06,3.8762e-06,3.1577e-06,2.4355e-06,1.6979e-06,9.5547e-07,1.968e-07,-5.6863e-07,-1.3508e-06,-2.1406e-06,-2.9452e-06,-3.755e-06,-4.5761e-06,-5.4023e-06,-6.2456e-06,-7.107e-06,-7.9914e-06,-8.8425e-06,-9.7446e-06,-1.064e-05,-1.155e-05,-1.247e-05,-1.3401e-05,-1.4344e-05,-1.5299e-05,-1.6265e-05,-1.7244e-05,-1.8234e-05,-1.9234e-05,-2.0244e-05,-2.1264e-05,-2.2297e-05,-2.3343e-05,-2.4405e-05,-2.5478e-05,-2.6556e-05,-2.7629e-05,-2.8749e-05,-2.9853e-05,-3.0979e-05,-3.2113e-05,-3.3262e-05,-3.442e-05,-3.5592e-05,-3.6774e-05,-3.7969e-05,-3.9172e-05,-4.0389e-05,-4.1614e-05,-4.2855e-05,-4.4106e-05,-4.5373e-05,-4.6647e-05,-4.7931e-05,-4.922e-05,-5.0527e-05,-5.1858e-05,-5.3177e-05,-5.4522e-05,-5.5878e-05,-5.7241e-05,-5.862e-05,-6.0007e-05,-6.1408e-05,-6.2816e-05,-6.424e-05,-6.5672e-05,-6.712e-05,-6.8576e-05,-7.0047e-05,-7.1524e-05,-7.3013e-05,-7.451e-05,-7.6024e-05,-7.755e-05,-7.9091e-05,-8.0623e-05,-8.2192e-05,-8.3756e-05,-8.5337e-05,-8.6926e-05,-8.8529e-05,-9.014e-05,-9.1764e-05,-9.3398e-05,-9.5045e-05,-9.6701e-05,-9.8369e-05,-0.00010004,-0.00010173,-0.00010343,-0.00010514,-0.00010686,-0.00010859,-0.00011033,-0.00011208,-0.00011385,-0.00011561,-0.00011739,-0.00011918,-0.00012099,-0.0001228,-0.00012462,-0.00012645,-0.00012829,-0.00013014,-0.000132,-0.00013387,-0.00013575,-0.00013764,-0.00013954,-0.00014144,-0.00014336,-0.00014528,-0.00014722,-0.00014916,-0.00015111,-0.00015307,-0.00015504,-0.00015702,-0.000159,-0.00016099,-0.000163,-0.000165,-0.00016702,-0.00016905,-0.00017108,-0.00017313,-0.00017518,-0.00017723,-0.0001793,-0.00018136,-0.00018345,-0.00018553,-0.00018763,-0.00018972,-0.00019183,-0.00019394,-0.00019606,-0.00019818,-0.00020031,-0.00020245,-0.00020459,-0.00020674,-0.0002089,-0.00021106,-0.00021323,-0.00021539,-0.00021757,-0.00021975,-0.00022194,-0.00022413,-0.00022633,-0.00022852,-0.00023072,-0.00023294,-0.00023514,-0.00023736,-0.00023958,-0.0002418,-0.00024403,-0.00024626,-0.00024849,-0.00025072,-0.00025296,-0.0002552,-0.00025744,-0.00025968,-0.00026193,-0.00026418,-0.00026643,-0.00026868,-0.00027093,-0.00027319,-0.00027544,-0.00027769,-0.00027995,-0.00028221,-0.00028446,-0.00028672,-0.00028898,-0.00029123,-0.00029349,-0.00029574,-0.00029799,-0.00030025,-0.0003025,-0.00030475,-0.00030699,-0.00030924,-0.00031148,-0.00031372,-0.00031596,-0.0003182,-0.00032043,-0.00032266,-0.00032489,-0.00032711,-0.00032933,-0.00033154,-0.00033375,-0.00033596,-0.00033816,-0.00034036,-0.00034255,-0.00034474,-0.00034692,-0.00034909,-0.00035126,-0.00035343,-0.00035558,-0.00035773,-0.00035987,-0.00036201,-0.00036414,-0.00036626,-0.00036838,-0.00037048,-0.00037258,-0.00037467,-0.00037675,-0.00037882,-0.00038088,-0.00038293,-0.00038498,-0.00038701,-0.00038903,-0.00039105,-0.00039305,-0.00039504,-0.00039702,-0.00039899,-0.00040095,-0.00040289,-0.00040482,-0.00040675,-0.00040865,-0.00041055,-0.00041243,-0.0004143,-0.00041615,-0.00041799,-0.00041982,-0.00042163,-0.00042343,-0.00042521,-0.00042698,-0.00042873,-0.00043047,-0.00043219,-0.0004339,-0.00043559,-0.00043726,-0.00043891,-0.00044055,-0.00044217,-0.00044377,-0.00044536,-0.00044692,-0.00044847,-0.00045,-0.00045151,-0.000453,-0.00045447,-0.00045592,-0.00045735,-0.00045876,-0.00046015,-0.00046152,-0.00046286,-0.00046419,-0.00046549,-0.00046678,-0.00046804,-0.00046927,-0.00047049,-0.00047168,-0.00047285,-0.00047399,-0.00047511,-0.00047621,-0.00047728,-0.00047833,-0.00047935,-0.00048035,-0.00048132,-0.00048227,-0.00048319,-0.00048408,-0.00048495,-0.00048579,-0.00048661,-0.00048739,-0.00048815,-0.00048888,-0.00048958,-0.00049026,-0.00049091,-0.00049152,-0.00049211,-0.00049267,-0.0004932,-0.0004937,-0.00049417,-0.00049461,-0.00049501,-0.00049539,-0.00049574,-0.00049605,-0.00049634,-0.00049659,-0.0004968,-0.00049699,-0.00049715,-0.00049726,-0.00049735,-0.00049741,-0.00049743,-0.00049741,-0.00049736,-0.00049728,-0.00049716,-0.00049701,-0.00049682,-0.0004966,-0.00049634,-0.00049605,-0.00049572,-0.00049535,-0.00049495,-0.00049451,-0.00049403,-0.00049352,-0.00049297,-0.00049238,-0.00049175,-0.00049108,-0.00049038,-0.00048964,-0.00048886,-0.00048804,-0.00048718,-0.00048628,-0.00048534,-0.00048436,-0.00048334,-0.00048228,-0.00048118,-0.00048004,-0.00047886,-0.00047764,-0.00047637,-0.00047507,-0.00047372,-0.00047233,-0.0004709,-0.00046943,-0.00046791,-0.00046635,-0.00046475,-0.00046311,-0.00046142,-0.00045969,-0.00045791,-0.00045609,-0.00045423,-0.00045232,-0.00045037,-0.00044838,-0.00044634,-0.00044425,-0.00044212,-0.00043995,-0.00043773,-0.00043546,-0.00043315,-0.00043079,-0.00042839,-0.00042594,-0.00042344,-0.0004209,-0.00041831,-0.00041568,-0.000413,-0.00041027,-0.00040749,-0.00040467,-0.0004018,-0.00039888,-0.00039592,-0.00039291,-0.00038985,-0.00038674,-0.00038358,-0.00038038,-0.00037713,-0.00037383,-0.00037048,-0.00036708,-0.00036364,-0.00036014,-0.0003566,-0.00035301,-0.00034937,-0.00034568,-0.00034194,-0.00033815,-0.00033431,-0.00033043,-0.00032649,-0.00032251,-0.00031847,-0.00031439,-0.00031025,-0.00030607,-0.00030184,-0.00029756,-0.00029322,-0.00028884,-0.00028441,-0.00027993,-0.00027539,-0.00027081,-0.00026618,-0.0002615,-0.00025677,-0.00025199,-0.00024716,-0.00024227,-0.00023734,-0.00023236,-0.00022733,-0.00022225,-0.00021712,-0.00021194,-0.0002067,-0.00020142,-0.00019609,-0.00019071,-0.00018528,-0.0001798,-0.00017427,-0.00016869,-0.00016306,-0.00015738,-0.00015165,-0.00014587,-0.00014004,-0.00013416,-0.00012823,-0.00012225,-0.00011623,-0.00011015,-0.00010402,-9.7852e-05,-9.1628e-05,-8.5356e-05,-7.9035e-05,-7.2666e-05,-6.6247e-05,-5.978e-05,-5.3265e-05,-4.6702e-05,-4.009e-05,-3.3429e-05,-2.6721e-05,-1.9966e-05,-1.3163e-05,-6.3109e-06,5.8828e-07,7.5345e-06,1.4527e-05,2.1569e-05,2.8655e-05,3.579e-05,4.297e-05,5.0198e-05,5.7472e-05,6.4792e-05,7.2158e-05,7.957e-05,8.7027e-05,9.4531e-05,0.00010208,0.00010967,0.00011731,0.000125,0.00013272,0.0001405,0.00014831,0.00015617,0.00016408,0.00017202,0.00018002,0.00018805,0.00019613,0.00020425,0.00021241,0.00022062,0.00022886,0.00023715,0.00024548,0.00025385,0.00026227,0.00027072,0.00027922,0.00028775,0.00029632,0.00030494,0.00031359,0.00032229,0.00033102,0.00033979,0.0003486,0.00035745,0.00036633,0.00037526,0.00038422,0.00039321,0.00040225,0.00041132,0.00042043,0.00042957,0.00043875,0.00044796,0.00045721,0.00046649,0.00047581,0.00048516,0.00049454,0.00050396,0.00051341,0.00052289,0.00053241,0.00054196,0.00055154,0.00056115,0.00057079,0.00058046,0.00059016,0.00059989,0.00060965,0.00061944,0.00062926,0.00063911,0.00064898,0.00065888,0.00066881,0.00067877,0.00068875,0.00069876,0.00070879,0.00071885,0.00072894,0.00073905,0.00074918,0.00075933,0.00076951,0.00077972,0.00078994,0.00080019,0.00081046,0.00082074,0.00083105,0.00084139,0.00085174,0.0008621,0.00087249,0.0008829,0.00089333,0.00090377,0.00091423,0.00092471,0.0009352,0.00094571,0.00095624,0.00096678,0.00097733,0.0009879,0.00099848,0.0010091,0.0010197,0.0010303,0.0010409,0.0010516,0.0010622,0.0010729,0.0010836,0.0010943,0.001105,0.0011157,0.0011264,0.0011371,0.0011478,0.0011585,0.0011693,0.00118,0.0011908,0.0012015,0.0012123,0.001223,0.0012338,0.0012446,0.0012553,0.0012661,0.0012769,0.0012877,0.0012984,0.0013092,0.00132,0.0013308,0.0013415,0.0013523,0.0013631,0.0013738,0.0013846,0.0013953,0.0014061,0.0014168,0.0014275,0.0014383,0.001449,0.0014597,0.0014704,0.0014811,0.0014918,0.0015025,0.0015132,0.0015238,0.0015345,0.0015451,0.0015557,0.0015663,0.0015769,0.0015875,0.0015981,0.0016087,0.0016192,0.0016297,0.0016402,0.0016507,0.0016612,0.0016717,0.0016821,0.0016925,0.0017029,0.0017133,0.0017237,0.001734,0.0017443,0.0017546,0.0017649,0.0017752,0.0017854,0.0017956,0.0018058,0.0018159,0.001826,0.0018361,0.0018462,0.0018563,0.0018663,0.0018763,0.0018863,0.0018962,0.0019061,0.001916,0.0019258,0.0019356,0.0019454,0.0019552,0.0019649,0.0019746,0.0019842,0.0019938,0.0020034,0.0020129,0.0020225,0.0020319,0.0020414,0.0020508,0.0020601,0.0020695,0.0020787,0.002088,0.0020972,0.0021064,0.0021155,0.0021246,0.0021336,0.0021426,0.0021516,0.0021605,0.0021693,0.0021782,0.002187,0.0021957,0.0022044,0.002213,0.0022216,0.0022302,0.0022387,0.0022472,0.0022556,0.0022639,0.0022722,0.0022805,0.0022887,0.0022969,0.002305,0.0023131,0.0023211,0.002329,0.0023369,0.0023448,0.0023526,0.0023603,0.002368,0.0023756,0.0023832,0.0023908,0.0023982,0.0024056,0.002413,0.0024203,0.0024275,0.0024347,0.0024418,0.0024489,0.0024559,0.0024629,0.0024698,0.0024766,0.0024834,0.0024901,0.0024967,0.0025033,0.0025098,0.0025163,0.0025227,0.002529,0.0025353,0.0025415,0.0025476,0.0025537,0.0025597,0.0025657,0.0025715,0.0025774,0.0025831,0.0025888,0.0025944,0.0026,0.0026054,0.0026109,0.0026162,0.0026215,0.0026267,0.0026318,0.0026369,0.0026419,0.0026468,0.0026517,0.0026565,0.0026612,0.0026659,0.0026705,0.002675,0.0026794,0.0026838,0.0026881,0.0026923,0.0026964,0.0027005,0.0027045,0.0027084,0.0027123,0.0027161,0.0027198,0.0027234,0.002727,0.0027305,0.0027339,0.0027372,0.0027405,0.0027437,0.0027468,0.0027498,0.0027528,0.0027557,0.0027585,0.0027612,0.0027639,0.0027664,0.002769,0.0027714,0.0027737,0.002776,0.0027782,0.0027803,0.0027824,0.0027843,0.0027862,0.002788,0.0027897,0.0027914,0.002793,0.0027945,0.0027959,0.0027972,0.0027985,0.0027997,0.0028008,0.0028018,0.0028027,0.0028036,0.0028044,0.0028051,0.0028057,0.0028063,0.0028068,0.0028072,0.0028075,0.0028077,0.0028079,0.0028079,0.0028079,0.0028079,0.0028077,0.0028075,0.0028072,0.0028068,0.0028063,0.0028057,0.0028051,0.0028044,0.0028036,0.0028027,0.0028018,0.0028008,0.0027997,0.0027985,0.0027972,0.0027959,0.0027945,0.002793,0.0027914,0.0027897,0.002788,0.0027862,0.0027843,0.0027824,0.0027803,0.0027782,0.002776,0.0027737,0.0027714,0.002769,0.0027664,0.0027639,0.0027612,0.0027585,0.0027557,0.0027528,0.0027498,0.0027468,0.0027437,0.0027405,0.0027372,0.0027339,0.0027305,0.002727,0.0027234,0.0027198,0.0027161,0.0027123,0.0027084,0.0027045,0.0027005,0.0026964,0.0026923,0.0026881,0.0026838,0.0026794,0.002675,0.0026705,0.0026659,0.0026612,0.0026565,0.0026517,0.0026468,0.0026419,0.0026369,0.0026318,0.0026267,0.0026215,0.0026162,0.0026109,0.0026054,0.0026,0.0025944,0.0025888,0.0025831,0.0025774,0.0025715,0.0025657,0.0025597,0.0025537,0.0025476,0.0025415,0.0025353,0.002529,0.0025227,0.0025163,0.0025098,0.0025033,0.0024967,0.0024901,0.0024834,0.0024766,0.0024698,0.0024629,0.0024559,0.0024489,0.0024418,0.0024347,0.0024275,0.0024203,0.002413,0.0024056,0.0023982,0.0023908,0.0023832,0.0023756,0.002368,0.0023603,0.0023526,0.0023448,0.0023369,0.002329,0.0023211,0.0023131,0.002305,0.0022969,0.0022887,0.0022805,0.0022722,0.0022639,0.0022556,0.0022472,0.0022387,0.0022302,0.0022216,0.002213,0.0022044,0.0021957,0.002187,0.0021782,0.0021693,0.0021605,0.0021516,0.0021426,0.0021336,0.0021246,0.0021155,0.0021064,0.0020972,0.002088,0.0020787,0.0020695,0.0020601,0.0020508,0.0020414,0.0020319,0.0020225,0.0020129,0.0020034,0.0019938,0.0019842,0.0019746,0.0019649,0.0019552,0.0019454,0.0019356,0.0019258,0.001916,0.0019061,0.0018962,0.0018863,0.0018763,0.0018663,0.0018563,0.0018462,0.0018361,0.001826,0.0018159,0.0018058,0.0017956,0.0017854,0.0017752,0.0017649,0.0017546,0.0017443,0.001734,0.0017237,0.0017133,0.0017029,0.0016925,0.0016821,0.0016717,0.0016612,0.0016507,0.0016402,0.0016297,0.0016192,0.0016087,0.0015981,0.0015875,0.0015769,0.0015663,0.0015557,0.0015451,0.0015345,0.0015238,0.0015132,0.0015025,0.0014918,0.0014811,0.0014704,0.0014597,0.001449,0.0014383,0.0014275,0.0014168,0.0014061,0.0013953,0.0013846,0.0013738,0.0013631,0.0013523,0.0013415,0.0013308,0.00132,0.0013092,0.0012984,0.0012877,0.0012769,0.0012661,0.0012553,0.0012446,0.0012338,0.001223,0.0012123,0.0012015,0.0011908,0.00118,0.0011693,0.0011585,0.0011478,0.0011371,0.0011264,0.0011157,0.001105,0.0010943,0.0010836,0.0010729,0.0010622,0.0010516,0.0010409,0.0010303,0.0010197,0.0010091,0.00099848,0.0009879,0.00097733,0.00096678,0.00095624,0.00094571,0.0009352,0.00092471,0.00091423,0.00090377,0.00089333,0.0008829,0.00087249,0.0008621,0.00085174,0.00084139,0.00083105,0.00082074,0.00081046,0.00080019,0.00078994,0.00077972,0.00076951,0.00075933,0.00074918,0.00073905,0.00072894,0.00071885,0.00070879,0.00069876,0.00068875,0.00067877,0.00066881,0.00065888,0.00064898,0.00063911,0.00062926,0.00061944,0.00060965,0.00059989,0.00059016,0.00058046,0.00057079,0.00056115,0.00055154,0.00054196,0.00053241,0.00052289,0.00051341,0.00050396,0.00049454,0.00048516,0.00047581,0.00046649,0.00045721,0.00044796,0.00043875,0.00042957,0.00042043,0.00041132,0.00040225,0.00039321,0.00038422,0.00037526,0.00036633,0.00035745,0.0003486,0.00033979,0.00033102,0.00032229,0.00031359,0.00030494,0.00029632,0.00028775,0.00027922,0.00027072,0.00026227,0.00025385,0.00024548,0.00023715,0.00022886,0.00022062,0.00021241,0.00020425,0.00019613,0.00018805,0.00018002,0.00017202,0.00016408,0.00015617,0.00014831,0.0001405,0.00013272,0.000125,0.00011731,0.00010967,0.00010208,9.4531e-05,8.7027e-05,7.957e-05,7.2158e-05,6.4792e-05,5.7472e-05,5.0198e-05,4.297e-05,3.579e-05,2.8655e-05,2.1569e-05,1.4527e-05,7.5345e-06,5.8828e-07,-6.3109e-06,-1.3163e-05,-1.9966e-05,-2.6721e-05,-3.3429e-05,-4.009e-05,-4.6702e-05,-5.3265e-05,-5.978e-05,-6.6247e-05,-7.2666e-05,-7.9035e-05,-8.5356e-05,-9.1628e-05,-9.7852e-05,-0.00010402,-0.00011015,-0.00011623,-0.00012225,-0.00012823,-0.00013416,-0.00014004,-0.00014587,-0.00015165,-0.00015738,-0.00016306,-0.00016869,-0.00017427,-0.0001798,-0.00018528,-0.00019071,-0.00019609,-0.00020142,-0.0002067,-0.00021194,-0.00021712,-0.00022225,-0.00022733,-0.00023236,-0.00023734,-0.00024227,-0.00024716,-0.00025199,-0.00025677,-0.0002615,-0.00026618,-0.00027081,-0.00027539,-0.00027993,-0.00028441,-0.00028884,-0.00029322,-0.00029756,-0.00030184,-0.00030607,-0.00031025,-0.00031439,-0.00031847,-0.00032251,-0.00032649,-0.00033043,-0.00033431,-0.00033815,-0.00034194,-0.00034568,-0.00034937,-0.00035301,-0.0003566,-0.00036014,-0.00036364,-0.00036708,-0.00037048,-0.00037383,-0.00037713,-0.00038038,-0.00038358,-0.00038674,-0.00038985,-0.00039291,-0.00039592,-0.00039888,-0.0004018,-0.00040467,-0.00040749,-0.00041027,-0.000413,-0.00041568,-0.00041831,-0.0004209,-0.00042344,-0.00042594,-0.00042839,-0.00043079,-0.00043315,-0.00043546,-0.00043773,-0.00043995,-0.00044212,-0.00044425,-0.00044634,-0.00044838,-0.00045037,-0.00045232,-0.00045423,-0.00045609,-0.00045791,-0.00045969,-0.00046142,-0.00046311,-0.00046475,-0.00046635,-0.00046791,-0.00046943,-0.0004709,-0.00047233,-0.00047372,-0.00047507,-0.00047637,-0.00047764,-0.00047886,-0.00048004,-0.00048118,-0.00048228,-0.00048334,-0.00048436,-0.00048534,-0.00048628,-0.00048718,-0.00048804,-0.00048886,-0.00048964,-0.00049038,-0.00049108,-0.00049175,-0.00049238,-0.00049297,-0.00049352,-0.00049403,-0.00049451,-0.00049495,-0.00049535,-0.00049572,-0.00049605,-0.00049634,-0.0004966,-0.00049682,-0.00049701,-0.00049716,-0.00049728,-0.00049736,-0.00049741,-0.00049743,-0.00049741,-0.00049735,-0.00049726,-0.00049715,-0.00049699,-0.0004968,-0.00049659,-0.00049634,-0.00049605,-0.00049574,-0.00049539,-0.00049501,-0.00049461,-0.00049417,-0.0004937,-0.0004932,-0.00049267,-0.00049211,-0.00049152,-0.00049091,-0.00049026,-0.00048958,-0.00048888,-0.00048815,-0.00048739,-0.00048661,-0.00048579,-0.00048495,-0.00048408,-0.00048319,-0.00048227,-0.00048132,-0.00048035,-0.00047935,-0.00047833,-0.00047728,-0.00047621,-0.00047511,-0.00047399,-0.00047285,-0.00047168,-0.00047049,-0.00046927,-0.00046804,-0.00046678,-0.00046549,-0.00046419,-0.00046286,-0.00046152,-0.00046015,-0.00045876,-0.00045735,-0.00045592,-0.00045447,-0.000453,-0.00045151,-0.00045,-0.00044847,-0.00044692,-0.00044536,-0.00044377,-0.00044217,-0.00044055,-0.00043891,-0.00043726,-0.00043559,-0.0004339,-0.00043219,-0.00043047,-0.00042873,-0.00042698,-0.00042521,-0.00042343,-0.00042163,-0.00041982,-0.00041799,-0.00041615,-0.0004143,-0.00041243,-0.00041055,-0.00040865,-0.00040675,-0.00040482,-0.00040289,-0.00040095,-0.00039899,-0.00039702,-0.00039504,-0.00039305,-0.00039105,-0.00038903,-0.00038701,-0.00038498,-0.00038293,-0.00038088,-0.00037882,-0.00037675,-0.00037467,-0.00037258,-0.00037048,-0.00036838,-0.00036626,-0.00036414,-0.00036201,-0.00035987,-0.00035773,-0.00035558,-0.00035343,-0.00035126,-0.00034909,-0.00034692,-0.00034474,-0.00034255,-0.00034036,-0.00033816,-0.00033596,-0.00033375,-0.00033154,-0.00032933,-0.00032711,-0.00032489,-0.00032266,-0.00032043,-0.0003182,-0.00031596,-0.00031372,-0.00031148,-0.00030924,-0.00030699,-0.00030475,-0.0003025,-0.00030025,-0.00029799,-0.00029574,-0.00029349,-0.00029123,-0.00028898,-0.00028672,-0.00028446,-0.00028221,-0.00027995,-0.00027769,-0.00027544,-0.00027319,-0.00027093,-0.00026868,-0.00026643,-0.00026418,-0.00026193,-0.00025968,-0.00025744,-0.0002552,-0.00025296,-0.00025072,-0.00024849,-0.00024626,-0.00024403,-0.0002418,-0.00023958,-0.00023736,-0.00023514,-0.00023294,-0.00023072,-0.00022852,-0.00022633,-0.00022413,-0.00022194,-0.00021975,-0.00021757,-0.00021539,-0.00021323,-0.00021106,-0.0002089,-0.00020674,-0.00020459,-0.00020245,-0.00020031,-0.00019818,-0.00019606,-0.00019394,-0.00019183,-0.00018972,-0.00018763,-0.00018553,-0.00018345,-0.00018136,-0.0001793,-0.00017723,-0.00017518,-0.00017313,-0.00017108,-0.00016905,-0.00016702,-0.000165,-0.000163,-0.00016099,-0.000159,-0.00015702,-0.00015504,-0.00015307,-0.00015111,-0.00014916,-0.00014722,-0.00014528,-0.00014336,-0.00014144,-0.00013954,-0.00013764,-0.00013575,-0.00013387,-0.000132,-0.00013014,-0.00012829,-0.00012645,-0.00012462,-0.0001228,-0.00012099,-0.00011918,-0.00011739,-0.00011561,-0.00011385,-0.00011208,-0.00011033,-0.00010859,-0.00010686,-0.00010514,-0.00010343,-0.00010173,-0.00010004,-9.8369e-05,-9.6701e-05,-9.5045e-05,-9.3398e-05,-9.1764e-05,-9.014e-05,-8.8529e-05,-8.6926e-05,-8.5337e-05,-8.3756e-05,-8.2192e-05,-8.0623e-05,-7.9091e-05,-7.755e-05,-7.6024e-05,-7.451e-05,-7.3013e-05,-7.1524e-05,-7.0047e-05,-6.8576e-05,-6.712e-05,-6.5672e-05,-6.424e-05,-6.2816e-05,-6.1408e-05,-6.0007e-05,-5.862e-05,-5.7241e-05,-5.5878e-05,-5.4522e-05,-5.3177e-05,-5.1858e-05,-5.0527e-05,-4.922e-05,-4.7931e-05,-4.6647e-05,-4.5373e-05,-4.4106e-05,-4.2855e-05,-4.1614e-05,-4.0389e-05,-3.9172e-05,-3.7969e-05,-3.6774e-05,-3.5592e-05,-3.442e-05,-3.3262e-05,-3.2113e-05,-3.0979e-05,-2.9853e-05,-2.8749e-05,-2.7629e-05,-2.6556e-05,-2.5478e-05,-2.4405e-05,-2.3343e-05,-2.2297e-05,-2.1264e-05,-2.0244e-05,-1.9234e-05,-1.8234e-05,-1.7244e-05,-1.6265e-05,-1.5299e-05,-1.4344e-05,-1.3401e-05,-1.247e-05,-1.155e-05,-1.064e-05,-9.7446e-06,-8.8425e-06,-7.9914e-06,-7.107e-06,-6.2456e-06,-5.4023e-06,-4.5761e-06,-3.755e-06,-2.9452e-06,-2.1406e-06,-1.3508e-06,-5.6863e-07,1.968e-07,9.5547e-07,1.6979e-06,2.4355e-06,3.1577e-06,3.8762e-06,4.5792e-06,5.2782e-06,5.9651e-06,6.6225e-06,7.3199e-06,7.963e-06,8.5915e-06,9.2265e-06,9.851e-06,1.0472e-05,1.1075e-05,1.1671e-05,1.2249e-05,1.2824e-05,1.3383e-05,1.3941e-05,1.4484e-05,1.5026e-05,1.5552e-05,1.6075e-05,1.6582e-05,1.7085e-05,1.7564e-05,1.8084e-05,1.8502e-05,1.8975e-05,1.944e-05,1.9894e-05,2.0327e-05,2.0751e-05,2.1163e-05,2.1572e-05,2.1972e-05,2.2369e-05,2.2756e-05,2.3138e-05,2.3509e-05,2.3872e-05,2.4224e-05,2.4568e-05,2.4901e-05,2.5227e-05,2.5562e-05,2.5803e-05,2.6176e-05,2.6474e-05,2.6748e-05,2.7012e-05,2.7279e-05,2.7543e-05,2.7807e-05,2.8061e-05,2.8311e-05,2.8547e-05,2.878e-05,2.8995e-05,2.9209e-05,2.9406e-05,2.9605e-05,2.9787e-05,2.9972e-05,3.0141e-05,3.027e-05,3.0563e-05,3.0627e-05,3.074e-05,3.0894e-05,3.1037e-05,3.119e-05,3.1316e-05,3.1446e-05,3.1546e-05,3.1655e-05,3.1736e-05,3.1829e-05,3.1896e-05,3.198e-05,3.2037e-05,3.2112e-05,3.216e-05,3.2228e-05,3.2266e-05,3.2462e-05,3.2159e-05,3.2344e-05,3.2432e-05,3.2503e-05,3.2513e-05,3.2533e-05,3.2511e-05,3.2514e-05,3.2482e-05,3.248e-05,3.2444e-05,3.2442e-05,3.2403e-05,3.2398e-05,3.2356e-05,3.2346e-05,3.2297e-05,3.2281e-05,3.2223e-05,3.1467e-05,3.2652e-05,3.2433e-05,3.2213e-05,3.2065e-05,3.1901e-05,3.1797e-05,3.1665e-05,3.1586e-05,3.1471e-05,3.1402e-05,3.1293e-05,3.1226e-05,3.1115e-05,3.1042e-05,3.0924e-05,3.0843e-05,3.0713e-05,3.0621e-05,3.048e-05,0.00087222};
    //{0.0009237,0.00092406,0.00092442,0.00092477,0.00092512,0.00092547,0.00092582,0.00092616,0.0009265,0.00092684,0.00092717,0.0009275,0.00092783,0.00092816,0.00092848,0.0009288,0.00092912,0.00092944,0.00092975,0.00093006,0.00093037,0.00093067,0.00093097,0.00093127,0.00093157,0.00093186,0.00093215,0.00093244,0.00093273,0.00093301,0.00093329,0.00093356,0.00093384,0.00093411,0.00093438,0.00093464,0.00093491,0.00093517,0.00093542,0.00093568,0.00093593,0.00093618,0.00093642,0.00093667,0.00093691,0.00093714,0.00093738,0.00093761,0.00093784,0.00093807,0.00093829,0.00093851,0.00093873,0.00093894,0.00093916,0.00093937,0.00093957,0.00093978,0.00093998,0.00094017,0.00094037,0.00094056,0.00094075,0.00094094,0.00094112,0.0009413,0.00094148,0.00094166,0.00094183,0.000942,0.00094217,0.00094233,0.00094249,0.00094265,0.00094281,0.00094296,0.00094311,0.00094326,0.0009434,0.00094354,0.00094368,0.00094382,0.00094395,0.00094408,0.00094421,0.00094433,0.00094445,0.00094457,0.00094469,0.0009448,0.00094491,0.00094502,0.00094512,0.00094522,0.00094532,0.00094542,0.00094551,0.0009456,0.00094569,0.00094577,0.00094585,0.00094593,0.00094601,0.00094608,0.00094615,0.00094622,0.00094628,0.00094634,0.0009464,0.00094646,0.00094651,0.00094656,0.00094661,0.00094665,0.00094669,0.00094673,0.00094677,0.0009468,0.00094683,0.00094686,0.00094688,0.00094691,0.00094692,0.00094694,0.00094695,0.00094696,0.00094697,0.00094697,0.00094698,0.00094697,0.00094697,0.00094696,0.00094695,0.00094694,0.00094692,0.00094691,0.00094688,0.00094686,0.00094683,0.0009468,0.00094677,0.00094673,0.00094669,0.00094665,0.00094661,0.00094656,0.00094651,0.00094646,0.0009464,0.00094634,0.00094628,0.00094622,0.00094615,0.00094608,0.00094601,0.00094593,0.00094585,0.00094577,0.00094569,0.0009456,0.00094551,0.00094542,0.00094532,0.00094522,0.00094512,0.00094502,0.00094491,0.0009448,0.00094469,0.00094457,0.00094445,0.00094433,0.00094421,0.00094408,0.00094395,0.00094382,0.00094368,0.00094354,0.0009434,0.00094326,0.00094311,0.00094296,0.00094281,0.00094265,0.00094249,0.00094233,0.00094217,0.000942,0.00094183,0.00094166,0.00094148,0.0009413,0.00094112,0.00094094,0.00094075,0.00094056,0.00094037,0.00094017,0.00093998,0.00093978,0.00093957,0.00093937,0.00093916,0.00093894,0.00093873,0.00093851,0.00093829,0.00093807,0.00093784,0.00093761,0.00093738,0.00093714,0.00093691,0.00093667,0.00093642,0.00093618,0.00093593,0.00093568,0.00093542,0.00093517,0.00093491,0.00093464,0.00093438,0.00093411,0.00093384,0.00093356,0.00093329,0.00093301,0.00093273,0.00093244,0.00093215,0.00093186,0.00093157,0.00093127,0.00093097,0.00093067,0.00093037,0.00093006,0.00092975,0.00092944,0.00092912,0.0009288,0.00092848,0.00092816,0.00092783,0.0009275,0.00092717,0.00092684,0.0009265,0.00092616,0.00092582,0.00092547,0.00092512,0.00092477,0.00092442,0.00092406,0.0009237};
    private Complex[] env_fft;
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

