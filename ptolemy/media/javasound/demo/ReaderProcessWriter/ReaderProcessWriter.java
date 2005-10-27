/* A simple application that demonstrates the use of SoundReader
 and SoundWriter by performing soft clipping on an input sound file.

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
package ptolemy.media.javasound.demo.ReaderProcessWriter;

import ptolemy.media.javasound.SoundReader;
import ptolemy.media.javasound.SoundWriter;

////////////////////////////////////////////////////

/** This is a simple application that demonstrates the use of SoundReader
 and SoundWriter by performing soft clipping on an input sound file.


 Samples are read from a sound file specified as a URL, simple
 *  processing is performed on the audio date, and the processed data is written to a sound file.
 *  For this application, the processing consists
 *  of a simple soft clipping operation.
 @author Brian K. Vogel
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red (vogel)
 */
public class ReaderProcessWriter {
    public static void main(String[] args) {
        // Set this to "true" to turn on debugging information.
        boolean _debug = true;

        // URL path to the input sound file, which is stored in a relative
        //directory in the ptolemy tree
        String sourceURL = "file:../../../../actor/lib/javasound/voice.wav";

        // File name of the output file to create.
        String writeFile = "demoOutput.wav";

        // Amount of data to read or write from/to the internal buffer
        // at a time. This should be set smaller than the internal buffer
        // size.
        int getSamplesSize = 256;

        try {
            // Construct a SoundReader object that is used to read
            // sound samples from an audio file.
            SoundReader soundReader = new SoundReader(sourceURL, getSamplesSize);

            // The sample rate to playback at, in Hz.
            // Set the playback sample rate to the sample rate of the
            // input sound file.
            float sampleRate = soundReader.getSampleRate();

            if (_debug) {
                System.out.println("Sample rate of the input file is "
                        + sampleRate + " Hz.");
            }

            // Number of bits per sample.
            int bitsPerSample = soundReader.getBitsPerSample();

            if (_debug) {
                System.out.println("Bits per sample for the input file is "
                        + bitsPerSample);
            }

            // 1 for mono, 2 for stereo, etc.
            int channels = soundReader.getChannels();

            if (_debug) {
                System.out.println("Number of channels for the input file is "
                        + channels);
            }

            int putSamplesSize = getSamplesSize;

            // Construct a sound writer object that is used to write
            // audio samples to a sound file.
            SoundWriter soundWriter = new SoundWriter(writeFile, sampleRate,
                    bitsPerSample, channels, putSamplesSize);

            double[][] capturedSamplesArray = new double[channels][getSamplesSize];
            boolean done = false;

            // The main loop.
            while (!done) {
                // Read in some audio samples.
                capturedSamplesArray = soundReader.getSamples();

                if (_debug) {
                    System.out.println("Read some samples...");
                }

                if (capturedSamplesArray == null) {
                    // reached end of file.
                    done = true;
                    System.out.println("Reached end of file.");
                } else {
                    // Do some simple processing on the
                    // captured audio.
                    for (int j = 0; j < channels; j++) {
                        for (int i = 0; i < getSamplesSize; i++) {
                            //  ********** INSERT PROCESSING CODE HERE ****
                            // Perform soft clipping using the arc tangent.
                            capturedSamplesArray[j][i] = java.lang.Math
                                    .atan(capturedSamplesArray[j][i]) * 0.6;
                        }
                    }

                    // Write the processed audio samples.
                    soundWriter.putSamples(capturedSamplesArray);
                }
            }

            // Close the input sound file, because we are done with it.
            soundReader.closeFile();

            // Close the output sound file, because we are done with it.
            soundWriter.closeFile();
        } catch (Exception ex) {
            System.err.println(ex);
        }

        System.out.println("Done.");
    }
}
