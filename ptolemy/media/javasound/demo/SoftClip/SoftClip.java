/* A simple application that demonstrates the use of SoundCapture
and SoundPlayback by performing soft clipping in real-time.

 Copyright (c) 2000 The Regents of the University of California.
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

package ptolemy.media.javasound.demo.SoftClip;

import ptolemy.media.javasound.*;

////////////////////////////////////////////////////
/** A simple application that demonstrates the use of SoundCapture
   and SoundPlayback. This application performs real-time
   capture, processing, and playback of audio data.
   Sound samples are captured from the computer's audio
   input port. The processing consists of a simple
   soft-clipping funtion (the arc tangent is used). The
   soft-clipped audio data is then played out the speaker.
   @author Brian K. Vogel (vogel@eecs.berkeley.edu)
 */
public class SoftClip {
    public static void main(String[] args) {
	// Create a sound capture object that captures audio
	// from the computer's audio input port (mic or
	// line-in).
	float sampleRate = 44100; // in Hz
	int sampleSizeInBits = 16;
	int channels = 2; // stereo.
	int inBufferSize = 4096;  // Internal buffer size for capture.
	int outBufferSize = 4096; // Internal buffer size for playback.

	// Amount of data to read or write from/to the internal buffer
	// at a time. This should be set smaller than the internal buffer
	// size!
	int getSamplesSize = 256; 

	SoundCapture soundCapture = 
	    new SoundCapture(sampleRate, sampleSizeInBits,
			     channels, inBufferSize,
			     getSamplesSize);

	int putSamplesSize = getSamplesSize;

	// Construct a sound playback object that plays audio 
	//through the computer's speaker.
	SoundPlayback soundPlayback = new SoundPlayback(sampleRate,
					  sampleSizeInBits,
					  channels,
					  outBufferSize,
					  putSamplesSize);

	// Initialize and begin real-time capture and playback.
	try{
	    soundCapture.startCapture();
	    soundPlayback.startPlayback();
	} catch (Exception ex) {
	    System.err.println(ex);
	}

	
	double[][] capturedSamplesArray = 
	    new double[channels][getSamplesSize];

	try{
	    // Loop forever.
	    while(true) {
		// Read in some captured audio.
		capturedSamplesArray = soundCapture.getSamples();

		// Do some simple signal processing on the
		// captured audio.
		// For each channel.
		for (int j=0; j< channels; j++) {
		    // For each sample in the current channel.
		    for (int i=0; i< getSamplesSize; i++) {
			//  ********** INSERT SIGNAL PROCESSING CODE HERE ****

			// Perform soft clipping using the arc tangent.
			capturedSamplesArray[j][i] =
			    java.lang.Math.atan(capturedSamplesArray[j][i])*0.6;
		    }
		}

		// Play the processed audio samples.
		soundPlayback.putSamples(capturedSamplesArray);
		}
	} catch (Exception ex) {
	    System.err.println(ex);
	}   
    }
}
