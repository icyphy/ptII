/* An SDF actor that outputs the sequence of sample values from an
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating
*/

package ptolemy.domains.sdf.lib.javasound;

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
import ptolemy.domains.sdf.kernel.*;

/////////////////////////////////////////////////////////////////
//// AudioSource
/**
Sequentially output the samples from an audio source. The
DoubleTokens produced by this actor will be in the range [-1,1].
Possible
audio sources include microphone, line-in, a sound file, or
a URL to a sound file. For the case where the audio source is
a microphone or line-in, this actor should be fired often enough
to prevent overflow of the internal audio buffer.
<p>
The output is of type DoubleToken, and semantically, one output
token is produced on each channel, on each firing, corresponding
to the number of audio channels. In the actual implementation,
several tokens may be produced on each channel, on each
firing, in order to
improve performance. The number of tokens produced on each
channel on each firing is set by parameter <i>tokenProductionRate</i>.
<p>
<h2>Notes on audio sources and required parameters</h2>
<p>(1) Real-time capture from a microphone or line-in
<p> Java cannot
select between microphone and line-in sources. Use the OS
to select whether audio capture is from the mic or line-in.
The following parameters are relevant to audio capture from
a mic or line-in, and should be set accordingly:
<ul>
<li><i>source</i> should be set to "mic".
<li><i>sampleRate</i> should be set to desired sample rate.
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
<li><i>tokenProductionRate</i> may be set to optimize
performance.
</ul>
<p>(2) Capture from a sound file (via URL).
<p>
The following parameters are relevant to audio capture from
a sound file, and should be set accordingly:
<ul>
<li><i>source</i> should be set to "URL".
<li><i>pathName</i> should be set to the name of the file.
<li><i>tokenProductionRate</i> may be set to optimize
performance. The default value should result in reasonable
performance.
</ul>
<p>The sound file is not periodically repeated by this actor.
postfire()
will therefore return false when the end of the sound file is reached.
<p>There are security issues involved with accessing files.
Applications have no restrictions. Applets, however, are
only allowed access to files specified by a URL and located
on the machine from which the applet is loaded. The
.java.policy file may be modified to grant applets more
privileges, if desired.
<p>
Note: Requires Java 2 v1.3.0 RC1 or later.
@author Brian K. Vogel
@version $Id$
@see ptolemy.media.javasound.SoundCapture
@see ptolemy.media.javasound.SoundPlayback
@see ptolemy.domains.sdf.lib.javasound.AudioSink
*/

public class AudioSource extends SDFAtomicActor {

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

	output = new SDFIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
	output.setMultiport(true);

	pathName = new Parameter(this, "pathName",
                new StringToken("soundFile.wav"));
	source = new Parameter(this, "source", new StringToken("mic"));
	source.setTypeEquals(BaseType.STRING);

	sampleRate = new Parameter(this, "sampleRate", new IntToken(44100));
	sampleRate.setTypeEquals(BaseType.INT);

	sampleSizeInBits = new Parameter(this, "sampleSizeInBits",
                new IntToken(16));
	sampleSizeInBits.setTypeEquals(BaseType.INT);

	channels = new Parameter(this, "channels",
                new IntToken(1));
	channels.setTypeEquals(BaseType.INT);

	bufferSize = new Parameter(this, "bufferSize",
                new IntToken(4096));
	bufferSize.setTypeEquals(BaseType.INT);

	tokenProductionRate = new Parameter(this, "tokenProductionRate",
                new IntToken(256));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. */
    public SDFIOPort output;

    /** The sound source. Possible sound sources are:
     *  <p>(1) The microphone or line in port. To capture from
     *  this source, set <i>source</i> to "mic". This is the
     *  default behavior.
     *  <p>(2) A sound file loaded from a URL. To capture from
     *  this source, set <i>source</i> to "URL"
     *  <p>
     *  For case (2) above, parameter <i>pathName</i>
     *  must be set to the sound file location.
     */
    public Parameter source;

    /** The name of the file to read from. The path must be a valid
     *  URL. Note that it is possible to load a file from the native
     *  file system by using the prefix "file:///" instead of "http://".
     *  The sound file format is determined from the file extension.
     *  For example, "file:///C:/someDir/someFile.wav" will be
     *  interpretted as a WAVE file.
     *  <p>
     *  To read data from a sound file,  <i>source</i> must be set
     *  to "URL" and <i>pathName</i> must be set a a fully qualified URL.
     *  This parameter will be ignored if audio is captured from
     *  a microphone or line-in (i.e., if <i>source</i> is set to "mic").
     *  <p>
     *  Note: For a list of allowable audio file formats, refer to the
     *  ptolemy.media.javasound package documentation.
     */
    public Parameter pathName;

    /** The desired sample rate to use, in Hz.
     *  The default value of the sample rate is 44100 Hz.
     *  <p>
     *  Note that it is only necessary to set this parameter for the
     *  case where audio is captured in real-time from the microphone
     *  or line-in. The sample rate is automatically determined when
     *  capturing samples from a sound file.
     */
    public Parameter sampleRate;

    /** The number desired number of bits per sample.
     *  The default value is 16.
     *  <p>
     *  Note that it is only necessary to set this parameter for the
     *  case where audio is captured in real-time from the microphone
     *  or line-in. The sample size is automatically determined when
     *  capturing samples from a sound file.
     */
    public Parameter sampleSizeInBits;

    /** The number of audio channels to use. 1 for mono,
     *  2 for stereo, etc.
     *  The default value is 1 (mono).
     *  <p>
     *  This parameter is automatically set when capturing from
     *  a sound file.
     */
    public Parameter channels;

    /** Requested size of the internal audio input
     *  buffer in samples. This controls the latency. Ideally, the
     *  smallest value that gives acceptable performance (no overflow)
     *  should be used. The value should be chosen larger than the
     *  production rate of this actor.
     *  The default value is 4096.
     *  <p>
     *  Note that it is only necessary to set this parameter for the
     *  case where audio is captured in real-time from the microphone
     *  or line-in.
     */
    public Parameter bufferSize;

    /** The token production rate of this actor. The value of
     *  the production rate affects performance only. It is
     *  semantically meaningless. Semantically, only one token
     *  is produced when this actor is fired. However, choosing
     *  a large production rate value can improve performance,
     *  since production rate many tokens are processed when
     *  this actor is fired.
     *  This parameter
     *  also affects the latency, since production rate tokens
     *  must be available before this actor can fire.
     *  <p>
     *  The default value is 256.
     */
    public Parameter tokenProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify the director when type changes in the parameters occur.
     *  This will cause type resolution to be redone at the next opportunity.
     *  It is assumed that type changes in the parameters are implemented
     *  by the director's change request mechanism, so they are implemented
     *  when it is safe to redo type resolution.
     *  If there is no director, then do nothing.
     */
    public void attributeTypeChanged(Attribute attribute) {
        Director dir = getDirector();
        if (dir != null) {
            dir.invalidateResolvedTypes();
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            AudioSource newobj = (AudioSource)super.clone(ws);
            newobj.source =
		(Parameter)newobj.getAttribute("source");
            newobj.pathName =
		(Parameter)newobj.getAttribute("pathName");
	    newobj.pathName =
		(Parameter)newobj.getAttribute("sampleRate");
	    newobj.pathName =
		(Parameter)newobj.getAttribute("sampleSizeInBits");
	    newobj.pathName =
		(Parameter)newobj.getAttribute("channels");
	    newobj.pathName =
		(Parameter)newobj.getAttribute("bufferSize");
	    newobj.pathName =
		(Parameter)newobj.getAttribute("tokenProductionRate");
            newobj.output =
		(SDFIOPort)newobj.getPort("output");
            // set the type constraints.
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Output sample values of the sound file. Semantically,
     *  only one token (a DoubleToken) is output. In the actual
     *  implementation, however, the number of tokens output is
     *  equal to the token production rate.
     *  @return True if there are samples available from the
     *  audio source. False if there are no more samples (end
     *  of sound file reached).
     */
    // FIXME: If audio is read from file and file channels < parameter
    // channels => exception thrown. Set channels param automatically.
    public boolean postfire() throws IllegalActionException {
	//System.out.println("AudioSource: postfire(): invoked");
	try {
	    // Read in audio data.
	    _audioInDoubleArray = _soundCapture.getSamples();
	} catch (Exception ex) {
	    throw new IllegalActionException(
		    "Cannot capture audio:\n" +
		    ex.getMessage());
	}
	//System.out.println("AudioSource: postfire(): after getSamples");
	// Check that the read was successful

	if (_audioInDoubleArray != null) {
	    // For each channel (in both the audio and Ptolemy II sense):

	    for (int j = 0; j < _channels; j++) {

		//_audioTokenArray = new DoubleToken[_productionRate];
		// Convert to DoubleToken[].
		for (int i = 0; i < _productionRate; i++) {
		    _audioTokenArray[i] =
			new DoubleToken(_audioInDoubleArray[j][i]);
		}
		//System.out.println("AudioSource: postfire(): getWidth " +
		//	   output.getWidth());
		output.sendArray(j, _audioTokenArray);
	    }

	    //System.out.println("AudioSource: postfire(): returning true");
	    return true;
	} else {
	    // Read was unsuccessful.
	    // Output array of zeros and return false.

	    // This generally means
	    // that the end of the sound file has been reached.
	    // Output productionRate many zeros.
	    //_audioTokenArray = new DoubleToken[_productionRate];
	    // Convert to DoubleToken[].
	    for (int i = 0; i < _productionRate; i++) {
		_audioTokenArray[i] = new DoubleToken(0);
	    }
	    // Output an array of zeros on each channel.
	    for (int j = 0; j < _channels; j++) {
		output.sendArray(j, _audioTokenArray);
	    }
	    System.out.println("AudioSource: postfire(): returning false");
	    return false;
	}
    }

    /** Set up the output port's production rate.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();

	_productionRate =
	    ((IntToken)tokenProductionRate.getToken()).intValue();
	_channels =
	    ((IntToken)channels.getToken()).intValue();

	output.setTokenProductionRate(_productionRate);

    }

    /** Check parameters and begin the sound capture process. If the
     *  capture source is a sound file, the file is reopened and
     *  capture is reset to the beginning of the file.
     *  @exception IllegalActionException If the parameters
     *             are out of range.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	if(_debugging) _debug("AudioSource: initialize(): invoked");
	//String sourceStr = ((StringToken)source.getToken()).toString();
	String sourceStr = ((StringToken)source.getToken()).stringValue();
	if(_debugging) _debug("AudioSource: source = " + sourceStr);
	//System.out.println("AudioSource: source = " + sourceStr2);
        if (sourceStr.equals("URL")) {
            // Load audio from a URL.
            String theURL =
                ((StringToken)pathName.getToken()).stringValue();
            _soundCapture = new SoundCapture(theURL,
                    _productionRate);
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

        } else if (sourceStr.equals("mic")) {

            int sampleRateInt =
                ((IntToken)sampleRate.getToken()).intValue();

            int sampleSizeInBitsInt =
                ((IntToken)sampleSizeInBits.getToken()).intValue();
            int channelsInt =
                ((IntToken)channels.getToken()).intValue();
            int bufferSizeInt =
                ((IntToken)bufferSize.getToken()).intValue();
            int getSamplesSizeInt = _productionRate;
            _soundCapture = new SoundCapture((float)sampleRateInt,
                    sampleSizeInBitsInt,
                    channelsInt,
                    bufferSizeInt,
                    getSamplesSizeInt);
            try {
		// Start capturing audio.
		_soundCapture.startCapture();
	    } catch (IOException ex) {
		throw new IllegalActionException(
		    "Cannot capture audio:\n" +
		    ex.getMessage());
	    }
        } else {
            throw new IllegalActionException(this.getFullName() +
		    ": Parameter " +
                    source.getFullName() +
                    " is not set to a valid string." +
                    " Valid choices are \"speaker\" or " +
                    "\"file\". The invalid parameter was:" +
		    sourceStr + ".");
        }



        // Allocate array for postfire()
        _audioTokenArray = new DoubleToken[_productionRate];
    }

    /** Stop capturing audio. Free up any system resources involved
     *  in the capturing process and close any open sound files.
     */
    public void wrapup() throws IllegalActionException {
	if(_debugging) _debug("AudioSource: wrapup(): invoked");
	// Stop capturing audio.
	if (_soundCapture != null) {
	    try {
		_soundCapture.stopCapture();
	    } catch (IOException ex) {
		throw new IllegalActionException(
		    "Cannot capture audio:\n" +
		    ex.getMessage());
	    }
	}

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private SoundCapture _soundCapture;

    private int _productionRate;

    private int _channels;

    private double[][] _audioInDoubleArray;

    private DoubleToken[] _audioTokenArray;
}
