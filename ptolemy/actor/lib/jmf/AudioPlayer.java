/* An actor that plays a DataSource containing a music file.

@Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jmf;

import ptolemy.actor.lib.Sink;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.MediaException;
import javax.media.Player;
import javax.media.Time;
import javax.media.protocol.DataSource;
import javax.swing.JFrame;

//////////////////////////////////////////////////////////////////////////
//// AudioPlayer
/**
   This actor accepts an ObjectToken that contains a DataSource.
   This is typically obtained from the output of the StreamLoader
   actor.  This actor will play Datasources containing a MP3, MIDI, and
   CD Audio file.  After the model is run, a window will pop up
   allowing control of playing, rate of playback, and volume control.

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.1
*/

public class AudioPlayer extends Sink implements ControllerListener {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioPlayer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to notification of a change in controller status.
     *  @param event The event.
     */
    public synchronized void controllerUpdate(ControllerEvent event) {
        notifyAll();
    }

    /** Accept an ObjectToken containing a DataSource, and set it up
     *  for playing.
     *  @exception IllegalActionException If there is no director,
     *  if the file cannot be opened, or if the Java Media Framework
     *  throws an exception.
     *  @return super.postfire()
     */
    public boolean postfire() throws IllegalActionException {
        ObjectToken objectToken = (ObjectToken) input.get(0);
        DataSource input = (DataSource) objectToken.getValue();
        if (_player != null) {
            _player.removeControllerListener(this);
        }
        try {
            _player = Manager.createRealizedPlayer(input);
            _player.addControllerListener(this);
            _player.prefetch();
        } catch (IOException ex) {
            throw new IllegalActionException(this,
                    "Cannot open file: " + ex.toString());
        } catch (MediaException ex) {
            throw new IllegalActionException(this,
                    "Exception thrown by media framework: " + ex.toString());
        }

        _player.setMediaTime(_startTime);

        _frame = new JFrame();
        _container = _frame.getContentPane();
        Component controlPanel = _player.getControlPanelComponent();
        _container.add(controlPanel);
        _frame.pack();
        _frame.show();

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The container that contains the control panel components. */
    private Container _container;

    /** The JFrame where the the container is put. */
    private JFrame _frame;

    /** The player. */
    private Player _player;

    /** Start time for the audio clip. */
    private Time _startTime = new Time(0.0);
}




