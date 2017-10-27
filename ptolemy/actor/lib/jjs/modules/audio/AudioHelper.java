/* Helper for the audio module

   Copyright (c) 2017 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;

/** Helper for the audio.js JavaScript module.
 *  See the module for documentation.
 *  
 *  This class is based on the LiveSoundJavaSE class in Ptolemy II, written by
 *  Brian K. Vogel, Neil E. Turner, Steve Neuendorffer, and Edward A. Lee,
 *  with contributions from Dennis Geurts, Ishwinder Singh.
 *
 *  @author Edward A. Lee 
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class AudioHelper extends VertxHelperBase {

    /** Create an audio helper.
     *  @param actor The actor associated with this helper.
     *  @param currentObj The JavaScript object that this is helping.
     */
    public AudioHelper(Object actor, ScriptObjectMirror currentObj) {
        super(actor, currentObj);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an array of supported byte formats for audio.
     *  @return An array of strings.
     */
    public static String[] byteFormats() {
        String[] result = new String[_BYTE_FORMATS.values().length];
        int i = 0;
        for (_BYTE_FORMATS value : _BYTE_FORMATS.values()) {
            result[i++] = value.toString();
        }
        return result;
    }
    
    /** Play an array of audio samples. This method returns immediately
     *  and invokes the specified callback function when the samples
     *  have been successfully queued into the audio system.
     *  This method should not be called again before the callback has
     *  been invoked.
     *  <p>
     *  The samples should be in the range (-1, 1). Samples that are
     *  outside this range will be hard-clipped so that they fall
     *  within this range.
     *  <p>
     *  The first index of the specified array
     *  represents the channel number (0 for first channel, 1 for
     *  second channel, etc.). The number of channels is set by the
     *  setChannels() method. The second index represents the
     *  sample index within a channel. For example,
     *  putSamplesArray[n][m] contains the (m+1)th sample
     *  of the (n+1)th channel.
     *
     *  @param samplesArray A two dimensional array containing
     *   the samples to play.
     *  @param callback A callback function to invoke when the samples
     *   have been queued into the audio system.
     *
     *  @exception IOException If the calling program does not have permission
     *   to access the audio playback resources.
     *
     *  @exception IllegalStateException If audio playback is currently
     *   inactive. That is, If startPlayback() has not yet been called
     *   or if stopPlayback() has already been called.
     */
    public void putSamples(double[][] samplesArray, final Runnable callback)
            throws IllegalStateException, IOException {
        synchronized(this) {
            if (!_playbackIsActive) {
                throw new IllegalStateException(
                        "Attempted to play audio data, but "
                                + "playback is inactive.  Try to startPlayback().");
            }
        }
        // Convert array of double valued samples into
        // the proper byte array format.
        final byte[] playbackData = _doubleArrayToByteArray(samplesArray);
        
        Thread worker = new Thread() {
            public void run() {
                // The following function call will block if the buffer gets full.
                _putSamples(playbackData);
                
                // Invoke the callback.
                if (callback != null) {
                    _issueResponse(() -> {
                        callback.run();
                    });
                }
            }
        };
        worker.start();
    }

    /** Play an array of audio samples given as a byte array.
     *  This method returns immediately
     *  and invokes the specified callback function when the samples
     *  have been successfully queued into the audio system.
     *  This method should not be called again before the callback has
     *  been invoked.
     *
     *  @param audioData Byte array to play.
     *  @param callback A callback function to invoke when the samples
     *   have been queued into the audio system.
     *
     *  @exception IOException If the calling program does not have permission
     *   to access the audio playback resources.
     *  @exception IllegalStateException If audio playback is currently
     *   inactive. That is, If startPlayback() has not yet been called
     *   or if stopPlayback() has already been called.
     *  @throws UnsupportedAudioFileException If the audioData provided is
     *   not in a supported format.
     */
    public void putBytes(byte[] audioData, final Runnable callback)
            throws IllegalStateException, IOException, UnsupportedAudioFileException {
        synchronized(this) {
            if (!_playbackIsActive) {
                throw new IllegalStateException(
                        "Attempted to play audio data, but "
                                + "playback is inactive.  Try to startPlayback().");
            }
        }
        
        if (!_playbackFileFormat.equals("raw")) {
            // Input data is encoded. Attempt to transcode it to the specified output format.
            ByteArrayInputStream stream = new ByteArrayInputStream(audioData);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(stream);
            AudioInputStream targetStream = AudioSystem.getAudioInputStream(_playbackFormat, audioStream);
            long length = targetStream.getFrameLength();
            if (length > Integer.MAX_VALUE) {
                throw new UnsupportedAudioFileException("Audio segment is too long.");
            }
            int frameSize = targetStream.getFormat().getFrameSize();
            int bufferSize = ((int) length) * frameSize;
            if (bufferSize < 0) {
                throw new IllegalStateException("Attempted to transcode playback format \"" + _playbackFormat
                                                + "\", but the targetStream " + targetStream
                                                + "\nyielded a frame size of " + frameSize
                                                + "\n which when multiplied by the length of " + length
                                                + "\nyielded a buffer size of " + bufferSize
                                                + "\n which is less than zero."
                                                + "\nThe length of the input audioData was + " + audioData.length
                                                + "\nThe targetStream frame length was: " + targetStream.getFrameLength()
                                                + "\n");
            }
            if (_playbackData == null || _playbackData.length != bufferSize) {
                // Hopefully, the allocation is done only once.
                _playbackData = new byte[bufferSize];

            }
            targetStream.read(_playbackData, 0, bufferSize);
        } else {
            _playbackData = audioData;
        }
        
        Thread worker = new Thread() {
            public void run() {
                // The following function call will block if the buffer gets full.
                _putSamples(_playbackData);
                
                // Invoke the callback.
                if (callback != null) {
                    _issueResponse(() -> {
                        callback.run();
                    });
                }
            }
        };
        worker.start();
    }

    /** Set the capture parameters for audio.
     *  This will stop any active capture and restart it.
     *  
     *  The captureOptions argument contains the following
     *  entries:
     *  <ol>
     *    <li> bigEndian: 1 if big endian, 0 if little endian.  The
     *    default is big endian.</li>
     *    <li> bitsPerSample: The number of bits per sample.</li>
     *    <li> channels: The number of channels.</li>
     *    <li> sampleRate: Sample rate in Hz.</li>
     *  </ol>
     *  Allowable values for sampleRate are (most likely) 8000, 11025,
     *  22050, 44100, and 48000 Hz. If this method is not invoked,
     *  then the default value of 8000 Hz is used.
     *
     *  WAVE (aka .wav) is 16 bits, 1 channel, little endian, 44100 Hz.
     *
     *  @param captureOptions The parameters for capture.
     *  @param captureTime The amount of time (in ms) per capture.
     *  @param outputFormat The requested output format.
     *
     *  @exception IOException If the specified sample rate is
     *   not supported by the audio hardware or by Java.
     *  @throws LineUnavailableException If the audio line is not
     *   available.
     */
    public void setCaptureParameters(
            Map<String,Integer> captureOptions,
            int captureTime,
            String outputFormat
            ) throws IOException, LineUnavailableException {
        
        int bitsPerSample = captureOptions.get("bitsPerSample");
        boolean bigEndian = true;
        int channels = captureOptions.get("channels");

        int sampleRate = captureOptions.get("sampleRate");

        if (captureOptions.containsKey("bigEndian")) {
            bigEndian = captureOptions.get("bigEndian") == 1 ? true : false;
        }

        // Set persistent variables that depend on the parameters.
        _captureChannels = channels;
        _captureBytesPerSample = bitsPerSample / 8;
        _captureTransferSize = (int) (sampleRate * (captureTime / 1000.0));

        // Maximum value for the given number of bits.
        _maxSampleReciprocal = 1.0 / (1L << bitsPerSample);
                
        // Make sure the outputFormat matches one of those supported.
        try {
            _outputFormat = _BYTE_FORMATS.valueOf(outputFormat);
        } catch (IllegalArgumentException ex) {
            _formatError(outputFormat);
        }
        
        // For PCM data, the size of a frame (in bytes) is always
        // equal to the size of a sample (in bytes) times the number of channels .
        int frameSize = channels * _captureBytesPerSample;
        float frameRate = sampleRate;
        _captureFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, bitsPerSample,
                channels, frameSize, frameRate, bigEndian);

        // Third argument is the desired buffer size. Should we match what we need?
        int bufferSize = _captureTransferSize * channels * _captureBytesPerSample;
        _targetInfo = new DataLine.Info(TargetDataLine.class,
                _captureFormat, bufferSize);
        
        if (!AudioSystem.isLineSupported(_targetInfo)) {
            throw new IOException(
                    "Unsupported audio line. Possible encodings for\n" + _captureFormat
                    + "\n are:\n" + _encodings(_captureFormat));
        }
        // System.out.println("Capture format: " + _captureFormat);
        // System.out.println("Possible capture encodings: " + _encodings(_captureFormat));

        synchronized(this) {
            if (_captureIsActive) {
                // Restart capture with new parameters.
                _stopCapture();
                _startCapture();
            }
        }
    }
    
    /** Set the playback parameters for audio.
     *  This will stop any active playback and restart it.
     *  
     *  The playbackOptions argument contains the following entries:
     *  <ol>
     *    <li> bigEndian: 1 if big endian, 0 if little endian.  The
     *    default is big endian.</li>
     *    <li> bitsPerSample: The number of bits per sample.</li>
     *    <li> channels: The number of channels.</li>
     *    <li> sampleRate: Sample rate in Hz.</li>
     *  </ol>
     *  Allowable values for sampleRate are (most likely) 8000, 11025,
     *  22050, 44100, and 48000 Hz. If this method is not invoked,
     *  then the default value of 8000 Hz is used.
     *
     *  WAVE (aka .wav) is 16 bits, 1 channel, little endian, 44100 Hz.
     *
     *  @param playbackOptions The parameters for playback.
     *  @param playbackFormat The expected format of the input data.
     *
     *  @exception IOException If the specified sample rate is
     *   not supported by the audio hardware or by Java.
     *  @throws LineUnavailableException If the audio line is not
     *   available.
     */
    public void setPlaybackParameters(
            Map<String,Integer> playbackOptions,
            String playbackFormat
            ) throws IOException, LineUnavailableException {
        
        int bitsPerSample = playbackOptions.get("bitsPerSample");
        boolean bigEndian = true;
        int channels = playbackOptions.get("channels");
        int sampleRate = playbackOptions.get("sampleRate");

        if (playbackOptions.containsKey("bigEndian")) {
            bigEndian = playbackOptions.get("bigEndian") == 1 ? true : false;
        }

        // Set persistent variables that depend on the parameters.
        _playbackChannels = channels;
        _playbackBytesPerSample = bitsPerSample / 8;
                        
        // Should more alternatives be provided?
        // For PCM data, the size of a frame (in bytes) is always
        // equal to the size of a sample (in bytes) times the number of channels .
        int frameSize = channels * _playbackBytesPerSample;
        float frameRate = sampleRate;
        _playbackFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, bitsPerSample,
                channels, frameSize, frameRate, bigEndian);
        _playbackFileFormat = playbackFormat;
        
        synchronized(this) {
            if (_playbackIsActive) {
                // Restart capture with new parameters.
                _stopPlayback();
                _startPlayback();
            }
        }
    }

    /** Start audio capture if it is not already active.
     *  @throws IOException If the audio target line cannot be acquired.
     */
    public synchronized void startCapture() throws IOException {
        if (_captureIsActive) {
            return;
        }
        _startCapture();
        _captureIsActive = true;
    }
    
    /** Start audio playback if it is not already active.
     *  @throws IOException If the audio target line cannot be acquired.
     */
    public synchronized void startPlayback() throws IOException {
        if (_playbackIsActive) {
            return;
        }
        _startPlayback();
        _playbackIsActive = true;
    }

    /** Stop audio capture if it is active.
     */
    public synchronized void stopCapture() {
        if (!_captureIsActive) {
            return;
        }
        _captureIsActive = false;
        _stopCapture();
    }

    /** Stop audio playback if it is active.
     */
    public synchronized void stopPlayback() {
        if (!_playbackIsActive) {
            return;
        }
        _playbackIsActive = false;
        _stopPlayback();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Convert a byte array of audio samples in linear signed PCM big endian
     * format into a double array of audio samples (-1, 1) range.
     * @param doubleArray The resulting array of doubles.
     * @param byteArray  The linear signed pcm big endian byte array
     * formatted array representation of audio data.
     */
    private void _byteArrayToDoubleArray(double[][] doubleArray,
            byte[] byteArray) {
        int lengthInSamples = byteArray.length / (_captureBytesPerSample * _captureChannels);

        for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {
            // For each channel,
            for (int currChannel = 0; currChannel < _captureChannels; currChannel++) {
                // Starting index of relevant bytes.
                int j = currSamp * _captureBytesPerSample * _captureChannels
                        + _captureBytesPerSample * currChannel;
                // Note: preserve sign of high order bits.
                int result = byteArray[j++];

                // Shift and add in low order bits.
                // Note that it is ok to fall through the cases here (I think).
                switch (_captureBytesPerSample) {
                case 4:
                    result <<= 8;
                    // Dennis Geurts:
                    // Use & instead of | here.
                    // Running ptolemy/actor/lib/javasound/test/auto/testAudioCapture_AudioPlayer.xml
                    // results in a much better sound.
                    // See https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=356
                    result += byteArray[j++] & 0xff;

                case 3:
                    result <<= 8;
                    result += byteArray[j++] & 0xff;

                case 2:
                    result <<= 8;
                    result += byteArray[j++] & 0xff;
                }

                doubleArray[currChannel][currSamp] = result
                        * _maxSampleReciprocal;
            }
        }
    }

    /** Convert the specified raw audio byte array into the specified
     *  output format and return an object containing the converted
     *  data. The nature of the object will depend on the format.
     *  <p>
     *  If the output format is "array", then
     *  the returned object is an array of arrays containing
     *  audio samples will have values in the range
     *  [-1.0, 1.0], regardless of the audio bit resolution (bits per
     *  sample). The first index of the returned array
     *  represents the channel number (0 for first channel, 1 for
     *  second channel). The second index represents the
     *  sample index within a channel.
     *
     *  @return Converted data.
     *  @throws IOException If the output format is not supported.
     */
    private Object _convertBytesToFormat(byte[] rawData, _BYTE_FORMATS outputFormat) throws IOException {
        switch(outputFormat) {
        case raw:
            return rawData;
        case array:
            // Check whether we need to reallocate.
            if (_captureChannels != _audioInDoubleArray.length
                    || _captureTransferSize != _audioInDoubleArray[0].length) {
                // Reallocate
                _audioInDoubleArray = new double[_captureChannels][_captureTransferSize];
            }

            // Convert byte array to double array.
            _byteArrayToDoubleArray(_audioInDoubleArray, rawData);
            return _audioInDoubleArray;
        default:
            ByteArrayInputStream byteStream = new ByteArrayInputStream(rawData);
            AudioInputStream rawStream = new AudioInputStream(byteStream, _captureFormat, _captureTransferSize);
            ByteArrayOutputStream codedStream = new ByteArrayOutputStream();
            AudioFileFormat.Type type = null;
            switch(outputFormat) {
            case wav:
                type = AudioFileFormat.Type.WAVE;
                break;
            case aiff:
                type = AudioFileFormat.Type.AIFF;
                break;
            case aifc:
                type = AudioFileFormat.Type.AIFC;
                break;
            case au:
                type = AudioFileFormat.Type.AU;
                break;
            default:
                _formatError(outputFormat.name());
            }
            AudioSystem.write(rawStream, type, codedStream);
            return codedStream.toByteArray();
        }
    }
    
    /** Convert a double array of audio samples into a byte array of
     *  audio samples in linear signed PCM big endian format. The
     *  samples contained in <i>doubleArray</i> should be in the
     *  range (-1, 1). Samples outside this range will be hard clipped
     *  to the range (-1, 1).
     *  @param doubleArray Two dimensional array holding audio samples.
     *   For each channel, m, doubleArray[m] is a single dimensional
     *   array containing samples for channel m. All channels are
     *   required to have the same number of samples, but this is
     *   not checked.
     *  @return The linear signed PCM big endian byte array formatted
     *   array representation of <i>doubleArray</i>. The length of
     *   the returned array is (doubleArray[i].length*bytesPerSample*channels).
     */
    private byte[] _doubleArrayToByteArray(double[][] doubleArray) {
        // This method is most efficient if repeated calls pass the same size
        // array. In this case, it does not re-allocate the byte array that
        // it returns, but rather reuses the same array on the heap.
        int numberOfSamples = doubleArray[0].length;
        int bufferSize = numberOfSamples * _playbackBytesPerSample * _playbackChannels;
        if (_playbackData == null || _playbackData.length != bufferSize) {
            // Hopefully, the allocation is done only once.
            _playbackData = new byte[bufferSize];
        }
        int scaleFactor = (1 << (_playbackBytesPerSample * 8 - 1));
        
        // Iterate over the samples.
        for (int currSamp = 0; currSamp < doubleArray[0].length; currSamp++) {
            // For each channel,
            for (int currChannel = 0; currChannel < _playbackChannels; currChannel++) {
                double sample = doubleArray[currChannel][currSamp];

                // Perform clipping, if necessary.
                if (sample >= 1.0) {
                    sample = 1.0;
                } else if (sample < -1.0) {
                    sample = -1.0;
                }

                // signed integer representation of current sample of the
                // current channel.
                // Note: Floor instead of cast to remove deadrange at zero.
                int intValue = (int) Math.floor(sample * (1 << (_playbackBytesPerSample * 8 - 1)));

                // Corner case.
                if (intValue == scaleFactor) {
                    intValue--;
                }

                int base = currSamp * _playbackBytesPerSample * _playbackChannels
                        + _playbackBytesPerSample * currChannel;
                // Create byte representation of current sample.
                // Note: unsigned Shift right.
                // Note: fall through from higher number cases.
                switch (_playbackBytesPerSample) {
                case 4:
                    _playbackData[base + 3] = (byte) intValue;
                    intValue >>>= 8;
                case 3:
                    _playbackData[base + 2] = (byte) intValue;
                    intValue >>>= 8;
                case 2:
                    _playbackData[base + 1] = (byte) intValue;
                    intValue >>>= 8;
                case 1:
                    _playbackData[base] = (byte) intValue;
                }
            }
        }
        return _playbackData;
    }

    /** Return a string that describes the possible encodings for an
     *  AudioFormat.
     *  @param format The audio format.
     *  @return A string describing the audio formats available.
     */
    private String _encodings(AudioFormat format) {
        // Print out the possible encodings
        AudioFormat.Encoding[] encodings = AudioSystem
                .getTargetEncodings(format);
        StringBuffer encodingDescriptions = new StringBuffer();
        for (Encoding encoding : encodings) {
            encodingDescriptions.append(encoding + "\n");
            AudioFormat[] formats = AudioSystem.getTargetFormats(encoding,
                    format);
            for (AudioFormat format2 : formats) {
                encodingDescriptions.append("  " + format2 + "\n");
            }
        }
        return encodingDescriptions.toString();
    }
    
    /** Return an array of captured audio bytes. This method blocks
     *  until a buffer is full and should be repeatedly called to obtain
     *  uninterrupted audio data.
     *  
     *  @param consumer The object that has an exclusive lock on
     *   the capture audio resources.
     *  @return An array of bytes representing raw captured audio data,
     *   or null capture has been stopped.
     */
    private byte[] _getBytes() {
        if (!_captureIsActive) {
            return null;
        }
        // Real-time capture. NOTE: This blocks!!!!
        int numBytesRead = _targetLine.read(_captureData[_captureDataBuffer], 0,
                _captureData[_captureDataBuffer].length);

        // If the number of bytes captured is not correct, then capture
        // was stopped prematurely. Presumably, we don't want the data,
        // so it is discarded.
        if (numBytesRead == _captureData[_captureDataBuffer].length) {
            return _captureData[_captureDataBuffer];
        } else {
            return null;
        }
    }

    /** Throw an exception indicating that the specified format is not supported.
     *  @param format The output format that is not supported.
     *  @throws IOException Always thrown.
     */
    private void _formatError(String format) throws IOException {
        throw new IOException(
                "Unrecognized format: " + format
                + "\nThe supported formats are:\n" + Arrays.toString(byteFormats()));
    }

    /** Play audio data, blocking until they are queued.
     *  This method should be invoked often
     *  enough to prevent underflow of the internal audio buffer.
     *  Underflow is undesirable since it will cause audible gaps
     *  in audio playback, but no exception or error condition will
     *  occur.
     *
     *  @param playbackData The audio data to play.
     *
     *  @exception IOException If the calling program does not have permission
     *   to access the audio playback resources.
     *
     *  @exception IllegalStateException If audio playback is currently
     *  inactive. That is, If startPlayback() has not yet been called
     *  or if stopPlayback() has already been called.
     */
    private void _putSamples(byte[] playbackData) throws IllegalStateException {
        synchronized(this) {
            if (!_playbackIsActive) {
                return;
            }
        }
        // Now write the array to output device.
        int written = _sourceLine.write(playbackData, 0, playbackData.length);

        if (written != playbackData.length) {
            System.err.println("dropped audio samples during playback!");
        }
    }

    /** Start audio capture.
     * 
     *  @throws IOException If the audio target line cannot be acquired.
     */
    private void _startCapture() throws IOException {
        try {
            _targetLine = (TargetDataLine) AudioSystem.getLine(_targetInfo);

            // Note: 2nd parameter is the buffer size (in bytes).
            // Larger values increase latency but may be required if
            // garbage collection, etc. is an issue.
            // Here, we set it to the number of bytes that will be collected at once.
            _targetLine.open(_captureFormat, _captureTransferSize * _captureBytesPerSample * _captureChannels);
        } catch (IllegalArgumentException ex) {
            IOException exception = new IOException(
                    "Incorrect argument, possible encodings for\n" + _captureFormat
                    + "\n are:\n" + _encodings(_captureFormat));
            exception.initCause(ex);
            throw exception;
        } catch (LineUnavailableException ex2) {
            throw new IOException("Audio line is not available: " + ex2);
        }

        // Array of audio samples in byte format.
        // This is a double buffer so that conversions can occur in parallel with capture.
        _captureData[0] = new byte[_captureTransferSize * _captureBytesPerSample * _captureChannels];
        _captureData[1] = new byte[_captureTransferSize * _captureBytesPerSample * _captureChannels];
        _audioInDoubleArray = new double[_captureChannels][_captureTransferSize];

        // Start the target data line
        _targetLine.start();
        
        // Start a thread to read the audio data repeatedly until stopped.
        Thread worker = new Thread() {
            @Override
            public void run() {
                while(_captureIsActive) {
                    // The following call blocks until the requested number of
                    // samples have been captured.
                    final byte[] rawData = _getBytes();
                    // Switch buffers so that new data is stored in the new buffer.
                    if (_captureDataBuffer == 0) {
                        _captureDataBuffer = 1;
                    } else {
                        _captureDataBuffer = 0;
                    }
                    // Capture may have been stopped while we were blocked.
                    if (rawData != null && _captureIsActive) {
                        // Make this callback in the director thread instead of
                        // the verticle thread so that all outputs associated with
                        // a request are emitted simultaneously.
                        _issueResponse(() -> {
                            if (_captureIsActive) {
                                try {
                                    // This conversion should occur in a new thread, and we should switch buffers.
                                    Object outputData = _convertBytesToFormat(rawData, _outputFormat);
                                    _currentObj.callMember("_captureData", outputData);
                                } catch (IOException e) {
                                    // This should not occur because setCaptureParameters checks outputFormat.
                                    System.err.println("Internal error: outputFormat not supported: " + _outputFormat);
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        };
        worker.start();
    }
    
    /** Start playback.
     *  @throws IOException If the format is not supported or if the line
     *   is not available.
     */
    private void _startPlayback() throws IOException {
        // Source DataLine is really a target for
        // audio data, not a source. Dumb name.
        try {
            _sourceLine = AudioSystem.getSourceDataLine(_playbackFormat);
            _sourceLine.open(_playbackFormat);
        } catch (IllegalArgumentException ex) {
            IOException exception = new IOException(
                    "Incorrect argument, possible encodings for\n" + _playbackFormat
                    + "\n are:\n" + _encodings(_playbackFormat));
            exception.initCause(ex);
            throw exception;
        } catch (LineUnavailableException ex) {
            throw new IOException("Unable to open the line for "
                    + "real-time audio playback: " + ex);
        }
        // Start the source data line
        _sourceLine.start();
    }

    /** Stop audio capture.
     */
    private void _stopCapture() {
        if (_targetLine != null) {
            if (_targetLine.isOpen() == true) {
                _targetLine.stop();
                _targetLine.flush();
                _targetLine.close();
                _targetLine = null;
            }
        }
    }

    /** Stop audio playback.
     */
    private void _stopPlayback() {
        if (_sourceLine != null) {
            // Do not drain. Stop immediately.
            // _sourceLine.drain();
            _sourceLine.stop();
            // Discard queued data.
            _sourceLine.flush();
            _sourceLine.close();
        }

        _sourceLine = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Array of audio samples in double format. */
    private double[][] _audioInDoubleArray;

    /** List of formats for byte stream encoding of audio. */
    private enum _BYTE_FORMATS {raw, array, aiff, aifc, au, wav};

    /** The number of bytes per sample, default 2. */
    private int _captureBytesPerSample = 2;

    /** The number of channels. Deafult is 1. */
    private int _captureChannels = 1;

    /** Array of audio samples in byte format. This is double buffered. */
    private byte[][] _captureData = new byte[2][];
    
    /** Indicator of which buffer we are capturing into right now. */
    private int _captureDataBuffer = 0;

    /** Audio format for audio capture. */
    private AudioFormat _captureFormat;
    
    /** true is audio capture is currently active. */
    private boolean _captureIsActive = false;

    /** The number of audio samples to transfer per channel when
     * getSamples() is invoked. */
    private int _captureTransferSize = 128;

    /** Maximum value for the given number of bits. */
    private double _maxSampleReciprocal = 1.0 / (1L << 16);
    
    /** Specification of the output format to use. */
    private _BYTE_FORMATS _outputFormat = _BYTE_FORMATS.raw;
    
    /** Number of playback bytes per sample. */
    private int _playbackBytesPerSample = 2;

    /** Number of playback channels. */
    private int _playbackChannels = 1;
    
    /** Byte buffer used for playback data. */
    private byte[] _playbackData;

    /** The format of the playback data. */
    private AudioFormat _playbackFormat;

    /** The format of the playback byte array. */
    private String _playbackFileFormat = "raw";
    
    /** Indicator of whether playback is active. */
    private boolean _playbackIsActive = false;
    
    /** Line into which to send audio data to be played over the speakers. */
    private SourceDataLine _sourceLine;
    
    /** Information about the data line. */
    private Info _targetInfo;

    /** Interface to the hardware for producing sound. */
    private TargetDataLine _targetLine;
}
