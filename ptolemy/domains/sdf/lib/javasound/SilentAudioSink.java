/* An actor that does nothing, but can be used instead of AudioSink
for testing purposes.

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
@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib.javasound;
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

//import ptolemy.media.javasound.*;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SilentAudioSink
/**
This actor does nothing, but can be used as a drop-in replacement for
the AudioSink actor. This is typically usefull for testing purposes.
For example, this actor will run under JDK 1.2, whereas AudioSink
will not.
@author  Brian K. Vogel
@version  $Id$
@see ptolemy.media.javasound.SoundCapture
@see ptolemy.media.javasound.SoundPlayback
@see ptolemy.domains.sdf.lib.javasound.AudioSource
*/
public class SilentAudioSink extends SDFAtomicActor {

    public SilentAudioSink(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new SDFIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
	input.setMultiport(true);

	pathName = new Parameter(this, "pathName",
                new StringToken("soundFile.wav"));

	sink = new Parameter(this, "sink",
                new StringToken("speaker"));

        sampleRate = new Parameter(this, "sampleRate", new IntToken(22050));
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

	tokenConsumptionRate = new Parameter(this, "tokenConsumptionRate",
                new IntToken(256));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. */
    public SDFIOPort input;

    /** The destination of the audio samples that are read in
     *  by this actor. Possible choices are:
     *  <p>(1) The speaker. Audio samples are sent to the speaker.
     *  To use this mode, <i>sink</i> must be set to "speaker". The
     *  latency between when the samples are received by this actor
     *  and when they actually make it to the speaker roughly
     *  corresponds to the size of the internal audio buffer.
     *  If this mode is used, than it is important to call the
     *  fire() method of this actor often enough to prevent
     *  underflow of the internal audio buffer.
     *  <p>(2)  A sound file. Audio samples are sent to
     *  the sound file specified by parameter <i>pathName</i>.
     *  To use this mode, <i>sink</i> must be set to "file".
     *  <p> The default value of <i>sink</i> is "speaker".
     */
    public Parameter sink;

    /** The name of the file to write to. If no value is specified,
     *  the default value of "soundFile.wav" will be used. Note
     *  that audio will only be written to a sound file if
     *  parameter <i>sink</i> is set to "file".
     *  <p>
     *  For a list of allowable audio file formats, refer to the
     *  ptolemy.media.javasound package documentation.
     */
    public Parameter pathName;

    /** The desired sample rate to use, in Hz.
     *  The default value of the sample rate is 44100 Hz.
     */
    public Parameter sampleRate;

    /** The number desired number of bits per sample.
     *  The default value is 16.
     */
    public Parameter sampleSizeInBits;

    /** The number of audio channels to use. 1 for mono,
     *  2 for stereo, etc.
     *  The default value is 1.
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
     *  case where audio data is sent to the speaker in real-time.
     *  This parameter has no effect when the audio data is only
     *  saved to a file.
     */
    public Parameter bufferSize;

    /** The token consumption rate of this actor. The value of
     *  the consumption rate affects performance only. It is
     *  semantically meaningless. Semantically, only one token
     *  is consumed when this actor is fired. However, choosing
     *  a large production rate value can improve performance,
     *  since consumption rate many tokens are processed when
     *  this actor is fired.
     *  This parameter
     *  also affects the latency, since consumption rate tokens
     *  must be available before this actor can fire.
     *  <p>
     *  The default value is 256.
     */
    public Parameter tokenConsumptionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        SilentAudioSink newobj = (SilentAudioSink)super.clone(ws);
        newobj.input = (SDFIOPort)newobj.getPort("input");
        newobj.sink = (Parameter)newobj.getAttribute("sink");
        newobj.pathName = (Parameter)newobj.getAttribute("pathName");
        newobj.sampleRate = (Parameter)newobj.getAttribute("sampleRate");
        newobj.sampleSizeInBits =
            (Parameter)newobj.getAttribute("sampleSizeInBits");
        newobj.channels = (Parameter)newobj.getAttribute("channels");
        newobj.bufferSize = (Parameter)newobj.getAttribute("bufferSize");
        newobj.tokenConsumptionRate =
            (Parameter)newobj.getAttribute("tokenConsumptionRate");
        return newobj;
    }

    /** Read <i>tokenConsumptionRate</i> tokens from each channel.
     *  Write these tokens to a sound file and/or send them to
     *  the speaker, depending on the current mode, which is
     *  determined by the value of <i>sink</i>.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
	//System.out.println("AudioSink: postfire(): invoked");

	// For each channel (in both the audio and Ptolemy II sense):
	for (int j = 0; j < _channels; j++) {

	    input.getArray(j, _audioTokenArray);

	    // For each samples in the current channel:
	    for (int i = 0; i < _consumptionRate; i++) {
		// Convert to double[].
		_audioInDoubleArray[j][i] = _audioTokenArray[i].doubleValue();
	    }
	}
	try {
	    // write out samples to speaker and/or file.
	    //_soundPlayback.putSamples(_audioInDoubleArray);
	} catch (Exception ex) {
	    throw new IllegalActionException(
		    "Cannot playback audio:\n" +
		    ex.getMessage());
	}
	return true;
    }

    /** Open the specified file, if any.  Note changes to the fileName
     *  parameter during execution are ignored until the next execution.
     *  @exception IllegalActionException If the file cannot be opened,
     *   or if the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	//System.out.println("AudioSink: initialize(): invoked");
	if (((StringToken)sink.getToken()).toString() == "file") {
	    // Write audio data to a file.
	    //System.out.println("AudioSink: initialize(): playback to file");
	    String pathNameString =
		((StringToken)pathName.getToken()).toString();
	    int sampleRateInt = ((IntToken)sampleRate.getToken()).intValue();
	    int sampleSizeInBitsInt =
                ((IntToken)sampleSizeInBits.getToken()).intValue();
	    int channelsInt = ((IntToken)channels.getToken()).intValue();
	    int bufferSizeInt = ((IntToken)bufferSize.getToken()).intValue();

	    //_soundPlayback = new SoundPlayback(pathNameString,
	    //      sampleRateInt,
	    //      sampleSizeInBitsInt,
	    //      channelsInt,
	    //      bufferSizeInt,
	    //      _consumptionRate);
	} else if (((StringToken)sink.getToken()).toString() == "speaker") {
	    // Send audio data to the speaker.
	    //System.out.println("AudioSink: initialize(): playback to speaker");
            int sampleRateInt = ((IntToken)sampleRate.getToken()).intValue();
            int sampleSizeInBitsInt =
                ((IntToken)sampleSizeInBits.getToken()).intValue();
            int channelsInt = ((IntToken)channels.getToken()).intValue();
            int bufferSizeInt = ((IntToken)bufferSize.getToken()).intValue();

            //_soundPlayback = new SoundPlayback(sampleRateInt,
	    //      sampleSizeInBitsInt,
	    //      channelsInt,
	    //      bufferSizeInt,
	    //      _consumptionRate);
            //System.out.println("AudioSink: initialize(): SoundPlayback created");

	} else if (((StringToken)sink.getToken()).toString() == "both") {
	    // Write audio data to a file.
	    // *AND*
	    // Send audio data to the speaker.

	} else {
	    throw new IllegalActionException("Parameter " +
                    sink.getFullName() +
                    " is not set to a valid string." +
                    " Valid choices are \"speaker\", " +
                    "\"file\", and \"both\"");
	}

	//	try {
	    // Start audio playback.
	    //_soundPlayback.startPlayback();
	    //System.out.println("AudioSink: initialize(): return");
	///} catch (IOException ex) {
	// throw new IllegalActionException(
	//    "Cannot playback audio:\n" +
	//    ex.getMessage());
	//}

	// Allocate arrays for postfire()
	_audioTokenArray = new DoubleToken[_consumptionRate];

    }

    /** Set up the input port's consumption rate. For optimization,
     *  allocate variables
     *  for use in the postfire() method.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();

	_consumptionRate =
	    ((IntToken)tokenConsumptionRate.getToken()).intValue();
	_channels =
	    ((IntToken)channels.getToken()).intValue();
	_audioInDoubleArray = new double[_channels][_consumptionRate];

	input.setTokenConsumptionRate(_consumptionRate);

    }

    /** Close the specified file, if any.
     */
    public void wrapup() throws IllegalActionException {
	//System.out.println("AudioSink: wrapup(): invoked");
	// Stop playback. Close any open sound files. Free
	// up audio system resources.
	//if (_soundPlayback != null) {
	//   try {
	// _soundPlayback.stopPlayback();
	//   } catch (IOException ex) {
	// throw new IllegalActionException(
	//    "Cannot capture audio:\n" +
	//    ex.getMessage());
	//   }
	//}
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private int _consumptionRate;

    //private SoundPlayback _soundPlayback;

    private int _channels;

    private double[][] _audioInDoubleArray;

    private DoubleToken[] _audioTokenArray;
}
