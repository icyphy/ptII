/* An actor that outputs audio samples that are captured from the
   audio input port of the computer.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.javasound;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import javax.sound.sampled.*;

import ptolemy.media.javasound.*;

/////////////////////////////////////////////////////////////////
//// AudioCapture
/**
This actor sequentially outputs audio samples that are captured
from the audio input port of the computer. The audio input port
typically corresponds to either the microphone input, line-in,
or cd audio from the cdrom or dvd drive. It is not possible to
select the desired input port under Java. This must be done from
the operating system. This actor should be fired often enough to
prevent overflow of the internal audio capture buffer.
Overflow should be avoided, since it will result in loss of
data. Each captured audio sample is converted to a double that
may range from -1.0 to 1.0. Thus, the output type of this actor
is DoubleToken.
<p>
The following parameters should be set accordingly. In all cases, 
an exception is thrown if an illegal parameter value is used. 
Note that these parameters may be changed while audio playback 
is active. If this actor is used in conjunction with an 
AudioPlayer actor, changing a parameter will cause the corresponding 
parameter value of the AudioPlayer to automatically be set to the 
same value. This behavior is required because the AudioCapture and
AudioPlayback actors both share access to the audio hardware, which
is associated with a single sample rate, bit resolution, and
number of channels.
<ul>
<li><i>sampleRate</i> should be set to the desired sample rate, in Hz.
The default value is 8000. Allowable values are 8000, 11025,
22050, 44100, and 48000 Hz. Note that Java does not support
96000 Hz operation, even if the audio hardware supports it.
<li><i>bitsPerSample</i> should be set to the desired bit
resolution. The default value is 16. Allowable values are 8 and 16.
Note that Java does not support 20 or 24 bit audio, even if the
audio hardware supports it.
<li><i>channels</i> should be set to desired number of audio
channels. The default value is 1 (for mono audio). Allowable
values are 1 and 2 (for stereo). Note that more than two 
channels of audio is not currently supported in Java, even if 
the audio hardware supports it.
</ul>
<p>
It should be noted that at most one AudioCapture and one AudioPlayer
actor may be used simultaneously. Otherwise, an exception will
occur. This restriction may be lifted in a future version of
this actor.
<p>
There are security issues involved with accessing files and audio
resources in applets. Applets are not
allowed to capture audio from the audio input port (e.g., the
microphone) by default since this could present a security risk.
Therefore, the actor will not run in an applet by default. The
.java.policy file may be modified to grant applets more
privileges.
<p>
Note: Requires Java 2 v1.3.0 or later.
@author Brian K. Vogel
@version $Id$
@see ptolemy.media.javasound.LiveSound
@see AudioPlayback
@see SoundReader
@see SoundWriter
*/
public class AudioCapture extends Source implements LiveSoundListener {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the parameters and initialize them to their default values.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioCapture(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output.setTypeEquals(BaseType.DOUBLE);
	output.setMultiport(true);

	sampleRate = new Parameter(this, "sampleRate", new IntToken(8000));
	sampleRate.setTypeEquals(BaseType.INT);
	bitsPerSample = new Parameter(this, "bitsPerSample",
                new IntToken(16));
	bitsPerSample.setTypeEquals(BaseType.INT);
	channels = new Parameter(this, "channels",
                new IntToken(1));
	channels.setTypeEquals(BaseType.INT);
	attributeChanged(channels);
	// Add this class as a listener of live sound change
	// events.
	LiveSound.addLiveSoundListener(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** The desired sample rate to use, in Hz. Valid values
     *  are dependent on the audio hardware (sound card), but typically
     *  include at least 8000, 11025, 22050, 44100, and 48000. The
     *  default value of the sample rate is 8000 Hz. Some sound
     *  cards support 96000 Hz audio, but this is not supported by
     *  Java.
     *  <p>
     *  If this actor is used simultaneously with an AudioPlayer actor,
     *  then this parameter will be constrained to be the same for
     *  both actors, since most sound cards require the capture and 
     *  playback parameters to be the same. This actor will 
     *  automatically cause the parameters of an AudioPlayer actor 
     *  to be set to the same values as the parameters of this actor.
     *  <p>
     *  An exception will occur if this parameter is set to an
     *  unsupported sample rate.
     */
    public Parameter sampleRate;

    /** The number desired number of bits per sample. Allowed 
     *  values are dependent on the audio hardware, but typically 
     *  at least include 8 and 16. The default value is 16. Some
     *  sound cards suport 20 and 24 bit audio, but this is not
     *  supported by Java.
     *  <p>
     *  If this actor is used simultaneously with an AudioPlayer actor,
     *  then this parameter will be constrained to be the same for
     *  both actors, since most sound cards require the capture and 
     *  playback parameters to be the same. This actor will 
     *  automatically cause the parameters of an AudioPlayer actor 
     *  to be set to the same values as the parameters of this actor.
     *  <p>
     *  An exception will occur if this parameter is set to an
     *  unsupported sample size.
     */
    public Parameter bitsPerSample;

    /** The number of audio channels to use. Valid values
     *  are dependent on the audio hardware (sound card), but typically
     *  at least include 1 (for mono) and 2 (for stereo). The
     *  default value is 1. Some sound cards support more than two
     *  audio channels, but this is not supported by Java.
     *  <p>
     *  If this actor is used simultaneously with an AudioPlayer actor,
     *  then this parameter will be constrained to be the same for
     *  both actors, since most sound cards require the capture and 
     *  playback parameters to be the same. This actor will 
     *  automatically cause the parameters of an AudioPlayer actor 
     *  to be set to the same values as the parameters of this actor.
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
	if (_debugInfo) {
	    System.out.println("AudioCapture: attributeChanged() invoked on: " +
			       attribute.getName());
	}
	try {
	    if (attribute == channels) {
		_channels =
		    ((IntToken)channels.getToken()).intValue();
		if (_channels < 1) {
		    throw new IllegalActionException(this,
		      "Attempt to set channels parameter to an illegal " +
		      "value of: " +  _channels + " . The value must be a " +
		      "positive integer.");
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
	    throw new IllegalActionException(
		 "Cannot perform audio capture " +
		 "with the specified parameter values." +
					     ex);
	}
    }

    /** Read parameter values and begin the sound capture process.
     *  An exception will occur if there is a problem starting
     *  the audio capture. This will occur if another AudioCapture actor has
     *  already started capturing.
     *  @exception IllegalActionException If there is a problem
     *   starting audio capture.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	// FIXME: remove this after debug.
	if (_debugInfo) {
	    System.out.println("AudioCapture: initialize(): invoked");
	}
	 try {
	     _initializeCapture();
	 } catch (IOException ex) {
	     throw new IllegalActionException(
                            "Cannot initialize audio capture " +
                            ex);
	 }
	_safeToInitialize = true;
	_haveASample = false;
    }

    /** Invoke <i>count</i> iterations of this actor. This method
     *  causes audio samples to be captured from the audio
     *  input device (e.g., the microphone or line-in).
     *  One token is written to the output port in an iteration.
     *  This method should be invoked
     *  often enough to prevent overflow of the internal audio capture
     *  buffer. Overflow should be avoided, since it will result in loss
     *  of data. This method will block until the samples have been
     *  written, so it is not possible to invoke this method too
     *  frequently.
     *  <p>
     *  This method should be called instead of the prefire(),
     *  fire(), and postfire() methods when this actor is used in a
     *  domain that supports vectorized actors. 
     *
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times.
     *  @exception IllegalActionException If there is a problem capturing
     *   audio.
     */
    public int iterate(int count) throws IllegalActionException {
	// Note: If audio is read from file and file channels < parameter
	// channels then exception thrown.
	if (_debugInfo) {
	    System.out.println("AudioCapture: iterate(): invoked with count = " +
			       count);
	}
	// Check if we need to reallocate the output token array.
	if (count > _audioSendArray.length) {
	    _audioSendArray = new DoubleToken[count];
	}
	// For each sample.
	for (int i = 0; i < count; i++) {
	    if (_haveASample == false) {
		// Need to capture more data.
		try {
		    // Read in audio data.
		    _audioInDoubleArray = LiveSound.getSamples(this);
		} catch (Exception ex) {
		    throw new IllegalActionException(
                            "Cannot capture audio: " +
                            ex);
		}
		_getSamplesArrayPointer = 0;
		// Check that the read was successful
		if (_audioInDoubleArray != null) {
		    _haveASample = true;
		}
	    }
	    if (_haveASample == true) {
		// Copy a sample to the output array.
		// For each channel.
		for (int j = 0; j < _channels; j++) {

		    _audioSendArray[i] =
			new DoubleToken(_audioInDoubleArray[j][_getSamplesArrayPointer]);
		}
		_getSamplesArrayPointer++;
		// Check if we still have at least one sample left.
		if ((_audioInDoubleArray[0].length - _getSamplesArrayPointer) <= 0) {
		    // We just ran out of samples.
		    _haveASample = false;
		}
	    }
	}
	// Check that the read was successful
	if (_audioInDoubleArray != null) {
	    // Send.
	    for (int j = 0; j < _channels; j++) {
		output.send(j, _audioSendArray, count);
	    }
	    return COMPLETED;
	} else {
	    // Note: This code may now be unnecessary.
	    // Read was unsuccessful, so output an array of zeros.
	    // This generally means that the end of the sound file
	    // has been reached.
	    // Convert to DoubleToken[].
	    for (int i = 0; i < count; i++) {
		_audioSendArray[i] = new DoubleToken(0);
	    }
	    // Output an array of zeros on each channel.
	    for (int j = 0; j < _channels; j++) {
		output.send(j, _audioSendArray, count);
	    }
	    return STOP_ITERATING;
	}
    }

    /** React to a change in an audio parameters of LiveSound. 
     *  LiveSound will call this method when an audio parameter
     *  such as the sample rate, number of channels, or bit
     *  resolution changes.
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
		if (_debugInfo) {
		    System.out.println("AudioCapture: liveSoundChanged() invoked");
		    System.out.println("AudioCapture: liveSoundChanged() " +
			       "activeSampleRate = " + activeSampleRate +
			       ", thisActorSampleRate = " +
			       thisActorSampleRate);
		}
		// Only set the sampleRate parameter if it is different from
		// the new sample rate.
		if (activeSampleRate != thisActorSampleRate) {
		    sampleRate.setToken(new IntToken(activeSampleRate));
		    attributeChanged(sampleRate);
		}
	    } else if (changedParameter == LiveSoundEvent.CHANNELS) {
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

    /** Capture and output a single audio sample on each channel.
     *  This method causes audio samples to be captured from the audio
     *  input device (e.g., the microphone or line-in).
     *  One token is written to the output port in an invocation.
     *  This method should be invoked
     *  often enough to prevent overflow of the internal audio capture
     *  buffer. Overflow should be avoided, since it will result in loss
     *  of data. This method will block until the samples have been
     *  read, so it is not possible to invoke this method too
     *  frequently.
     *
     *  @return True 
     *  @exception IllegalActionException If audio cannot be captured.
     */
    public boolean postfire() throws IllegalActionException {
	int returnVal = iterate(1);
	if (returnVal == COMPLETED) {
	    return true;
	} else if (returnVal == NOT_READY) {
	    // This should never happen.
	    throw new IllegalActionException(this, "Actor " +
                    "is not ready to fire.");
	} else if (returnVal == STOP_ITERATING) {
	    return false;
	}
	return false;
    }

    /** Stop capturing audio. Free up any system resources involved
     *  in the capturing process.
     */
    public void wrapup() throws IllegalActionException {
	if (_debugInfo) {
	    System.out.println("AudioCapture: wrapup(): invoked");
	}
	// Stop capturing audio.
	if (LiveSound.isCaptureActive()) {
	    try {
		LiveSound.stopCapture(this);
	    } catch (IOException ex) {
		throw new IllegalActionException(
                        "Error stopping audio capture : \n" +
                        ex);
	    }
	}
	_safeToInitialize = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize audio resources. Reread all parameters, and start
     *  audio capture.
     *  <p>
     *  This method is synchronized since it is not safe to call
     *  LiveSound methods while this method is executing.
     *  @exception IllegalActionException If there is a problem initializing
     *   audio capture.
     */
    private synchronized void _initializeCapture()
        throws IllegalActionException, IOException {
	if (_debugInfo) {
	    System.out.println("AudioCapture: _initializeCapture() invoked.");
	}
	if (LiveSound.isCaptureActive()) {
	    throw new IllegalActionException(this,
               "This actor cannot start audio capture because " +
               "another actor currently has access to the audio " +
               "capture resource. Only one AudioCapture actor may " +
					     "be used at a time.");
	    //LiveSound.stopCapture(this);

	}
	// Now initialize audio capture.

	// Use live capture mode.
	int sampleRateInt =
	    ((IntToken)sampleRate.getToken()).intValue();
	int bitsPerSampleInt =
	    ((IntToken)bitsPerSample.getToken()).intValue();
	int channelsInt =
	    ((IntToken)channels.getToken()).intValue();

	if (LiveSound.getSampleRate() != sampleRateInt) {
	    LiveSound.setSampleRate(sampleRateInt);
	}
	if (LiveSound.getBitsPerSample() != bitsPerSampleInt) {
	    LiveSound.setBitsPerSample(bitsPerSampleInt);
	}
	if (LiveSound.getChannels() != channelsInt) {
	    LiveSound.setChannels(channelsInt);
	}
	// Set the buffer size to 4096 samples per channel.
	// This affects the latency. We hide this from the
	// user for simplicity, and because low-latency
	// operation is not possible under Java anyway.
	if (LiveSound.getBufferSize() != 4096) {
	    LiveSound.setBufferSize(4096);
	}
	// Set the size of the array that is returned by
	// LiveSound.getSamples().
	if (LiveSound.getTransferSize() != 128) {
	LiveSound.setTransferSize(128);
	}

	try {
	    // Start capturing audio.
	    LiveSound.startCapture(this);
	    } catch (IOException ex) {
		throw new IllegalActionException(
                        "Cannot capture audio:\n" +
                        ex);
	    }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _channels;
    private double[][] _audioInDoubleArray;
    private boolean _haveASample;
    private int _getSamplesArrayPointer;
    private DoubleToken[] _audioSendArray = new DoubleToken[1];
    // Hard code the the fraction of of the buffer to get data
    // at a time = 1/getFactor.
    private int _getFactor;
    private boolean _safeToInitialize = false;
    //private boolean _debugInfo = true;
    private boolean _debugInfo = false;
}
