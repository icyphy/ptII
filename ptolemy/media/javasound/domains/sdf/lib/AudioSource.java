/* An actor that outputs the sequence of sample values of a sound file.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import collections.LinkedList;

import ptolemy.media.*;
import javax.media.sound.sampled.AudioStream;
import javax.media.sound.sampled.AudioSystem;
import javax.media.sound.sampled.AudioFormat;
import javax.media.sound.sampled.AudioFormat.Encoding;
import javax.media.sound.sampled.FileStream;
import javax.media.sound.sampled.OutputChannel;
import javax.media.sound.sampled.Channel;
import javax.media.sound.sampled.Mixer;
import javax.media.sound.sampled.AudioUnavailableException;

//////////////////////////////////////////////////////////////////////////
//// AudioSource
/**
Read a sound file and sequentially output the samples. The output
is of type DoubleToken, and one output token is produced on
each firing. This actor will periodically repeat the the sound
file if the <i>isPeriodic</i> parameter is set to be true. 
Otherwise, this actor finishes after the last sample is output.
The location of the sound file is given by the <i>pathName</i>
parameter. This parameter contains the URL or filename of the sound file.
Java applet security requires the file URL to be on the computer
from which the applet was launched. If this actor is used in an application,
there are no restrictions on the URL, and files can be read from the
file system on the machine from which the applet is run.
<p>
Note: For a list of allowable audio file formats, refer to the
ptolemy.media package documentation.

@author Brian K. Vogel
@version
*/

public class AudioSource extends SequenceSource {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>isPeriodic</i> and <i>pathName</i> parameters. Initialize <i>isPeriodic</i>
     *  to BooleanToken with value false, and <i>pathName</i> to StringToken with value "http://localhost/soundFile.au".
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


	isPeriodic = new Parameter(this, "isPeriodic", new BooleanToken(false));
	pathName = new Parameter(this, "pathName", new StringToken("http://localhost/soundFile.au"));
	
        isPeriodic.setTypeEquals(BooleanToken.class);


	// set the type constraints.
	output.setTypeEquals(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////



    /** An indicator of whether the signal should be periodically
     *  repeated. There are two possible vaules:
     *  false: This actor will sequentially output all of the samples
     *  of an audio file (from beginning to end) and quit when the
     *  end of the audio is reached.
     *  true: This actor will continue from the beginning (first sample)
     *  of the audio file whenever the end is reached.
     */
    public Parameter isPeriodic;

    /** The name of the file to read from. This can be a URL or a
     *  file on the file system on which the code is run. This
     *  parameter contains a StringToken. Note: When this actor is used in
     *  an applet, a java.security.AccessControlException will
     *  be thrown if an attempt is made to create a network connection
     *  to any computer other than the one from which the code was
     *  loaded.
     */
    public Parameter pathName;


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

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>isPeriodic</code> and <code>pathName</code>
     *  public members to the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        AudioSource newobj = (AudioSource)super.clone(ws);
	newobj.isPeriodic = (Parameter)newobj.getAttribute("isPeriodic");
	newobj.pathName = (Parameter)newobj.getAttribute("pathName");
	// set the type constraints.
        return newobj;
    }

    /** Output the sample value of the sound file corresponding to the
     *  current index.
     */
    public void fire() {
        try {
            super.fire();
	    _sampleValue = audioArray[_index];
	    sampleValueToken = new DoubleToken(_sampleValue);
            output.broadcast(sampleValueToken);
        } catch (IllegalActionException ex) {
            // Should not be thrown because this is an output port.
            throw new InternalErrorException(ex.getMessage());
        }
    }

    /** Read in the sound file specified by the <i>pathName</i> parameter
     *  and initialize the current sample index to 0.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

	

	try {

	    NewAudio na  = new NewAudio(((StringToken)pathName.getToken()).stringValue());
	    
	    audioArray = na.getDoubleArray();

	    
	    /* Set the firing count limit equal to the number of samples
	     * in the sound file for the case where periodic repetition of
	     * the audio signal is not desired.
	     */
	    if (((BooleanToken)isPeriodic.getToken()).booleanValue() == false)
		{
		    firingCountLimit.setToken(new IntToken(audioArray.length));
		}

	    // Initialize the index to the first sample of the sound file.
	    _index = 0;

	} catch (MalformedURLException e) {
	    System.err.println(e.toString());
	} catch (FileNotFoundException e) {
	    System.err.println("VAudioDemoApplet: file not found: "+e);
	} catch (IOException e) {
	    System.err.println("VAudioDemoApplet: error reading"+
			       " input file: " +e);
	} 
	
    }

    /** Increment the current sample index. If we are currently at the
     *  end of the file, reset the current sample index to the beginning
     *  of the sound file.
     *  @return False if the number of iterations matches the number requested.
     *  @exception IllegalActionException If the firingCountLimit parameter
     *   has an invalid expression.
     */
    public boolean postfire() throws IllegalActionException {
	// _sampleValueToken = _sampleValueToken.add(step.getToken());
	

	// Increment the index to the next sample in the sound file.
	_index++;

	if (_index == audioArray.length)
	    {
		// Reached the end of the audio signal array.
		// Reset to the beginning of the array.
		_index = 0;
	    }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _sampleValue;
       
    private int _index;
    
    private double[] audioArray;

    private DoubleToken sampleValueToken;
}
