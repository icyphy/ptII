/* An actor that outputs the sequence of sample values from an
   audio source.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (vogel@eecs.berkeley.edu)
@AcceptedRating 
*/

package ptolemy.actor.lib.javasound;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
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
//// AudioSource
/**
Sequentially output the samples from an audio source. The
DoubleTokens produced by this actor will be in the range [-1,1].
This actor can operate in two distinct modes: live capture, and
capture from a sound file. When live capture mode is used, this
actor captures audio samples from the audio input port of the
computer which typically includes the microphone, line-in, or cd
audio. When capture from a sound file is used, the audio source is
a sound file specified as a URL. Note that it is still possible to
specify local files as a URL.  The mode that is used is controlled 
by the <i>pathName</i> parameter. If <i>pathName</i> is set to the 
string "", then live capture mode is used, otherwise samples are 
captured from a sound file.
<p>
<h2>Notes on modes and required parameters</h2>
<p>(1) Using live capture mode.
<p>
Java cannot
select between microphone and line-in sources. Use the OS
to select whether audio capture is from the mic, line-in, or
CD output.
When this actor is in "live capture mode", this actor should
be fired often enough (by invoking postfire() or iterate()) to
prevent overflow of the internal audio capture buffer.
Overflow should be avoided, since it will result in loss of
data.
<p>
The following parameters are relevant to live capture mode, and 
should be set accordingly. In all cases, an exception is thrown if
an illegal parameter value is used:
<ul>
<li><i>pathName</i> should be set to "". The absence of a URL
name tells this actor to use live capture mode. The default
value of this parameter is "".
<li><i>sampleRate</i> should be set to desired sample rate, in Hz. 
The default value is 44100.
<li><i>sampleSizeInBits</i> should be set to desired bit
resolution. The default value is 16.
<li><i>channels</i> should be set to desired number of audio
channels. Allowable values are 1 and 2. The default value is 1.
<li><i>bufferSize</i> may be set to optimize latency.
This controls the delay in samples from the time audio sample are read by this
actor until the audio is actually heard at the speaker.  A particular
Java implementation may choose to ignore this parameter, however.
A lower bound on the latency is given by
(<i>bufferSize</i> / <i>sampleRate</i>) seconds.
Ideally, the smallest value that gives acceptable performance (no underflow)
should be used. The default value is 4096.
</ul>
<p>(2) Capture from a sound file (via URL).
<p>
The following parameters are relevant to audio capture from
a sound file, and should be set accordingly. In all cases, an 
exception is thrown if an illegal parameter value is used:
<ul>
<li><i>pathName</i> should be set to the name of the file, specified
as a fully qualified string representation of a URL. The default
value is the string "", which causes live capture mode to be used.
</ul>
<p>The sound file is not periodically repeated by this actor, so 
postfire() will therefore return false when the end of the sound 
file is reached.
<p>
There are security issues involved with accessing files and audio
resources in applets. Applets are
only allowed access to files specified by a URL and located
on the machine from which the applet is loaded. Applets are not
allowed to capture audio from the microphone by default, so live
capture mode will not work in an applet by default. The
.java.policy file may be modified to grant applets more
privileges.
<p>
Note: Requires Java 2 v1.3.0 or later.
@author Brian K. Vogel
@version $Id$
@see ptolemy.media.javasound.SoundCapture
@see ptolemy.media.javasound.SoundPlayback
@see ptolemy.actor.lib.javasound.AudioSink
*/
public class AudioSource extends Source {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the parameters and initialize them to their default values.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioSource(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output.setTypeEquals(BaseType.DOUBLE);
	output.setMultiport(true);
	pathName = new Parameter(this, "pathName",
                new StringToken(""));
	sampleRate = new Parameter(this, "sampleRate", new IntToken(44100));
	sampleRate.setTypeEquals(BaseType.INT);
	sampleSizeInBits = new Parameter(this, "sampleSizeInBits",
                new IntToken(16));
	sampleSizeInBits.setTypeEquals(BaseType.INT);
	channels = new Parameter(this, "channels",
                new IntToken(1));
	channels.setTypeEquals(BaseType.INT);
	attributeChanged(channels);
	int intBufferSize = 4096;
	bufferSize = new Parameter(this, "bufferSize",
                new IntToken(intBufferSize));
	bufferSize.setTypeEquals(BaseType.INT);
	// Hardcode the the fraction of of the buffer to get data
	// at a time = 1/getFactor.
	_getFactor = 8;
	attributeChanged(bufferSize);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** The name of the file to read from. Live capture mode will be
     *  used if this parameter is set to "". The path must be a valid
     *  URL. Note that it is possible to load a file from the native
     *  file system by using the prefix "file:///" instead of "http://".
     *  The sound file format is determined from the file extension.
     *  For example, "file:///C:/someDir/someFile.wav" will be
     *  interpreted as a WAVE file. Allowable file formats include
     *  WAV, AU, and AIFF.
     *  <p>
     *  If this parameter is changed during execution, any currently
     *  open sound file will be saved.
     *  <p>
     *  An exception will be occur if the path references a
     *  non-exsistant or unsupported sound file.
     */
    public Parameter pathName;

    /** The desired sample rate to use, in Hz.
     *  Valid values
     *  are dependent on the audio hardware (sound card), but typically
     *  include at least 8000, 11025, 22050, 44100, and 48000. The 
     *  default value of the sample rate is 44100 Hz.
     *  <p>
     *  Note that it is only necessary to set this parameter for the
     *  case where audio is captured in real-time from the microphone
     *  or line-in. The sample rate is automatically determined when
     *  capturing samples from a sound file.
     *  <p>
     *  If this parameter is changed during execution when file writing
     *  mode is used, all data collected so far will be discarded, and
     *  a new file with the updated sample rate will be created.
     *  <p>
     *  An exception will be occur if this parameter is set to an
     *  unsupported sample rate.
     */
    public Parameter sampleRate;

    /** The number desired number of bits per sample.
     *  Allowed values are dependent
     *  on the audio hardware, but typically at least include
     *  8 and 16. The default value is 16. 
     *  <p>
     *  Note that it is only necessary to set this parameter for the
     *  case where audio is captured in real-time from the microphone
     *  or line-in. The sample size is automatically determined when
     *  capturing samples from a sound file.
     *  <p>
     *  An exception will occur if this parameter is set to an
     *  unsupported sample size.
     */
    public Parameter sampleSizeInBits;

    /** The number of audio channels to use. . Valid values
     *  are dependent on the audio hardware (sound card), but typically
     *  at least include 1 (for mono) and 2 (for stereo). The
     *  default value is 1.
     *  <p>
     *  This parameter is automatically set when capturing from
     *  a sound file.
     *  <p>
     *  An exception will occur if this parameter is set to an
     *  an unsupported channel number.
     */
    public Parameter channels;

    /** Requested size of the internal audio input
     *  buffer in samples. A particular Java implementation may choose
     *  to ignore this parameter. This controls the delay in samples 
     *  from the time audio sample are read by this
     *  actor until the audio is actually heard at the speaker. A lower
     *  bound on the latency is given by
     *  (<i>bufferSize</i> / <i>sampleRate</i>) seconds.
     *  Ideally, the smallest value that gives acceptable performance 
     *  (no underflow) should be used. Allowable values are dependent
     *  on the platform, jdk, and audio hardware. The default value is 4096.
     *  <p>
     *  Note that this parameter has no effect unless live capture
     *  mode is used.
     *  <p>
     *  An exception should not occur if this parameter is set to
     *  an unsupported buffer size, since this parameter is only
     *  taken as a hint.
     */
    public Parameter bufferSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle change requests for all parameters. An exception is
     *  thrown if the requested change is not allowed.
     *  @exception IllegalActionException If the change is not
     *   allowed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
	if(_debugging) _debug("attributeChanged() invoked on: " + 
			      attribute.getName());
	if (attribute == channels) {
	    _channels =
	    ((IntToken)channels.getToken()).intValue();
	    if (_channels < 1) {
		throw new IllegalActionException(this,
		    "Attempt to set channels parameter to an illegal " +
		    "value of: " +  _channels + " . The value must be a " +
                    "positive integer.");
	    }
	} else if (attribute == pathName) {
	    // Nothing for now...
	} else if (attribute == sampleRate) {
	    // Nothing for now...
	} else if (attribute == sampleSizeInBits) {
	    // Nothing for now...
	} else if (attribute == bufferSize) {
	    int intBufferSize =
	    ((IntToken)bufferSize.getToken()).intValue();
	    if (intBufferSize < _getFactor) {
		throw new IllegalActionException(this,
		    "Attempt to set bufferSize parameter to an illegal " +
		    "value of: " +  intBufferSize + " . The value must be " +
                    "greater than " + _getFactor + ".");
	    }
	    _getSampleSize = intBufferSize/_getFactor;
	} else {
	    super.attributeChanged(attribute);
	    return;
	}
	if (_safeToInitialize == true) {
	    _initializeCapture();
	}
    }

    /** Clone the actor into the specified workspace.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        AudioSource newobj = (AudioSource)super.clone(ws);
        newobj.pathName = (Parameter)newobj.getAttribute("pathName");
        newobj.sampleRate = (Parameter)newobj.getAttribute("sampleRate");
        newobj.sampleSizeInBits =
            (Parameter)newobj.getAttribute("sampleSizeInBits");
        newobj.channels = (Parameter)newobj.getAttribute("channels");
        newobj.bufferSize = (Parameter)newobj.getAttribute("bufferSize");
        return newobj;
    }

    /** Check parameters and begin the sound capture process. If the
     *  capture source is a sound file, the file is opened for writing.
     *  Any existing file of the same name will be silently overwritten.
     *  @exception IllegalActionException If the parameters
     *             are out of range.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	if(_debugging) _debug("AudioSource: initialize(): invoked");
	_initializeCapture();
	_haveASample = false;
    }

    /** Invoke <i>count</i> iterations of this actor. This method
     *  causes audio samples to be captured from the audio source,
     *  which can be a sound file or live capture from the audio
     *  input device (e.g., the microphone or line-in).
     *  One token is written to the output port in an iteration. 
     *  When live capture mode is used, this method should be invoked 
     *  often enough to prevent overflow of the internal audio capture 
     *  buffer. Overflow should be avoided, since it will result in loss 
     *  of data.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Return STOP_ITERATING if the
     *   end of the soundfile is reached.
     *  @exception IllegalActionException If audio cannot be captured.
     */
    public int iterate(int count) throws IllegalActionException {
	// Note: If audio is read from file and file channels < parameter
	// channels => exception thrown.
	//System.out.println("AudioSource: iterate(): invoked with count = " +
	//		   count);
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
		    _audioInDoubleArray = _soundCapture.getSamples();
		    //System.out.println("AudioSource: iterate(): Invoking getSamples() and getting this many samples: " + _audioInDoubleArray[0].length);
		} catch (Exception ex) {
		    throw new IllegalActionException(
						     "Cannot capture audio: " +
						     ex.getMessage());
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

    /** Capture and output a single audio sample on each channel. 
     *  This method causes audio samples to be captured from the audio source,
     *  which can be a sound file or live capture from the audio
     *  input device (e.g., the microphone or line-in).
     *  One token is written to the output port in an invocation. 
     *  When live capture mode is used, this method should be invoked 
     *  often enough to prevent overflow of the internal audio capture 
     *  buffer. Overflow should be avoided, since it will result in loss 
     *  of data.
     *  @return True if there are samples available from the
     *  audio source. False if there are no more samples (end
     *  of sound file reached).
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
     *  in the capturing process and close any open sound files.
     */
    public void wrapup() throws IllegalActionException {
	if(_debugging) _debug("AudioSource: wrapup(): invoked");
	System.out.println("AudioSource: wrapup(): invoked");
	// Stop capturing audio.
	if (_soundCapture != null) {
	    try {
		System.out.println("AudioSource: wrapup(): shuting down audio.");
		_soundCapture.stopCapture();
		System.out.println("AudioSource: wrapup(): shuting down audio succeeded.");
	    } catch (IOException ex) {
		throw new IllegalActionException(
		    "Cannot capture audio:\n" +
		    ex.getMessage());
	    }
	}
	System.out.println("AudioSource: wrapup(): returning now...");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize/Reinitialize audio resources. First stop playback,
     *  and close any open sound files, if necessary. Then reread
     *  all parameters, create a new SoundPlayback object, and start
     *  playback of audio.
     *  <p>
     *  This method is synchronized since it is not safe to call
     *  SoundCapture methods while this method is executing.
     *  @exception IllegalActionException If there is a problem initializing
     *   audio playback.
     */
    private synchronized void _initializeCapture() throws IllegalActionException {
		//String sourceStr = ((StringToken)source.getToken()).toString();
	String modeStr = ((StringToken)pathName.getToken()).stringValue();
	if(_debugging) _debug("AudioSource: source = " + modeStr);
	//System.out.println("AudioSource: source = " + sourceStr2);
        if (modeStr.equals("")) {
	    // Use live capture mode.
            int sampleRateInt =
                ((IntToken)sampleRate.getToken()).intValue();
            int sampleSizeInBitsInt =
                ((IntToken)sampleSizeInBits.getToken()).intValue();
            int channelsInt =
                ((IntToken)channels.getToken()).intValue();
            int bufferSizeInt =
                ((IntToken)bufferSize.getToken()).intValue();
            _soundCapture = new SoundCapture((float)sampleRateInt,
                    sampleSizeInBitsInt,
                    channelsInt,
                    bufferSizeInt,
                    _getSampleSize);
            try {
		// Start capturing audio.
		_soundCapture.startCapture();
	    } catch (IOException ex) {
		throw new IllegalActionException(
		    "Cannot capture audio:\n" +
		    ex.getMessage());
	    }
        } else {
	    // Load audio from a URL.
            String theURL =
                ((StringToken)pathName.getToken()).stringValue();
            _soundCapture = new SoundCapture(theURL,
                    _getSampleSize);
	    try {
		// Start capturing audio.
		_soundCapture.startCapture();
	    } catch (IOException ex) {
		throw new IllegalActionException(
		    "Cannot capture audio:\n" +
		    ex.getMessage());
	    }

            // Read the number of audio channels and set
            // parameter accordingly.
            _channels = _soundCapture.getChannels();
            channels.setToken(new IntToken(_channels));
        } 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private SoundCapture _soundCapture;
    private int _channels;
    private double[][] _audioInDoubleArray;
    private int _getSampleSize;
    private boolean _haveASample;
    private int _getSamplesArrayPointer;
    private DoubleToken[] _audioSendArray = new DoubleToken[1];
    // Hardcode the the fraction of of the buffer to get data
    // at a time = 1/getFactor.
    private int _getFactor;
    private boolean _safeToInitialize = false;
}
