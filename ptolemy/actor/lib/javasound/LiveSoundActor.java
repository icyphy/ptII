/* An actor that reads in audio samples and plays the audio data.

@Copyright (c) 2000-2003 The Regents of the University of California.
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
@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (chf@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.javasound;

import java.io.IOException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.media.javasound.LiveSound;
import ptolemy.media.javasound.LiveSoundEvent;
import ptolemy.media.javasound.LiveSoundListener;


/////////////////////////////////////////////////////////
//// LiveSoundActor
/**
This actor forms a base class for actors that interact with real-time
sound through the ptolemy.media.LiveSound class.  This class manages the 
parameters for live sound.
<p>
Note: Requires Java 2 v1.3.0 or later.
@author Steve Neuendorffer
@version  $Id$
@since Ptolemy II 1.0
@see ptolemy.media.javasound.LiveSound
@see AudioPlayer
@see AudioCapture
*/
public class LiveSoundActor extends TypedAtomicActor 
    implements LiveSoundListener {

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
        
        sampleRate = new Parameter(this, "sampleRate", new IntToken(8000));
        sampleRate.setTypeEquals(BaseType.INT);

        bitsPerSample = new Parameter(this, "bitsPerSample",
                new IntToken(16));
        bitsPerSample.setTypeEquals(BaseType.INT);

        channels = new Parameter(this, "channels",
                new IntToken(1));
        channels.setTypeEquals(BaseType.INT);
 
        transferSize = new Parameter(this, "transferSize");
        transferSize.setExpression("1");
        transferSize.setTypeEquals(BaseType.INT);

        // Add this class as a listener of live sound change
        // events.
        LiveSound.addLiveSoundListener(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The desired sample rate to use, in Hz. The default value
     *  is an IntToken equal to 8000.
     *  <p>
     *  An exception will be occur if this parameter is set to an
     *  unsupported sample rate.
     */
    public Parameter sampleRate;

    /** The number desired number of bits per sample. The default
     *  value is an IntToken equal to 16.
     *  <p>
     *  An exception will occur if this parameter is set to an
     *  unsupported bit resolution.
     */
    public Parameter bitsPerSample;

    /** The number of audio channels to use. The default value is
     *  an IntToken equal to 1.
     *  <p>
     *  An exception will occur if this parameter is set to an
     *  an unsupported channel number.
     */
    public Parameter channels;

    /** The number of samples that will be transfered to the audio driver
     *  together.  The default is 128.
     */
    public Parameter transferSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle change requests for all parameters. An exception is
     *  thrown if the requested change is not allowed.
     *  @exception IllegalActionException If the change is not
     *   allowed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        try {
            if (attribute == transferSize) {
                // The size of the array (in samples per channel) to pass
                // to LiveSound.putSamples().
                _transferSize =
                    ((IntToken)transferSize.getToken()).intValue();
                if (!_isExecuting && 
                        LiveSound.getTransferSize() != _transferSize) {
                    LiveSound.setTransferSize(_transferSize);
                }
            } else if (attribute == channels) {
                int channelsInt =
                    ((IntToken)channels.getToken()).intValue();
                if (channelsInt < 1) {
                    throw new IllegalActionException(this,
                            "Attempt to set channels parameter to an illegal "
                            + "value of: " +  channelsInt
                            + " . The value must be a "
                            + "positive integer.");
                }
               
                // Only set the channels if it is different than
                // the currently active channels.
                if (!_isExecuting && 
                        LiveSound.getChannels() != channelsInt) {
                    LiveSound.setChannels(channelsInt);
                }
            }  else if (attribute == sampleRate) {
                int sampleRateInt =
                    ((IntToken)sampleRate.getToken()).intValue();
                // Only set the sample rate if it is different than
                // the currently active sample rate.
                if (!_isExecuting && 
                        LiveSound.getSampleRate() != sampleRateInt) {
                    LiveSound.setSampleRate(sampleRateInt);
                }
            } else if (attribute == bitsPerSample) {
                int bitsPerSampleInt =
                    ((IntToken)bitsPerSample.getToken()).intValue();
                // Only set the bitsPerSample if it is different than
                // the currently active bitsPerSample.
                if (!_isExecuting && 
                        LiveSound.getBitsPerSample() != bitsPerSampleInt) {
                    LiveSound.setBitsPerSample(bitsPerSampleInt);
                }
            } 
            super.attributeChanged(attribute);
            return;
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot perform audio playback " +
                    "with the specified parameter values.");
        }
    }

    /** Initialize this actor.  Derived classes should extend this method
     *  to initialize the appropriate audio resource.
     *  @exception IllegalActionException If there is a problem
     *   beginning audio playback.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _isExecuting = true;
    }

    /** Notify this actor that the an audio parameter of LiveSound has
     *  changed.
     *
     *  @param event The live sound change event.
     */
    public void liveSoundChanged(LiveSoundEvent event) {
        // Check to see what parameter was changed.
        int changedParameter = event.getSoundParameter();
        try {
            if (changedParameter == LiveSoundEvent.SAMPLE_RATE) {
                // Get the currently active sample rate.
                int activeSampleRate = LiveSound.getSampleRate();
                // Get the current value of this actor's sampleRate parameter.
                int thisActorSampleRate =
                    ((IntToken)sampleRate.getToken()).intValue();
                // Only set the sampleRate parameter if it is different from
                // the new sample rate.
                if (activeSampleRate != thisActorSampleRate) {
                    sampleRate.setToken(new IntToken(activeSampleRate));
                }
            }  else if (changedParameter == LiveSoundEvent.CHANNELS) {
                // Get the currently active number of channels.
                int activeChannels = LiveSound.getChannels();
                // Get the current value of this actor's sampleRate parameter.
                int thisActorChannels =
                    ((IntToken)channels.getToken()).intValue();
                // Only set the channels parameter if it is different from
                // the new channels.
                if (activeChannels != thisActorChannels) {
                    channels.setToken(new IntToken(activeChannels));
                }
            } else if (changedParameter == LiveSoundEvent.BITS_PER_SAMPLE) {
                // Get the currently active bitsPerSample.
                int activeBitsPerSample = LiveSound.getBitsPerSample();
                // Get the current value of this actor's bitsPerSample
                // parameter.
                int thisActorBitsPerSample =
                    ((IntToken)bitsPerSample.getToken()).intValue();
                // Only set the channels parameter if it is different from
                // the new channels.
                if (activeBitsPerSample != thisActorBitsPerSample) {
                    bitsPerSample.setToken(new IntToken(activeBitsPerSample));
                }
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(
                    "Error responding to audio parameter change. " +
                    ex);
        }
    }

    /** Wrapup execution.  Derived classes should override this method
     *  to release access to the audio resources.
     *  @exception IllegalActionException May be thrown by derived classes.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _isExecuting = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the audio system.  Set parameters in the audio system
     *  according to the parameters of this actor.
     *  Derived classes should call this method during initialize().
     */
    protected synchronized void _initializeAudio()
            throws IllegalActionException, IOException {
              
        // Initialize audio.
        _transferSize = ((IntToken)transferSize.getToken()).intValue();
        _channels = ((IntToken)channels.getToken()).intValue();
        int sampleRateInt = ((IntToken)sampleRate.getToken()).intValue();
        int bitsPerSampleInt =
            ((IntToken)bitsPerSample.getToken()).intValue();
  
        if (LiveSound.getSampleRate() != sampleRateInt) {
            LiveSound.setSampleRate(sampleRateInt);
        }
        if (LiveSound.getBitsPerSample() != bitsPerSampleInt) {
            LiveSound.setBitsPerSample(bitsPerSampleInt);
        }
        if (LiveSound.getChannels() != _channels) {
            LiveSound.setChannels(_channels);
        }
        if (LiveSound.getBufferSize() != 1000) {
            LiveSound.setBufferSize(1000);
        }
        if (LiveSound.getTransferSize() != _transferSize) {
            LiveSound.setTransferSize(_transferSize);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////

    protected int _channels;
    protected int _transferSize;
    protected boolean _isExecuting = false;
}
