/* Perform real-time pitch scaling of audio signals.

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

import ptolemy.media.javasound.SoundCapture;
import ptolemy.media.javasound.SoundPlayback;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// ProcessAudio

/**
 Perform real-time pitch shifting of audio signals. This only works
 for audio signals that have either a unique pitch or no pitch at
 any given time (pitched or unpitched, voiced or unvoiced). Examples
 include human vocal sounds and sounds from musical instruments capable
 of playing only one note at a times (e.g., horns, flute). The pitch
 shifting algorithm is based on the algorithm proposed by Keith Lent
 in his paper: "An efficient Method for Pitch Shifting Digitally
 Sampled Sounds", published in the Computer Music Journal, Vol 13,
 No. 4, Winter 1989. The algorithm is presented with more mathematical
 rigore in the paper by Robert Bristow-Johnson:
 "A Detailed Analysis of a Time-Domain Formant-Corrected Pitch-
 Shifting Algorithm", in J. Audio Eng. Soc., Vol 43, No. 5, May 1995.
 <p>
 The pitch shifting algorithm uses a pitch-synchronous overlap-add (PSOLA)
 based algorithm, and therefore requires the pitch of the input signal.
 The pitch detector used in Keith Lent's algorithm consists of a
 bandpass filter followed by a simple negative-slop zero-crossing detector.
 I found such a simple pitch detector to be completely unusable for
 vocal and musical instrument sounds. I therefore decided to implement
 a more robust pitch detector. I am currently using a pitch detector
 that uses cepstrum analysis. The (real) cepstrum is computed, and
 then peak finding is performed on the high-time region of the cepstrum.
 This cepstral technique works well for vocal sounds but does not
 currently perform well for pitches above about 600 Hz.
 <p>
 Note: This application requires JDK 1.3. and at least a
 Pentium II 400 MHz class processor (for 22050 Hz sample rate).

 @author Brian K. Vogel
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red (vogel)
 */
public class ProcessAudio implements Runnable {
    String errStr;

    Thread thread;

    // Set the default sample rate.
    double sampleRate = 22050;

    // Default pitch scale factor(s).
    double pitchScaleIn1 = 1.0;

    public void start() {
        System.out.println("Sampling rate = " + sampleRate + " Hz.");
        errStr = null;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        thread = null;
    }

    private void shutDown(String message) {
        if ((errStr = message) != null) {
            System.err.println(errStr);
        }

        if (thread != null) {
            thread = null;

            // Now exit.
            StringUtilities.exit(0);
        }
    }

    // Update the pitch scale factor.
    public void updatePitchScaleFactor(double pitchScaleIn) {
        this.pitchScaleIn1 = pitchScaleIn;
    }

    // Set the sampling rate. Valid sampling rates are 11025, 22050, 44100.
    // This method should be the first method called in this class.
    public void setSamplingRate(double sr) {
        this.sampleRate = sr;
    }

    @Override
    public void run() {
        // Capture specific stuff:
        int sampleSizeInBits = 16;
        int channels = 1;

        int inBufferSize = 6000; // Internal buffer size for capture.
        int outBufferSize = 6000; // Internal buffer size for playback.

        // Amount of data to read or write from/to the internal buffer
        // at a time. This should be set smaller than the internal buffer
        // size!
        int getSamplesSize = 256;

        SoundCapture soundCapture = new SoundCapture((float) sampleRate,
                sampleSizeInBits, channels, inBufferSize, getSamplesSize);

        int putSamplesSize = getSamplesSize;

        // Construct a sound playback object that plays audio
        //through the computer's speaker.
        SoundPlayback soundPlayback = new SoundPlayback((float) sampleRate,
                sampleSizeInBits, channels, outBufferSize, putSamplesSize);

        // Initialize and begin real-time capture and playback.
        try {
            soundCapture.startCapture();
            soundPlayback.startPlayback();
        } catch (Exception ex) {
            System.err.println(ex);
        }

        double[][] capturedSamplesArray /* Avoid Dead Store: = new double[channels][getSamplesSize] */;

        // Initialize the pitch detector.
        int vectorSize = getSamplesSize * channels;
        PitchDetector pd = new PitchDetector(vectorSize, (int) sampleRate);

        // Initialize the pitch shifter.
        PitchShift ps = new PitchShift((float) sampleRate);

        double[] currPitchArray;

        while (thread != null) {
            try {
                // Read in some captured audio.
                capturedSamplesArray = soundCapture.getSamples();

                ///////////////////////////////////////////////////////////
                //////   Do processing on audioInDoubleArray here     /////
                currPitchArray = pd.performPitchDetect(capturedSamplesArray[0]);

                capturedSamplesArray[0] = ps.performPitchShift(
                        capturedSamplesArray[0], currPitchArray, pitchScaleIn1);

                // Play the processed audio samples.
                soundPlayback.putSamples(capturedSamplesArray);
            } catch (Exception e) {
                shutDown("Error during playback: " + e);
                break;
            }
        }

        // we reached the end of the stream.  let the data play out, then
        // stop and close the line.
        shutDown(null);
    }
}
