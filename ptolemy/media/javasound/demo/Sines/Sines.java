/* A simple application that demonstrates the use of SoundPlayback 
  by performing simple additive synthesis in real-time.

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

package ptolemy.media.javasound.demo.Sines;

import ptolemy.media.javasound.*;

////////////////////////////////////////////////////
/** A simple application that demonstrates the use of SoundPlayback. 
   This application synthesizes and plays a simple harmonic
   signal, using simple additive synthesis. The signal
   is the some of a few harmonically related sinusoids.
   @author Brian K. Vogel (vogel@eecs.berkeley.edu)
   @version $Id$
 */
public class Sines {
    public static void main(String[] args) {
	// Create a sound capture object that captures audio
	// from the computer's audio input port (mic or
	// line-in).

	// The pitch of the signal to synthesize.
	double fundamental_Hz = 220;

	float sampleRate = 44100; // in Hz
	int sampleSizeInBits = 16;
	int channels = 2; // stereo.
	int outBufferSize = 4096; // Internal buffer size for playback.

	// Amount of data to read or write from/to the internal buffer
	// at a time. This should be set smaller than the internal buffer
	// size!
	int getSamplesSize = 256; 

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
	    soundPlayback.startPlayback();
	} catch (Exception ex) {
	    System.err.println(ex);
	}

	double[][] samplesArray = 
	    new double[channels][getSamplesSize];
	double[] samples = new double[channels];

	try{
	    // Loop forever.
	    while(true) {

		// Do some simple processing on the
		// captured audio.
		for (int j=0; j< channels; j++) {
		    for (int i=0; i< getSamplesSize; i++) {
			//  ********** INSERT PROCESSING CODE HERE ****

			// Generate a harmonic signal.
			samplesArray[j][i] =
			    java.lang.Math.sin(fundamental_Hz*2*java.lang.Math.PI*samples[j])*0.4 +
			    java.lang.Math.sin(2*fundamental_Hz*2*java.lang.Math.PI*samples[j])*0.3 +
			    java.lang.Math.sin(3*fundamental_Hz*2*java.lang.Math.PI*samples[j])*0.25 +
			    java.lang.Math.sin(4*fundamental_Hz*2*java.lang.Math.PI*samples[j])*0.2;
			// Increment time.
			samples[j] = samples[j] + 1.0/sampleRate;
		    }
		}

		// Play the processed audio samples.
		soundPlayback.putSamples(samplesArray);
		}
	} catch (Exception ex) {
	    System.err.println(ex);
	}   
    }

}
