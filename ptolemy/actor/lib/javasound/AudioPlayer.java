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
@ProposedRating Yellow (vogel@eecs.berkeley.edu)
@AcceptedRating Yellow (chf@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.javasound;

import ptolemy.actor.lib.Sink;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
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

import java.io.IOException;


/////////////////////////////////////////////////////////
//// AudioPlayer
/**
This actor reads audio samples and plays them. Specifically,
the input stream that this actor reads is interpreted as
consisting of audio samples. This actor writes this stream
of audio samples to the audio output port of the computer,
which typically consists of the computer speaker or the
headphones output. The audio samples that are supplied to
this actor should be doubles in the range [-1.0, 1.0]. Thus,
the input port of this actor is of type DoubleToken. Any input
tokens that are outside of the valid range will be hard-clipped
to fall within the range [-1.0, 1.0] before they are written
to the audio output port of the computer.
<p>
This actor should be fired often enough to prevent underflow of
the internal audio playback buffer. Underflow should be avoided,
since it will result in audio discontinuities (heard as clicks)
in the output. No exception will be thrown if underflow occurs.
<p>
The following parameters should be set accordingly. In all cases,
an exception is thrown if an illegal parameter value is used.
Note that these parameters may be changed while audio capture is
active. If this actor is used in conjunction with an AudioCapture
actor, changing a parameter of this actor will cause the
corresponding parameter value of the AudioCapture actor to
automatically be set to the same value. This behavior is required
because the AudioCapture and AudioPlayer actors both share access
to the audio hardware, which is associated with a single sample rate,
bit resolution, and number of channels.
<p>
<ul>
<li><i>sampleRate</i> should be set to desired sample rate, in Hz.
The default value is 8000. Allowable values are 8000, 11025,
22050, 44100, and 48000 Hz. Some sound cards support 96000 Hz
operation, but this is not supported in Java.
<li><i>bitsPerSample</i> should be set to desired bit
resolution. The default value is 16. Allowable values are 8 and 16.
Some sound cards support 20 and 24 bit audio, but this is not
supported in Java.
<li><i>channels</i> should be set to desired number of audio
channels. Allowable values are 1 (for mono) and 2 (for stereo).
The default value is 1. Some sound cards support more than two
audio channels, but this is not supported in Java.
</ul>
<p>
It should be noted that at most one AudioCapture and one AudioPlayer
actor may be used simultaneously. Otherwise, an exception will
occur. This restriction may be lifted in a future version of
this actor.
<p>
Note: Requires Java 2 v1.3.0 or later.
@author  Brian K. Vogel
@version  $Id$
@since Ptolemy II 1.0
@see ptolemy.media.javasound.LiveSound
@see AudioCapture
@see AudioReader
@see AudioWriter
*/
public class AudioPlayer extends Sink implements LiveSoundListener {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioPlayer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);


        sampleRate = new Parameter(this, "sampleRate", new IntToken(8000));
        sampleRate.setTypeEquals(BaseType.INT);

        bitsPerSample = new Parameter(this, "bitsPerSample",
                new IntToken(16));
        bitsPerSample.setTypeEquals(BaseType.INT);
        channels = new Parameter(this, "channels",
                new IntToken(1));
        channels.setTypeEquals(BaseType.INT);
        attributeChanged(channels);

        // Hard code the the fraction of of the buffer to put data
        // at a time = 1/putFactor.
        _curElement = 0;
        // The size of the array (in samples per channel) to pass
        // to LiveSound.putSamples().
        _putSampleSize = 128;
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
            if (attribute == channels) {
                // FIXME: It probably doesn't make sense to allow
                // changes in this parameter at runtime.
                _channels =
                    ((IntToken)channels.getToken()).intValue();
                if (_channels < 1) {
                    throw new IllegalActionException(this,
                            "Attempt to set channels parameter to an illegal "
                            + "value of: " +  _channels
                            + " . The value must be a "
                            + "positive integer.");
                }
                // Check if we need to reallocate.
                if ((_inArray == null) || (_channels != _inArray.length)) {
                    _inArray = new Token[_channels][];
                }
                if ((_audioPutArray == null)
                        || (_channels != _audioPutArray.length)) {
                    _audioPutArray = new double[_channels][];
                }
                for (int i = 0; i < _channels; i++) {
                    _audioPutArray[i] = new double[_putSampleSize];
                }
                // Only set the channels if it is different than
                // the currently active channels.
                if (LiveSound.getChannels() != _channels) {
                    LiveSound.setChannels(_channels);
                }
            }  else if (attribute == sampleRate) {
                int sampleRateInt =
                    ((IntToken)sampleRate.getToken()).intValue();
                // Only set the sample rate if it is different than
                // the currently active sample rate.
                if (LiveSound.getSampleRate() != sampleRateInt) {
                    LiveSound.setSampleRate(sampleRateInt);
                }
            } else if (attribute == bitsPerSample) {
                int bitsPerSampleInt =
                    ((IntToken)bitsPerSample.getToken()).intValue();
                // Only set the bitsPerSample if it is different than
                // the currently active bitsPerSample.
                if (LiveSound.getBitsPerSample() != bitsPerSampleInt) {
                    LiveSound.setBitsPerSample(bitsPerSampleInt);
                }
            } else {
                super.attributeChanged(attribute);
                return;
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this,
                    "Cannot perform audio playback " +
                    "with the specified parameter values." +
                    ex);
        }
    }

    /** Obtain access to the audio playback hardware, and start playback.
     *  An exception will occur if there is a problem starting
     *  playback. This will occur if another AudioPlayer actor is
     *  playing audio.
     *  @exception IllegalActionException If there is a problem
     *   beginning audio playback.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Initialize/Reinitialize audio resources.
        try {
            _initializePlayback();
        } catch (IOException ex) {
            throw new IllegalActionException(this,
                    "Cannot initialize audio playback " +
                    ex);
        }
        _safeToInitialize = true;
    }

    /** If there are at least <i>count</i> tokens on the input
     *  port, invoke <i>count</i> iterations of this actor.
     *  Otherwise, do nothing, and return a value of NOT_READY.
     *  One token is read from each channel in an iteration
     *  and written to the audio output port of the computer,
     *  which is typically the computer speaker or the headphones output.
     *  <p>
     *  This method should be called instead of the prefire(),
     *  fire(), and postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, return NOT_READY if there
     *   are not enough tokens on the input port, or throw an exception
     *   if there is a problem writing audio samples to the audio sink.
     *  @see ptolemy.actor.Executable
     *  @exception IllegalActionException If the <i>count</i> samples
     *   cannot be written to the audio output device.
     */
    public int iterate(int count) throws IllegalActionException {
        for (int j = 0; j < _channels; j++) {
            if (input.hasToken(j, count)) {
                // NOTE: inArray[j].length may be > count, in which case
                // only the first count tokens are valid.
                _inArray[j] = input.get(j, count);
            } else {
                // Not enough tokens on the input port, so just return.
                return NOT_READY;
            }
        }
        // For each sample.
        for (int k = 0; k < count; k++) {
            // For each channel.
            for (int m = 0; m < _channels; m++) {
                // Keep writing samples until the array argument to
                // putSamples() is full, then call putSamples().
                // Array argument to putSamples() is not full yet,
                // so write another sample for each channel.
                _audioPutArray[m][_curElement] =
                    ((DoubleToken)_inArray[m][k]).doubleValue();
            }
            // Increment pointer.
            _curElement++;
            if (_curElement == _putSampleSize) {
                try {
                    // write out samples to speaker and/or file.
                    LiveSound.putSamples(this, _audioPutArray);
                } catch (Exception ex) {
                    throw new IllegalActionException(this,
                            "Cannot playback audio:\n" +
                            ex);
                }
                // Reset pointer to beginning of array.
                _curElement = 0;
            }
        }
        return COMPLETED;
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
                    attributeChanged(sampleRate);
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
                    attributeChanged(channels);
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
                    attributeChanged(bitsPerSample);
                }
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(
                    "Error responding to audio parameter change. " +
                    ex);
        }
    }


    /** At most one token is read from each channel and written to the
     *  audio output port of the computer, which is typically the
     *  computer speaker or the headphones output.
     *  @exception IllegalActionException If there is a problem
     *   playing audio.
     */
    public boolean postfire() throws IllegalActionException {
        for (int j = 0; j < _channels; j++) {
            if (input.hasToken(j)) {
                // NOTE: inArray[j].length may be > count, in which case
                // only the first count tokens are valid.
                _inArray[j] = input.get(j, 1);
            }
        }
        // For each channel.
        for (int m = 0; m < _channels; m++) {
            // Keep writing samples until the array argument to
            // putSamples() is full, then call putSamples().
            // Array argument to putSamples() is not full yet,
            // so write another sample for each channel.
            _audioPutArray[m][_curElement] =
                ((DoubleToken)_inArray[m][0]).doubleValue();
        }
        // Increment pointer.
        _curElement++;
        if (_curElement == _putSampleSize) {
            try {
                // write out samples to speaker and/or file.
                LiveSound.putSamples(this, _audioPutArray);
            } catch (Exception ex) {
                throw new IllegalActionException(this,
                        "Cannot playback audio:\n" +
                        ex);
            }
            // Reset pointer to beginning of array.
            _curElement = 0;
        }
        return true;
    }

    /** Set up the number of channels to use.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _channels =
            ((IntToken)channels.getToken()).intValue();
    }

    /** Stop audio playback and free up any audio resources used
     *  for audio playback.
     *  @exception IllegalActionException If there is a problem
     *   stopping audio playback.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        // Stop playback. Close any open sound files. Free
        // up audio system resources.
        if (LiveSound.isPlaybackActive()) {
            try {
                LiveSound.stopPlayback(this);
            } catch (IOException ex) {
                throw new IllegalActionException(this,
                        "Cannot free audio resources:\n" +
                        ex);
            }
        }
        _safeToInitialize = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize/Reinitialize audio resources. First stop playback,
     *  and close any open sound files, if necessary. Then reread
     *  all parameters, create a new SoundPlayback object, and start
     *  playback of audio.
     *  <p>
     *  This method is synchronized since it is not safe to call
     *  SoundPlayback methods while this method is executing.
     *  @exception IllegalActionException If there is a problem initializing
     *   audio playback.
     */
    private synchronized void _initializePlayback()
            throws IllegalActionException, IOException {
        // Stop playback. Close any open sound files. Free
        // up audio system resources.
        if (LiveSound.isPlaybackActive()) {
            throw new IllegalActionException(this,
                    "This actor cannot start audio playback because " +
                    "another actor currently has access to the audio " +
                    "playback resource. Only one AudioPlayer actor may " +
                    "be used at a time.");
        }
        for (int i = 0; i < _channels; i++) {
            _audioPutArray[i] = new double[_putSampleSize];
        }
        // Initialize audio playback.
        int sampleRateInt = ((IntToken)sampleRate.getToken()).intValue();
        int bitsPerSampleInt =
            ((IntToken)bitsPerSample.getToken()).intValue();
        int channelsInt = ((IntToken)channels.getToken()).intValue();
        if (LiveSound.getSampleRate() != sampleRateInt) {
            LiveSound.setSampleRate(sampleRateInt);
        }
        if (LiveSound.getBitsPerSample() != bitsPerSampleInt) {
            LiveSound.setBitsPerSample(bitsPerSampleInt);
        }
        if (LiveSound.getChannels() != channelsInt) {
            LiveSound.setChannels(channelsInt);
        }
        if (LiveSound.getBufferSize() != 4096) {
            LiveSound.setBufferSize(4096);
        }
        if (LiveSound.getTransferSize() != _putSampleSize) {
            LiveSound.setTransferSize(_putSampleSize);
        }
        // Start audio playback.
        LiveSound.startPlayback(this);
        // Reset the current index pointer to 0 for each channel.
        _curElement = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _channels;
    private int _putSampleSize;
    private double[][] _audioPutArray;
    // Pointer to the current sample of the array parameter of
    // putSamples() method of SoundPlayback.
    private int _curElement;
    private Token[][] _inArray;
    private boolean _safeToInitialize = false;
}
