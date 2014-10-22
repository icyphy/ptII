/* An actor that plays audio data from a specified file.

 @Copyright (c) 2007-2014 The Regents of the University of California.
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

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.ClassUtilities;

///////////////////////////////////////////////////////////////////
//// ClipPlayer

/**
 An actor that plays an audio clip given in a file. If the <i>overlay</i>
 parameter is false (the default), then it will terminate any previously
 playing clip before playing the new instance. Otherwise, it will mix
 in the new instance with the currently playing clip.
 @author  Edward A. Lee
 @version  $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 @see ptolemy.media.javasound.LiveSound
 @see AudioCapture
 @see AudioReader
 @see AudioWriter
 */
public class ClipPlayer extends TypedAtomicActor implements LineListener {
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
        // Use $CLASSPATH instead of $PTII so that this actor can find its
        // audio file under Web Start.
        fileOrURL
                .setExpression("$CLASSPATH/ptolemy/actor/lib/javasound/voice.wav");

        overlay = new Parameter(this, "overlay");
        overlay.setTypeEquals(BaseType.BOOLEAN);
        overlay.setExpression("false");

        playToCompletion = new Parameter(this, "playToCompletion");
        playToCompletion.setTypeEquals(BaseType.BOOLEAN);
        playToCompletion.setExpression("false");

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file or URL giving the audio clip.
     *  This is set by default to a file containing a voice signal.
     */
    public FileParameter fileOrURL;

    /** Output port used to indicate starts and stops.
     *  This is a boolean port. A true output indicates that
     *  a clip has been started, and a false output indicates that
     *  one has stopped.
     */
    public TypedIOPort output;

    /** If true, then if the actor fires before the previous clip
     *  has finished playing, then a new instance of the clip will
     *  be played on top of the tail of the previous instance, as
     *  long as the underlying mixer supports adding additional clips.
     *  This is a boolean that is false by default, which means that
     *  the clip is stopped and restarted each time the actor fires.
     */
    public Parameter overlay;

    /** If true, then play the clip to completion before returning
     *  from firing. This is a boolean that defaults to false.
     */
    public Parameter playToCompletion;

    /** The trigger.  When this port receives a token of any type,
     *  the actor begins playing the audio clip.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ClipPlayer newObject = (ClipPlayer) super.clone(workspace);

        newObject._outputEvents = new LinkedList<BooleanToken>();
        newObject._clips = new LinkedList<Clip>();

        return newObject;
    }

    /** Produce outputs indicating that the clip has started or stopped.
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void fire() throws IllegalActionException {
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }
        super.fire();

        boolean overlayValue = ((BooleanToken) overlay.getToken())
                .booleanValue();
        if (overlayValue || _clips.size() == 0) {
            // If there is an inactive clip in the list, then use that.
            // Otherwise, create a new one.
            for (Clip clip : _clips) {
                if (!clip.isActive()) {
                    clip.setFramePosition(0);
                    clip.start();
                }
            }
            try {
                Clip clip = AudioSystem.getClip();
                clip.addLineListener(this);
                AudioInputStream stream = null;
                try {
                    stream = AudioSystem.getAudioInputStream(fileOrURL.asURL());
                } catch (IOException ex) {
                    // Handle jar urls from WebStart or the installer
                    URL possibleJarURL = ClassUtilities
                            .jarURLEntryResource(fileOrURL.getExpression());
                    stream = AudioSystem.getAudioInputStream(possibleJarURL);
                }

                clip.open(stream);
                clip.start();
                _clips.add(clip);
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Error opening audio file or URL: "
                                + fileOrURL.getExpression());
            }
        } else {
            // Restart the last clip.
            Clip clip = _clips.get(_clips.size() - 1);
            // NOTE: Possible race condition: could become inactive
            // before the stop() is called, which could result in
            // two stop notifications to the update() method.
            // Will the Clip give to stop notifications?
            if (clip.isActive()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }
        boolean playToCompletionValue = ((BooleanToken) playToCompletion
                .getToken()).booleanValue();
        if (playToCompletionValue) {
            // Wait until the clip is finished.
            synchronized (_outputEvents) {
                while (true) {
                    if (_outputEvents.size() > 0) {
                        BooleanToken lastMessage = _outputEvents
                                .get(_outputEvents.size() - 1);
                        if (!lastMessage.booleanValue()) {
                            // Got the STOP message.
                            break;
                        }
                    }
                    // Either the thread hasn't started yet or it hasn't
                    // finished yet. Wait for one of those to occur.
                    try {
                        _outputEvents.wait();
                    } catch (InterruptedException e) {
                        throw new IllegalActionException(this,
                                "Wait for completion interrupted");
                    }
                }
            }
        }

        // Produce all outputs that have been requested.
        synchronized (_outputEvents) {
            for (BooleanToken token : _outputEvents) {
                output.send(0, token);
            }
            _outputEvents.clear();
        }
    }

    /** Called by the clip to notify this object of changes
     *  in the status of a clip.
     *  @param event The event, with one of type OPEN, CLOSE,
     *   START, STOP of class LineEvent.Type.
     */
    @Override
    public void update(LineEvent event) {
        if (event.getType().equals(LineEvent.Type.STOP)) {
            synchronized (_outputEvents) {
                _outputEvents.add(BooleanToken.FALSE);
                _outputEvents.notifyAll();
            }
        } else if (event.getType().equals(LineEvent.Type.START)) {
            synchronized (_outputEvents) {
                _outputEvents.add(BooleanToken.TRUE);
                _outputEvents.notifyAll();
            }
        }
        try {
            getDirector().fireAtCurrentTime(this);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }

    /** Stop audio playback and free up any audio resources used
     *  for audio playback.
     *  @exception IllegalActionException If there is a problem
     *   stopping audio playback.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        // Stop playback. Close any open sound files. Free
        // up audio system resources.
        for (Clip clip : _clips) {
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The output values to be produced on the next firing. */
    private List<BooleanToken> _outputEvents = new LinkedList<BooleanToken>();
}
