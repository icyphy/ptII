/* Base class for actors performing audio I/O.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SoundActor

/**
 This actor forms a base class for actors that interact with real-time
 sound through sampled data. This replaces an older and more limited
 actor LiveSoundActor.
 <p>
 Note: Requires Java 5.0.
 @author Edward A. Lee (based on code by Steve Neuendorffer)
 @version  $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (chf)
 @see ptolemy.media.javasound.LiveSound
 @see AudioPlayer
 @see AudioCapture
 */
public class SoundActor extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SoundActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sampleRate = new SharedParameter(this, "sampleRate", SoundActor.class);
        sampleRate.setExpression("8000");
        sampleRate.setTypeEquals(BaseType.INT);

        bytesPerSample = new SharedParameter(this, "bytesPerSample",
                SoundActor.class);
        bytesPerSample.setExpression("2");
        bytesPerSample.setTypeEquals(BaseType.INT);

        channels = new SharedParameter(this, "channels", SoundActor.class);
        channels.setExpression("1");
        channels.setTypeEquals(BaseType.INT);

        bufferSize = new SharedParameter(this, "bufferSize", SoundActor.class);
        bufferSize.setExpression("1024");
        bufferSize.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of bytes per sample. This is an integer that
     *  defaults to 2. This parameter is shared by all instances
     *  of this class and subclasses in the model, so changing it in
     *  one of those actors will cause it to change in all.
     *  An exception will be thrown if this parameter is set to an
     *  unsupported bit resolution.
     *  Changing the value of this parameter will have an effect
     *  only when the model is next initialized.
     */
    public SharedParameter bytesPerSample;

    /** The requested buffer size for transferring samples. This
     *  affects how far ahead of real time the model can get.
     *  A larger buffer size may limit the responsivity of the
     *  model because changes in the model will be heard only
     *  after the buffer has been flushed. This is an integer
     *  that defaults to 1024, representing a buffer with
     *  1024 samples per channel. At an 8 kHz sample rate,
     *  this means a worst-case latency of about 1/8 seconds.
     *  This parameter is shared by all instances
     *  of this class and subclasses in the model, so changing it in
     *  one of those actors will cause it to change in all.
     *  Changing the value of this parameter will have an effect
     *  only when the model is next initialized.
     */
    public SharedParameter bufferSize;

    /** The number of audio channels. This is an integer that
     *  defaults to 1. This parameter is shared by all instances
     *  of this class and subclasses in the model, so changing it in
     *  one of those actors will cause it to change in all.
     *  An exception will be thrown if this parameter is set to an
     *  an unsupported channel number.
     *  Changing the value of this parameter will have an effect
     *  only when the model is next initialized.
     */
    public SharedParameter channels;

    /** The sample rate in samples per second. This is an integer that
     *  defaults to 8000. This parameter is shared by all instances
     *  of this class and subclasses in the model, so changing it in
     *  one of those actors will cause it to change in all.
     *  An exception will be thrown if this parameter is set to an
     *  unsupported sample rate.
     *  Changing the value of this parameter will have an effect
     *  only when the model is next initialized.
     */
    public SharedParameter sampleRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle change requests for all parameters. An exception is
     *  thrown if the requested change is not allowed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not
     *   allowed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == bufferSize) {
            _bufferSize = ((IntToken) bufferSize.getToken()).intValue();
        } else if (attribute == channels) {
            int channelsValue = ((IntToken) channels.getToken()).intValue();
            if (channelsValue < 1) {
                throw new IllegalActionException(this,
                        "Unsupported number of channels: " + channelsValue);
            }
            _channels = channelsValue;
        } else if (attribute == sampleRate) {
            _sampleRate = ((IntToken) sampleRate.getToken()).intValue();
        } else if (attribute == bytesPerSample) {
            _bytesPerSample = ((IntToken) bytesPerSample.getToken()).intValue();
            // Cache the maximum value as a double.
            _maxSample = Math.pow(2.0, _bytesPerSample * 8) - 1.0;
        }

        super.attributeChanged(attribute);
    }

    /** Initialize this actor.  Derived classes should extend this method
     *  to initialize the appropriate audio resource.
     *  @exception IllegalActionException If there is a problem
     *   beginning audio playback.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _isExecuting = true;
    }

    /** Wrapup execution.  Derived classes should override this method
     *  to release access to the audio resources.
     *  @exception IllegalActionException May be thrown by derived classes.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _isExecuting = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Convert a double array of audio samples into a byte array of
     * audio samples in linear signed PCM big endian format. The
     * samples contained in <i>doubleArray</i> should be in the
     * range (-1, 1). Samples outside this range will be hard clipped
     * to the range (-1, 1). The result is put into the byte array
     * given by <i>playbackData</i>, which is required to be big enough
     * (or an ArrayIndexOutOfBounds exception will be thrown).
     * @param doubleArray Two dimensional array holding audio samples.
     *  For each channel, m, doubleArray[m] is a single dimensional
     *  array containing samples for channel m. All channels are
     *  required to have the same number of samples, but this is
     *  not checked.
     * @param playbackData An array into which to put the results, which
     *  have bytes representing linear signed PCM big endian formatted
     *  audio data.
     * @exception IllegalArgumentException Not thrown in this base class.
     */
    protected void _doubleArrayToByteArray(double[][] doubleArray,
            byte[] playbackData) throws IllegalArgumentException {
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
                    playbackData[base + 3] = (byte) intValue;
                    intValue >>>= 8;
                case 3:
                    playbackData[base + 2] = (byte) intValue;
                    intValue >>>= 8;
                case 2:
                    playbackData[base + 1] = (byte) intValue;
                    intValue >>>= 8;
                case 1:
                    playbackData[base] = (byte) intValue;
                    break;
                default:
                    throw new IllegalArgumentException(_bytesPerSample
                            + " is not supported.");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Value of the bytesPerSample parameter. */
    protected int _bytesPerSample;

    /** The requested buffer size. */
    protected int _bufferSize;

    /** The number of channels.  Initialized from the channels parameter. */
    protected int _channels;

    /** Cashed value of the maximum integer value, default for 16 bits. */
    private static double _maxSample = 32767;

    /** The value of the sampleRate parameter. */
    protected int _sampleRate;

    /** True if this actor is executing.  Set to true by initialize(),
     *  set to false by wrapup().
     */
    protected boolean _isExecuting = false;
}
