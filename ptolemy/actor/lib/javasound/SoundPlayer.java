/* An actor that reads in audio samples and plays the audio data.

 @Copyright (c) 2007-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.javasound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SoundPlayer

/**
 This actor plays audio samples provided on the input port.
 The audio samples that are supplied to
 this actor should be doubles in the range -1.0 to 1.0, provided
 as a DoubleMatrix, where the first index of the matrix represents
 the channel and the second index is the sample number. That is,
 each row is a sequence of samples for the channel corresponding
 to the row number. Any input
 value that is outside of the valid range will be hard-clipped
 to fall within the range [-1.0, 1.0] before it is written
 to the audio output port of the computer.
 <p>
 The parameters are as follows:
 <ul>
 <li><i>sampleRate</i> should be set to desired sample rate, in Hz.
 The default value is 8000. Allowable values are 8000, 11025,
 22050, 44100, and 48000 Hz.
 <li><i>bytesPerSample</i> gives the resolution of audio samples.
 This is an integer that defaults to 2, meaning 16-bit samples.
 <li><i>channels</i> should be set to desired number of audio
 channels. Allowable values are 1 (for mono) and 2 (for stereo).
 The default value is 1. Some sound cards support more than two
 audio channels, but this is not supported in Java.
 FIXME: If this differs from the input dimensions?
 <li><i>transferSize</i> The number of samples that will
 be transferred to the audio driver
 together.  This is an integer with default 128.
 <li><i></i>  The requested buffer size in the audio hardware. This
 affects how far ahead of real time the model can get. There is no
 harm in making this large because this actor will overwrite previously
 queued values if necessary. This is an integer
 that defaults to 8000, representing a buffer with
 8000 samples per channel. At an 8 kHz sample rate,
 this corresponds to one second of sound.
 </ul>
 <p>
 All of these parameters are shared by all audio actors that
 use the audio hardware, so changing them in
 one of those actors will cause it to change in all.
 <p>
 Note: Requires Java 2 v1.3.0 or later.
 @author  Edward A. Lee
 @version  $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 @see ptolemy.media.javasound.LiveSound
 @see AudioCapture
 @see AudioReader
 @see AudioWriter
 */
public class SoundPlayer extends SoundActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SoundPlayer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE_MATRIX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This port must receive double matrix tokens (in the
     *  range of -1.0 to 1.0).
     */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Obtain access to the audio playback hardware, and start playback.
     *  An exception will occur if there is a problem starting
     *  playback. This will occur if another SoundPlayer actor is
     *  playing audio.
     *  @exception IllegalActionException If there is a problem
     *   beginning audio playback.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_sourceDataLine != null) {
            if (_sourceDataLine.isOpen()) {
                // The following method will block until prior output completes.
                _sourceDataLine.drain();
                _sourceDataLine.close();
            }
        }
        boolean signed = true; // For readability.
        boolean bigEndian = true; // For readability.
        AudioFormat format = new AudioFormat(_sampleRate, _bytesPerSample * 8,
                _channels, signed, bigEndian);
        // Get a source data line from the default mixer.
        // NOTE: THis is really a target for audio data, not a source.
        try {
            _sourceDataLine = AudioSystem.getSourceDataLine(format);
            // Open line and suggest a buffer size (in bytes) to use or
            // the internal audio buffer.
            _sourceDataLine.open(format, _bufferSize * _bytesPerSample
                    * _channels);
            _sourceDataLine.start();
        } catch (LineUnavailableException e) {
            throw new IllegalActionException(this, e,
                    "Specified audio format is not available on this hardware.");
        }

        // FIXME: Consider allowing alternative mixers, obtained using something like:
        /*
        Info[] mixers = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixers.length; i++) {
            System.out.println(mixers[i].getName());
            System.out.println(mixers[i].getDescription());
            System.out.println(mixers[i].getVendor());
        }
         */
    }

    /** Read an input array and send to the audio hardware.
     *  If the audio buffer cannot accept the samples, then this
     *  method will stall the calling thread until it can.
     *  @exception IllegalActionException If there is a problem
     *   playing audio.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            DoubleMatrixToken token = (DoubleMatrixToken) input.get(0);
            double[][] data = token.doubleMatrix();

            // This method is most efficient if repeated calls pass the same size
            // array. In this case, it does not re-allocate the byte array that
            // it returns, but rather reuses the same array on the heap.
            int numberOfSamples = data[0].length;
            int bufferSize = numberOfSamples * _bytesPerSample * _channels;
            if (_playbackData == null || _playbackData.length != bufferSize) {
                // Hopefully, the allocation is done only once.
                _playbackData = new byte[bufferSize];
            }
            _doubleArrayToByteArray(data, _playbackData);
            _sourceDataLine.write(_playbackData, 0, bufferSize);
        }
        return super.postfire();
    }

    /** Stop audio playback and free up any audio resources used
     *  for audio playback.
     *  @exception IllegalActionException If there is a problem
     *   stopping audio playback.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        // Stop playback. Close any open sound files. Free
        // up audio system resources.
        if (_sourceDataLine != null) {
            _sourceDataLine.drain();
            _sourceDataLine.stop();
            _sourceDataLine.close();
        }
        _sourceDataLine = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Byte buffer used for playback data. */
    private byte[] _playbackData;

    /** The data line being used for playback. */
    protected SourceDataLine _sourceDataLine;
}
