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
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.ClassUtilities;

///////////////////////////////////////////////////////////////////
//// ClipPlayer

/**
 An actor that plays an audio clip given in a file. Each time this
 actor fires, it starts playing the clip given by the
 <i>fileOrURL</i> parameter. If the <i>overlay</i>
 parameter is false (the default), then it will terminate any previously
 playing clip before playing the new instance. Otherwise, it will mix
 in the new instance with the currently playing clip.
 If <i>playToCompletion</i> is true, then each firing returns only after
 the clip has finished playing. Otherwise, the firing returns immediately,
 and another firing will result in either truncating the current clip or
 overlaying a new instance of it, depending on the value of <i>overlay</i>.
 If <i>outputOnlyOnStop</i> is false (the default), then this actor will
 produce an output (with value false) only when the current clip has finished playing.
 Otherwise, it will also produce an output (with value true) when the clip starts
 playing. If <i>playToCompletion</i> is true, both of these outputs will occur
 in the same firing. Otherwise, when the clip starts or stops (which occurs
 in another thread), this actor will request that the director fire it,
 and when it fires, it will produce the appropriate output.
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
        new SingletonParameter(trigger, "_showName").setToken(BooleanToken.TRUE);

        stop = new TypedIOPort(this, "stop", true, false);
        new SingletonParameter(stop, "_showName").setToken(BooleanToken.TRUE);

        fileOrURL = new FilePortParameter(this, "fileOrURL");
        // Use $CLASSPATH instead of $PTII so that this actor can find its
        // audio file under Web Start.
        fileOrURL.setExpression("$CLASSPATH/ptolemy/actor/lib/javasound/voice.wav");
        // new SingletonParameter(fileOrURL.getPort(), "_showName").setToken(BooleanToken.TRUE);
        new StringAttribute(fileOrURL.getPort(), "_cardinal").setExpression("SOUTH");

        overlay = new Parameter(this, "overlay");
        overlay.setTypeEquals(BaseType.BOOLEAN);
        overlay.setExpression("false");

        playToCompletion = new Parameter(this, "playToCompletion");
        playToCompletion.setTypeEquals(BaseType.BOOLEAN);
        playToCompletion.setExpression("false");
        
        outputOnlyOnStop = new Parameter(this, "outputOnlyOnStop");
        outputOnlyOnStop.setTypeEquals(BaseType.BOOLEAN);
        outputOnlyOnStop.setExpression("false");

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.BOOLEAN);
        
        _outputOnlyOnStop = false;
        _playToCompletion = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file or URL giving the audio clip.
     *  This is set by default to a file containing a voice signal.
     */
    public FilePortParameter fileOrURL;

    /** Output port used to indicate starts and stops.
     *  This is a boolean port. A true output indicates that
     *  a clip has been started, and a false output indicates that
     *  one has stopped.
     */
    public TypedIOPort output;
    
    /** If true, only produce a single FALSE token upon stop.  Useful for
     * chaining clips together in the SDF domain where multiple output tokens 
     * will cause an exception to be thrown.
     */
    public Parameter outputOnlyOnStop;

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
    
    /** Stop playback when this port receives a token of any type, if any
     * clip is playing.
     */
    public TypedIOPort stop;

    /** The trigger.  When this port receives a token of any type,
     *  the actor begins playing the audio clip.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** React to a change in an attribute.  In this case, check the
     *  value of the <i>path</i> attribute to make sure it is a valid URI.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == outputOnlyOnStop) {
            _outputOnlyOnStop = ((BooleanToken) outputOnlyOnStop.getToken())
                    .booleanValue();
        } else if (attribute == playToCompletion) {
            _playToCompletion = ((BooleanToken) playToCompletion.getToken())
                    .booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ClipPlayer newObject = (ClipPlayer) super.clone(workspace);

        newObject._clips = new LinkedList<Clip>();
        newObject._outputEvents = new LinkedList<BooleanToken>();
        newObject._outputOnlyOnStop = false;
        newObject._playToCompletion = false;
        

        return newObject;
    }

    /** Produce any pending outputs indicating that the clip has started or stopped,
     *  then if the stop input has a token, stop all clips that may be playing,
     *  then if the trigger input has a token, start a new instance of the clip
     *  playing. If playToCompletion is true, then do not return until the clip
     *  has completed playing. Otherwise, return immediately.
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void fire() throws IllegalActionException {
       
        super.fire();
        
        fileOrURL.update();
        
        // If refired to send an output, do only that.
        // This actor will be refired once per output.  Send exactly one token
        // per refiring.  (0 tokens if outputting only on STOP and refiring 
        // due to a START event).
        synchronized (_outputEvents) {
            if (!_outputEvents.isEmpty()) {
            // Produce all outputs that have been requested.
                BooleanToken token = _outputEvents.get(0);
                
                if (_outputOnlyOnStop){
                
                    // Check for STOP (i.e. FALSE) token.  If any present, 
                    // produce a FALSE token on the output port
                    if (!token.booleanValue()) {
                        output.send(0, token);
                    }
                } else {
                    output.send(0, token);
                } 
                _outputEvents.remove(0);
                return;
            }
        }
        
        boolean hasStop = false;
        boolean hasTrigger = false;
        
        // Consume any trigger inputs
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                hasTrigger = true;
                trigger.get(i);
            }
        }
        
        // If stop port has a token, stop playback of any clips and return.
        // If both stop and trigger have a token, stop playback first, then
        // start playback
        
        for (int i = 0; i < stop.getWidth(); i++) {
            if (stop.hasToken(i)) {
                hasStop = true;
                stop.get(i);
            }
        }
        
        if (hasStop) {
            for (Clip clip : _clips) {
                if (clip.isActive()) {
                    clip.stop();
                }
            }
         
            if(!hasTrigger) {
                return;
            }
        }

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
                
                if (_outputOnlyOnStop){
                    // Check for STOP (i.e. FALSE) token.  If any present, 
                    // produce a FALSE token on the output port
                    for (BooleanToken token : _outputEvents) {
                        if (!token.booleanValue()) {
                            output.send(0, token);
                        }
                    }
                } else {
                    for (BooleanToken token : _outputEvents) {
                        output.send(0, token);
                    }
                } 
               _outputEvents.clear();
            }
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
        
        // Don't need to refire in case of playToCompletion, since actor
        // will block waiting for a STOP output event.
        if (!_playToCompletion) {
            try {
                getDirector().fireAtCurrentTime(this);
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
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
        
        synchronized(_outputEvents) {
            _outputEvents.clear();
        }

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
    
    /** True if an output should only be generated once the clip is finished
     * playing or is stopped via the stop input port. 
     */
    private boolean _outputOnlyOnStop;
    
    /** True if the actor should block until the end of the clip is reached. */
    private boolean _playToCompletion;
}
