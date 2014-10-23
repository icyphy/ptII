/* A buffer that supports the reading of audio samples from a sound
 file specified as a URL.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.media.javasound;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

///////////////////////////////////////////////////////////////////
//// SoundReader

/**
 This class is a buffer that supports the reading of audio samples
 from a sound file that is specified as a URL. Specifically, this
 buffer supports the reading of double-valued audio samples. The
 maximum valid range of sample values is from -1.0 to 1.0. Any
 values outside of this range will be hard-clipped to fall within
 this range.
 <p>
 <b>Supported file types</b>
 <p>
 Valid sound file formats are WAVE (.wav), AIFF (.aif, .aiff),
 AU (.au). Valid sample rates are 8000, 11025, 22050, 44100, and
 48000 Hz. Both 8 bit and 16 bit audio are supported. Mono and
 stereo files are supported.
 <p>
 <b>Usage</b>
 <p>
 The path to the sound file, specified as a URL, is given as a
 constructor parameter. The constructor also takes an array
 length parameter, which is explained below. The constructor will
 attempt to open the specified file.
 <p>
 After invoking the constructor, the getSamples() method should
 be repeatedly invoked to read samples from the specified sound
 file. The getSamples() method takes a multidimensional array
 as a parameter. The first index represents the channel number
 (0 for first channel, 1 for  second channel, etc.). The second
 index represents the sample index within a channel. For each
 channel i, the size of the array, getSamplesArray[i].length, must
 be equal to the constructor parameter getSamplesArraySize.
 Otherwise an exception will occur. When the end of the sound
 file is reached, this method will return null.
 <p>
 The getChannels(), getSampleRate(), and getBitsPerSample()
 methods may be invoked at any time to obtain information about
 the format of the sound file.
 <p>
 When no more samples are desired, the closeFile() method should
 be invoked to close the sound file. An exception will occur if
 getSamples() is invoked at any point after closeFile() is invoked.
 <p>
 <b>Security Issues</b>
 <p>
 Applications have no restrictions on the  capturing or playback
 of audio. Applets, however, may by default only capture
 audio from a file specified as a URL on the same machine as the
 one the applet was loaded from. Applet code is not allowed to
 read or write native files. The .java.policy file may be
 modified to grant applets more privileges.
 <p>
 Note: Requires Java 2 v1.3.0 or later.

 @author Brian K. Vogel
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.media.javasound.SoundWriter
 */
public class SoundReader {
    /** Construct a sound reader object that reads audio samples
     *  from a sound file specified as a string describing a
     *  URL and open the file at the specified URL.
     *  For example, to capture samples from the
     *  URL http://localhost/soundfile.wav, sourceURL should be set
     *  to the string "http://localhost/soundfile.wav" Note that the
     *  "http://" is required.
     *  Note that it is still possible to capture audio from a file on
     *  the local file system. For example, to capture from a sound
     *  file located at "C:\someDir\someFile.wave", <i>sourceURL</i>
     *  should be set to "file:c:\someDir\someFile.wave". Relative
     *  file paths are not supported, so the complete path must always
     *  be specified, using the prefix "file:" It is safe
     *  to call getSamples() immediately after this constructor returns.
     *
     *  @param sourceURL A string describing a URL.
     *  @param getSamplesArraySize The number of samples per channel
     *   returned by getSamples().
     *  @exception IOException If opening the sourceURL throws it,
     *  if the file format is not supported or if there is no audio
     *  to play.
     */
    public SoundReader(String sourceURL, int getSamplesArraySize)
            throws IOException {
        _productionRate = getSamplesArraySize;

        // Create a URL corresponding to the sound file location.
        URL soundURL = new URL(sourceURL);

        // Open the specified sound file.
        _openFileFromURL(soundURL);
        _isAudioCaptureActive = true;
    }

    /** Construct a sound reader object that reads audio samples
     *  from a sound file specified as a URL and open the file at
     *  the specified URL. It is safe
     *  to call getSamples() immediately after this constructor returns.
     *  @param soundURL The URL of a sound file.
     *  @param getSamplesArraySize The number of samples per channel
     *   returned by getSamples().
     *  @exception IOException If opening the sourceURL throws it,
     *  if the file format is not supported or if there is no audio
     *  to play.
     */
    public SoundReader(URL soundURL, int getSamplesArraySize)
            throws IOException {
        _productionRate = getSamplesArraySize;

        // Open the specified sound file.
        _openFileFromURL(soundURL);
        _isAudioCaptureActive = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of audio channels. This method should
     *  be called while the file is open (i.e., before closeFile()
     *  is called). Otherwise an exception will occur.
     *  @return The number of audio channels.
     *  @exception IllegalStateException If this method is called
     *   before openFile() is called or after closeFile()
     *   is called.
     */
    public int getChannels() throws IllegalStateException {
        if (_isAudioCaptureActive == true) {
            return _channels;
        } else {
            throw new IllegalStateException("SoundReader: "
                    + "getChannels() was called while audio capture was"
                    + " inactive (openFile() was never called).");
        }
    }

    /** Return the sampling rate in Hz. An exception will occur if
     *  this method is invoked after closeFile() is called.
     *  @return The sample rate in Hz.
     *  @exception IllegalStateException If this method is called
     *   after closeFile()
     *   is called.
     */
    public float getSampleRate() throws IllegalStateException {
        if (_isAudioCaptureActive == true) {
            return _sampleRate;
        } else {
            throw new IllegalStateException("SoundReader: "
                    + "getSampleRate() was called while audio capture was"
                    + " inactive (openFile() was never called).");
        }
    }

    /** Return an array of captured audio samples. This method
     *  should be repeatedly called to obtain audio data.
     *  The returned audio samples will have values in the range
     *  [-1, 1], regardless of the audio bit resolution (bits per
     *  sample). This method performs a blocking read, so it is not
     *  possible to invoke this method too frequently.
     *  <p>
     *  The array size is set by the <i>getSamplesSize</i>
     *  parameter in the constructor.
     *  @return Two dimensional array of captured audio samples.
     *   Return null if end of the audio file is reached.
     *   The first index represents the channel number (0 for
     *   first channel, 1 for second channel, etc.). The second
     *   index represents the sample index within a channel. For
     *   example, <i>returned array</i>[n][m] contains the (m+1)th sample
     *   of the (n+1)th channel. For each channel, n, the length of
     *   <i>returned array</i>[n] is equal to <i>getSamplesSize</i>.
     *
     *  @exception IOException If there is a problem reading the audio
     *   samples from the input file.
     *  @exception IllegalStateException If closeFile() has already
     *   been called.
     */
    public double[][] getSamples() throws IOException, IllegalStateException {
        if (_isAudioCaptureActive == true) {
            if (_debug) {
                System.out.println("SoundReader: getSamples(): invoked");
            }

            int numBytesRead;

            if (_debug) {
                System.out.println("SoundReader: getSamples(): "
                        + "bytes available = "
                        + _properFormatAudioInputStream.available());
            }

            // Capture audio from file.
            numBytesRead = _properFormatAudioInputStream.read(_data);

            if (_debug) {
                System.out.println("SoundReader: getSamples(): "
                        + "numBytesRead = " + numBytesRead);
            }

            if (numBytesRead == _data.length) {
                // Convert byte array to double array.
                _audioInDoubleArray = _byteArrayToDoubleArray(_data,
                        _bytesPerSample, _channels);
                return _audioInDoubleArray;
            } else if (numBytesRead == -1) {
                // Ran out of samples to play. This generally means
                // that the end of the sound file has been reached.
                if (_debug) {
                    System.out.println("SoundReader: getSamples(): "
                            + "numBytesRead = -1, so "
                            + "returning null now...");
                }

                return null;
            } else if (numBytesRead != _data.length) {
                // Read fewer samples than productionRate many samples.
                // Note: There appears to be a java sound bug that
                // causes AudioInputStream.read(array) to sometimes
                // return fewer bytes than requested, even though
                // the end of the file has not yet been reached.
                _audioInDoubleArray = _byteArrayToDoubleArray(_data,
                        _bytesPerSample, _channels);
                return _audioInDoubleArray;
            } else {
                return null;
            }
        } else {
            throw new IllegalStateException("SoundReader: "
                    + "getSamples() was called while audio capture was"
                    + " inactive (openFile() was never called or "
                    + "closeFile has already been called).");
        }
    }

    /** Close the file at the specified URL. This method should
     *  be called when no more calls to getSamples(). are required.
     *
     *  @exception IOException If there is a problem closing the
     *  file.
     */
    public void closeFile() throws IOException {
        System.out.println("SoundReader: closeFile() invoked");

        if (_isAudioCaptureActive == true) {
            // Free up audio system resources.
            if (_audioInputStream != null) {
                _audioInputStream.close();
            }

            if (_properFormatAudioInputStream != null) {
                _properFormatAudioInputStream.close();
            }
        }

        _isAudioCaptureActive = false;
    }

    /** Return the number of bits per audio sample. An exception
     *  will occur if this method is called after closeFile() is
     *  invoked.
     *
     * @return The sample size in bits.
     *
     * @exception IllegalStateException If this method is called
     *  after closeFile()
     *  is called.
     */
    public int getBitsPerSample() throws IllegalStateException {
        if (_isAudioCaptureActive == true) {
            return _sampleSizeInBits;
        } else {
            throw new IllegalStateException("SoundReader: "
                    + "getSampleSizeInBits() was called while "
                    + "audio capture was"
                    + " inactive (openFile() was never called).");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Perform initialization. Open the file at the specified URL.
     * Discover the format of the file and set the corresponding
     * private variables to the sample rate, bits per samples, and
     * number of audio channels of the file.
     */
    private void _openFileFromURL(URL soundURL) throws IOException {
        if (soundURL != null) {
            try {
                _audioInputStream = AudioSystem.getAudioInputStream(soundURL);
            } catch (UnsupportedAudioFileException e) {
                throw new IOException("Unsupported AudioFile :" + e);
            }
        }

        // make sure we have something to play
        if (_audioInputStream == null) {
            throw new IOException("No loaded audio to play back");
        }

        AudioFormat origFormat = _audioInputStream.getFormat();

        // Now convert to PCM_SIGNED_BIG_ENDIAN so that can get double
        // representation of samples.
        _sampleRate = origFormat.getSampleRate();

        if (_debug) {
            System.out.println("SoundReader: sampling rate = " + _sampleRate);
        }

        _sampleSizeInBits = origFormat.getSampleSizeInBits();
        _bytesPerSample = _sampleSizeInBits / 8;

        if (_debug) {
            System.out.println("SoundReader: sample size in bits = "
                    + _sampleSizeInBits);
        }

        _channels = origFormat.getChannels();

        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(_sampleRate, _sampleSizeInBits,
                _channels, signed, bigEndian);

        if (_debug) {
            System.out.println("Converted format: " + format.toString());
        }

        try {
            _properFormatAudioInputStream = AudioSystem.getAudioInputStream(
                    format, _audioInputStream);
        } catch (IllegalArgumentException e) {
            // Interpret a failed conversion to mean that
            // the input sound file has an unsupported format.
            throw new IOException("Unsupported audio file format: " + e);
        }

        _frameSizeInBytes = format.getFrameSize();

        // Array of audio samples in byte format.
        _data = new byte[_productionRate * _frameSizeInBytes];

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
            int bytesPerSample, int channels) {
        int lengthInSamples = byteArray.length / (bytesPerSample * channels);

        // Check if we need to reallocate.
        if (channels != _doubleArray.length
                || lengthInSamples != _doubleArray[0].length) {
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

        // Check if we need to reallocate.
        // Note: This test is really not needed since bytesPerSample
        // is set in the constructor. It should never change.
        if (bytesPerSample != _b.length) {
            _b = new byte[bytesPerSample];
        }

        for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {
            // For each channel,
            for (int currChannel = 0; currChannel < channels; currChannel++) {
                for (int i = 0; i < bytesPerSample; i += 1) {
                    // Assume we are dealing with big endian.
                    _b[i] = byteArray[currSamp * bytesPerSample * channels
                                      + bytesPerSample * currChannel + i];
                }

                int result = _b[0] >> 7;

                for (int i = 0; i < bytesPerSample; i += 1) {
                    result = (result << 8) + (_b[i] & 0xff);
                }

                _doubleArray[currChannel][currSamp] = result
                        * maxSampleReciprocal;
            }
        }

        return _doubleArray;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private AudioInputStream _properFormatAudioInputStream;

    private AudioInputStream _audioInputStream;

    private int _productionRate;

    // Array of audio samples in double format.
    private double[][] _audioInDoubleArray;

    // Array of audio samples in byte format.
    private byte[] _data;

    private int _frameSizeInBytes;

    private int _sampleSizeInBits;

    private float _sampleRate;

    private int _channels;

    private int _bytesPerSample;

    private boolean _isAudioCaptureActive;

    private byte[] _b = new byte[1];

    private double[][] _doubleArray = new double[1][1];

    /////////////// For debugging: ///////////////////////////////
    // Set this variable to "true" to enable debugging information.
    private boolean _debug = false;

    ///////////////////////////////////////////////////////////////////
}
