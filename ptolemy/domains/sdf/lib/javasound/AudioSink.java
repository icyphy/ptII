/* An actor that writes input audio data to a sound file and/or plays
the audio data.

@Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating 
*/

package ptolemy.domains.sdf.lib.javasound;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.*;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import java.awt.*;

import java.io.*;
import java.net.*;
import java.util.*;
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
//// AudioSink
/** 
This actor reads in audio data from one input channel and records the
data to a sound file and/or plays the audio data. The input is of type
DoubleToken. Each DoubleToken read from the input represents one sample
of the audio data and should be in the range [-1,1]. Currently, only
single-channel audio is supported.
<p>
This actor will play the accumulated audio data on wrapup if the
<i>playAudio</i> parameter is true. It is true by default. This actor
will save the accumulated audio data to the file specified by the
<i>fileName</i> parameter if <i>saveAudio</i> is true. It is true
by default. The audio file format to use is inferred from the 
<i>fileName</i> parameter. Refer to the Java Sound API documentation
for a list of supported file formats (or just look at this code).
<p>
The sampling rate to use can be specified by the <i>sampRate</i>
parameter. The default sampling rate is 22050 Hz. The number of
bits/sample can be specified by the <i>sampleSizeInBits</i>
parameter. The default sample size is 16 bits.

@author  Brian K. Vogel
@version 
*/
public class AudioSink extends Sink {

    public AudioSink(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the input port.
        input.setTypeEquals(DoubleToken.class);

        fileName = new Parameter(this, "fileName", new StringToken("audioFile.au"));
        fileName.setTypeEquals(StringToken.class);

        sampRate = new Parameter(this, "sampRate", new IntToken(22050));
        sampRate.setTypeEquals(IntToken.class);

        playAudio = new Parameter(this, "playAudio", new BooleanToken(true));
        playAudio.setTypeEquals(BooleanToken.class);

        saveAudio = new Parameter(this, "saveAudio", new BooleanToken(true));
        saveAudio.setTypeEquals(BooleanToken.class);

        sampleSizeInBits = new Parameter(this, "sampleSizeInBits", new IntToken(16));
        sampleSizeInBits.setTypeEquals(IntToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the file to write to. This parameter contains
     *  a StringToken.
     */
    public Parameter fileName;

    /** The sampling rate to use, in Hz.
     */
    public Parameter sampRate;

    /** Play the accumulated audio data on wrapup if true.
     */
    public Parameter playAudio;

    /** Save the accumulated audio data on wrapup if true.
     */
    public Parameter saveAudio;

    /** Number of bits to use for each sample.
     */
    public Parameter sampleSizeInBits;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        AudioSink newobj = (AudioSink)super.clone(ws);
        newobj.fileName = (Parameter)newobj.getAttribute("fileName");
        newobj.sampRate = (Parameter)newobj.getAttribute("sampRate");
        newobj.playAudio = (Parameter)newobj.getAttribute("playAudio");
        newobj.saveAudio = (Parameter)newobj.getAttribute("saveAudio");
        newobj.sampleSizeInBits = (Parameter)newobj.getAttribute("sampleSizeInBits");
        return newobj;
    }

    /** Read at most one token from each input channel and write its
     *  string value to the specified file.  Each value is terminated
     *  with a newline character.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
      // FIXME: This currently ignores the width (reads only from input channel 0).
      //int width = input.getWidth();
	/*
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                String value = token.stringValue();
                System.out.println(value + "\n");
            }
        }
	*/
        if (input.hasToken(0)) {
	  // Read in a single sample.
	  DoubleToken token = (DoubleToken)input.get(0);
	  // Put the sample at the end of the AudioQueue
	  na.putSample(token.doubleValue());
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
        double samplingRate = ((IntToken)sampRate.getToken()).intValue();
        int sampleSizeInBitsInt = ((IntToken)sampleSizeInBits.getToken()).intValue();
        int channels = 1; // If change this, then need to change 
        // frameSizeInBits and frameRate accordingly.
        int frameSizeInBits = sampleSizeInBitsInt;
        double frameRate = samplingRate;
        //try {
        AudioFormat af = new   AudioFormat(Encoding.PCM_SIGNED_BIG_ENDIAN, samplingRate, sampleSizeInBitsInt, channels, frameSizeInBits, frameRate);
	      
        na  = new NewAudio(af);

    }


  /** Close the specified file, if any.
   */
  public void wrapup() throws IllegalActionException {
  	try {
	  String fileToSave = ((StringToken)fileName.getToken()).stringValue();

          if (((BooleanToken)saveAudio.getToken()).booleanValue()) {
              // Save the file in the appropriate format determined by
              // the file extension.
              // Separate the extension from the file using a period.
              StringTokenizer st = new StringTokenizer(fileToSave, ".");
              
              // Do error checking:
              if (st.countTokens() != 2) {
                  System.err.println("Error: Incorrect file name format. Format: filname.extension");
              }

              st.nextToken(); // Advance to the file extension.

              String fileExtension = st.nextToken();

              if (fileExtension.equalsIgnoreCase("au")) {
                  na.saveAs(fileToSave, FileStream.FileType.AU);  // Save the file.
              } else if (fileExtension.equalsIgnoreCase("aiff")) {
                  na.saveAs(fileToSave, FileStream.FileType.AIFF);  // Save the file.
              } else if (fileExtension.equalsIgnoreCase("wave")) {
                  na.saveAs(fileToSave, FileStream.FileType.WAVE);  // Save the file.
              } else if (fileExtension.equalsIgnoreCase("wav")) {
                  na.saveAs(fileToSave, FileStream.FileType.WAVE);  // Save the file.
              } else if (fileExtension.equalsIgnoreCase("aifc")) {
                  na.saveAs(fileToSave, FileStream.FileType.AIFC);  // Save the file.
              } else {
                  System.err.println("Error saving file: Unknown file format: " + fileExtension);
              }

              

              na.saveAs(fileToSave, FileStream.FileType.AU);  // Save the file.
          }
          if (((BooleanToken)playAudio.getToken()).booleanValue()) {
              na.startPlayback();  // Play the audio data.
          }

	} catch (IOException e) {
	  System.err.println("AudioSink: error saving" +
			     " file: " + e);
	} catch (AudioUnavailableException e) {
	    System.err.println("Audio is Unavailable" + e);
	}
  }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

  private NewAudio na;
}
