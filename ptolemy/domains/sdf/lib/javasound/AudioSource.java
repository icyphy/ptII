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
//import collections.LinkedList;

import javax.media.sound.sampled.*;

import ptolemy.domains.sdf.kernel.*;

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

public class AudioSource extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>pathName</i> and <i>isURL</i> parameters Initialize <i>isURL</i>
     *  to true and <i>pathName</i> to StringToken with value
     *  "http://localhost/soundFile.au".
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
	// FIXME: Allow this to be set as parameter.
	productionRate = 512;
	output.setTokenProductionRate(productionRate);
	output.setMultiport(true); // ???
	pathName = new Parameter(this, "pathName",
                new StringToken("http://localhost/soundFile.au"));
	isURL = new Parameter(this, "isURL", new BooleanToken(true));
	isURL.setTypeEquals(BaseType.BOOLEAN);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. */
    public SDFIOPort output;

    /** Specify whether <i>pathName</i> is a URL or a file.
     * If <i>isURL</i> is true then <i>pathName</i> is a URL.
     * Else if <i>isURL</i>  is false then <i>pathName</i> is a
     * file. The default value of <i>isURL</i> is true.
     * FIXME: This must be set false (no support for Url yet).
     */
    public Parameter isURL;

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
        try {
            AudioSource newobj = (AudioSource)super.clone(ws);
            newobj.isURL = (Parameter)newobj.getAttribute("isURL");
            newobj.pathName = (Parameter)newobj.getAttribute("pathName");
            newobj.output = (SDFIOPort)newobj.getPort("output");
            // set the type constraints.
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Output the sample value of the sound file corresponding to the
     *  current index.
     */
    public boolean postfire() throws IllegalActionException {
        try {
	    int i;

	    int numBytesRead;


            // Read some audio into data[].
            numBytesRead =
                properFormatAudioInputStream.read(data);
            if (numBytesRead == -1) {
                // Ran out of samples to play. This generally means
                // that the end of the sound file has been reached.
                // Output productionRate many zeros.
                audioTokenArray = new DoubleToken[productionRate];
                // Convert to DoubleToken[].
                // FIXME: I don't think this is very efficient. Currently
                // creating a new token for each sample!
                for (i = 0; i < productionRate; i++) {
                    audioTokenArray[i] = new DoubleToken(0);
                }

                output.sendArray(0, audioTokenArray);
                return false;
            } else if (numBytesRead != data.length) {
                // Read fewer samples than productionRate many samples.

                // Ran out of samples to play. This generally means
                // that the end of the sound file has been reached.
                // Output productionRate many zeros.
                audioTokenArray = new DoubleToken[productionRate];
                // Convert to DoubleToken[].
                // FIXME: I don't think this is very efficient. Currently
                // creating a new token for each sample!
                for (i = 0; i < productionRate; i++) {
                    audioTokenArray[i] = new DoubleToken(0);
                }

                output.sendArray(0, audioTokenArray);
                return false;
            }


            // Convert byte array to double array.
            audioInDoubleArray = _byteArrayToDoubleArray(data, frameSizeInBytes);



            audioTokenArray = new DoubleToken[productionRate];
            // Convert to DoubleToken[].
            // FIXME: I don't think this is very efficient. Currently
            // creating a new token for each sample!
            for (i = 0; i < productionRate; i++) {
                audioTokenArray[i] = new DoubleToken(audioInDoubleArray[i]);
	    }


            output.sendArray(0, audioTokenArray);



        } catch (IllegalActionException ex) {
            // Should not be thrown because this is an output port.
            throw new InternalErrorException(ex.getMessage());
        } catch (IOException ex) {

            throw new InternalErrorException(ex.getMessage());
        }
	return true;
    }

    /** Read in the sound file specified by the <i>pathName</i> parameter
     *  and initialize the current sample index to 0.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();



	try {


	    //Sound sound = new Sound();


	    if (((BooleanToken)isURL.getToken()).booleanValue() == true) {
		// Load audio from a URL.
		// Create a URL corresponing to the sound file location.
		URL soundURL =
		    new URL(((StringToken)pathName.getToken()).toString());
                //		sound.load(soundURL);
	    } else {
		// Load audio from a file.
		File soundFile =
		    new File(((StringToken)pathName.getToken()).toString());
		//sound.load(soundFile);
		if (soundFile != null && soundFile.isFile()) {

		    try {
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		    } catch (UnsupportedAudioFileException e) {
			System.err.println("UnsupportedAudioFileException " + e);
		    } catch (IOException e) {
			System.err.println("IOException " + e);
		    }

		    String fileName = soundFile.getName();
		}

		// make sure we have something to play
		if (audioInputStream == null) {
		    System.err.println("No loaded audio to play back");
		    return;
		}

		AudioFormat origFormat = audioInputStream.getFormat();
		// Now convert to PCM_SIGNED_BIG_ENDIAN so that can get double
		// representation of samples.
		float sampleRate = origFormat.getSampleRate();
		int sampleSizeInBits = origFormat.getSampleSizeInBits();
		int channels = origFormat.getChannels();
		boolean signed = true;
		boolean bigEndian = true;
		AudioFormat format = new AudioFormat(sampleRate,
                        sampleSizeInBits, channels,
                        signed, bigEndian);
		//System.out.println("Converted format: " + format.toString());

		properFormatAudioInputStream =
		    AudioSystem.getAudioInputStream(format, audioInputStream);
		frameSizeInBytes = format.getFrameSize();
		// Array of audio samples in byte format.
		data = new byte[productionRate*frameSizeInBytes];

	    }

	    // Put all the samples in a double array.
            //	    audioArray = sound.getDoubleArray();

	    // Initialize the index to the first sample of the sound file.
	    _index = 0;


	} catch (MalformedURLException e) {
	    System.err.println(e.toString());
	} catch (IOException e) {
	    System.err.println("AudioSource: error reading"+
                    " input file: " +e);
	}

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Convert a byte array of audio samples in linear signed pcm big endian
     * format into a double array of audio samples (-1,1) range.
     * FIXME: This method only works for mono (single channel) audio.
     */
    private double[] _byteArrayToDoubleArray(byte[] byteArray, int _bytesPerSample) {

	//System.out.println("_bytesPerSample = " + _bytesPerSample);
	//System.out.println("byteArray length = " + byteArray.length);
	int lengthInSamples = byteArray.length / _bytesPerSample;
	double[] doubleArray = new double[lengthInSamples];
	double mathDotPow = Math.pow(2, 8 * _bytesPerSample - 1);

	for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {

	    byte[] b = new byte[_bytesPerSample];
	    for (int i = 0; i < _bytesPerSample; i += 1) {
		// Assume we are dealing with big endian.
		b[i] = byteArray[currSamp*_bytesPerSample + i];
	    }
	    long result = (b[0] >> 7) ;
	    for (int i = 0; i < _bytesPerSample; i += 1)
		result = (result << 8) + (b[i] & 0xff);
	    doubleArray[currSamp] = ((double) result/
                    (mathDotPow));
        }
	//System.out.println("a value " + doubleArray[34]);
	return doubleArray;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private AudioInputStream  properFormatAudioInputStream;

    private AudioInputStream audioInputStream;

    private int productionRate;

    private double[] audioArray;

    // Array of audio samples in double format.
    private double[] audioInDoubleArray;

    private DoubleToken[] audioTokenArray;

    private int _index;

    // An array of length productionRate containing samples to output.
    // private DoubleToken[] audioTokenArray;

    byte[] data;
    int frameSizeInBytes;
}
