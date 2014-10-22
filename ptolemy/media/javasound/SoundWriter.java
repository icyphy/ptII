/* A buffer that supports the writing of audio samples to a sound file.

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

///////////////////////////////////////////////////////////////////
//// SoundWriter

/**
 This class is a buffer that supports the writing of audio samples
 to a sound file. Specifically, this buffer supports the writing of
 double-valued audio samples. The maximum valid range of sample
 values is from -1.0 to 1.0. Any values outside of this range will
 be hard-clipped to fall within this range.
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
 The path to the sound file to write is given as a constructor
 parameter, along with parameters that specify the desired
 audio format. The constructor also takes an array length
 parameter, which is explained below.
 <p>
 After invoking the constructor, the putSamples() method should
 be repeatedly invoked to write samples to the specified sound
 file. This method is blocking, so it will not return until the
 samples have been written. The putSamples() method takes a
 multidimensional array as a parameter. The first index
 represents the channel number (0 for first channel, 1 for  second
 channel, etc.). The second index represents the sample index within
 a channel. For each channel i, the size of the array,
 putSamplesArray[i].length, must be equal to the constructor
 parameter putSamplesArraySize. Otherwise an exception will
 occur. Thus, each call to putSamples() writes putSamplesArraySize
 on each channel. It should be noted that the putSamples() method
 does not write samples directly to a sound file, but instead
 writes samples to an internal array. The internal array of
 samples is written to the sound file by the closeFile() method.
 <p>
 The closeFile() method should be invoked when no more samples
 need to be written. This method will write the internal array
 of samples to the output file and close the file. It is not
 possible to write any more samples after closeFile() has been
 called. An exception will occur if putSamples() is invoked
 at any point after closeFile() is invoked.
 <p>
 <b>Security issues</b>
 <p>
 Applications have no restrictions on the
 capturing or playback of audio. Applet code is not allowed to
 write native files by default. The .java.policy file must be
 modified to grant applets more privileges.
 <p>
 Note: Requires Java 2 v1.3.0 or later.

 @author Brian K. Vogel
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.media.javasound.SoundCapture
 */
public class SoundWriter {
    /** Construct a sound writer object with the specified name.
     *  Valid sound file formats are WAVE (.wav), AIFF (.aif,
     *  .aiff), AU (.au). The file format is automatically
     *  determined from the file extension.
     *
     *  @param fileName The file name to create. If the file already
     *  exists, overwrite it. Valid sound file formats are WAVE (.wav),
     *  AIFF (.aif, .aiff), AU (.au). The file format to write is
     *  determined automatically from the file extension.
     *  @param sampleRate Sample rate in Hz. Must be in the range: 8000
     *  to 48000.
     *  @param bitsPerSample Number of bits per sample (valid choices are
     *  8 or 16).
     *  @param channels Number of audio channels. 1 for mono, 2 for
     *  stereo.
     *  @param putSamplesArraySize Size of the array parameter of
     *   putSamples(). There is no restriction on the value of
     *   this parameter, but typical values are 64-2024.
     */
    public SoundWriter(String fileName, float sampleRate, int bitsPerSample,
            int channels, int putSamplesArraySize) {
        _isAudioWriterActive = false;
        this._fileName = fileName;
        this._bitsPerSample = bitsPerSample;
        this._sampleRate = sampleRate;
        this._channels = channels;
        _initializeAudio();
        _isAudioWriterActive = true;

        if (_debug) {
            System.out.println("SoundWriter: constructor : fileName = "
                    + fileName);
            System.out.println("SoundWriter: constructor : bitsPerSample = "
                    + bitsPerSample);
            System.out.println("SoundWriter: constructor : sampleRate = "
                    + sampleRate);
            System.out.println("SoundWriter: constructor : channels = "
                    + channels);
            System.out
                    .println("SoundWriter: constructor : putSamplesArraySize = "
                            + putSamplesArraySize);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ///  Public Methods                                         ///

    /** Append the audio data contained in <i>putSamplesArray</i>
     *  to an internal array. The audio samples in this array will
     *  be written to the sound file specified in the constructor
     *  when the closeFile() method is invoked.
     *  <p>
     *  The samples should be in the range (-1, 1). Samples that are
     *  outside this range will be hard-clipped so that they fall
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
     *  equal to <i>putSamplesSize</i>, for all channels n. An exception
     *  will occur if this is not the case.
     *
     *  @exception IllegalStateException If closeFile() has already
     *   been called.
     */
    public void putSamples(double[][] putSamplesArray)
            throws IllegalStateException {
        if (_isAudioWriterActive == true) {
            // Convert array of double valued samples into
            // the proper byte array format.
            _data = _doubleArrayToByteArray(putSamplesArray, _bytesPerSample,
                    _channels);

            // Add new audio data to the file buffer array.
            for (byte element : _data) {
                _toFileBuffer.add(Byte.valueOf(element));
            }
        } else {
            throw new IllegalStateException("SoundWriter: "
                    + "putSamples() was called while audio playback was"
                    + " inactive (startPlayback() was never called or "
                    + "stopPlayback has already been called).");
        }
    }

    /** Open a the file specified in the constructor for writing,
     *  write the accumulated audio samples (obtained via
     *  putSamples()) to the file specified in the constructor,
     *  and close the file. This method should be called when
     *  no more calls to putSamples() are required. An exception
     *  will occur if putSamples() is called after this method is invoked.
     *
     *  @exception IOException If there is a problem closing the
     *   audio resources, or if the "write audio data
     *   to file" constructor was used  and the sound file has an
     *   unsupported format.
     */
    public void closeFile() throws IOException {
        // IMPLEMENTATION NOTE: It is probably better to open the
        // file for writing in the constructor. putSamples() should
        // probably write samples to the file on each invocation.
        // closeFile() should only close the file. This would result in
        // more efficient operation and reduced memory usage. There
        // does not appear to be an easy way to do this in javasound,
        // however. This is because in javasound, there is an
        // AudioInputStream but no AudioOutputStream class.
        if (_debug) {
            System.out.println("SoundWriter: stopPlayback(): invoked");
        }

        if (_isAudioWriterActive == true) {
            // Record data to sound file.
            _stopPlaybackToFile();
        }

        _isAudioWriterActive = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Perform initialization that must be done before putSamples()
     *  can be invoked.
     */
    private void _initializeAudio() {
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

        _playToFileFormat = new AudioFormat(_sampleRate, _bitsPerSample,
                _channels, signed, bigEndian);

        _frameSizeInBytes = _playToFileFormat.getFrameSize();
        _bytesPerSample = _bitsPerSample / 8;
    }

    /** Open a the file specified in the constructor for writing,
     *  write the accumulated audio samples (obtained via
     *  putSamples()) to the file specified in the constructor,
     *  and close the file. This method should be called when
     *  no more calls to putSamples() are required.
     */
    private void _stopPlaybackToFile() throws IOException {
        int size = _toFileBuffer.size();
        byte[] audioBytes = new byte[size];

        for (int i = 0; i < size; i++) {
            Byte j = (Byte) _toFileBuffer.get(i);
            audioBytes[i] = j.byteValue();
        }

        ByteArrayInputStream byteInputArrayStream = new ByteArrayInputStream(
                audioBytes);

        AudioInputStream audioInputStream = new AudioInputStream(
                byteInputArrayStream, _playToFileFormat, audioBytes.length
                        / _frameSizeInBytes);

        outFile = new File(_fileName);

        try {
            StringTokenizer st = new StringTokenizer(_fileName, ".");

            // Do error checking:
            if (st.countTokens() != 2) {
                throw new IOException("Error: Incorrect "
                        + "file name format. " + "Format: filename.extension");
            }

            st.nextToken(); // Advance to the file extension.

            String fileExtension = st.nextToken();

            if (fileExtension.equalsIgnoreCase("au")) {
                // Save the file.
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.AU,
                        outFile);
            } else if (fileExtension.equalsIgnoreCase("aiff")) {
                // Save the file.
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.AIFF,
                        outFile);
            } else if (fileExtension.equalsIgnoreCase("wave")) {
                // Save the file.
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE,
                        outFile);
            } else if (fileExtension.equalsIgnoreCase("wav")) {
                // Save the file.
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE,
                        outFile);
            } else if (fileExtension.equalsIgnoreCase("aifc")) {
                // Save the file.
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.AIFC,
                        outFile);
            } else {
                throw new IOException("Error saving "
                        + "file: Unknown file format: " + fileExtension);
            }
        } catch (IOException e) {
            throw new IOException("SoundWriter: error saving" + " file: " + e);
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

        maxDoubleValuedSample = (maxSample - 2) / maxSample;

        byte[] byteArray = new byte[lengthInSamples * bytesPerSample * channels];
        byte[] b = new byte[bytesPerSample];

        for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {
            int l;

            // For each channel,
            for (int currChannel = 0; currChannel < channels; currChannel++) {
                // Perform clipping, if necessary.
                if (doubleArray[currChannel][currSamp] >= maxDoubleValuedSample) {
                    l = (int) maxSample - 2;
                } else if (doubleArray[currChannel][currSamp] <= -maxDoubleValuedSample) {
                    l = (int) -maxSample + 2;
                } else {
                    // signed integer representation of current sample of the
                    // current channel.
                    l = (int) (doubleArray[currChannel][currSamp] * maxSample);
                }

                // Create byte representation of current sample.
                for (int i = 0; i < bytesPerSample; i += 1, l >>= 8) {
                    b[bytesPerSample - i - 1] = (byte) l;
                }

                // Copy the byte representation of current sample to
                // the linear signed pcm big endian formatted byte array.
                for (int i = 0; i < bytesPerSample; i += 1) {
                    byteArray[currSamp * bytesPerSample * channels
                            + bytesPerSample * currChannel + i] = b[i];
                }
            }
        }

        return byteArray;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The output file to write to.
    private File outFile;

    private String _fileName;

    private int _bitsPerSample;

    private float _sampleRate;

    private int _channels;

    // Array of audio samples in byte format.
    private byte[] _data;

    private int _frameSizeInBytes;

    private ArrayList _toFileBuffer;

    // This is the format of _toFileBuffer.
    private AudioFormat _playToFileFormat;

    private int _bytesPerSample;

    private boolean _isAudioWriterActive;

    /////////////// For debugging: ///////////////////////////////
    // Set this variable to "true" to enable debugging information.
    //private boolean _debug = true;
    private boolean _debug = false;

    ///////////////////////////////////////////////////////////////////
}
