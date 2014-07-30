/*
 Class containing the platform independent code of LiveSound.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

///////////////////////////////////////////////////////////////////
//// LiveSoundCommon

/**
Class containing the platform independent code of LiveSound actor.

@author Brian K. Vogel and Neil E. Turner and Steve Neuendorffer, Edward A. Lee, Contributor: Dennis Geurts, Ishwinder Singh
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (ishwinde)
@Pt.AcceptedRating Red (ishwinde)
 */
public class LiveSoundCommon {

    /** Add a live sound listener. The listener will be notified
     *  of all changes in live audio parameters. If the listener
     *  is already listening, then do nothing.
     *
     *  @param listener The LiveSoundListener to add.
     *  @see #removeLiveSoundListener(LiveSoundListener)
     */
    public void addLiveSoundListener(LiveSoundListener listener) {
        if (!_liveSoundListeners.contains(listener)) {
            _liveSoundListeners.add(listener);
        }
    }

    /** Return the number of bits per sample.
     *  @return The number of bits per sample, which in this base
     *  class defaults to 16.
     */
    public int getBitsPerSample() {
        return _bitsPerSample;
    }

    /** Return the suggested size of the internal capture and playback audio
     *  buffers, in samples per channel. This parameter is set by the
     *  setBufferSize() method.  There is no guarantee that the value returned
     *  is the actual buffer size used for capture and playback.
     *  Furthermore, the buffers used for capture and playback may have
     *  different sizes.  The default value of this parameter is 4096.
     *
     *  @return The suggested internal buffer size in samples per
     *   channel.
     */
    public int getBufferSize() {
        return _bufferSize;
    }

    /** Return the number of audio channels, which is set by
     *  the setChannels() method. The default value of this
     *  parameter is 1 (for mono audio).
     *
     *  @return The number of audio channels.
     */
    public int getChannels() {
        return _channels;
    }

    /** Return the current sampling rate in Hz, which is set
     *  by the setSampleRate() method. The default value of
     *  this parameter is 8000 Hz.
     *
     *  @return The sample rate in Hz.
     */
    public int getSampleRate() {
        return (int) _sampleRate;
    }

    /** Get the array length (in samples per channel) to use
     *  for capturing and playing samples via the putSamples()
     *  and getSamples() methods. This method gets the size
     *  of the 2nd dimension of the 2-dimensional array
     *  used by the putSamples() and getSamples() methods. This
     *  method returns the value that was set by the
     *  setTransferSize(). If setTransferSize() was not invoked,
     *  the default value of 128 is returned.
     *
     *  @return The size of the 2nd dimension of the 2-dimensional
     *   array used by the putSamples() and getSamples() methods.
     *  @see #setTransferSize(int)
     */
    public int getTransferSize() {
        return _transferSize;
    }

    /** Return true if audio capture is currently active.
     *  Otherwise return false.
     *
     *  @return True If audio capture is currently active.
     *  Otherwise return false.
     */
    public boolean isCaptureActive() {
        return _captureIsActive;
    }

    /** Return true if audio playback is currently active.
     *  Otherwise return false.
     *
     *  @return True If audio playback is currently active.
     *  Otherwise return false.
     */
    public boolean isPlaybackActive() {
        return _playbackIsActive;
    }

    /** Remove a live sound listener. If the listener is
     *  is not listening, then do nothing.
     *
     *  @param listener The LiveSoundListener to remove.
     *  @see #addLiveSoundListener(LiveSoundListener)
     */
    public void removeLiveSoundListener(LiveSoundListener listener) {
        if (_liveSoundListeners.contains(listener)) {
            _liveSoundListeners.remove(listener);
        }
    }

    /** Set the array length (in samples per channel) to use
     *  for capturing and playing samples via the putSamples()
     *  and getSamples() methods. This method sets the size
     *  of the 2nd dimension of the 2-dimensional array
     *  used by the putSamples() and getSamples() methods. If
     *  this method is not invoked, the default value of 128 is
     *  used.
     *  <p>
     *  This method should only be called while audio capture and
     *  playback are inactive. Otherwise an exception will occur.
     *
     *  @param transferSize The  size of the 2nd dimension of
     *   the 2-dimensional array used by the putSamples() and
     *   getSamples() methods
     *
     *  @exception IllegalStateException If this method is called
     *   while audio capture or playback are active.
     *  @see #getTransferSize()
     */
    public void setTransferSize(int transferSize) throws IllegalStateException {
        // This change only affects capture, so it's OK for it to occur
        // while there is playback.
        if (_captureIsActive) {
            throw new IllegalStateException("LiveSound: "
                    + "setTransferSize() was called while audio capture "
                    + "or playback was active.");
        } else {
            _transferSize = transferSize;
        }
    }

    /** Convert a byte array of audio samples in linear signed PCM big endian
     * format into a double array of audio samples (-1, 1) range.
     * @param doubleArray The resulting array of doubles.
     * @param byteArray  The linear signed pcm big endian byte array
     * formatted array representation of audio data.
     */
    protected void _byteArrayToDoubleArray(double[][] doubleArray,
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

    /** Convert a double array of audio samples into a byte array of
     * audio samples in linear signed PCM big endian format. The
     * samples contained in <i>doubleArray</i> should be in the
     * range (-1, 1). Samples outside this range will be hard clipped
     * to the range (-1, 1).
     * @param doubleArray Two dimensional array holding audio samples.
     *  For each channel, m, doubleArray[m] is a single dimensional
     *  array containing samples for channel m. All channels are
     *  required to have the same number of samples, but this is
     *  not checked.
     * @return The linear signed PCM big endian byte array formatted
     *  array representation of <i>doubleArray</i>. The length of
     *  the returned array is (doubleArray[i].length*bytesPerSample*channels).
     */
    protected byte[] _doubleArrayToByteArray(double[][] doubleArray) {
        // This method is most efficient if repeated calls pass the same size
        // array. In this case, it does not re-allocate the byte array that
        // it returns, but rather reuses the same array on the heap.
        int numberOfSamples = doubleArray[0].length;
        int bufferSize = numberOfSamples * _bytesPerSample * _channels;
        if (_playbackData == null || _playbackData.length != bufferSize) {
            // Hopefully, the allocation is done only once.
            _playbackData = new byte[bufferSize];
        }
        // Iterate over the samples.
        for (int currSamp = 0; currSamp < doubleArray[0].length; currSamp++) {
            // For each channel,
            for (int currChannel = 0; currChannel < _channels; currChannel++) {
                double sample = doubleArray[currChannel][currSamp];

                // Perform clipping, if necessary.
                if (sample > 1.0) {
                    sample = 1.0;
                } else if (sample < -1.0) {
                    sample = -1.0;
                }

                // signed integer representation of current sample of the
                // current channel.
                // Note: Floor instead of cast to remove deadrange at zero.
                int intValue = (int) Math.floor(sample * _maxSample);

                int base = currSamp * _bytesPerSample * _channels
                        + _bytesPerSample * currChannel;
                // Create byte representation of current sample.
                // Note: unsigned Shift right.
                // Note: fall through from higher number cases.
                switch (_bytesPerSample) {
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

    /** Notify the live sound listeners about a change in an audio
     *  parameter.
     *
     *  @param parameter The audio parameter of LiveSound that
     *   has changed. The value of parameter should be one of
     *   LiveSoundEvent.SAMPLE_RATE, LiveSoundEvent.CHANNELS,
     *   LiveSoundEvent.BUFFER_SIZE, or
     *   LiveSoundEvent.BITS_PER_SAMPLE.
     */
    protected void _notifyLiveSoundListeners(int parameter) {
        if (_liveSoundListeners.size() > 0) {
            LiveSoundEvent event = new LiveSoundEvent(parameter);
            Iterator listeners = _liveSoundListeners.iterator();

            while (listeners.hasNext()) {
                ((LiveSoundListener) listeners.next()).liveSoundChanged(event);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Array of audio samples in double format. */
    protected double[][] _audioInDoubleArray;

    /** The number of bits per sample. Default is 16. */
    protected int _bitsPerSample = 16;

    /** The requested buffer size in samples per channel. */
    protected int _bufferSize = 1024;

    /** The number of bytes per sample, default 2. */
    protected int _bytesPerSample = 2;

    /** true is audio capture is currently active. */
    protected boolean _captureIsActive = false;

    /** The number of channels. Deafult is 1. */
    protected int _channels = 1;

    /** Array of audio samples in byte format. */
    protected byte[] _captureData;

    /** Byte buffer used for playback data. */
    protected byte[] _playbackData;

    /** The list of listeners. */
    protected List _liveSoundListeners = new LinkedList();

    /** Cached value of the maximum value scaling factor, default for
     * 16 bits.
     */
    protected double _maxSampleReciprocal = 1.0 / 32768;

    /** Cached value of the maximum integer value, default for 16
     *  bits.
     */
    protected double _maxSample = 32767;

    /** true is audio playback is currently active. */
    protected boolean _playbackIsActive = false;

    /** The sample rate. */
    protected float _sampleRate;

    /** The list of sound consumers. */
    protected List _soundConsumers = new LinkedList();

    /** The number of audio samples to transfer per channel when
     * getSamples() is invoked. */
    protected int _transferSize = 128;

}
