/* Perform pitch shifting using a pitch-synchronous overlap-add
 algorithm.

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
package ptolemy.actor.lib.javasound.test.pitchshift;

///////////////////////////////////////////////////////////////////
//// PitchShift

/** Perform pitch scaling of an input signal.

 @author Brian K. Vogel
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red (vogel)
 */
public class PitchShift {
    /* Initialize the current output ring buffer read postition.
     * Maybe need to add ringBuffSize because % does not like negative numbers?
     */
    public PitchShift(float sampleRate) {
        _sampleRate = (int) sampleRate;
        _pitchDetectorDelay = 2048;

        OUTPUT_BUFFER_DELAY = (int) (2000 * (float) _sampleRate / 44100);
        outputDelay = OUTPUT_BUFFER_DELAY;
        ringBufSize = RING_BUFFER_SIZE;

        ///////////////////////////////////////////////
        // Debug
        //this.inputRingBufWritePos = 0;
        ////////////////////////////////////////////
        // This is delay from the pitch detector.
        _inputRingBufWritePos = 0;

        //////////////////////////////////////
        readPos = (_inputRingBufWritePos - outputDelay + ringBufSize)
                % ringBufSize;

        // The input ring buffer:
        _inputRingBuf = new double[RING_BUFFER_SIZE];

        // The output ring buffer:
        _outputRingBuf = new double[RING_BUFFER_SIZE];

        _outputRingBufPitchMarkerPos = 0;

        // Starting period. Initialize to default (unvoiced) pitch.
        inputPeriodLength = 100;
        samplesLeftInPeriod = 0;

        // Initialize to unvoiced, since don't know the pitch yet.
        isUnvoiced = 1;
    }

    /** Perform pitch shifting on the input signal. Read in an array
     *  of doubles in <i>in[]</i>, sampled at <i>_sampleRate</i>, with
     *  corresponding pitch values given by <i>pitchArray</i>. The
     *  desired pitch scale factor is passed via
     *  <i>pitchScaleIn</i>. The delayed and pitch shifted signal is
     *  returned as a vector of the same length as <i>in[]</i>. The
     *  delay (latency) of this pitch shifter is given by
     *  <i>_pitchDetectorDelay</i> + OUTPUT_BUFFER_DELAY +
     *  <i>_sampleRate</i>/<i>MINIMUM_PITCH</i>. The delay of the
     *  external pitch detector (delay between a sample of <i>in</i>
     *  and the sample of <i>pitchArray</i> containing the
     *  corresponding pitch) is set by <i>_pitchDetectorDelay</i>.
     */
    public double[] performPitchShift(double[] in, double[] pitchArray,
            double pitchScaleIn) {
        minimumPitchSamps = (int) (1 / minimumPitch * _sampleRate);

        int inputPitchInPtr = 0;
        int curOutSamp = 0;

        // Initialize input pos
        int curInSamp = 0;
        double[] out = new double[in.length];

        // Temporary variables
        int size = in.length;
        double periodRatio;
        double correctedPitchScale;
        double correctedPitchIn;
        double windowVal; // Current value of window function.

        /* Current position in OLA process relative to current synthesis
         * pitch marker.
         */
        int olaIndex;

        int outLag; // Set to 1 if x->outputRingBufPitchMarkerPos

        // lags x->inputRingBufWritePos, else set to zero.

        /* This is a marker for the element half way around the cirucular
         buffer w.r.t. x->inputRingBufWritePos */
        int inHalfAway;

        /* The main loop. This will iterate <MSP vector size> times for each
         *  call to this perform
         * method. */
        while (size > 0) {
            // Write the current input sample into the input circular array.
            //inputRingBuf[inputRingBufWritePos] = in[curInSamp];
            // Add some delay, to compensate for the pitch detector.
            _inputRingBuf[(_inputRingBufWritePos + _pitchDetectorDelay)
                    % ringBufSize] = in[curInSamp];

            //////////////////////////////////////////////////////
            //////////////////////////////////////////////////////
            // Do all interesting processing here.

            /* Check if have reached the end of the current period in the
             input signal.
             */
            if (samplesLeftInPeriod == 0) {
                /* Check if ok to do an OLA in the output buffer. */
                /* That is, check if the the outputRingBufPitchMarkerPos
                 *  lags nputRingBufWritePos.
                 */
                outLag = 1;
                inHalfAway = (_inputRingBufWritePos + ringBufSize / 2)
                        % ringBufSize;

                if (inHalfAway < ringBufSize / 2) {
                    /* The zero element of the input buffer lies
                     in (inptr, inHalfAway] */
                    if (_outputRingBufPitchMarkerPos < inHalfAway
                            || _outputRingBufPitchMarkerPos > _inputRingBufWritePos) {
                        // The current input element lags current
                        // synthesis pitch marker.
                        outLag = 0;
                    }
                } else {
                    /* The zero element of the input buffer lies
                     in (inHalfAway, inptr] */
                    if (_outputRingBufPitchMarkerPos > _inputRingBufWritePos
                            && _outputRingBufPitchMarkerPos < inHalfAway) {
                        // The current input element lags current
                        // synthesis pitch marker.
                        outLag = 0;
                    }
                }

                while (outLag == 1) {
                    // Do an OLA

                    /* Update the synthesis pitch marker posistion
                     (in the output buffer)/
                     */

                    // Do error checking
                    if (pitchScaleIn <= 0.1 || pitchScaleIn > 6.0
                            || isUnvoiced == 1) {
                        // UhOh, out of range. Fix that.
                        correctedPitchScale = 1.0;
                    } else {
                        correctedPitchScale = pitchScaleIn;
                    }

                    // Period scale factor.
                    periodRatio = 1.0 / correctedPitchScale;
                    _outputRingBufPitchMarkerPos = (int) (_outputRingBufPitchMarkerPos + inputPeriodLength
                            * periodRatio)
                            % ringBufSize;

                    /* Do an OLA (in the output buffer)
                     * about the synthesis pitch
                     * marker. Note that this
                     * implementation differs slightly
                     * from Lent's algorithm, in that 1
                     * input signal is subtracted from the
                     * current input pointer
                     * position. This is done in order to
                     * reduce latency and should not have
                     * an audible impact, I think.
                     */
                    for (olaIndex = -inputPeriodLength; olaIndex <= inputPeriodLength; ++olaIndex) {
                        windowVal = (1 + Math.cos(Math.PI * olaIndex
                                / inputPeriodLength)) * 0.5;

                        _outputRingBuf[(olaIndex + _outputRingBufPitchMarkerPos + ringBufSize)
                                % ringBufSize] += windowVal
                                * _inputRingBuf[(olaIndex
                                        + _inputRingBufWritePos
                                        - minimumPitchSamps + ringBufSize)
                                        % ringBufSize];
                    }

                    // Update loop condition variable.
                    outLag = 1;
                    inHalfAway = (_inputRingBufWritePos + ringBufSize / 2)
                            % ringBufSize;

                    if (inHalfAway < ringBufSize / 2) {
                        /* The zero element of the input buffer lies in
                         * (inptr, inHalfAway] */
                        if (_outputRingBufPitchMarkerPos < inHalfAway
                                || _outputRingBufPitchMarkerPos > _inputRingBufWritePos) {
                            // The current input element lags current
                            // synthesis pitch marker.
                            outLag = 0;
                        }
                    } else {
                        /* The zero element of the input buffer lies in
                         * (inHalfAway, inptr] */
                        if (_outputRingBufPitchMarkerPos > _inputRingBufWritePos
                                && _outputRingBufPitchMarkerPos <= inHalfAway) {
                            // The current input element lags
                            // current synthesis pitch marker.
                            outLag = 0;
                        }
                    }
                }

                /* Update input period value */

                // Do error checking on input pitch signal value.
                if (pitchArray[inputPitchInPtr] <= minimumPitch) {
                    // UhOh, pitch below range. Fix that.
                    correctedPitchIn = DEFAULT_PITCH;
                    isUnvoiced = 1;
                } else {
                    correctedPitchIn = pitchArray[inputPitchInPtr];
                    isUnvoiced = 0;
                }

                // correctedPitchIn = 441.0;  // FOR DEBUG
                inputPeriodLength = (int) (1.0 / correctedPitchIn * _sampleRate);

                // inputPeriodLength = 100;  // FOR DEBUG
                samplesLeftInPeriod = inputPeriodLength;
            }

            --samplesLeftInPeriod;

            // End of all interesting processing.
            ////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////
            // Read an output sample from the output Ring buffer.
            out[curOutSamp] = _outputRingBuf[readPos];

            //*out = inputRingBuf[readPos];
            // Now set the element just read from to zero, since it is no
            // longer needed.
            _outputRingBuf[readPos] = 0;

            // Update the pointers.
            _inputRingBufWritePos++;

            // Make the write postition pointer wrap back to the beginning after it
            // reaches the end of the buffer.
            _inputRingBufWritePos %= ringBufSize;

            readPos++;

            // Make the write postition pointer wrap back to the beginningg after it
            // reaches the end of the buffer.
            readPos %= ringBufSize;

            curInSamp++;
            curOutSamp++;
            inputPitchInPtr++;
            size--;
        }

        return out;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   private variables                   ////////
    private static final int RING_BUFFER_SIZE = 10000;

    private static int OUTPUT_BUFFER_DELAY;

    private static final double MINIMUM_PITCH = 20.0;

    private static final double DEFAULT_PITCH = MINIMUM_PITCH;

    private static int _sampleRate;

    // Delay of the pitch detector, in samples.
    // FIXME: this should be public, and set in constructor.
    private static int _pitchDetectorDelay;

    // The input ring buffer:
    private double[] _inputRingBuf;

    // The output ring buffer:
    private double[] _outputRingBuf;

    private int _inputRingBufWritePos;

    private double minimumPitch = MINIMUM_PITCH;

    /* This contains the element in the output ring buffer corresponding to
     * to the current synthesis pitch marker, about which OLA (OverLap Add)
     * is being performed. This variable gets incremented by the current
     * synthesis period when necessary. This parameter is called <outptr> in
     * Keith Lent's paper.
     */
    private int _outputRingBufPitchMarkerPos;

    /* The ring buffer size (number of elements). Both the input and output
     * ring buffers are the same size. The choice of size is somewhat arbitrary
     * but must be chosen large enough that a few periods of the waveform
     * can fit in it.
     */
    private int ringBufSize;

    // Output buffer delay, in samples
    private int outputDelay;

    /* Current element to read from in output ring buffer.
     *  This element is obtained by subtracting OUTPUT_BUFFER_DELAY from
     *  inputRingBufWritePos.
     */
    private int readPos;

    /* Current period length (in samples) of input signal. This gets updated once
     * per input signal period.
     */
    private int inputPeriodLength;

    /* The number of remaining samples to process before upding <inputPeriodLength>.
     * <inputPeriodLength> is updated when <samplesLeftInPeriod> is 0.
     */
    private int samplesLeftInPeriod;

    private int isUnvoiced; // 1 if unvoice (no pitch), else set to 0.

    private int minimumPitchSamps; // This is minimumPitch converted to samples.
}
