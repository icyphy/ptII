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
This actor reads in audio data from the input port and records the
data to a sound file or plays the audio data. The input port is of type
DoubleToken. Each DoubleToken read from the input represents one sample
of the audio data and should be in the range [-1, 1]. Single channel
(mono) and two channel (stero) audio are supported. For single channel
audio, tokens are read from channel 0 of the input port. For stereo
, tokens are read from channel 0 (left) and channel 1
(right) of the input port. Semantically, one
token is read on each channel of the input port, on each firing,
corresponding
to the number of audio channels. In the actual implementation,
several tokens may be consumed on each channel, on each
firing, in order to
improve performance. The number of tokens consumed on each
channel on each firing is set by parameter <i>FIXME</i>.
<p>
<h2>Notes on audio sinks and required parameters</h2>
<p>(1) Real-time (live) playback of audio. Note that this
actor cannot automatically set the appropriate sample
rate or number of audio channels. These parameters must
be set manually.
<p>
<ul>
<li><i>sink</i> should be set to "live".
<li><i>sampleRate</i> should be set to desired sample rate, in Hz. The
DoubleTokens read in by this actor will be interpreted as having
this sample rate.
<li><i>sampleSizeInBits</i> should be set to desired bit
resolution.
<li><i>channels</i> should be set to desired number of audio
channels.
<li><i>bufferSize</i> may be set to optimize latency.
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
file.
<ul>
<li><i>sink</i> should be set to "file".
<li><i>channels</i> should be set to desired number of audio
channels.
<li><i>sampleRate</i> should be set to desired sample rate, in Hz. The
DoubleTokens read in by this actor will be interpreted as having
this sample rate.
<li><i>sampleSizeInBits</i> should be set to desired bit
resolution.
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
                new StringToken("soundFile.wav"));
	sink = new Parameter(this, "sink",
                new StringToken("live"));	
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

    /** The destination of the audio samples that are read in
     *  by this actor. Possible choices are:
     *  <p>(1) The speaker. Audio samples are sent to the speaker.
     *  To use this mode, <i>sink</i> must be set to "live". The
     *  latency between when the samples are received by this actor
     *  and when they actually make it to the speaker roughly
     *  corresponds to the size of the internal audio buffer.
     *  If this mode is used, than it is important to call the
     *  fire() method of this actor often enough to prevent
     *  underflow of the internal audio buffer.
     *  <p>(2)  A sound file. Audio samples are sent to
     *  the sound file specified by parameter <i>pathName</i>.
     *  To use this mode, <i>sink</i> must be set to "file".
     *  <p> The default value of <i>sink</i> is "live".
     *  <p>
     *  It is safe to change this parameter during excecution. If
     *  this parameter is changed while in file writing mode,
     *  all data captured so far will be saved to the file before
     *  switching to live mode.
     */
    // FIXME: This parameter should go away. Write audio to
    // speaker if pathName is "".
    public Parameter sink;

    /** The name of the file to write to. If no value is specified,
     *  the default value of "soundFile.wav" will be used. Note
     *  that audio will only be written to a sound file if
     *  parameter <i>sink</i> is set to "file".
     *  <p>
     *  The encoding to use is determined by the file exention.
     *  E.g., "somefile.au" will create a Sun AU format file.
     *  The allowable file formats are AU, WAV, and , AIFF.
     *  <p>
     *  It is safe to change this parameter during excecution. If
     *  this parameter is changed while in file writing mode,
     *  all data captured so far will be saved.
     */
    public Parameter pathName;

    /** The desired sample rate to use, in Hz. Valid values
     *  are determined by the hardware, but typically at
     *  least include : 8000, 11025, 22050, 44100, and 48000.
     *  The default value of the sample rate is 44100 Hz. An
     *  exception will be thrown if an illegal value is used.
     *  <p>
     *  It is safe to change this parameter during excecution.
     *  However, doing so in file writing mode will cause all data
     *  saved up until the change to be lost.
     */
    public Parameter sampleRate;

    /** The number desired number of bits per sample.
     *  The default value is 16. Allowed values are
     *  8 and 16 bits. 24 bit is not currently supported,
     *  An execption will be thrown if an illegal value is
     *  used.
     *  <p>
     *  It is safe to change this parameter during excecution.
     *  However, doing so in file writing mode will cause all data
     *  saved up until the change to be lost.
     */
    public Parameter sampleSizeInBits;

    /** The number of audio channels to use. This value must
     *  an integer of value 1 (single channel) or 2 (stereo). 
     *  More than two channels is not supported by current Java 
     *  implementations. The default value is 1.
     *  An exception will be thrown if an illegal value is used.
     *  <p>
     *  It is safe to change this parameter during excecution.
     *  However, doing so in file writing mode will cause all data
     *  saved up until the sample rate change to be lost.
     */
    public Parameter channels;

    /** Requested size of the internal audio playback buffer, in samples. 
     *  This parameter controls the output latency. Ideally, the
     *  smallest value that gives acceptable performance (no overflow)
     *  should be used. The default value is 4096.
     *  <p>
     *  This parameter has no effect when the audio data is only
     *  saved to a file.
     *  <p>
     *  It is safe to change this parameter during excecution.
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
	    // Check if we need to reallocate.
	    if ((_inArray == null) || (_channels != _inArray.length)) {
		_inArray = new Token[_channels][];
	    }
	    if ((_audioPutArray == null) || (_channels != _audioPutArray.length)) {
		_audioPutArray = new double[_channels][];
	    }
	} else if (attribute == pathName) {
	    // Nothing for now...
	} else if (attribute == sink) {
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
        newobj.sink = (Parameter)newobj.getAttribute("sink");
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
	// Initialize/Reinitialize audio resources.
	_initializePlayback();
	_safeToInitialize = true;
    }

    /** If there are at least <i>count</i> tokens on the input
     *  port, invoke <i>count</i> iterations of this actor.
     *  Otherwise, do nothing, and return a value of COMPLETED.
     *  One token is read from each channel in an iteration. 
     *  The audio output is either
     *  a sound file and/or the speaker, depending on the current 
     *  mode, which is controlled by the value of the <i>sink</i> 
     *  parameter.
     *  @exception IllegalActionException If audio cannot be played.
     */
    public int iterate(int count) throws IllegalActionException {
	if(_debugging) _debug("iterate(count) with count = " + count);
	//System.out.println("AudioSink: iterate(count) with count = " + count);
	for (int j = 0; j < _channels; j++) {
	    if (input.hasToken(j, count)) {
		// NOTE: inArray[j].length may be > count, in which case
		// only the first count tokens are valid.
		_inArray[j] = input.get(j, count);
	    } else {
		// Not enough tokens on the input port, so just return.
		return COMPLETED;
	    }
	}
	// For each sample.
	for (int k = 0; k < count; k++) {
	    // For each channel.
	    for (int m = 0; m < _channels; m++) {
		// Keep writing samples until the array argument to
		// putSamples() is full, then call putSamples().

		//System.out.println("AudioSink: iterate(): _curElement < _putSampleSize: with: _curElement =  " + _curElement + " and _putSampleSize = " + _putSampleSize);
		// Array argument to putSamples() is not full yet,
		// so write another sample for each channel.
		double deleteMePlease = 
		    ((DoubleToken)_inArray[m][k]).doubleValue();
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
		    //System.out.println("AudioSink: iterate(): Invoking putSamples() and puting this many samples: " + _audioPutArray[0].length);
		    // write out samples to speaker and/or file.
		    _soundPlayback.putSamples(_audioPutArray);
		    //System.out.println("AudioSink: iterate(): Returned from putSamples()");
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
     *  controlled by the value of the <i>sink</i> parameter.
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
	System.out.println("AudioSink: wrapup(): invoked");
	// Stop playback. Close any open sound files. Free
	// up audio system resources.
	if (_soundPlayback != null) {
	     try {
		 System.out.println("AudioSink: wrapup(): trying to shut down audio.");
		 _soundPlayback.stopPlayback();
		 System.out.println("AudioSink: wrapup(): finished shuting down audio.");
	     } catch (IOException ex) {
		 throw new IllegalActionException(
		    "Cannot free audio resources:\n" +
		    ex.getMessage());
	     }
	}
	System.out.println("AudioSink: wrapup(): returning now....");
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
	if(_debugging) _debug("_initializePlayback() invoked.");
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
		String sinkStr = ((StringToken)sink.getToken()).stringValue();
	if (sinkStr.equals("file")) {
	    // Write audio data to a file.
	    if(_debugging) _debug("AudioSink: initialize(): playback to file");
	    String pathNameString =
		((StringToken)pathName.getToken()).stringValue();
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
	} else if (sinkStr.equals("live")) {

	    // Send audio data to the speaker.
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
	    throw new IllegalActionException("Parameter " +
                    sink.getFullName() +
                    " is not set to a valid string." +
                    " Valid choices are \"live\" or " +
                    "\"file\". The invalid parameter was:" +
		    sinkStr + ".");
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
