/* Perform real-time pitch scaling of audio signals.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
import java.io.*;
import javax.media.sound.sampled.*;


//////////////////////////////////////////////////////////////////////////
//// ProcessAudio
/**
Perform real-time pitch shifting of audio signals. This only works
for audio signals that have either a unique pitch or no pitch at
any given time (pitched or unpitched, voiced or unvoiced). Examples
inlude human vocal sounds and sounds from musical instruments capable
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
@version
 */
public class ProcessAudio implements Runnable {

    String errStr;
    AudioInputStream audioInputStream;
    AudioInputStream properFormatAudioInputStream;

    Thread thread;

    // Set the default sample rate.
    double sampleRate = 22050;

    // Default pitch scale factor(s).
    double pitchScaleIn1 = 1.0;
    //double pitchScaleIn2 = 1.0;
    //double pitchScaleIn3 = 1.5;
    //double pitchScaleIn4 = 1.75;
    //double pitchScaleIn5 = 2.0;


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
	    System.exit(0);
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

    public void run() {

	// Capture specific stuff:

	// Number of sample frames to attempt to read from the target data
	// line. Also number of sample frames to attempt to wrte to the
	// source data line. This size should be chosen smaller (1/2 to 1/8)
	// the size of the queues used by JavaSound.
	// The number of samples frames to attempt to read/write from the
	// target/source data line is given by (readWriteDataSizeInFrames*
	// jsBufferSizeOverReadWriteSize). The sampleRate is in there
	// to force the delay (in seconds) to be be independent of the
	// sample rate.
	int readWriteDataSizeInFrames = (int)(512*sampleRate/44100);
	int jsBufferSizeOverReadWriteSize = 8;
	TargetDataLine targetLine;


        int sampleSizeInBitsInt = 16;
        int channels = 1; // If change this, then need to change
        //frameSizeInBits and frameRate accordingly.
        int frameSizeInBits = sampleSizeInBitsInt;
        double frameRate = sampleRate;
        boolean signed = true;
        boolean bigEndian = true;



        AudioFormat format = new AudioFormat((float)sampleRate,
                sampleSizeInBitsInt,
                channels, signed, bigEndian);

        int frameSizeInBytes = format.getFrameSize();


	DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class,
                null, null,
                new Class[0], format
                ,  AudioSystem.NOT_SPECIFIED);


        if (!AudioSystem.isSupportedLine(targetInfo)) {
            shutDown("Line matching " + targetInfo + " not supported.");
            return;
        }

        try {
            targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetLine.open(format, readWriteDataSizeInFrames*jsBufferSizeOverReadWriteSize);
        } catch (LineUnavailableException ex) {
            shutDown("Unable to open the line: " + ex);
            return;
        }




        System.out.println("JavaSound target (microphone/line in)" +
                "line buffer size in sample frames = " +
                targetLine.getBufferSize());

        int targetBufferLengthInBytes = readWriteDataSizeInFrames *
            frameSizeInBytes;
        byte[] targetData = new byte[targetBufferLengthInBytes];

        int numFramesRead;

	// uses 32768 sample frames for buffer length no matter what
	// I request! :(
	DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class,
                null, null,
                new Class[0], format,
                AudioSystem.NOT_SPECIFIED);



	if (!AudioSystem.isSupportedLine(sourceInfo)) {
	    shutDown("Line matching " + sourceInfo + " not supported.");
	    return;
	}

	// get and open the source data line for playback.
	SourceDataLine sourceLine;
	try {
	    sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
	    sourceLine.open(format, readWriteDataSizeInFrames*jsBufferSizeOverReadWriteSize);
	} catch (LineUnavailableException ex) {
	    shutDown("Unable to open the line: " + ex);
	    return;
	}


	System.out.println("JavaSound source (audio out/speaker) " +
                "line buffer size in sample frames  = " +
                sourceLine.getBufferSize());

	// Array of audio samples in double format.
	double[] audioInDoubleArray;

	byte[] audioOutByteArray;
	int numBytesRead = 0;

	// start the target data line
	targetLine.start();

	// start the source data line
	sourceLine.start();

	if (thread == null) {
	    System.out.println("thread == null !!!!");
	}


	// Initialize the pitch detector.
	PitchDetector pd = new PitchDetector(readWriteDataSizeInFrames,
                (int)sampleRate);
	// Initialize the pitch shifter.
	PitchShift ps = new PitchShift((float)sampleRate);

	double[] psArray1 = new double[readWriteDataSizeInFrames];
	double[] psArray2 = new double[readWriteDataSizeInFrames];

	double[] currPitchArray;

	while (thread != null) {
	    try {



		// Read some audio into data[].
		if ((numFramesRead = targetLine.read(targetData, 0,
                        readWriteDataSizeInFrames)) == -1) {
                    break;
		}



		audioInDoubleArray = _byteArrayToDoubleArray(targetData,
                        frameSizeInBytes);

		///////////////////////////////////////////////////////////
		//////   Do processing on audioInDoubleArray here     /////

		currPitchArray = pd.performPitchDetect(audioInDoubleArray);

		audioInDoubleArray = ps.performPitchShift(audioInDoubleArray,
                        currPitchArray, pitchScaleIn1);

		audioOutByteArray = _doubleArrayToByteArray(audioInDoubleArray,
                        frameSizeInBytes);

		int numFramesRemaining = numFramesRead;


		// I think this while loop is not needed, since it should
		// only execute 1 iteration.
		while (numFramesRemaining > 0) {
		    // Write the data to the output device.
		    numFramesRemaining -= sourceLine.write(audioOutByteArray,
                            0, numFramesRemaining);
		}
	    } catch (Exception e) {
		shutDown("Error during playback: " + e);
		break;
	    }
	}
	// we reached the end of the stream.  let the data play out, then
	// stop and close the line.
	if (thread != null) {
	    sourceLine.drain();
	}
	sourceLine.stop();
	sourceLine.close();
	sourceLine = null;
	shutDown(null);

    }



    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /* Convert a byte array of audio samples in linear signed pcm big endian
     * format into a double array of audio samples (-1,1) range.
     * FIXME: This method only works for mono (single channel) audio.
     */
    private double[] _byteArrayToDoubleArray(byte[] byteArray,
            int _bytesPerSample) {

	//System.out.println("_bytesPerSample = " + _bytesPerSample);
	//System.out.println("byteArray length = " + byteArray.length);
	int lengthInSamples = byteArray.length / _bytesPerSample;
	double[] doubleArray = new double[lengthInSamples];
	double mathDotPow = Math.pow(2, 8 * _bytesPerSample - 1)
	    ;
	for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {

	    byte[] b = new byte[_bytesPerSample];
	    for (int i = 0; i < _bytesPerSample; i += 1) {
		// Assume we are dealing with big endian.
		b[i] = byteArray[currSamp*_bytesPerSample + i];
	    }
	    long result = (b[0] >> 7) ;
	    for (int i = 0; i < _bytesPerSample; i += 1)
		result = (result << 8) + (b[i] & 0xff);
	    doubleArray[currSamp] = ((double) result/
                    (mathDotPow));
        }
	//System.out.println("a value " + doubleArray[34]);
	return doubleArray;
    }

    /* Convert a double array of audio samples in linear signed pcm big endian
     * format into a byte array of audio samples (-1,1) range.
     * FIXME: This method only works for mono (single channel) audio.
     */
    private byte[] _doubleArrayToByteArray(double[] doubleArray,
            int _bytesPerSample) {

	//System.out.println("_bytesPerSample = " + _bytesPerSample);
	int lengthInSamples = doubleArray.length;
	double mathDotPow = Math.pow(2, 8 * _bytesPerSample - 1);
	byte[] byteArray = new byte[lengthInSamples * _bytesPerSample];
	for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {
	    long l = Math.round((doubleArray[currSamp] * mathDotPow));
	    byte[] b = new byte[_bytesPerSample];
	    for (int i = 0; i < _bytesPerSample; i += 1, l >>= 8)
		b[_bytesPerSample - i - 1] = (byte) l;
	    for (int i = 0; i < _bytesPerSample; i += 1) {
		//if (_isBigEndian)
                byteArray[currSamp*_bytesPerSample + i] = b[i];
                //else put(b[_bytesPerSample - i - 1]);
	    }
	}
	return byteArray;
    }


}

