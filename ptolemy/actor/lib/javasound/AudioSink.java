/* An actor that writes input audio data to a sound file or plays
the audio data.

@Copyright (c) 2000 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.javasound;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.*;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import java.awt.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Enumeration;

import ptolemy.media.javasound.*;

//////////////////////////////////////////////////////////////////////////
//// AudioSink
/**
Read audio samples from the input port and send the data to an
appropriate sink (speaker or sound file). This actor can operate in 
two distinct modes: live playback, and record to a sound file. The 
input port is of type
DoubleToken. Each DoubleToken read from the input represents one sample
of the audio data and should be in the range [-1, 1]. Single channel
(mono) and two channel (stereo) audio are supported. For single channel
audio, tokens are read from channel 0 of the input port. For stereo
, tokens are read from channel 0 (left) and channel 1
(right) of the input port.
<p>
<h2>Notes on audio sinks and required parameters</h2>
<p>(1) Using live playback mode. 
<p>
When this actor is in "live playback mode", this actor should
be fired often enough (by invoking postfire() or iterate()) to
prevent underflow of the internal audio playback buffer.
Underflow should be avoided, since it will result in audio 
discontinuities (heard as clicks) in the output.
<p>
The following parameters are relevant to live playback mode, and 
should be set accordingly. In all cases, an exception is thrown if
an illegal parameter value is used:
<ul>
<li><i>sampleRate</i> should be set to desired sample rate, in Hz. The
DoubleTokens read in by this actor will be interpreted as having
this sample rate. The default value is 44100.
<li><i>sampleSizeInBits</i> should be set to desired bit
resolution. The default value is 16.
<li><i>channels</i> should be set to desired number of audio
channels. Allowable values are 1 and 2. The default value is 1.
<li><i>bufferSize</i> may be set to optimize latency. A particular
Java implementation may choose to ignore this parameter, however.
This controls the delay from the time audio sample are read by this
actor until the audio is actually heard at the speaker. A lower
bound on the latency is given by
(<i>bufferSize</i> / <i>sampleRate</i>) seconds.
Ideally, the smallest value that gives acceptable performance (no underflow)
should be used.
</ul>
<p>(2) Write to a sound file on the native file system.
<p>
The following parameters are relevant to writing to a sound
file. In all cases, an 
exception is thrown if an illegal parameter value is used:
<ul>
<li><i>channels</i> should be set to desired number of audio
channels. Allowable values are 1 and 2. The default value is 1.
<li><i>sampleRate</i> should be set to desired sample rate, in Hz. The
DoubleTokens read in by this actor will be interpreted as having
this sample rate. The default value is 44100.
<li><i>sampleSizeInBits</i> should be set to desired bit
resolution. The default value is 16.
</ul>
<p>
Note: Requires Java 2 v1.3.0 or later.
@author  Brian K. Vogel
@version  $Id$
@see ptolemy.media.javasound.SoundCapture
@see ptolemy.media.javasound.SoundPlayback
@see ptolemy.actor.lib.javasound.AudioSource
*/
public class AudioSink extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioSink(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	if(_debugging) _debug("AudioSink: Constructor invoked");
        input.setTypeEquals(BaseType.DOUBLE);

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
	// Hardcode the the fraction of of the buffer to put data
	// at a time = 1/putFactor.
	_putFactor = 8;
	attributeChanged(bufferSize);
	_curElement = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the file to write to. This parameter determines
     *  which mode this actor will operate in. A value of "" will
     *  cause live playback mode to be used. Otherwise, record to
     *  a file mode will be used, where the filename will be the
     *  value of this parameter. If no value is specified,
     *  the default value of "" will be used.
     *  <p>
     *  The encoding to use is determined by the file extension.
     *  E.g., "somefile.au" will create a Sun AU format file.
     *  The allowable file formats are AU, WAV, and , AIFF.
     *  <p>
     *  It is safe to change this parameter during execution. If
     *  this parameter is changed while in file writing mode,
     *  all data captured so far will be saved, and the sound file
     *  will be closed.
     *  <p>
     *  An exception will be occur if the path references a
     *  non-exsistant or unsupported sound file.
     */
    public Parameter pathName;

    /** The desired sample rate to use, in Hz. Valid values
     *  are determined by the hardware, but typically at
     *  least include : 8000, 11025, 22050, 44100, and 48000.
     *  The default value of the sample rate is 44100 Hz. 
     *  <p>
     *  It is safe to change this parameter during execution.
     *  However, doing so in file writing mode will cause all data
     *  saved up until the change to be lost.
     *  <p>
     *  An exception will be thrown if an illegal value is used.
     */
    public Parameter sampleRate;

    /** The desired number of bits per sample.
     *  The default value is 16. Allowed values are determined
     *  by the hardware and Java implementation, but typically at
     *  least include 8 and 16 bits. 
     *  <p>
     *  It is safe to change this parameter during execution.
     *  However, doing so in file writing mode will cause all data
     *  saved up until the change to be lost.
     *  <p>
     *  An exception will be thrown if an illegal value is
     *  used.
     */
    public Parameter sampleSizeInBits;

    /** The number of audio channels to use. This value must
     *  a positive integer. Allowed values are dependent on hardware
     *  and the Java implementation, but typically at least include
     *  1 (single channel) and 2 (stereo). The default value is 1.
     *  <p>
     *  It is safe to change this parameter during execution.
     *  However, doing so in file writing mode will cause all data
     *  saved up until the sample rate change to be lost.
     *  <p>
     *  An exception will be thrown if an illegal value is used.
     */
    public Parameter channels;

    /** Requested size of the internal audio playback buffer, in samples.
     *  This parameter controls the output latency. Ideally, the
     *  smallest value that gives acceptable performance (no overflow)
     *  should be used. The default value is 4096.  
     *  This parameter is taken as a hint and a Java implementation may
     *  choose to ignore it. 
     *  <p>
     *  This parameter has no effect when the audio data is only
     *  saved to a file.
     *  <p>
     *  It is safe to change this parameter during execution.
     *  However, doing so in file writing mode will cause all data
     *  saved up until the change to be lost.
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
	if(_debugging) _debug("AudioSink: attributeChanged() invoked on: " + 
			      attribute.getName());
	//System.out.println("AudioSink: attributeChanged() invoked on: " + 
	//	      attribute.getName());
	if (attribute == channels) {
	    _channels =
	    ((IntToken)channels.getToken()).intValue();
	    if (_channels < 1) {
		throw new IllegalActionException(this,
		    "Attempt to set channels parameter to an illegal " +
		    "value of: " +  _channels + " . The value must be a " +
                    "positive integer.");
	    }
	    // Check if we need to reallocate.
	    if ((_inArray == null) || (_channels != _inArray.length)) {
		_inArray = new Token[_channels][];
	    }
	    if ((_audioPutArray == null) || (_channels != _audioPutArray.length)) {
		_audioPutArray = new double[_channels][];
	    }
	    for (int i = 0; i < _channels; i++) {
		_audioPutArray[i] = new double[_putSampleSize];
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
	    if (intBufferSize < _putFactor) {
		throw new IllegalActionException(this,
		    "Attempt to set bufferSize parameter to an illegal " +
		    "value of: " +  intBufferSize + " . The value must be " +
                    "greater than " + _putFactor + ".");
	    }
	    _putSampleSize = intBufferSize/_putFactor;
	    for (int i = 0; i < _channels; i++) {
		_audioPutArray[i] = new double[_putSampleSize];
	    }
	} else {
	    super.attributeChanged(attribute);
	    return;
	}
	if (_safeToInitialize == true) {
	    _initializePlayback();
	}
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        AudioSink newobj = (AudioSink)super.clone(ws);
        newobj.pathName = (Parameter)newobj.getAttribute("pathName");
        newobj.sampleRate = (Parameter)newobj.getAttribute("sampleRate");
        newobj.sampleSizeInBits =
            (Parameter)newobj.getAttribute("sampleSizeInBits");
        newobj.channels = (Parameter)newobj.getAttribute("channels");
        newobj.bufferSize = (Parameter)newobj.getAttribute("bufferSize");
        return newobj;
    }

    /** Depending on the mode, open a new audio file for writing,
     *  or open audio resources for live playback.
     *  If file writing mode is used, any existing file of the same
     *  name will be overwritten.
     *  @exception IllegalActionException If the file cannot be opened,
     *   or if the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	if(_debugging) _debug("AudioSink: initialize(): invoked");
	//System.out.println("AudioSink: initialize(): invoked");
	// Initialize/Reinitialize audio resources.
	_initializePlayback();
	_safeToInitialize = true;
    }

    /** If there are at least <i>count</i> tokens on the input
     *  port, invoke <i>count</i> iterations of this actor.
     *  Otherwise, do nothing, and return a value of NOT_READY.
     *  One token is read from each channel in an iteration. 
     *  The audio output is either
     *  a sound file and/or the speaker, depending on the current 
     *  mode, which is controlled by the value of the <i>pathName</i> 
     *  parameter.
     *  <p>
     *  This method should be called instead of the prefire(), 
     *  fire(), and postfire() methods when this actor is used in a
     *  domain that supports vectorized actors. It is recommended for
     *  performance reasons that a large value of <i>count</i> be used 
     *  when this actor is used in live playback mode. This actor is 
     *  optimized to provide good performance even if the value of 
     *  <i>count</i> changes often.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, return NOT_READY if there
     *   are not enough tokens on the input port, or throw an exception
     *   if there is a problem writing audio samples to the audio sink.
     *  @exception IllegalActionException If the <i>count</i> samples
     *   cannot be written to the audio output device.
     */
    public int iterate(int count) throws IllegalActionException {
	if(_debugging) _debug("iterate(count) with count = " + count);
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
		    //System.out.println("iterate: ");
		    //for (int n = 0; n < _audioPutArray[0].length; n++) {
		    //	System.out.println(_audioPutArray[0][n]);
		    //}
		    //System.out.println("iterate: ***************");
		    // write out samples to speaker and/or file.
		    _soundPlayback.putSamples(_audioPutArray);
		} catch (Exception ex) {
		    throw new IllegalActionException(
				"Cannot playback audio:\n" +
				ex.getMessage());
		}
		// Reset pointer to begining of array.
		_curElement = 0;
	    }
	}
	return COMPLETED;
    }

    /** At most one token is read from each channel and written to the
     *  audio output. The audio output is either a sound file and/or 
     *  the speaker, depending on the current mode, which is 
     *  controlled by the value of the <i>pathName</i> parameter.
     *  @exception IllegalActionException If audio cannot be played.
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

    /** Set up the input port's consumption rate. For optimization,
     *  allocate variables
     *  for use in the postfire() method.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();
	_channels =
	    ((IntToken)channels.getToken()).intValue();
    }

    /** Close the specified file and any open audio resources, 
     *  if any.
     *  @exception IllegalActionException If the audio resources
     *   cannot be freed.
     */
    public void wrapup() throws IllegalActionException {
	super.wrapup();
	if(_debugging) _debug("AudioSink: wrapup(): invoked");
	//System.out.println("AudioSink: wrapup(): invoked");
	// Stop playback. Close any open sound files. Free
	// up audio system resources.
	if (_soundPlayback != null) {
	     try {
		 _soundPlayback.stopPlayback();
	     } catch (IOException ex) {
		 throw new IllegalActionException(
		    "Cannot free audio resources:\n" +
		    ex.getMessage());
	     }
	}
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
    private synchronized void _initializePlayback() throws IllegalActionException {
	if(_debugging) _debug("AudioSink: _initializePlayback() invoked.");
	//System.out.println("AudioSink: _initializePlayback() invoked.");
	// Stop playback. Close any open sound files. Free
	// up audio system resources.
	if (_soundPlayback != null) {
	     try {
		 _soundPlayback.stopPlayback();
	     } catch (IOException ex) {
		 throw new IllegalActionException(
		    "Cannot playback audio:\n" +
		    ex.getMessage());
	     }
	}
	// Initialize audio playback.
	String pathNameString =
	    ((StringToken)pathName.getToken()).stringValue();
	if (pathNameString.equals("")) {
	    // Use live playback mode.
	    if(_debugging) _debug("AudioSink: initialize(): playback to speaker");
            int sampleRateInt = ((IntToken)sampleRate.getToken()).intValue();
            int sampleSizeInBitsInt =
                ((IntToken)sampleSizeInBits.getToken()).intValue();
            int channelsInt = ((IntToken)channels.getToken()).intValue();
            int bufferSizeInt = ((IntToken)bufferSize.getToken()).intValue();
	    int putSamplesSize = _putSampleSize;

            _soundPlayback = new SoundPlayback(sampleRateInt,
                    sampleSizeInBitsInt,
                    channelsInt,
                    bufferSizeInt,
                    putSamplesSize);
	} else {
	    // Write audio data to a file.
	    if(_debugging) _debug("AudioSink: initialize(): playback to file");
	    int sampleRateInt = ((IntToken)sampleRate.getToken()).intValue();
	    int sampleSizeInBitsInt =
                ((IntToken)sampleSizeInBits.getToken()).intValue();
	    int channelsInt = ((IntToken)channels.getToken()).intValue();
	    int bufferSizeInt = ((IntToken)bufferSize.getToken()).intValue();
	    int putSamplesSize = _putSampleSize;
	    
	    _soundPlayback = new SoundPlayback(pathNameString,
                    sampleRateInt,
                    sampleSizeInBitsInt,
                    channelsInt,
                    bufferSizeInt,
                    putSamplesSize);
	} 
	try {
	    // Start audio playback.
	    _soundPlayback.startPlayback();
	    // Reset the current index pointer to 0 for each channel.
	    _curElement = 0;
	} catch (IOException ex) {
	    throw new IllegalActionException(
		    "Cannot playback audio:\n" +
		    ex.getMessage());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private SoundPlayback _soundPlayback;
    private int _channels;
    private int _putSampleSize;
    private double[][] _audioPutArray;
    // Pointer to the current sample of the array parameter of 
    // putSamples() method of SoundPlayback.
    private int _curElement;
    private Token[][] _inArray;
    // Hardcode the the fraction of of the buffer to put data
    // at a time = 1/putFactor.
    private int _putFactor;
    private boolean _safeToInitialize = false;
}
