/* A buffer supporting the capturing of audio.

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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.media.javasound;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import javax.sound.sampled.*;

//////////////////////////////////////////////////////////////////////////
//// SoundCapture
/**
   <h2>Overview</h2>
   A buffer supporting the capture of audio. This class supports the
   real-time capture of audio from the audio input port (mic or line-in)
   as well as the capture of audio from a sound file. Single channel
   (mono) and multichannel audio (stereo) are supported. This class,
   along with SoundPlayback, intends to provide an easy to use interface
   to Java Sound, Java's audio API.
   <p>
   Depending on available audio
   system resources, it may be possible to run an instance of this
   class and an instance of SoundPlayback concurrently. This allows
   for the concurrent capture, processing, and playback of audio data.
   <p>
   <h2>Usage</h2>
   Two constructors are provided. One constructor creates a sound capture
   object that captures from the line-in or microphone port. If this
   constructor is used, there will be a small
   delay between the time that the audio enters the microphone or
   line-in and the time that the corresponding audio samples are
   available via getSamples().
   This latency can be adjusted by setting the <i>bufferSize</i>
   constructor parameter. Another constructor creates a sound capture
   object that captures audio from a sound file.
   <p>
   After calling the appropriate constructor, startCapture()
   must be called to initialize the audio system for capture.
   The getSamples() method should then be repeatedly
   invoked to obtain audio data in the form of a multidimensional
   array of audio sample values. For the case where
   audio is captured from the mic or line-in, it is important to
   invoke getSamples() often enough to prevent overflow of
   the internal audio buffer. The size of the internal buffer is
   set in the constructor.
   Finally, after no more audio data is desired, stopCapture()
   should be called to free up audio system resources.
   <p>
   <h2>Security issues</h2>
   Applications have no restrictions on the
   capturing or playback of audio. Applets, however, may only capture
   audio from a file specified as a URL on the same machine as the
   one the applet was loaded from. Applet code is not allowed to
   read or write native files. The .java.policy file must be
   modified to grant applets more privileges.
   <p>
   Note: Requires Java 2 v1.3.0 RC1 or later.

   @author Brian K. Vogel
   @version $Id$
   @see ptolemy.media.javasound.SoundPlayback
*/

public class SoundCapture {

    /** Construct a sound capture object that captures audio from a computer's
     *  audio input port.  If this constructor is used, then it
     *  is important that getSamples() be
     *  invoked often enough to prevent overflow of the internal audio
     *  input buffer.
     *  @param sampleRate Sample rate in Hz. Must be in the range: 8000
     *   to 48000.
     *  @param sampleSizeInBits Number of bits per sample. Choices are
     *   8 or 16.
     *  @param channels Number of audio channels. 1 for mono, 2 for
     *   stereo.
     *  @param bufferSize Requested size of the internal audio input
     *   buffer in samples. This controls the latency. A lower bound
     *   on the latency is given by (<i>bufferSize</i> / <i>sampleRate</i>)
     *   seconds. Ideally, the
     *   smallest value that gives acceptable performance (no overflow)
     *   should be used. Typical values are about 1/10 th the sample
     *   rate. For example, at 44100 Hz sample rate, a typical buffer
     *   size value might be 4410.
     *  @param getSamplesSize Size of the array returned by
     *   <i>getSamples()</i>. For performance reasons, the size should
     *   be chosen smaller than <i>bufferSize</i>. Typical values
     *   are 1/2 to 1/16th of <i>bufferSize</i>.
     */
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

    /** Construct a sound capture object that caputures audio from a
     *  sound file. This constructor creates an
     *  object that captures audio from a sound file.
     *  
     *  @param isURL True means that a URL to a file is given. False means
     *   that the file name specifies the location of the file on
     *   the local file system.
     *  @param fileName The name of the file. This can be either a URL
     *   file name or a local file system file name. Valid sound file
     *   formats are WAVE (.wav), AIFF (.aif, .aiff), AU (.au). The file
     *   format is automatically determined from the file extension.
     *  @param getSamplesSize The number of samples per channel
     *   returned by getSamples().
     */
    // FIXME: Get rid of isURL!!!!! NOt needed!  Have it take a
    //  URL, not a file name.
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
     *  to the first invocation of getSamples(). If this is not
     *  done, then getSamples() will throw an exception when
     *  it is invoked. It is safe
     *  to call getSamples() immediately after this method returns.
     *   This method must not be called more than 
     *  once between invocations of stopCapture(). Calling
     *  this method more than once between invocations of 
     *  stopCapture() will cause this method to throw an exception.
     *  
     *  @exception IOException If there is a problem setting up
     *  the system for audio capture. This will occur if the
     *  a URL cannot be oppened or if the audio in port cannot
     *  be accessed.
     *  @exception IllegalStateException If this method is called
     *  more than once between invocations of stopCapture().
     */
    public void startCapture() throws IOException, 
                               IllegalStateException {
	// FIXME: check and throw Exceptions
	if (_isRealTime == true) {
	    _startCaptureRealTime();
	} else {
	    _startCaptureFromFile();
	}
    }

    /** Stop capturing audio. This method should be called when
     *  no more calls to <i>getSamples()</i>. are required, so
     *  that the system resources involved in the audio capture
     *  may be freed.
     *
     *  @exception IOException If there is a problem closing the
     *  audio resources.
     */
    public void stopCapture() throws IOException {
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

    /** Return an array of captured audio samples. This method
     *  should be repeatedly called to obtain audio data. The 
     *  array size
     *  is set by the <i>getSamplesSize</i> parameter in the
     *  constructor. For the case where audio is captured from
     *  the computer's audio-in port (mic or line-in), this
     *  method should be called often enough to prevent overflow
     *  of the internal audio buffer, the size of which is set
     *  in the constructor.
     *  @return Two dimensional array of captured audio samples.
     *   Return null
     *  if end of audio file is reached A null return value is
     *  only possible when capturing from a sound file.
     *  The first index
     *  represents the channel number (0 for first channel, 1 for
     *  second channel, etc.). The second index represents the
     *  sample index within a channel. For example,
     *  <i>returned array</i>[n][m] contains the (m+1)th sample
     *  of the (n+1)th channel. For each channel, n, the length of
     *  <i>returned array</i>[n] is equal to <i>getSamplesSize</i>.
     *
     *  @exception IOException If there is a problem capturing audio.
     *  @exception IllegalStateException If startCapture() has not
     *  yet been called.
     */
    public double[][] getSamples() {
	//System.out.println("SoundCapture: getSamples(): invoked");
	try {
	    int numBytesRead;
	    if (_isRealTime == true) {
		// Real-time capture.
		numBytesRead = _targetLine.read(_data, 0,
                        _productionRate*_frameSizeInBytes);

	    } else {
		// Capture audio from file.
		numBytesRead =
		    _properFormatAudioInputStream.read(_data);
	    }
            if (numBytesRead == _data.length) {
		// Convert byte array to double array.
		_audioInDoubleArray =
		    _byteArrayToDoubleArray(_data,
                            _bytesPerSample,
                            _channels);
		//System.out.println("SoundCapture: getSamples(): returning some data");
		return _audioInDoubleArray;
            } else if (numBytesRead != _data.length) {
                // Read fewer samples than productionRate many samples.

                // FIXME: Output the samples that were read + zeros?
                return null;
            } else if (numBytesRead == -1) {
		// Ran out of samples to play. This generally means
                // that the end of the sound file has been reached.
		//System.out.println("SoundCapture: getSamples(): returning null 1");
                return null;
	    }
        } catch (IOException ex) {
            System.err.println("Could not capture audio: " + ex);
	    ex.printStackTrace();
        }
	return null;
    }

    /** Return the number of audio channels. This method should
     *  be called while audio capture is active, i.e., after
     *  <i>startCapture()</i> is called and before <i>stopCapture</i>
     *  is called.
     * @return The number of audio channels. Return null if
     *  this method is called before <i>startCapture()</i>.
     */
    public int getChannels() {
	return _channels;
    }

    /** Return the number of bits per audio sample. This method should
     *  be called while audio capture is active, i.e., after
     *  <i>startCapture()</i> is called and before <i>stopCapture</i>
     *  is called.
     * @return The sample size in bits. Return null if
     *  this method is called before <i>startCapture()</i>.
     */
    public int getSampleSizeInBits() {
	return _sampleSizeInBits;
    }

    /** Return the sampling rate in Hz. This method should
     *  be called while audio capture is active, i.e., after
     *  <i>startCapture()</i> is called and before <i>stopCapture</i>
     *  is called.
     * @return The sample rate in Hz. Return null if
     *  this method is called before <i>startCapture()</i>.
     */
    public float getSampleRate() {
	return _sampleRate;
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
                format, AudioSystem.NOT_SPECIFIED);

        if (!AudioSystem.isLineSupported(targetInfo)) {
	    // FIXME: throw exception here.
            System.out.println("Line matching " + targetInfo +
                    " not supported.");
            return;
        }

        try {
            _targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
	    // Note: 2nd parameter is the buffer size (in bytes).
	    // Larger values increase latency but may be required if
	    // garbage collection, etc. is an issue.
            _targetLine.open(format, _bufferSize*_frameSizeInBytes);
        } catch (LineUnavailableException ex) {
	    // FIXME: is this right?
            System.err.println("Unable to open the line: " + ex);
            return;
        }

        System.out.println("JavaSound (microphone/line in)" +
                "line buffer size in samples = " +
                _targetLine.getBufferSize()/_frameSizeInBytes);

        int targetBufferLengthInBytes = _productionRate *
            _frameSizeInBytes;
	System.out.println("frameSizeInBytes = " + _frameSizeInBytes);

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

	_bytesPerSample = _sampleSizeInBits/8;

	// Start the target data line
	_targetLine.start();

    }


    /* Perform necessary initialization to capture from a sound
     * file. The sound file can be specified as a URL or as a
     * filename on the local file system.
     */
    private void _startCaptureFromFile() {
	try {
	    if (_isURL == true) {
		// Load audio from a URL.

		// Create a URL corresponding to the sound file location.
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

	    _sampleSizeInBits = origFormat.getSampleSizeInBits();
	    _bytesPerSample = _sampleSizeInBits/8;
	    System.out.println("AudioSource: sample size in bits = " +
                    _sampleSizeInBits);

	    _channels = origFormat.getChannels();
	    boolean signed = true;
	    boolean bigEndian = true;
	    AudioFormat format = new AudioFormat(sampleRate,
                    _sampleSizeInBits, _channels,
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
    private double[][] _byteArrayToDoubleArray(byte[] byteArray,
            int bytesPerSample,
            int channels) {
	int lengthInSamples = byteArray.length / (bytesPerSample*channels);
	double[][] doubleArray = new double[channels][lengthInSamples];
	//double maxSampleReciprocal = 1/(Math.pow(2, 8 * bytesPerSample - 1));
	// Could use above line, but hopefully, code below will
	// be faster.
	double maxSampleReciprocal;
	if (bytesPerSample == 2) {
	    // 1 / 32768
	    maxSampleReciprocal = 3.0517578125e-5;
	} else if (bytesPerSample == 1) {
	    // 1 / 128
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

	byte[] b = new byte[bytesPerSample];

	for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {

	    // For each channel,
	    for (int currChannel = 0; currChannel < channels; currChannel++) {
		for (int i = 0; i < bytesPerSample; i += 1) {
		    // Assume we are dealing with big endian.
		    b[i] = byteArray[currSamp*bytesPerSample*channels +
                            bytesPerSample*currChannel + i];
		}
		long result = (b[0] >> 7) ;
		for (int i = 0; i < bytesPerSample; i += 1)
		    result = (result << 8) + (b[i] & 0xff);
		doubleArray[currChannel][currSamp] =
		    ((double) result*maxSampleReciprocal);
	    }
        }
	return doubleArray;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private AudioInputStream  _properFormatAudioInputStream;
    private AudioInputStream _audioInputStream;
    private int _productionRate;
    // Array of audio samples in double format.
    private double[][] _audioInDoubleArray;
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
    private int _bytesPerSample;
}
