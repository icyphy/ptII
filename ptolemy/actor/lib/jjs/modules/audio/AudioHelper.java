/* Helper for the cameras JavaScript module.

   Copyright (c) 2014-2016 The Regents of the University of California.
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
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

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

    /** Return an array of supported capture formats.
     *  @return An array of strings.
     */
    public static String[] outputFormats() {
        return _OUTPUT_FORMATS;
    }
    
    /** Set the capture parameters for audio.
     *  This will stop any active capture and restart it.
     *  
     *  The captureOptions argument contains the following
     *  entries:
     *  <ol>
     *  <li> bitsPerSample: The number of bits per sample.
     *  <li> channels: The number of channels.
     *  <li> sampleRate: Sample rate in Hz.
     *  </ol>
     *  Allowable values for sampleRate are 8000, 11025,
     *  22050, 44100, and 48000 Hz. If this method is not invoked,
     *  then the default value of 8000 Hz is used.
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
        int channels = captureOptions.get("channels");
        int sampleRate = captureOptions.get("sampleRate");

        // Set persistent variables that depend on the parameters.
        _channels = channels;
        _bytesPerSample = bitsPerSample / 8;
        _transferSize = (int) (sampleRate * (captureTime / 1000.0));

        // Maximum value for the given number of bits.
        _maxSampleReciprocal = 1.0 / (1L << bitsPerSample);
                
        // Make sure the outputFormat matches one of those supported.
        boolean found = false;
        for (int i = 0; i < _OUTPUT_FORMATS.length; i++) {
            if (_OUTPUT_FORMATS[i].equals(outputFormat)) {
                found = true;
                break;
            }
        }
        if (!found) {
            _outputFormatError(outputFormat);
        }
        _outputFormat = outputFormat;
        
        // FIXME: Only supporting PCM_SIGNED, big-endian data.
        // Should more alternatives be provided?
        boolean bigEndian = true;
        // For PCM data, the size of a frame (in bytes) is always
        // equal to the size of a sample (in bytes) times the number of channels .
        int frameSize = channels * _bytesPerSample;
        float frameRate = sampleRate;
        _captureFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, bitsPerSample,
                channels, frameSize, frameRate, bigEndian);

        // Third argument is the desired buffer size. Should we match what we need?
        int bufferSize = _transferSize * channels * _bytesPerSample;
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

    /** Stop audio capture if it is active.
     */
    public synchronized void stopCapture() {
        if (_captureIsActive) {
            return;
        }
        _captureIsActive = false;
        _stopCapture();
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
        int lengthInSamples = byteArray.length / (_bytesPerSample * _channels);

        for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {
            // For each channel,
            for (int currChannel = 0; currChannel < _channels; currChannel++) {
                // Starting index of relevant bytes.
                int j = currSamp * _bytesPerSample * _channels
                        + _bytesPerSample * currChannel;
                // Note: preserve sign of high order bits.
                int result = byteArray[j++];

                // Shift and add in low order bits.
                // Note that it is ok to fall through the cases here (I think).
                switch (_bytesPerSample) {
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
     *  If the output format is "array" or "samples", then
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
    private Object _convertFormat(byte[] rawData, String outputFormat) throws IOException {
        if (outputFormat.equals("raw")) {
            return rawData;
        } else if (outputFormat.equals("array") || outputFormat.equals("samples")) {
            // Check whether we need to reallocate.
            if (_channels != _audioInDoubleArray.length
                    || _transferSize != _audioInDoubleArray[0].length) {
                // Reallocate
                _audioInDoubleArray = new double[_channels][_transferSize];
            }

            // Convert byte array to double array.
            _byteArrayToDoubleArray(_audioInDoubleArray, rawData);
            return _audioInDoubleArray;
        } else {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(rawData);
            AudioInputStream rawStream = new AudioInputStream(byteStream, _captureFormat, _transferSize);
            ByteArrayOutputStream codedStream = new ByteArrayOutputStream();
            if (outputFormat.equals("wav")) {
                AudioSystem.write(rawStream, AudioFileFormat.Type.WAVE, codedStream);
            } else if (outputFormat.equals("aiff")) {
                AudioSystem.write(rawStream, AudioFileFormat.Type.AIFF, codedStream);
            } else if (outputFormat.equals("aifc")) {
                AudioSystem.write(rawStream, AudioFileFormat.Type.AIFC, codedStream);
            } else if (outputFormat.equals("au")) {
                AudioSystem.write(rawStream, AudioFileFormat.Type.AU, codedStream);
            } else {
                _outputFormatError(outputFormat);
            }
            return codedStream.toByteArray();
        }
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

    /** Throw an exception indicating that the specified outputFormat is not supported.
     *  @param outputFormat The output format that is not supported.
     *  @throws IOException Always thrown.
     */
    private void _outputFormatError(String outputFormat) throws IOException {
        // Construct an informative error message.
        StringBuffer list = new StringBuffer();
        for (int i = 0; i < _OUTPUT_FORMATS.length; i++) {
            if (i > 0) {
                list.append(", ");
            }
            list.append(_OUTPUT_FORMATS[i]);
        }
        throw new IOException(
                "Unrecognized output format: " + outputFormat
                + "\nThe supported formats are:\n" + list);
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
            _targetLine.open(_captureFormat, _transferSize * _bytesPerSample * _channels);
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
        _captureData[0] = new byte[_transferSize * _bytesPerSample * _channels];
        _captureData[1] = new byte[_transferSize * _bytesPerSample * _channels];
        _audioInDoubleArray = new double[_channels][_transferSize];

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
                                    Object outputData = _convertFormat(rawData, _outputFormat);
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

    /** Stop audio capture.
     */
    private void _stopCapture() {
        if (_targetLine != null) {
            if (_targetLine.isOpen() == true) {
                _targetLine.stop();
                _targetLine.close();
                _targetLine = null;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Array of audio samples in double format. */
    private double[][] _audioInDoubleArray;

    /** The number of bytes per sample, default 2. */
    private int _bytesPerSample = 2;

    /** Array of audio samples in byte format. This is double buffered. */
    private byte[][] _captureData = new byte[2][];
    
    /** Indicator of which buffer we are capturing into right now. */
    private int _captureDataBuffer = 0;

    /** Audio format for audio capture. */
    private AudioFormat _captureFormat;
    
    /** true is audio capture is currently active. */
    private boolean _captureIsActive = false;

    /** The number of channels. Deafult is 1. */
    private int _channels = 1;
        
    /** Maximum value for the given number of bits. */
    private double _maxSampleReciprocal = 1.0 / (1L << 16);
    
    /** Specification of the output format to use. */
    private String _outputFormat = "raw";
    
    /** List of formats for the output. */
    private static final String[] _OUTPUT_FORMATS = {"raw", "array", "samples", "aiff", "aifc", "au", "wav"};

    /** Information about the data line. */
    private Info _targetInfo;

    /** Interface to the hardware for producing sound. */
    private TargetDataLine _targetLine;
    
    /** The number of audio samples to transfer per channel when
     * getSamples() is invoked. */
    private int _transferSize = 128;
}
