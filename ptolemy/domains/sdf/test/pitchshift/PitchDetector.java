/* Perform pitch detection using Cepstrum Analysis.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating
*/
package ptolemy.domains.sdf.test.pitchshift;

import ptolemy.domains.sdf.test.pitchshift.*;
import ptolemy.math.*;
import ptolemy.math.SignalProcessing;

//////////////////////////////////////////////////////////////////////////
//// PitchDetector
/** Perform pitch detection on an input signal using cepstrum analysis.
    The real cepstrum is computed as the
    IDFT(log(magnitude(DFT(input_signal)))).
    The maxumim value of the high-time part of the cepstrum is converted to
    the corresponding pitch value. The current implementation searches for
    pitches between 70 and 300 Hz. The latency of the pitch detector is 2048
    samples.
@author Brian K. Vogel
@version 1.0
*/
public class PitchDetector {

    // Array to hold recent input. Note that the size of this
    // array is independent from the size of the array read in
    // by performPitchDetect().
    // 2048 samples (for 44100 sampling rate) is enough to
    // resolve pitches down to about 60 Hz, provide that
    // the input signal has several harmonics.
    private int recentInputArraySize;

    // The sampling rate, in Hz. Should be one of 11025, 22050, 44100.
    private static int sampleRate;

    // Cutoff value in cepstrum for voiced/unvoiced. If largest peak
    // in the high-time part is greater than cepstralCutoff, then
    // call it voiced, else call it unvoiced.
    private final double cepstralCutoff = 0.03;

    // The lowest pitch to search for.
    private final double minAllowablePitch = 70;
    // The highest pitch to search for
    private final double maxAllowablePitch = 300;

    private double[] outPitchArray;

    private double[] windowedInput;
    private Complex[] dftInput;
    private Complex[] outCmplxArray;

    private double[] recentInputArray;
    private double[] logMagDFTWinInputArray;
    private double[] cepstrumArray;
    private int voiced; // == 1 for voiced, == 0, for unvoiced.

    // Initialize input pos
    private int recentInputArrayPos  = 0;

    private int vectorLoopPos;

    // Most recent pitch estimate (in Hz).
    // Set to -1 for unvoiced/don't know.
    private double currentPitch = -1;
    private double lastPitch = -1;
    private double tempPitch;

    // A hamming window.
    private double[] hammingWindow;

    /** Constructor
        Initialize the pitch detector.
        Parameter <i>vectorSize</i> sets the vector size to be used by
        <i>performPitchDetect()</i>. <i>vectorSize</i> may be any length.
    */
    public PitchDetector(int vectorSize,int sampleRate) {
        this.sampleRate = sampleRate;
        // FIXME: Should force recentInputArraySize to be a power of 2.
        this.recentInputArraySize = (int)(2048*(float)sampleRate/44100);

        this.outPitchArray = new double[vectorSize];
        this.windowedInput = new double[recentInputArraySize];
        this.logMagDFTWinInputArray = new double[recentInputArraySize];
        this.recentInputArray = new double[recentInputArraySize];
        // Initialize the hamming window.
        this.hammingWindow = makeHamming(recentInputArraySize);
    }

    /**
       Perform pitch detection. The input signal should uniformly
       partitioned
       into <i>vectorSize</i> length chunks of data.
       <i>performPitchDetect()</i>
       should be called on each consecutive vector of input data. A
       <i>vectorSize</i> length array of doubles containing the data is
       passed to <i>performPitchDetect()</i> via <i>inputArray[]</i>. Each
       element of <i>inputArray[]</i> is assumes to contain a single data
       sample. The <i>sampleRate</i> parameter contains that sample rate,
       in Hz.  <i>performPitchDetect()</i> returns a <i>vectorSize</i>
       length
       array of doubles containing the pitch estimates.
       <p>
       Note that there is a 2048 sample delay between an element of
       <i>inputArray[]</i> and the corresponding element of the returned
       vector of pitch estimates.
    */
    public double[] performPitchDetect(double [] inputArray) {

        //System.out.println("in");
        //for (int i=0; i < outArray.length; i++) {
        //outArray[i] = inputArray[i];
        //}
        //tempCmplxArray = FFTComplexOut(inputArray); // ???

        // Main loop.
        for (vectorLoopPos = 0; vectorLoopPos < inputArray.length;
             vectorLoopPos++) {
            //System.out.println("vectorLoopPos " + vectorLoopPos);
            if (recentInputArrayPos < recentInputArraySize) {
                // Ok to read in another samples, array not full yet.

                // Read in an input sample.
                recentInputArray[recentInputArrayPos] =
                    inputArray[vectorLoopPos];

                recentInputArrayPos++;
            } else {
                // Array is full now. Time to update pitch estimate.

                // Perforam pitch detection using Cepstral Analysis.

                // Step 1. Window recent input with a hamming window.
                for (int ind1 = 0; ind1 <
                         recentInputArray.length; ind1++) {

                    windowedInput[ind1] =
                        recentInputArray[ind1]*hammingWindow[ind1];
                }

                // Step 2. Take DFT of recent input.
                dftInput = SignalProcessing.FFTComplexOut(windowedInput);
                // Step 3. Take log of magnitude of dftInput.
                for (int ind2 = 0; ind2 <
                         recentInputArray.length; ind2++) {
                    logMagDFTWinInputArray[ind2] =
                        Math.log(dftInput[ind2].magnitude());
                }
                // Step 4. Take IDFT of logMagDFTWinInputArray.
                // FIXME: Is real idft correct? or complex?
                cepstrumArray =
                    SignalProcessing.IFFTRealOut(logMagDFTWinInputArray);
                // Step 5. Find the peak in the high time part. This is
                // the pitch. FIXME: clean this up.
                voiced = 0;

                // Initialize largest element found so far to 0.
                double largest = 0;

                // Ignore the low-time part of cepstrum.
                // FIXME: 12-16 is somwhat
                // arbitrary. Should be higher than 16 for vocal
                // signals (like 50-100).
                int startCepstralIndex =
                    (int)(Math.ceil((double)sampleRate/maxAllowablePitch));
                int stopCepstralIndex =
                    (int)Math.min((double)sampleRate/minAllowablePitch,
                            (double)recentInputArray.length/3);

                int pitchIndex = 0; // Index corresp to the pitch.
                int foundAPeak = 0;
                double linearLength2 = cepstrumArray.length/4 -
                    startCepstralIndex;
                double fadeFactor2;
                double octaveLargest = 0;
                double thirdOctaveLargest = 0;
                int octaveInd = 0;
                int thirdOctaveInd = 0;
                int initialPitchInd = 0;
                int secondEstPitchInd = 0;
                int thirdEstPitchInd = 0;
                int searchRange;
                // IMPORTANT NOTE: Increasing index number corresponds to
                // decreasing pitch!
                for (pitchIndex = startCepstralIndex; pitchIndex <
                         stopCepstralIndex; pitchIndex++) {

                    // Now find the largest element in the high-time
                    // part of the cepstrum.
                    if ((cepstrumArray[pitchIndex]) > largest) {

                        largest = cepstrumArray[pitchIndex];
                        initialPitchInd = pitchIndex;
                        //System.out.println("largest = " + largest);
                        //System.out.println("pitchIndex = " + pitchIndex);
                    }
                }
                // If the cepstral index of largest is more than 15%
                // of the length of the cepstrum, I do not check that
                // largest > cepstralCutoff. This is because peaks
                // higher in the high-time are more significant.

                if ((initialPitchInd > startCepstralIndex) &&
			(cepstrumArray[initialPitchInd] > cepstralCutoff)) {
                    // Current windowed signal is voiced (pitched).
                    voiced = 1;
                    currentPitch =
                        (double)sampleRate/(double)initialPitchInd;
		    // Now check that largest is greater than cepstralCutoff.
                }

                if (voiced == 0) {
                    // Found a peak in the ceptsrum

                    currentPitch = -1;
                    //System.out.println("UNVOICED" );
                } else {
                    /*
                      System.out.println(" " );
                      System.out.println("i1 i2 i3 = " +
                      thirdEstPitchInd + " " + secondEstPitchInd +
                      " " + initialPitchInd);
                      System.out.println("n1 n2 n3 = " +
                      cepstrumArray[thirdEstPitchInd] + " " +
                      cepstrumArray[secondEstPitchInd] + " " +
                      cepstrumArray[initialPitchInd]);
                      System.out.println("p1 p2 p3 = " +
                      (double)sampleRate/(double)thirdEstPitchInd +
                      " " + (double)sampleRate/(double)secondEstPitchInd +
                      " " + (double)sampleRate/(double)initialPitchInd);
                      System.out.println("currentPitch = " + currentPitch);
                      System.out.println(" " );
                    */
                }

                recentInputArrayPos = 0;
            }
            outPitchArray[vectorLoopPos] = currentPitch;
        }
        //System.out.println("out");
        return outPitchArray;
    }

    // Create a hamming window.
    private double[] makeHamming(int size) {

        double[] hammingWindow = new double[size];

        for (int i = 0; i < size; i++) {
            hammingWindow[i] = 0.54 - 0.46*Math.cos(2*Math.PI*i/size);
        }
        return hammingWindow;
    }

}
