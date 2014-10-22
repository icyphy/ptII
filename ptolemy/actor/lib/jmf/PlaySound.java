/* An actor that plays a sound from a file or URL.

 @Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jmf;

import java.io.IOException;

import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.GainControl;
import javax.media.Manager;
import javax.media.MediaException;
import javax.media.Player;
import javax.media.Time;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.IntRangeParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// PlaySound

/**
 This actor plays audio from a file or URL when it fires.
 If the input has value <i>true</i>, then the sound is played.
 If it has value <i>false</i>, then the sound is stopped.
 If the input is not connected, or the actor fires with no input,
 then the sound is played when it fires.
 It requires the Java Media Framework.

 @author  Edward Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PlaySound extends TypedAtomicActor implements ControllerListener {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PlaySound(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        fileNameOrURL = new FileParameter(this, "fileNameOrURL");

        synchronizedPlay = new Parameter(this, "synchronizedPlay");
        synchronizedPlay.setTypeEquals(BaseType.BOOLEAN);
        synchronizedPlay.setToken(BooleanToken.TRUE);

        onOff = new TypedIOPort(this, "onOff", true, false);
        onOff.setTypeEquals(BaseType.BOOLEAN);

        percentGain = new IntRangeParameter(this, "percentGain");

        // Set the default value to full scale.
        percentGain.setToken(new IntToken(100));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters and ports                  ////

    /** The file name or URL to read. */
    public FileParameter fileNameOrURL;

    /** The input port, which has type boolean.  A true input
     *  causes the sound to be played, and false input causes it
     *  to be stopped.
     */
    public TypedIOPort onOff;

    /** The gain (in percent).  This has as its value a record of the form
     *  {min = m, max = M, current = c}, where min <= c <= max.
     */
    public IntRangeParameter percentGain;

    /** Indicator to play to the end before returning from fire().
     *  This is a boolean, and defaults to true.
     */
    public Parameter synchronizedPlay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>fileNameOrURL</i>, then create a new
     *  player.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the file cannot be opened
     *   or if the base class throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileNameOrURL) {
            try {
                if (fileNameOrURL != null && fileNameOrURL.asURL() != null) {
                    if (_player != null) {
                        _player.removeControllerListener(this);
                    }

                    _player = Manager.createRealizedPlayer(fileNameOrURL
                            .asURL());
                    _player.addControllerListener(this);

                    // Initiate as much preprocessing as possible.
                    // _player.realize();
                    _player.prefetch();
                    _gainControl = _player.getGainControl();

                    if (percentGain != null) {
                        _gainControl.setLevel(0.01f * percentGain
                                .getCurrentValue());
                    }
                }
            } catch (IOException ex) {
                throw new IllegalActionException(this, "Cannot open file: "
                        + ex.toString());
            } catch (MediaException ex) {
                throw new IllegalActionException(this, ex,
                        "Exception thrown by media framework");
            }
        } else if (attribute == percentGain && _gainControl != null) {
            _gainControl.setLevel(0.01f * percentGain.getCurrentValue());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** React to notification of a change in controller status.
     *  event The event.
     */
    @Override
    public synchronized void controllerUpdate(ControllerEvent event) {
        notifyAll();
    }

    /** Play the audio file.
     *  @exception IllegalActionException If there's no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // Consume the inputs.
        // Default if there is no input is to play the sound.
        boolean playSound = true;

        if (onOff.isOutsideConnected() && onOff.hasToken(0)) {
            playSound = ((BooleanToken) onOff.get(0)).booleanValue();
        }

        // If there is no player, then no sound file has been specified.
        // Just return.
        if (_player == null) {
            return;
        }

        // Call this whether we have synchronized play or not, since
        // we may now have synchronized play but not have had it before.
        _player.stop();

        if (playSound) {
            // Specify that play should start at the beginning of the audio.
            // Start time for an audio clip.
            Time startTime = new Time(0.0);

            _player.setMediaTime(startTime);

            _player.start();

            // If synchronizedPlay is true, then wait for the play to complete.
            boolean synch = ((BooleanToken) synchronizedPlay.getToken())
                    .booleanValue();

            if (synch) {
                synchronized (this) {
                    while (_player.getState() == Controller.Started
                            && !_stopRequested) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                }
            }
        }
    }

    /** Override the base class to stop the currently playing audio.
     */
    @Override
    public void stopFire() {
        super.stopFire();

        if (_player != null) {
            // FIXME: Doesn't seem to stop the sound.
            _player.stop();
        }
    }

    /** Close the media processor.
     */
    @Override
    public void wrapup() {
        if (_player != null) {
            _player.stop();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The gain control associated with the player. */
    private GainControl _gainControl;

    /** The player. */
    private Player _player;
}
