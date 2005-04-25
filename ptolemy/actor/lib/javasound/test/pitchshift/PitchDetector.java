/* Perform pitch detection using an autocorrelation estimate.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.actor.lib.javasound.test.pitchshift;

import ptolemy.math.Complex;
import ptolemy.math.SignalProcessing;


//////////////////////////////////////////////////////////////////////////
//// PitchDetector

/** Perform pitch detection on an input signal using an autocorrelation
    estimate. The autocorrelation estimate is computed using the
    DFT (an FFT algorithm is used) for efficiency.

    @author Brian K. Vogel
    @version $Id$
    @since Ptolemy II 1.0
    @Pt.ProposedRating Red (vogel)
    @Pt.AcceptedRating Red (vogel)
*/
public class PitchDetector {
    /** Initialize the pitch detector.
     *  Parameter <i>vectorSize</i> sets the vector size to be used by
     *  <i>performPitchDetect()</i>. <i>vectorSize</i> may be any length.
     *  @param vectorSize The length of the array parameter to
     *   performPitchDetect().
     *  @param sampleRate The sample rate to use, in Hz. 22050 or
     *   44100 is recomended.
     */
    public PitchDetector(int vectorSize, int sampleRate) {
        PitchDetector._sampleRate = sampleRate;
        this._minAutoCorInd = (int) ((double) _sampleRate / _maxAllowablePitch);
        this._maxAutoCorInd = (int) ((1.1 * (double) _sampleRate) / _minAllowablePitch);

        // FIXME: Should force _recentInputArraySize to be a power of 2.
        this._recentInputArraySize = 2048;

        this._outPitchArray = new double[vectorSize];
        this._magSqDFT = new double[2 * _recentInputArraySize];

        this._autocorEst = new double[2 * _recentInputArraySize];

        // Make this double the length, since need zero padding for
        // the DFT.
        this._recentInputArray = new double[2 * _recentInputArraySize];
    }

    /** Perform pitch detection. The input signal should uniformly
     *  partitioned
     *  into <i>vectorSize</i> length chunks of data.
     *  <i>performPitchDetect()</i>
     *  should be called on each consecutive vector of input data. A
     *  <i>vectorSize</i> length array of doubles containing the data is
     *  passed to <i>performPitchDetect()</i> via <i>inputArray[]</i>. Each
     *  element of <i>inputArray[]</i> is assumes to contain a single data
     *  sample. The <i>_sampleRate</i> parameter contains that sample rate,
     *  in Hz.  <i>performPitchDetect()</i> returns a <i>vectorSize</i>
     *  length
     *  array of doubles containing the pitch estimates.
     *  <p>
     *  Note that there is a 2048 sample delay between an element of
     *  <i>inputArray[]</i> and the corresponding element of the returned
     *  vector of pitch estimates.
     *  @param inputArray The array of audio sample to process.
     *  @return An array of pitch estimates of the same length as
     *   the input array parameter.
     */
    public double[] performPitchDetect(double[] inputArray) {
        // The number of samples of the input signal to skip
        // between autocorrelation estimates. Must be a
        // non-negative integer. A value of zero means that
        // the FFTs are computed back-to-back. Increasing
        // this value will result in less frequent pitch updates,
        // and less CPU usage.
        int skipSamples = 0;

        int curSkipSmaple = 0;

        // Main loop.
        for (int vectorLoopPos = 0; vectorLoopPos < inputArray.length;
                        vectorLoopPos++) {
            //System.out.println("vectorLoopPos " + vectorLoopPos);
            // Want to leave half of the array full of zeros, so that
            // we will have the appropriate amount of zero-padding
            // required for the DFT.
            if (curSkipSmaple == 0) {
                if (_recentInputArrayPos < _recentInputArraySize) {
                    // Ok to read in another samples, array not full yet.
                    // Read in an input sample.
                    _recentInputArray[_recentInputArrayPos] = inputArray[vectorLoopPos];
                    _recentInputArrayPos++;
                } else {
                    // Step 2. Take DFT of recent input padded with zeros.
                    _dftInput = SignalProcessing.FFTComplexOut(_recentInputArray);

                    // Step 3. Take the Mag^2 of the DFT.
                    for (int ind2 = 0; ind2 < _recentInputArray.length;
                                    ind2++) {
                        _magSqDFT[ind2] = _dftInput[ind2].magnitude() * _dftInput[ind2]
                                        .magnitude();
                    }

                    // Step 4. Take IDFT of _magSqDFT.
                    _autocorEst = SignalProcessing.IFFTRealOut(_magSqDFT);

                    double maxAutoCorVal = _autocorEst[0];

                    // Normalize the autocorrelation estimate.
                    for (int i = 0; i < _maxAutoCorInd; i++) {
                        _autocorEst[i] = _autocorEst[i] / maxAutoCorVal;

                        //System.out.println("_autocorEst[" +i+"] = " + _autocorEst[i]);
                        if (_autocorEst[i] > 1.01) {
                            System.out.println("FIXME: _autocorEst[" + i
                                + "] = " + _autocorEst[i]);
                        }
                    }

                    // Step 5. Find the peak in the the autocorrelation estimate.
                    // Find the index at which the autocorrelation function
                    // becomes less than the threshold.
                    int firstZzeroIndex = -1;
                    double closeEnoughToZero = 0.25;

                    for (int j = _minAutoCorInd; j < _maxAutoCorInd; j++) {
                        if (_autocorEst[j] < closeEnoughToZero) {
                            firstZzeroIndex = j;

                            //System.out.println("firstZzeroIndex = " + firstZzeroIndex);
                            break;
                        }
                    }

                    double maxv = 0;
                    int maxInd = 0;

                    if (firstZzeroIndex > 0) {
                        for (int m = firstZzeroIndex; m < _maxAutoCorInd;
                                        m++) {
                            if (_autocorEst[m] > maxv) {
                                maxv = _autocorEst[m];
                                maxInd = m;
                            }
                        }
                    }

                    _currentPitch = -1;

                    if (maxv > 0.3) {
                        _currentPitch = (double) _sampleRate / (double) maxInd;
                    }

                    //if (_currentPitch < 0) {
                    //System.out.println("_currentPitch = " + _currentPitch);
                    //System.out.println("value = " + _autocorEst[maxInd]);
                    //System.out.println("index = " + maxInd);
                    //System.out.println("index = " + maxInd);
                    //}
                    _recentInputArrayPos = 0;
                    curSkipSmaple = skipSamples;

                    //System.out.println("Done");
                }
            } else {
                curSkipSmaple--;
            }

            _outPitchArray[vectorLoopPos] = _currentPitch;
        }

        return _outPitchArray;
    }

    ///////////////////////////////////////////////////////////////////
    ///              private variables                        /////
    // Array to hold recent input. Note that the size of this
    // array is independent from the size of the array read in
    // by performPitchDetect().
    // 2048 samples (for 44100 sampling rate) is enough to
    // resolve pitches down to about 60 Hz, provide that
    // the input signal has several harmonics.
    private int _recentInputArraySize;

    // The sampling rate, in Hz. Should be one of 11025, 22050, 44100.
    private static int _sampleRate;

    // The highest pitch to search for.
    private static final double _maxAllowablePitch = 900;

    // The lowest pitch to search for.
    private static final double _minAllowablePitch = 60;
    private int _maxAutoCorInd;
    private int _minAutoCorInd;
    private double[] _outPitchArray;

    //private double[] windowedInput;
    private Complex[] _dftInput;
    private double[] _recentInputArray;
    private double[] _magSqDFT;
    private double[] _autocorEst;

    // Initialize input pos
    private int _recentInputArrayPos = 0;

    // Most recent pitch estimate (in Hz).
    // Set to -1 for unvoiced/don't know.
    private double _currentPitch = -1;
}
