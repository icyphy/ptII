/* A library supporting the capturing of audio.

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

package ptolemy.media.javasound;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import javax.sound.sampled.*;

//////////////////////////////////////////////////////////////////////////
//// SoundCapture
/**
A library supporting the capturing of audio. This class supports the
real-time capture of audio from the audio input port (mic or line-in)
as well as the capture of audio from a sound file.
<p>
 Depending on available
system resorces, it may be possible to run multiple instances of this
class and an instance of SoundPlayback concurrently. It should
at least be possible to run one instance of this class and one
instance of SoundPlayback concurrently, thus enabling the real-time
capturing, processing, and playback of audio.
<p>
<i>Security issues</i>: Applications have no restrictions on the
capturing or playback of audio. Applets, however, may only capture
audio from a file specified as a URL on the same machine as the
one the applet was loaded from. The .java.policy file must be
modified to grant applets more privliiges.
<p>
Note: Requires Java 2 v1.3.0 RC1 or later.

@author Brian K. Vogel
@version $Id$
@see ptolemy.media.javasound.SoundPlayback
*/

public class SoundCapture {

    /** Construct a sound capture object. This constructor creates an
     *  object that captures audio from the computer's audio input
     *  port (mic or line-in). Note that getSamples() should be
     *  called often enough to prevent overflow of the internal audio
     *  input buffer.
     *  @param sampleRate Sample rate in Hz. Must be in the range (8000
     *  to 48000).
     *  @param sampleSizeInBits Number of bits per sample (choices are
     *  8 or 16).
     *  @param channels Number of audio channels. FIXME: must be 1
     *   for now.
     *  @param bufferSize Requested size of the internal audio input
     *   buffer in samples. This controls the latency. Ideally, the
     *   smallest value that gives acceptable performance (no overflow)
     *   should be used.
     *  @param getSamplesSize Size of the array returned by
     *   <i>getSamples()</i>. For performance reasons, the size should
     *   be chosen smaller than <i>bufferSize</i>. Typical values
     *   might be 1/2 to 1/16th of <i>bufferSize</i>.
     */
    // FIXME: channels must be set = 1.
    public SoundCapture(float sampleRate, int sampleSizeInBits,
			int channels, int bufferSize,
			int getSamplesSize) {
	System.out.println("SoundCapture: constructor 1: invoked");
	// Set mode to real-time.
	this._isRealTime = true;
	this._sampleSizeInBits = sampleSizeInBits;
	this._sampleRate = sampleRate;
	this._channels = channels;
	this._bufferSize = bufferSize;
	this._productionRate = getSamplesSize;

	System.out.println("SoundCapture: constructor 1: sampleSizeInBits = "
			   + sampleSizeInBits);
	System.out.println("SoundCapture: constructor 1: sampleRate = "
			   + sampleRate);
	System.out.println("SoundCapture: constructor 1: channels = "
			   + channels);
	System.out.println("SoundCapture: constructor 1: bufferSize = "
			   + bufferSize);
	System.out.println("SoundCapture: constructor 1: getSamplesSize = "
			   + getSamplesSize);
	
    }

    /** Construct a sound capture object. This constructor creates an
     *  object that captures audio from a sound file.
     *  @param isURL True means that a URL to a file is given. False means
     *  that the file name specifies the location of the file on
     *  the local filesystem.
     *  @param fileName The name of the file. This can be either a URL
     *  file name or a local file sytem file name.
     *  @param getSamplesSize Size of the array returned by
     *   <i>getSamples()</i>.
     */
    public SoundCapture(boolean isURL, String fileName,
			int getSamplesSize) {
	System.out.println("SoundCapture: constructor 2: invoked");
	// Set mode to "capture from file" (not real-time).
	this._isRealTime = false;
	this._isURL = isURL;
	this._fileName = fileName;
	this._productionRate = getSamplesSize;
    }

    ///////////////////////////////////////////////////////////////
    ///  Public Methods                                         ///
    
    /** Begin capturing audio. This method must be invoked prior
     *  to the first invocation of <i>getSamples</i>. This method
     *  must not be called more than once between invocations of
     *  <i>stopCapture()</i>.
     */
    public void startCapture() {
	
	if (_isRealTime == true) {
	    _startCaptureRealTime();
	} else {
	    _startCaptureFromFile();
	}
    }

    /** Stop capturing audio. This method should be called when
     *  no more calls to <i>getSamples()</i. are required, so
     *  that the system resources involved in the audio capture
     *  may be freed.
     */
    public void stopCapture() {
	try {
	    // Free up audio system resources.
	    // For capture from file:
	    if (_audioInputStream != null) {
		_audioInputStream.close();
	    }
	    if (_properFormatAudioInputStream != null) {
		_properFormatAudioInputStream.close();
	    }
	    // For real-time capture:
	    if (_targetLine != null) {
		_targetLine.close();
	    }
	} catch (IOException e) {
	    System.out.println("AudioSource: error closing"+
                    " audio resources: " +e);
	}
    }

    /** Return an array of captured audio samples. The array size
     *  is set by the <i>getSamplesSize</i> parameter in the
     *  constructor. For the case where audio is captured from
     *  the computer's audio-in port (mic or line-in), this
     *  method should be called often enough to prevent overflow
     *  of the internal audio buffer, the size of which is set
     *  in the constructor.
     *  @return Array of captured audio samples. Return null
     *  if end of audio file is reached (only applicable when
     *  capturing from a sound file).
     */
    // FIXME: Should return multidimensional array (For > 1 channel
    // case).
    // FIXME: What if < productionRate samples in sound file?
    public double[] getSamples() {
	//System.out.println("SoundCapture: getSamples(): invoked");
	try {
	    int numBytesRead;
	    if (_isRealTime == true) {
		//System.out.println("SoundCapture: getSamples(): _data.length "
		//		   + _data.length);
		//System.out.println("SoundCapture: getSamples(): _productionRate "
		//		   + _productionRate);
		//System.out.println("SoundCapture: getSamples(): _frameSizeInBytes "
		//		   + _frameSizeInBytes);
		

		// Real-time capture.
		numBytesRead = _targetLine.read(_data, 0,
				    _productionRate*_frameSizeInBytes);

		//System.out.println("SoundCapture: getSamples(): numBytesRead "
		//		   + numBytesRead);
		    
	    } else {
		// Capture audio from file.
		numBytesRead =
		    _properFormatAudioInputStream.read(_data);
	    }
	    // FIXME: Optimize ordering here.
            if (numBytesRead == -1) {
                // Ran out of samples to play. This generally means
                // that the end of the sound file has been reached.
		//System.out.println("SoundCapture: getSamples(): returning null 1");
                return null;
            } else if (numBytesRead != _data.length) {
                // Read fewer samples than productionRate many samples.

                // FIXME: Output the samples that were read + zeros?
		//System.out.println("SoundCapture: getSamples(): returning null 2");
                return null;
            } else {
		// Convert byte array to double array.
		_audioInDoubleArray =
		    _byteArrayToDoubleArray(_data, _frameSizeInBytes);
		//System.out.println("SoundCapture: getSamples(): returning some data");
		return _audioInDoubleArray;
	    }
        } catch (IOException ex) {
            System.out.println("Could not capture audio: " + ex);
	    ex.printStackTrace();
        }
	// FIXME
	//System.out.println("SoundCapture: getSamples(): returning null 3");
	return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _startCaptureRealTime() {
	
        int frameSizeInBits = _sampleSizeInBits;
        double frameRate = _sampleRate;
        boolean signed = true;
        boolean bigEndian = true;

        AudioFormat format = new AudioFormat(_sampleRate,
                _sampleSizeInBits,
                _channels, signed, bigEndian);

        _frameSizeInBytes = format.getFrameSize();

	DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class,
                format
                ,AudioSystem.NOT_SPECIFIED);

        if (!AudioSystem.isLineSupported(targetInfo)) {
	    // FIXME: throw exception here.
            System.out.println("Line matching " + targetInfo +
			       " not supported.");
            return;
        }

        try {
            _targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
	    // Note: 2nd parameter is the buffer size (in bytes).
	    // Larger vaules increase latency but may be required if
	    // garbage collection, etc. is an issue.
            _targetLine.open(format, _bufferSize*_frameSizeInBytes);
        } catch (LineUnavailableException ex) {
	    // FIXME: is this right?
            System.err.println("Unable to open the line: " + ex);
            return;
        }

        System.out.println("JavaSound target (microphone/line in)" +
                "line buffer size in bytes = " +
                _targetLine.getBufferSize());

        int targetBufferLengthInBytes = _productionRate *
            _frameSizeInBytes;
	System.out.println("frameSizeInBytes = " + _frameSizeInBytes);
        byte[] targetData = new byte[targetBufferLengthInBytes];

	DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class,
                format,
                AudioSystem.NOT_SPECIFIED);

	if (!AudioSystem.isLineSupported(sourceInfo)) {
	    //FIXME: handle this correctly.
	    System.err.println("Line matching " + sourceInfo + 
			       " not supported.");
	    return;
	}

	// Array of audio samples in byte format.
	_data = new byte[_productionRate*_frameSizeInBytes];

	// Start the target data line
	_targetLine.start();

    }


    /* Perform necessary initialization to caputre from a sound
     * file. The sound file can be specified as a URL or as a
     * filename on the local filesystem.
     */
    private void _startCaptureFromFile() {
	try {
	    if (_isURL == true) {
		// Load audio from a URL.

		// Create a URL corresponing to the sound file location.
		URL soundURL =
		    new URL(_fileName);
               
		if (soundURL != null) {
		    try {
			_audioInputStream = 
			    AudioSystem.getAudioInputStream(soundURL);
		    } catch (UnsupportedAudioFileException e) {
			System.out.println("UnsupportedAudioFileException "
					   + e);
		    } catch (IOException e) {
			System.out.println("IOException " + e);
		    }
		}

	    } else {
		// Load audio from a file.

		File soundFile =
		    new File(_fileName);
		
		if (soundFile != null && soundFile.isFile()) {
		    try {
			_audioInputStream = 
			    AudioSystem.getAudioInputStream(soundFile);
		    } catch (UnsupportedAudioFileException e) {
			System.out.println("UnsupportedAudioFileException "
					   + e);
		    } catch (IOException e) {
			System.out.println("IOException " + e);
		    }
		}
	    }
	    // make sure we have something to play
	    if (_audioInputStream == null) {
		System.out.println("No loaded audio to play back");
		return;
	    }
	    
	    AudioFormat origFormat = _audioInputStream.getFormat();
	    // Now convert to PCM_SIGNED_BIG_ENDIAN so that can get double
	    // representation of samples.
	    float sampleRate = origFormat.getSampleRate();
	    System.out.println("AudioSource: sampling rate = " +
			       sampleRate);
	    
	    int sampleSizeInBits = origFormat.getSampleSizeInBits();
	    System.out.println("AudioSource: sample size in bits = " +
			       sampleSizeInBits);
	    
	    int channels = origFormat.getChannels();
	    boolean signed = true;
	    boolean bigEndian = true;
	    AudioFormat format = new AudioFormat(sampleRate,
						 sampleSizeInBits, channels,
						 signed, bigEndian);
	    System.out.println("Converted format: " + format.toString());
	    
	    _properFormatAudioInputStream =
		AudioSystem.getAudioInputStream(format, _audioInputStream);
	    _frameSizeInBytes = format.getFrameSize();
	    // Array of audio samples in byte format.
	    _data = new byte[_productionRate*_frameSizeInBytes];
	    
	    // Initialize the index to the first sample of the sound file.
	    _index = 0;

	} catch (MalformedURLException e) {
	    System.out.println(e.toString());
	} catch (IOException e) {
	    System.out.println("AudioSource: error reading"+
                    " input file: " +e);
	}
    }

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
	double mathDotPow = Math.pow(2, 8 * _bytesPerSample - 1);

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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private AudioInputStream  _properFormatAudioInputStream;

    private AudioInputStream _audioInputStream;

    private int _productionRate;

    // Array of audio samples in double format.
    private double[] _audioInDoubleArray;

    // Array of audio samples in byte format.
    private byte[] _data;

    private int _index;

    private int _frameSizeInBytes;

    private boolean _isURL;

    private boolean _isRealTime;

    private String _fileName;


    private int _sampleSizeInBits;
    
    private float _sampleRate;
    
    private int _channels;

    private int _bufferSize;

    private TargetDataLine _targetLine;
}
