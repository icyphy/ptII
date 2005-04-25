/* A simple application that demonstrates the use of LiveSound by performing
   soft clipping in real-time.

   Copyright (c) 2000-2005 The Regents of the University of California.
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
package ptolemy.media.javasound.demo.SoftClip;

import ptolemy.media.javasound.LiveSound;


////////////////////////////////////////////////////

/**
   A simple application that demonstrates the use of LiveSound by
   performing soft clipping in real-time.  This application performs
   real-time capture, processing, and playback of audio data.  Sound
   samples are captured from the computer's audio input port. The
   processing consists of a simple soft-clipping function (the arc
   tangent is used). The soft-clipped audio data is then played out
   the speaker.

   @author Brian K. Vogel
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Red (vogel)
   @Pt.AcceptedRating Red (vogel)
*/
public class SoftClip {
    public static void main(String[] args) {
        // Create a sound capture object that captures audio
        // from the computer's audio input port (mic or
        // line-in).
        int sampleRate = 44100; // in Hz
        int sampleSizeInBits = 16;
        int channels = 2; // stereo.
        int inBufferSize = 4096; // Internal buffer size for capture.
        int outBufferSize = 4096; // Internal buffer size for playback.

        // the object that has access to the sound capture device.
        Object consumer = new Object();

        // the object that has access to the sound playback device.
        Object producer = new Object();

        // Amount of data to read or write from/to the internal buffer
        // at a time. This should be set smaller than the internal buffer
        // size!
        int getSamplesSize = 256;

        /*
          SoundCapture soundCapture =
          new SoundCapture(sampleRate, sampleSizeInBits,
          channels, inBufferSize,
          getSamplesSize);
        */

        // Construct a sound playback object that plays audio
        //through the computer's speaker.

        /*
          SoundPlayback soundPlayback = new SoundPlayback(sampleRate,
          sampleSizeInBits,
          channels,
          outBufferSize,
          putSamplesSize);
        */

        // Initialize and begin real-time capture and playback.
        try {
            //soundCapture.startCapture();
            // Set up LiveSound parameters for capture/playback
            LiveSound.setSampleRate(sampleRate);
            LiveSound.setBitsPerSample(sampleSizeInBits);
            LiveSound.setChannels(channels);
            LiveSound.setBufferSize(inBufferSize);
            System.out.println("Attempting to set both buffer sizes: "
                + outBufferSize + " samples.");
            LiveSound.setTransferSize(getSamplesSize);

            int putSamplesSize = getSamplesSize;

            LiveSound.startCapture(consumer);

            LiveSound.startPlayback(producer);

            //soundPlayback.startPlayback();
        } catch (Exception ex) {
            System.err.println(ex);
        }

        double[][] capturedSamplesArray = new double[channels][getSamplesSize];

        System.out.println("   Actual audio capture buffer size: "
            + LiveSound.getBufferSizeCapture() + " samples.");
        System.out.println("  Actual audio playback buffer size: "
            + LiveSound.getBufferSizePlayback() + " samples.");

        try {
            // Loop forever.
            System.out.println("starting");

            int count = 0;

            while (count < 1000) {
                count++;

                // Read in some captured audio.
                //capturedSamplesArray = soundCapture.getSamples();
                capturedSamplesArray = LiveSound.getSamples(consumer);

                // Do some simple processing on the
                // captured audio.
                for (int j = 0; j < channels; j++) {
                    for (int i = 0; i < getSamplesSize; i++) {
                        //  ********** PROCESSING CODE HERE **********
                        // Perform soft clipping using the arc tangent.
                        capturedSamplesArray[j][i] = java.lang.Math.atan(capturedSamplesArray[j][i]) * 0.6;
                    }
                }

                // Play the processed audio samples.
                //soundPlayback.putSamples(capturedSamplesArray);
                LiveSound.putSamples(producer, capturedSamplesArray);
            }

            // Stop capture.
            LiveSound.stopCapture(consumer);

            // Stop playback.
            //soundPlayback.stopPlayback();
            LiveSound.stopPlayback(producer);
            System.out.println("stopping");
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }
}
