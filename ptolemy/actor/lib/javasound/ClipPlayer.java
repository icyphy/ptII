/* An actor that plays audio data from a specified file.

 @Copyright (c) 2000-2005 The Regents of the University of California.
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
 */
package ptolemy.actor.lib.javasound;

import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/////////////////////////////////////////////////////////
//// ClipPlayer

/**
 FIXME: Update.
 This actor plays audio samples provided on the input port
 starting at a time corresponding to the time stamp of the input.
 The audio samples that are supplied to
 this actor should be doubles in the range [-1.0, 1.0], provided
 as a DoubleMatrix, where the first index of the matrix represents
 the channel and the second index is the sample number. Any input
 value that is outside of the valid range will be hard-clipped
 to fall within the range [-1.0, 1.0] before it is written
 to the audio output port of the computer.
 <p>
 If this actor is invoked multiple times with overlapping
 audio segments, then it will add the audio signals before sending
 to the hardware.
 <p>
 The parameters are as follows:
 <ul>
 <li><i>sampleRate</i> should be set to desired sample rate, in Hz.
 The default value is 8000. Allowable values are 8000, 11025,
 22050, 44100, and 48000 Hz.
 <li><i>bytesPerSample</i> gives the resolution of audio samples.
 This is an integer that defaults to 2, meaning 16-bit samples.
 <li><i>channels</i> should be set to desired number of audio
 channels. Allowable values are 1 (for mono) and 2 (for stereo).
 The default value is 1. Some sound cards support more than two
 audio channels, but this is not supported in Java.
 <li><i>transferSize</i> The number of samples that will
 be transferred to the audio driver
 together.  This is an integer with default 128. 
 <li><i></i>  The requested buffer size in the audio hardware. This
 affects how far ahead of real time the model can get. There is no
 harm in making this large because this actor will overwrite previously
 queued values if necessary. This is an integer
 that defaults to 8000, representing a buffer with
 8000 samples per channel. At an 8 kHz sample rate,
 this corresponds to one second of sound.
 </ul>
 <p>
 All of these parameters are shared by all audio actors that
 use the audio hardware, so changing them in
 one of those actors will cause it to change in all.
 <p>
 Note: Requires Java 2 v1.3.0 or later.
 @author  Edward A. Lee
 @version  $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 @see ptolemy.media.javasound.LiveSound
 @see AudioCapture
 @see AudioReader
 @see AudioWriter
 */
public class ClipPlayer extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ClipPlayer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        trigger = new TypedIOPort(this, "trigger", true, false);

        fileOrURL = new FileParameter(this, "fileOrURL");
        fileOrURL.setExpression("$PTII/ptolemy/actor/lib/javasound/voice.wav");
        
        overlay = new Parameter(this, "overlay");
        overlay.setTypeEquals(BaseType.BOOLEAN);
        overlay.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file or URL giving the audio clip.
     *  This is set by default to a file containing a voice signal.
     */
    public FileParameter fileOrURL;
    
    /** If true, then if the actor fires before the previous clip
     *  has finished playing, then a new instance of the clip will
     *  be played on top of the tail of the previous instance, as
     *  long as the underlying mixer supports adding additional clips.
     *  This is a boolean that is false by default, which means that
     *  the clip is stopped and restarted each time the actor fires.
     */
    public Parameter overlay;
    
    /** The trigger.  When this port receives a token of any type,
     *  the actor begins playing the audio clip.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read an input array and send to the audio hardware.
     *  If the audio buffer cannot accept the samples, then this
     *  method will stall the calling thread until it can.
     *  @exception IllegalActionException If there is a problem
     *   playing audio.
     */
    public boolean postfire() throws IllegalActionException {
        if (trigger.hasToken(0)) {
            trigger.get(0);
            boolean overlayValue = ((BooleanToken)overlay.getToken()).booleanValue();
            if (overlayValue || _clips.size() == 0) {
                // If there is an inactive clip in the list, then use that.
                // Otherwise, create a new one.
                for (Clip clip: _clips) {
                    if (!clip.isActive()) {
                        clip.setFramePosition(0);
                        clip.start();
                        return true;
                    }
                }
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream stream = AudioSystem.getAudioInputStream(fileOrURL.asURL());
                    clip.open(stream);
                    clip.start();
                    _clips.add(clip);
                } catch (Exception e) {
                    throw new IllegalActionException(this, e,
                            "Error opening audio file or URL: "
                            + fileOrURL.getExpression());
                }
            } else {
                // Restart the last clip.
                Clip clip = _clips.get(_clips.size() - 1);
                clip.stop();
                clip.setFramePosition(0);
                clip.start();
            }
        }
        return true;
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
        for (Clip clip: _clips) {
            clip.flush();
            clip.stop();
            clip.close();            
        }
        _clips.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The clip to playback. */
    protected List<Clip> _clips = new LinkedList<Clip>();
}
