/* A simple application that demonstrates the use of LiveSound by performing
   simple additive synthesis in real-time.

 Copyright (c) 2000-2003 The Regents of the University of California.
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
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.media.javasound.demo.LiveSines;

import java.io.IOException;

import ptolemy.media.javasound.LiveSound;

////////////////////////////////////////////////////
/**
A simple application that demonstrates the use of LiveSound.  This
application synthesizes and plays a simple harmonic signal, using simple
additive synthesis. The signal is the sum of a few harmonically related
sinusoids.

@author Brian K. Vogel, Neil E. Turner
@version $Id$
@since Ptolemy II 3.1
*/
public class LiveSines {

    public static void main(String[] args) {

        // The pitch of the signal to synthesize.
        double fundamental1_Hz = 220;

        // The pitch of the signal to synthesize.
        double fundamental2_Hz = 220*1.5;

        int sampleRate = 44100; // in Hz
        int sampleSizeInBits = 16;
        int channels = 2; // stereo.
        int outBufferSize = 4096; // Internal buffer size for playback.
        // Amount of data to read or write from/to the internal buffer
        // at a time. This should be set smaller than the internal buffer
        // size!
        int putSamplesSize = 25;

        try {
            LiveSound.setSampleRate(sampleRate);
            LiveSound.setChannels(channels);
            System.out.println("Attempting to set playback buffer size: " +
                    outBufferSize + " samples.");
            LiveSound.setBufferSize(outBufferSize);
            LiveSound.setBitsPerSample(sampleSizeInBits);
            LiveSound.setTransferSize(putSamplesSize);

            // Begin playback.
            LiveSound.startPlayback(_producer);
        } catch (IOException ex) {
            System.err.println(ex);
        }

        double[][] samplesArray = new double[channels][putSamplesSize];
        // keep track of time, used in calculating the sine wave values.
        double[] samples = new double[channels];
        
        System.out.println("                           Actual size: " +
                LiveSound.getBufferSizePlayback() + " samples.");

        try {
            int channelNumber;
            // Loop forever.
            while (true) {
                for (int i = 0; i < putSamplesSize; i++) {
                    channelNumber = 0; //Left channel.
                    // Generate a harmonic signal.
                    samplesArray[channelNumber][i] =
                            Math.sin(fundamental1_Hz * 2 * Math.PI *
                            samples[channelNumber]) * 0.1 +
                            Math.sin(2 * fundamental1_Hz * 2 * Math.PI *
                            samples[channelNumber]) * 0.3 +
                            Math.sin(3 * fundamental1_Hz * 2 * Math.PI *
                            samples[channelNumber]) * 0.3 +
                            Math.sin(4 * fundamental1_Hz * 2 * Math.PI *
                            samples[channelNumber]) * 0.2;
                    // Increment time for the signal on this channel.
                    samples[channelNumber] = samples[channelNumber] +
                            1.0 / sampleRate;

                    channelNumber = 1; //Right channel.
                    // Generate a harmonic signal.
                    samplesArray[channelNumber][i] =
                            Math.sin(fundamental2_Hz * 2 * Math.PI *
                            samples[channelNumber]) * 0.4 +
                            Math.sin(2 * fundamental2_Hz * 2 * Math.PI *
                            samples[channelNumber]) * 0.3 +
                            Math.sin(3 * fundamental2_Hz * 2 * Math.PI *
                            samples[channelNumber]) * 0.25 +
                            Math.sin(4 * fundamental2_Hz * 2 * Math.PI *
                            samples[channelNumber]) * 0.2;
                    // Increment time for the signal on this channel.
                    samples[channelNumber] = samples[channelNumber] +
                            1.0 / sampleRate;
                }

                // Play the processed audio samples.
                LiveSound.putSamples(_producer, samplesArray);
                
                // break out of loop after 10 seconds.  0 is the channel number
                // chosen arbitrarily
                if (samples[0] > 10.0) {
                    break;
                }
            }
            LiveSound.stopPlayback(_producer);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    private static Object _producer = new Object();
}
