/* A buffer supporting the playback of audio data and the the
   writing of audio data to a sound file.

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

@ProposedRating Yellow (vogel@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.media.javasound;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/////////////////////////////////////////////////////////////
//// SoundPlayback
/**
A buffer supporting the playback of audio data and the the
writing of audio data to a sound file.

   <h2>Overview</h2>
   A buffer supporting the real-time playback of audio and the writing
   of audio data to a sound file. Single channel
   (mono) and multichannel audio (stereo) are supported. This class,
   along with SoundCapture, intends to provide an easy to use interface
   to Java Sound, Java's audio API. Java Sound supports the writing
   of audio data to a sound file or the computer's audio output port,
   but only at the byte level, which is audio format specific. This class,
   however, provides higher level support for the writing of double
   or integer valued samples to the computer's audio output port or
   any supported sound file type. This class is therefore useful when
   it one desires to playback audio samples in an audio format independent
   way.
   <p>
   Depending on available system resources, it may be possible to
   run an instance of this class and an instance of SoundCapture
   concurrently. This allows for the concurrent capture, signal
   processing, and playback of audio data.
   <p>
   <h2>Usage</h2>
   Two constructors are provided. One constructor creates a sound playback
   object that sends audio data to the speaker. If this constructor is
   used, there will be a small
   delay between the time that the audio data is delivered to this
   object and the time that the corresponding audio is actually
   heard. This latency can be adjusted by setting the <i>bufferSize</i>
   constructor parameter.  Another constructor
   creates a sound playback object that sends audio data to a sound
   file.
   <p>
   After calling the appropriate constructor, startPlayback()
   must be called to initialize the audio system.
   The putSamples() or putSamplesInt() method should then be repeatedly
   called to deliver the audio data to the audio output device
   (speaker or file). The audio samples delivered to putSamples()
   should be in the proper range, or clipping will occur.
   putSamples() expects the samples to be in the range (-1, 1).
   putSamplesInt() expects the samples to be in the range
   (-2^(bits_per_sample/2), 2^(bits_per_sample/2)), where
   bits_per_sample is the number of bits per sample.
   Note that it is possible (but probably
   not useful) to interleave calls to putSamples() and
   putSamplesInt().
   Finally, after no more audio playback is desired, stopPlayback()
   should be called to free up audio system resources.
   <p>
   <h2>Security issues</h2>Applications have no restrictions on the
   capturing or playback of audio. Applet code is not allowed to
   write native files by default. The .java.policy file must be
   modified to grant applets more privileges.
   <p>
   Note: Requires Java 2 v1.3.0 or later.
   @author Brian K. Vogel
   @version $Id$
   @since Ptolemy II 1.0
   @see ptolemy.media.javasound.SoundCapture
*/

public class SoundPlayback {

    /** Construct a sound playback object that plays audio through the
     *  computer's speaker. Note
     *  that when this constructor is used, putSamples() should be
     *  called often enough to prevent underflow of the internal audio
     *  input buffer.
     *  @param sampleRate Sample rate in Hz. Must be in the range: 8000
     *  to 48000.
     *  @param sampleSizeInBits Number of bits per sample (valid choices are
     *  8 or 16).
     *  @param channels Number of audio channels. 1 for mono, 2 for
     *  stereo, etc.
     *  @param bufferSize Requested size of the internal audio input
     *   buffer in samples. This controls the latency (delay from
     *   the time putSamples() is called until the audio is
     *   actually heard). A lower bound on the latency is given by
     *   (<i>bufferSize</i> / <i>sampleRate</i>) seconds.
     *   Ideally, the
     *   smallest value that gives acceptable performance (no underflow)
     *   should be used. Typical values are about 1/10 th the sample
     *   rate. For example, at 44100 Hz sample rate, a typical buffer
     *   size value might be 4410.
     *  @param putSamplesSize Size of the array parameter of
     *   putSamples(). For performance reasons, the size should
     *   be chosen smaller than <i>bufferSize</i>. Typical values
     *   are 1/2 to 1/16 th of <i>bufferSize</i>.
     */
    public SoundPlayback(float sampleRate, int sampleSizeInBits,
            int channels, int bufferSize,
            int putSamplesSize) {
        _isAudioPlaybackActive = false;
        // Set mode to real-time.
        this._playbackMode = "speaker";
        this._sampleSizeInBits = sampleSizeInBits;
        this._sampleRate = sampleRate;
        this._channels = channels;
        this._bufferSize = bufferSize;
        this._putSamplesSize = putSamplesSize;
    }

    /** Construct a sound playback object that writes audio to
     *  a sound file with the specified name.  Valid sound file
     *  formats are WAVE (.wav), AIFF (.aif, .aiff), AU (.au). The file
     *  format is automatically determined from the file extension.
     *  The sound file will be initialized when startPlayback() is
     *  called. If there is a problem creating the sound file, an
     *  IOException will be thrown in startPlayback().
     *  Thereafter, each call to putSamples() will add
     *  <i>putSamplesSize</i> samples to the sound file. To
     *  close and save the sound file, call stopPlayback().
     *  <p>
     *  Note that the audio data will not actually be saved to file,
     *  <i>fileName</i>, until stopPlayback() is called. If an
     *  unknown audio format is used, an exception will be thrown
     *  in stopPlayback().
     *  @param fileName The file name to create. If the file already
     *  exists, overwrite it. Valid sound file formats are WAVE (.wav),
     *  AIFF (.aif, .aiff), AU (.au). The file format to write is
     *  determined automatically from the file extension.
     *  @param sampleRate Sample rate in Hz. Must be in the range: 8000
     *  to 48000.
     *  @param sampleSizeInBits Number of bits per sample (valid choices are
     *  8 or 16).
     *  @param channels Number of audio channels. 1 for mono, 2 for
     *  stereo.
     *  @param putSamplesSize Size of the array parameter of
     *   putSamples(). There is no restriction on the value of
     *   this parameter, but typical values are 64-2024.
     */
    public SoundPlayback(String fileName,
            float sampleRate, int sampleSizeInBits,
            int channels, int bufferSize,
            int putSamplesSize) {
        _isAudioPlaybackActive = false;
        this._playbackMode = "file";
        this._fileName = fileName;
        this._sampleSizeInBits = sampleSizeInBits;
        this._sampleRate = sampleRate;
        this._channels = channels;
        this._productionRate = putSamplesSize;
    }

    ///////////////////////////////////////////////////////////////////
    ///  Public Methods                                         ///

    /** Play an array of audio samples.
     *  If the "play audio to speaker" constructor was called,
     *  then queue the array of audio samples in
     *  <i>putSamplesArray</i> for playback. There will be a
     *  latency before the audio data is actually heard, since the
     *  audio data in <i>putSamplesArray</i> is queued to an
     *  internal audio buffer. The size of the internal buffer
     *  is set by the constructor. A lower bound on the latency
     *  is given by (<i>bufferSize</i> / <i>sampleRate</i>)
     *  seconds. If the "play audio to speaker" mode is
     *  used, then this method should be invoked often
     *  enough to prevent underflow of the internal audio buffer.
     *  Underflow is undesirable since it will cause audible gaps
     *  in audio playback, but no exception or error condition will
     *  occur. If the caller attempts to write more data than can
     *  be written, this method blocks until the data can be
     *  written to the internal audio buffer.
     *  <p>
     *  If the "write audio to file" constructor was used,
     *  then append the audio data contained in <i>putSamplesArray</i>
     *  to the sound file specified in the constructor. Note that
     *  underflow cannot occur for this case.
     *  <p>
     *  The samples should be in the range (-1, 1). Samples that are
     *  outside ths range will be hard-clipped so that they fall
     *  within this range.
     *  @param putSamplesArray A two dimensional array containing
     *  the samples to play or write to a file. The first index
     *  represents the channel number (0 for first channel, 1 for
     *  second channel, etc.). The second index represents the
     *  sample index within a channel. For example,
     *  putSamplesArray[n][m] contains the (m+1)th sample
     *  of the (n+1)th channel. putSamplesArray should be a
     *  rectangular array such that putSamplesArray.length() gives
     *  the number of channels and putSamplesArray[n].length() is
     *  equal to <i>putSamplesSize</i>, for all channels n. This
     *  is not actually checked, however.
     *
     *  @exception IOException If there is a problem playing audio.
     *  @exception IllegalStateException If audio playback is currently
     *  inactive. That is, If startPlayback() has not yet been called
     *  or if stopPlayback() has already been called.
     */
    public void putSamples(double[][] putSamplesArray) throws IOException,
            IllegalStateException {
        if (_isAudioPlaybackActive == true) {
            if (_playbackMode == "speaker") {

                // Convert array of double valued samples into
                // the proper byte array format.
                _data = _doubleArrayToByteArray(putSamplesArray,
                        _bytesPerSample,
                        _channels);

                // Note: _data is a byte array containing data to
                // be written to the output device.
                // Note: consumptionRate is amount of data to write, in bytes.

                // Now write the array to output device.
                _sourceLine.write(_data, 0, _putSamplesSize*_frameSizeInBytes);
            } else if (_playbackMode == "file") {
                // Convert array of double valued samples into
                // the proper byte array format.
                _data = _doubleArrayToByteArray(putSamplesArray,
                        _bytesPerSample,
                        _channels);
                // Add new audio data to the file buffer array.
                for (int i = 0; i < _data.length; i++) {
                    _toFileBuffer.add(new Byte(_data[i]));
                }
            } else {
                // Should not happen since caught by constructor.
            }
        } else {
            throw new IllegalStateException("SoundPlayback: " +
                    "putSamples() was called while audio playback was" +
                    " inactive (startPlayback() was never called or " +
                    "stopPlayback has already been called).");
        }
    }

    /** Play an array of audio samples.
     *  If the "play audio to speaker" constructor was called,
     *  then queue the array of audio samples in
     *  <i>putSamplesArray</i> for playback. The samples should be
     *  in the range (-2^(bits_per_sample/2), 2^(bits_per_sample/2)).
     *  There will be a latency before
     *  the audio data is actually heard, since the
     *  audio data in <i>putSamplesArray</i> is queued to an
     *  internal audio buffer. The size of the internal buffer
     *  is set by the constructor. A lower bound on the latency
     *  is given by (<i>bufferSize</i> / <i>sampleRate</i>)
     *  seconds. If the "play audio to speaker" mode is
     *  used, then this method should be invoked often
     *  enough to prevent underflow of the internal audio buffer.
     *  <p>
     *  If the "write audio to file" constructor was used,
     *  then append the audio data contained in <i>putSamplesArray</i>
     *  to the sound file specified in the constructor.
     *  <p>
     *  The samples should be in the range
     *  (-2^(bits_per_sample/2), 2^(bits_per_sample/2)). Samples
     *  that are outside this range will be hard-clipped.
     *  @param putSamplesArray A two dimensional array containing
     *  the samples to play or write to a file. The first index
     *  represents the channel number (0 for first channel, 1 for
     *  second channel, etc.). The second index represents the
     *  sample index within a channel. For example,
     *  putSamplesArray[n][m] contains the (m+1)th sample
     *  of the (n+1)th channel. putSamplesArray should be a
     *  rectangular array such that putSamplesArray.length() gives
     *  the number of channels and putSamplesArray[n].length() is
     *  equal to <i>putSamplesSize</i>, for all channels n. This
     *  is not actually checked, however.
     *
     *  @exception IOException If there is a problem playing audio.
     *  @exception IllegalStateException If audio playback is currently
     *  inactive. That is, If startPlayback() has not yet been called
     *  or if stopPlayback() has already been called.
     */
    public void putSamplesInt(int[][] putSamplesArray) throws IOException,
            IllegalStateException {
        if (_isAudioPlaybackActive == true) {
            if (_playbackMode == "speaker") {

                // Convert array of double valued samples into
                // the proper byte array format.
                _data = _intArrayToByteArray(putSamplesArray,
                        _bytesPerSample,
                        _channels);

                // Note: _data is a byte array containing data to
                // be written to the output device.
                // Note: consumptionRate is amount of data to write, in bytes.

                // Now write the array to output device.
                _sourceLine.write(_data, 0, _putSamplesSize*_frameSizeInBytes);
            } else if (_playbackMode == "file") {
                // Convert array of double valued samples into
                // the proper byte array format.
                _data = _intArrayToByteArray(putSamplesArray,
                        _bytesPerSample,
                        _channels);
                // Add new audio data to the file buffer array.
                for (int i = 0; i < _data.length; i++) {
                    _toFileBuffer.add(new Byte(_data[i]));
                }
            } else {
                // Should not happen since caught by constructor.
            }
        } else {
            throw new IllegalStateException("SoundPlayback: " +
                    "putSamples() was called while audio playback was" +
                    " inactive (startPlayback() was never called or " +
                    "stopPlayback has already been called).");
        }
    }

    /** Perform initialization for the playback of audio data.
     *  This method must be invoked prior
     *  to the first invocation of putSamples(). This method
     *  must not be called more than once between invocations of
     *  stopPlayback(), or an exception will be thrown.
     *
     *  @exception IOException If there is a problem setting up
     *  the system for audio playback. This will occur if
     *  a file cannot be opened or if the audio out port cannot
     *  be accessed.
     *  @exception IllegalStateException If this method is called
     *  more than once between invocations of stopCapture().
     */
    public void startPlayback() throws IOException,
            IllegalStateException {
        if (_isAudioPlaybackActive == false) {
            if (_playbackMode == "speaker") {
                // Real time playback to speaker.
                _startPlaybackRealTime();
            } else if (_playbackMode == "file") {
                // Record data to sound file.
                _startPlaybackToFile();
            } else  {
                throw new IOException("SoundPlayback: " +
                        "startPlayback(): unknown playback mode: " +
                        _playbackMode);
            }
            _bytesPerSample = _sampleSizeInBits/8;
            _isAudioPlaybackActive = true;
        } else {
            throw new IllegalStateException("SoundPlayback: " +
                    "startPlayback() was called while audio playback was" +
                    " already active (startPlayback() was called " +
                    "more than once between invocations of stopPlayback()).");
        }
    }

    /** Stop playing/writing audio. This method should be called when
     *  no more calls to putSamples(). are required, so
     *  that the system resources involved in the audio playback
     *  may be freed.
     *  <p>
     *  If the "write audio data to file" constructor was used, then
     *  the sound file specified by the constructor is saved and
     *  closed.
     *
     *  @exception IOException If there is a problem closing the
     *   audio resources, or if the "write audio data
     *   to file" constructor was used  and the sound file has an
     *   unsupported format.
     */
    public void stopPlayback() throws IOException {
        if (_isAudioPlaybackActive == true) {
            if (_playbackMode == "speaker") {
                // Stop real-time playback to speaker.
                if (_sourceLine != null) {
                    _sourceLine.drain();
                    _sourceLine.stop();
                    _sourceLine.close();
                }
                _sourceLine = null;
            } else if (_playbackMode == "file") {
                // Record data to sound file.
                _stopPlaybackToFile();
            } else  {
                // Should not happen.
            }
        }
        _isAudioPlaybackActive = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _startPlaybackRealTime() throws IOException {
        boolean signed = true;
        boolean bigEndian = true;

        AudioFormat format = new AudioFormat((float)_sampleRate,
                _sampleSizeInBits,
                _channels, signed, bigEndian);

        _frameSizeInBytes = format.getFrameSize();

        DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class,
                format,
                AudioSystem.NOT_SPECIFIED);

        // get and open the source data line for playback.
        try {
            // Source DataLinet is really a target for
            // audio data, not a source.
            _sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            // Open line and suggest a buffer size (in bytes) to use or
            // the internal audio buffer.
            _sourceLine.open(format, _bufferSize*_frameSizeInBytes);

        } catch (LineUnavailableException ex) {
            throw new IOException("Unable to open the line for " +
                    "real-time audio playback: " + ex);
        }

        // Array of audio samples in byte format.
        _data = new byte[_productionRate*_frameSizeInBytes*_channels];

        // Start the source data line
        _sourceLine.start();
    }

    private void _startPlaybackToFile() {
        // FIXME: Performance is not great when the incoming audio
        // samples are being captured in real-time, possibly
        // due to resizing of the ArrayList.
        //
        // Array to hold all data to be saved to file. Grows
        // as new data are added (via putSamples()).
        // Each element is a byte of audio data.
        _toFileBuffer = new ArrayList();

        boolean signed = true;
        boolean bigEndian = true;

        _playToFileFormat = new AudioFormat((float)_sampleRate,
                _sampleSizeInBits,
                _channels, signed, bigEndian);

        _frameSizeInBytes = _playToFileFormat.getFrameSize();
    }


    private void _stopPlaybackToFile() throws IOException {
        int size =  _toFileBuffer.size();
        byte[] audioBytes = new byte[size];
        for (int i = 0; i < size; i++) {
            Byte j = (Byte)_toFileBuffer.get(i);
            audioBytes[i] = j.byteValue();
        }
        ByteArrayInputStream byteInputArrayStream =
            new ByteArrayInputStream(audioBytes);

        AudioInputStream audioInputStream =
            new AudioInputStream(byteInputArrayStream,
                    _playToFileFormat,
                    audioBytes.length /  _frameSizeInBytes);

        File outFile = new File(_fileName);



        try {
            StringTokenizer st = new StringTokenizer(_fileName, ".");
            // Do error checking:
            if (st.countTokens() != 2) {
                throw new  IOException("Error: Incorrect " +
                        "file name format. " +
                        "Format: filename.extension");
            }
            st.nextToken(); // Advance to the file extension.

            String fileExtension = st.nextToken();

            if (fileExtension.equalsIgnoreCase("au")) {
                // Save the file.
                AudioSystem.write(audioInputStream,
                        AudioFileFormat.Type.AU, outFile);
            } else if (fileExtension.equalsIgnoreCase("aiff")) {
                // Save the file.
                AudioSystem.write(audioInputStream,
                        AudioFileFormat.Type.AIFF, outFile);
            } else if (fileExtension.equalsIgnoreCase("wave")) {
                // Save the file.
                AudioSystem.write(audioInputStream,
                        AudioFileFormat.Type.WAVE, outFile);
            } else if (fileExtension.equalsIgnoreCase("wav")) {
                // Save the file.
                AudioSystem.write(audioInputStream,
                        AudioFileFormat.Type.WAVE, outFile);
            } else if (fileExtension.equalsIgnoreCase("aifc")) {
                // Save the file.
                AudioSystem.write(audioInputStream,
                        AudioFileFormat.Type.AIFC, outFile);
            } else {
                throw new  IOException("Error saving " +
                        "file: Unknown file format: " +
                        fileExtension);
            }
        } catch (IOException e) {
            throw new IOException("SoundPlayback: error saving" +
                    " file: " + e);
        }
    }

    /* Convert a double array of audio samples into a byte array of
     * audio samples in linear signed pcm big endian format. The
     * samples contained in <i>doubleArray</i> should be in the
     * range (-1, 1). Samples outside this range will be hard clipped
     * to the range (-1, 1).
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
    private byte[] _doubleArrayToByteArray(double[][] doubleArray,
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

    /* Convert a integer array of audio samples into a byte array of
     * audio samples in linear signed pcm big endian format.
     * The samples contained by <i>intArray</i> should be in the range
     * (-2^(bits_per_sample/2), 2^(bits_per_sample/2)). Samples that
     * are outside this range will be hard-clipped to fall within this
     * range.
     * @param intArray Two dimensional array holding audio samples.
     * For each channel, m, doubleArray[m] is a single dimensional
     * array containing samples for channel m.
     * @param bytesPerSample Number of bytes per sample. Supported
     * bytes per sample by this method are 8, 16, 24, 32.
     * @param channels Number of audio channels.
     * @return The linear signed pcm big endian byte array formatted
     * array representation of <i>doubleArray</i>. The length of
     * the returned array is (doubleArray.length*bytesPerSample*channels).
     */
    private byte[] _intArrayToByteArray(int[][] intArray,
            int bytesPerSample, int channels) {
        // All channels had better have the same number
        // of samples! This is not checked!
        int lengthInSamples = intArray[0].length;

        byte[] byteArray =
            new byte[lengthInSamples * bytesPerSample * channels];
        byte[] b = new byte[bytesPerSample];
        for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {

            // For each channel,
            for (int currChannel = 0; currChannel < channels; currChannel++) {
                // signed integer representation of current sample of the
                // current channel.
                int l =
                    intArray[currChannel][currSamp];
                // Perform clipping, if necessary.
                int maxSample;
                if (bytesPerSample == 2) {
                    maxSample = 32768;
                } else if (bytesPerSample == 1) {
                    maxSample = 128;
                } else if (bytesPerSample == 3) {
                    maxSample = 8388608;
                } else if (bytesPerSample == 4) {
                    maxSample = 1474836480;
                } else {
                    // Should not happen.
                    maxSample = 0;
                }
                if (l > (maxSample - 1)) {
                    l = maxSample - 1;
                } else if (l < (-maxSample + 1)) {
                    l = -maxSample + 1;
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

    private int _productionRate;
    private String _fileName;
    private String _playbackMode;
    private int _sampleSizeInBits;
    private int _putSamplesSize;
    private float _sampleRate;
    private int _channels;
    private int _bufferSize;
    private SourceDataLine _sourceLine;
    // Array of audio samples in byte format.
    private byte[] _data;
    private int _frameSizeInBytes;
    private ArrayList _toFileBuffer;
    // This is the format of _toFileBuffer.
    private AudioFormat _playToFileFormat;
    private int _bytesPerSample;
    private boolean _isAudioPlaybackActive;
}
