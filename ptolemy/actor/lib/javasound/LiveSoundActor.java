/* An actor that reads in audio samples and plays the audio data.

 @Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.io.IOException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.media.javasound.LiveSound;

///////////////////////////////////////////////////////////////////
//// LiveSoundActor

/**
 This actor forms a base class for actors that interact with real-time
 sound through the ptolemy.media.LiveSound class.  This class manages the
 parameters for live sound.
 <p>
 Note: Requires Java 2 v1.3.0 or later.
 @author Steve Neuendorffer, Edward A. Lee (contributor)
 @version  $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (chf)
 @see ptolemy.media.javasound.LiveSound
 @see AudioPlayer
 @see AudioCapture
 */
public class LiveSoundActor extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LiveSoundActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sampleRate = new SharedParameter(this, "sampleRate",
                LiveSoundActor.class);
        sampleRate.setExpression("8000");
        sampleRate.setTypeEquals(BaseType.INT);

        bitsPerSample = new SharedParameter(this, "bitsPerSample",
                LiveSoundActor.class);
        bitsPerSample.setExpression("16");
        bitsPerSample.setTypeEquals(BaseType.INT);

        channels = new SharedParameter(this, "channels", LiveSoundActor.class);
        channels.setExpression("1");
        channels.setTypeEquals(BaseType.INT);

        transferSize = new SharedParameter(this, "transferSize",
                LiveSoundActor.class);
        transferSize.setExpression("128");
        transferSize.setTypeEquals(BaseType.INT);

        bufferSize = new SharedParameter(this, "bufferSize",
                LiveSoundActor.class);
        bufferSize.setExpression("1024");
        bufferSize.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of bits per sample. This is an integer that
     *  defaults to 16. This parameter is shared by all instances
     *  of this class and subclasses in the model, so changing it in
     *  one of those actors will cause it to change in all.
     *  An exception will be thrown if this parameter is set to an
     *  unsupported bit resolution (currently, only 8 and 16 bits
     *  are supported).
     */
    public SharedParameter bitsPerSample;

    /** The requested buffer size in the audio hardware. This
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
     */
    public SharedParameter bufferSize;

    /** The number of audio channels. This is an integer that
     *  defaults to 1. This parameter is shared by all instances
     *  of this class and subclasses in the model, so changing it in
     *  one of those actors will cause it to change in all.
     *  An exception will be thrown if this parameter is set to an
     *  an unsupported channel number.
     */
    public SharedParameter channels;

    /** The sample rate in samples per second. This is an integer that
     *  defaults to 8000. This parameter is shared by all instances
     *  of this class and subclasses in the model, so changing it in
     *  one of those actors will cause it to change in all.
     *  An exception will be thrown if this parameter is set to an
     *  unsupported sample rate.
     */
    public SharedParameter sampleRate;

    /** The number of samples that will be transferred to the audio driver
     *  together.  This is an integer with default 128.
     *  This parameter is shared by all instances
     *  of this class and subclasses in the model, so changing it in
     *  one of those actors will cause it to change in all.
     */
    public SharedParameter transferSize;

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
        try {
            if (attribute == transferSize) {
                _transferSize = ((IntToken) transferSize.getToken()).intValue();
                if (!_isExecuting
                        && LiveSound.getTransferSize() != _transferSize) {
                    LiveSound.setTransferSize(_transferSize);
                }
            } else if (attribute == bufferSize) {
                _bufferSize = ((IntToken) bufferSize.getToken()).intValue();
                if (!_isExecuting && LiveSound.getBufferSize() != _bufferSize) {
                    LiveSound.setBufferSize(_bufferSize);
                }
            } else if (attribute == channels) {
                _channels = ((IntToken) channels.getToken()).intValue();

                if (_channels < 1) {
                    throw new IllegalActionException(this,
                            "Attempt to set channels parameter to an illegal "
                                    + "value of: " + _channels
                                    + " . The value must be a "
                                    + "positive integer.");
                }

                // Only set the channels if it is different than
                // the currently active channels.
                if (!_isExecuting && LiveSound.getChannels() != _channels) {
                    LiveSound.setChannels(_channels);
                }
            } else if (attribute == sampleRate) {
                _sampleRate = ((IntToken) sampleRate.getToken()).intValue();

                // Only set the sample rate if it is different than
                // the currently active sample rate.
                if (!_isExecuting && LiveSound.getSampleRate() != _sampleRate) {
                    LiveSound.setSampleRate(_sampleRate);
                }
            } else if (attribute == bitsPerSample) {
                _bitsPerSample = ((IntToken) bitsPerSample.getToken())
                        .intValue();

                // Only set the bitsPerSample if it is different than
                // the currently active bitsPerSample.
                if (!_isExecuting
                        && LiveSound.getBitsPerSample() != _bitsPerSample) {
                    LiveSound.setBitsPerSample(_bitsPerSample);
                }
            }

            super.attributeChanged(attribute);
            return;
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot perform audio playback "
                            + "with the specified parameter values.");
        }
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
    ////                         private methods                   ////

    /** Initialize the audio system.  Set parameters in the audio system
     *  according to the parameters of this actor.
     *  Derived classes should call this method during initialize().
     *  @exception IllegalActionException If there is a problem
     *  reading or setting a parameter.
     *  @exception IOException If there is a problem setting the
     *  bits per sample, channels or buffer size.
     */
    protected synchronized void _initializeAudio()
            throws IllegalActionException, IOException {
        // Initialize audio.
        if (LiveSound.getSampleRate() != _sampleRate) {
            LiveSound.setSampleRate(_sampleRate);
        }
        if (LiveSound.getBitsPerSample() != _bitsPerSample) {
            LiveSound.setBitsPerSample(_bitsPerSample);
        }
        if (LiveSound.getChannels() != _channels) {
            LiveSound.setChannels(_channels);
        }
        if (LiveSound.getBufferSize() != _bufferSize) {
            LiveSound.setBufferSize(_bufferSize);
        }
        if (LiveSound.getTransferSize() != _transferSize) {
            LiveSound.setTransferSize(_transferSize);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Value of the bitsPerSample parameter. */
    protected int _bitsPerSample;

    /** The requested buffer size. */
    protected int _bufferSize;

    /** The number of channels.  Initialized from the channels parameter. */
    protected int _channels;

    /** The value of the sampleRate parameter. */
    protected int _sampleRate;

    /** The transfer size.  Initialized from the transferSize parameter. */
    protected int _transferSize;

    /** True if this actor is executing.  Set to true by initialize(),
     *  set to false by wrapup().
     */
    protected boolean _isExecuting = false;
}
