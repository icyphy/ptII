/* A class that supports live audio capture and playback.

 Copyright (c) 2000-2001 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.media.javasound;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import javax.sound.sampled.*;

//////////////////////////////////////////////////////////////////////////
//// LiveSound
/**

   @author Brian K. Vogel
   @version $Id$
   @see ptolemy.media.javasound.SoundReader
   @see ptolemy.media.javasound.SoundWriter
*/

public class LiveSound {

    
    public static float getSampleRate() {
	return _sampleRate;
    }

    public static int getBitsPerSample() {
	return _bitsPerSample;
    }

    public static int getChannels() {
	return _channels;
    }

    public static int getBufferSize() {
	return _bufferSize;
    }

    public static boolean isCaptureActive() {
	return _captureIsActive;
    }

    public static boolean isPlaybackActive() {
	return _playbackIsActive;
    }

    public static void setSampleRate(float sampleRate) {
	_sampleRate = sampleRate;
    }

    public static void setBitsPerSample(int bitsPerSample) {
	_bitsPerSample = bitsPerSample;
    }

    public static void setChannels(int channels) {
	_channels = channels;
    }

    public static void setBufferSize(int bufferSize) {
	_bufferSize = bufferSize;
    }

    public static void setTransferSize(int transferSize) {
	if (_debug) {
	    System.out.println("LiveSound: setTransferSize(transferSize) " +
			       " invoked with transferSize = " +
			       transferSize);
	}
			       
	_transferSize = transferSize;
    }

    public static int getTransferSize() {
	return _transferSize;
    }

    public static void resetCapture() {
	if (_targetLine != null) {
	    
	    if (_targetLine.isOpen() == true) {
		_targetLine.stop();
		_targetLine.close();
		_targetLine = null;
	    }
	}
    }

    public static void resetPlayback() {

    }

    public static void startCapture(Object consumer)
	throws IOException {
	if (_debug) {
	    System.out.println("LiveSound: startCapture(): invoked");
	}
	_startCapture();
    }

    public static void stopCapture(Object consumer)
	throws IOException {
	if (_debug) {
	    System.out.println("LiveSound: stopCapture(): invoked");
	}
	
	// Free up audio system resources.
	
	if (_targetLine != null) {
	    
	    if (_targetLine.isOpen() == true) {
		_targetLine.stop();
		_targetLine.close();
		_targetLine = null;
	    }
	}

    }

    public static void startPlayback(Object producer) {

    }

    public static void stopPlayback(Object producer) {

    }

    public static double[][] getSamples(Object consumer) 
	throws IOException,  IllegalStateException {
	if (_debug) {
	    System.out.println("LiveSound: getSamples(): invoked");
	    System.out.println("LiveSound: getSamples(): " +
			       "_transferSize = " + _transferSize);
	}
	    int numBytesRead;

		// Real-time capture.
		numBytesRead = _targetLine.read(_data, 0,
                        _transferSize*_frameSizeInBytes);


	    if (numBytesRead == _data.length) {
		// Convert byte array to double array.
		_audioInDoubleArray =
		    _byteArrayToDoubleArray(_data,
                            _bytesPerSample,
                            _channels);
		return _audioInDoubleArray;
	    } else if (numBytesRead != _data.length) {
		// Read fewer samples than productionRate many samples.
		//System.out.println("SoundCapture: getSamples(): Read fewer samples than productionRate many samples.");
		// FIXME: There appears to be a java sound bug that
		// causes AudioInputStream.read(array) to sometimes
		// return fewer bytes than requested, even though
		// the end of the file has not yet been reached.
		_audioInDoubleArray =
		    _byteArrayToDoubleArray(_data,
                            _bytesPerSample,
                            _channels);
		return _audioInDoubleArray;
	    } else if (numBytesRead == -1) {
		// Ran out of samples to play. This generally means
		// that the end of the sound file has been reached.
		return null;
	    }
	    return null;

    }

    public static void putSamples(Object producer, double[][] samplesArray) {
	
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** FIXME: this method should take an Object consumer as a parameter.
     */
    private static void _startCapture() throws IOException {
	
        int frameSizeInBits = _bitsPerSample;
        double frameRate = _sampleRate;
        boolean signed = true;
        boolean bigEndian = true;

        AudioFormat format = new AudioFormat(_sampleRate,
                _bitsPerSample,
                _channels, signed, bigEndian);

        _frameSizeInBytes = format.getFrameSize();

	DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class,
                format, AudioSystem.NOT_SPECIFIED);

	// The following works under Windows Java 1.3.0 RC2 but
	// not under Tritonus under Linux, so comment out.
        //if (!AudioSystem.isLineSupported(targetInfo)) {
	//    // FIXME: throw exception here.
        //    System.out.println("Line matching " + targetInfo +
        //            " not supported.");
        //    return;
        //}

        try {
            _targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
	    // Note: 2nd parameter is the buffer size (in bytes).
	    // Larger values increase latency but may be required if
	    // garbage collection, etc. is an issue.
            _targetLine.open(format, _bufferSize*_frameSizeInBytes);
        } catch (LineUnavailableException ex) {
	    throw new IOException("Unable to open the line for " +
                    "real-time audio capture: " + ex);
        }

        //System.out.println("SoundCapture: internal audio " +
	//      "buffer size = " +
	//      _targetLine.getBufferSize()/_frameSizeInBytes +
	//" samples.");

        int targetBufferLengthInBytes = _transferSize *
            _frameSizeInBytes;
	//System.out.println("frameSizeInBytes = " + _frameSizeInBytes);

	// The following works under Windows Java 1.3.0 RC2 but
	// not under Tritonus under Linux, so comment out.
	//if (!AudioSystem.isLineSupported(sourceInfo)) {
	//    //FIXME: handle this correctly.
	//    System.err.println("Line matching " + sourceInfo +
        //            " not supported.");
	//    return;
	//}

	// Array of audio samples in byte format.
	_data = new byte[_transferSize*_frameSizeInBytes];

	_bytesPerSample = _bitsPerSample/8;

	// Start the target data line
	_targetLine.start();

    }

    /* Convert a byte array of audio samples in linear signed pcm big endian
     * format into a double array of audio samples (-1, 1) range.
     * @param byteArray  The linear signed pcm big endian byte array
     * formatted array representation of audio data.
     * @param bytesPerSample Number of bytes per sample. Supported
     * bytes per sample by this method are 8, 16, 24, 32.
     * @param channels Number of audio channels. 1 for mono, 2 for
     * stereo.
     * @return Two dimensional array holding audio samples.
     * For each channel, m, doubleArray[m] is a single dimensional
     * array containing samples for channel m.
     */
    private static double[][] _byteArrayToDoubleArray(byte[] byteArray,
            int bytesPerSample,
            int channels) {
	int lengthInSamples = byteArray.length / (bytesPerSample*channels);
	// Check if we need to reallocate.
	if ((channels != _doubleArray.length) ||
                (lengthInSamples != _doubleArray[0].length)) {
	    // Reallocate
	    _doubleArray = new double[channels][lengthInSamples];
	}
	//double maxSampleReciprocal = 1/(Math.pow(2, 8 * bytesPerSample - 1));
	// Could use above line, but hopefully, code below will
	// be faster.
	double maxSampleReciprocal;
	if (bytesPerSample == 2) {
	    // 1 / 32768
	    maxSampleReciprocal = 3.0517578125e-5;
	} else if (bytesPerSample == 1) {	    // 1 / 128
	    maxSampleReciprocal = 7.8125e-3;
	} else if (bytesPerSample == 3) {
	    // 1 / 8388608
	    maxSampleReciprocal = 1.1920928955e07;
	} else if (bytesPerSample == 4) {
	    // 1 / 147483648e9
	    maxSampleReciprocal = 4.655661287308e-10;
	} else {
	    // Should not happen.
	    maxSampleReciprocal = 0;
	}

	// Check if we need to reallocate.
	// FIXME: This test is really not needed since bytesPerSample
	// is set in the constructor. It should never change.
	if (bytesPerSample != _b.length) {
	    _b = new byte[bytesPerSample];
	}

	for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {

	    // For each channel,
	    for (int currChannel = 0; currChannel < channels; currChannel++) {
		for (int i = 0; i < bytesPerSample; i += 1) {
		    // Assume we are dealing with big endian.
		    _b[i] = byteArray[currSamp*bytesPerSample*channels +
                            bytesPerSample*currChannel + i];
		}
		int result = (_b[0] >> 7) ;
		for (int i = 0; i < bytesPerSample; i += 1)
		    result = (result << 8) + (_b[i] & 0xff);
		_doubleArray[currChannel][currSamp] =
		    ((double) result*maxSampleReciprocal);
	    }
        }
	return _doubleArray;
    }

    /* Convert a double array of audio samples into a byte array of
     * audio samples in linear signed pcm big endian format. The
     * samples contained in <i>doubleArray</i> should be in the
     * range (-1,1). Samples outside this range will be hard clipped
     * to the range (-1,1).
     * @param doubleArray Two dimensional array holding audio samples.
     * For each channel, m, doubleArray[m] is a single dimensional
     * array containing samples for channel m.
     * @param bytesPerSample Number of bytes per sample. Supported
     * bytes per sample by this method are 8, 16, 24, 32.
     * @param channels Number of audio channels.
     * @return The linear signed pcm big endian byte array formatted
     * array representation of <i>doubleArray</i>. The length of
     * the returned array is (doubleArray.length*bytesPerSample*channels).
     */
    private static byte[] _doubleArrayToByteArray(double[][] doubleArray,
            int bytesPerSample, int channels) {
        // All channels had better have the same number
	// of samples! This is not checked!
	int lengthInSamples = doubleArray[0].length;
	//double  maxSample = Math.pow(2, 8 * bytesPerSample - 1);
	// Could use above line, but hopefully, code below will
	// be faster.
	double maxSample;
	double maxDoubleValuedSample;
	if (bytesPerSample == 2) {
	    maxSample = 32768;
	} else if (bytesPerSample == 1) {
	    maxSample = 128;
	} else if (bytesPerSample == 3) {
	    maxSample = 8388608;
	} else if (bytesPerSample == 4) {
	    maxSample = 147483648e9;
	} else {
	    // Should not happen.
	    maxSample = 0;
	}
	maxDoubleValuedSample = (maxSample - 2)/maxSample;
	byte[] byteArray =
	    new byte[lengthInSamples * bytesPerSample * channels];
	byte[] b = new byte[bytesPerSample];
	for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {

	    int l;
	    // For each channel,
	    for (int currChannel = 0; currChannel < channels; currChannel++) {
		// Perform clipping, if necessary.
		if (doubleArray[currChannel][currSamp] >=
                        maxDoubleValuedSample) {
		    l = (int)maxSample - 2;
		} else if (doubleArray[currChannel][currSamp] <=
                        -maxDoubleValuedSample) {
		    l = (int)(-maxSample) + 2;
		} else {
		    // signed integer representation of current sample of the
		    // current channel.
		    l =
			(int)(doubleArray[currChannel][currSamp] * maxSample);
		}
		// Create byte representation of current sample.
		for (int i = 0; i < bytesPerSample; i += 1, l >>= 8)
		    b[bytesPerSample - i - 1] = (byte) l;
		// Copy the byte representation of current sample to
		// the linear signed pcm big endian formatted byte array.
		for (int i = 0; i < bytesPerSample; i += 1) {
                    byteArray[currSamp*bytesPerSample*channels +
                            bytesPerSample*currChannel + i] = b[i];
		}
	    }
	}
	return byteArray;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Array of audio samples in double format.
    private static double[][] _audioInDoubleArray;
    private static byte[] _b = new byte[1];
    private static int _bitsPerSample;
    private static int _bufferSize;
    private static int _bytesPerSample;
    // true is audio capture is currently active
    private static boolean _captureIsActive;
    private static int _channels;
    // Array of audio samples in byte format.
    private static byte[] _data;
    private static double[][] _doubleArray = new double[1][1];
    private static int _frameSizeInBytes;
    // true is audio playback is currently active
    private static boolean _playbackIsActive;
    private static float _sampleRate;
    private static TargetDataLine _targetLine;
    // the number of audio samples to transfer per channel
    // when putSamples() or getSamples() is invoked.
    private static int _transferSize;
    // for debuging;
    private static boolean _debug = true;
}
