package ptolemy.actor.lib.jmf;

import java.awt.Component;
import java.awt.Container;

import java.io.IOException;


import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.protocol.DataSource;
import javax.media.GainControl;
import javax.media.Manager;
import javax.media.MediaException;
import javax.media.Player;
import javax.media.Time;
import javax.swing.JFrame;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

public class AudioPlayer extends TypedAtomicActor implements ControllerListener {

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
    
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.OBJECT);
    }

    public TypedIOPort input;

    /** React to notification of a change in controller status.
     *  event The event.
     */
    public synchronized void controllerUpdate(ControllerEvent event) {
        notifyAll();
    }

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
        //Component videoPanel = _player.getVisualComponent();
        //_container.add(videoPanel);
        _container.add(controlPanel);
        _frame.show();
        
         _player.start();
//         synchronized(this) {
//             while(_player.getState() == Controller.Started
//                     && !_stopRequested) {
//                 try {
//                     wait();
//                 } catch (InterruptedException ex) {
//                     break;
//                 }
//             } 
//         }
        return super.postfire();
    }
    
    /** Override the base class to stop the currently playing audio.
     */
    public void stopFire() {
        super.stopFire();
        if (_player != null) {
            // FIXME: Doesn't seem to stop the sound.
            _player.stop();
        }
    }

    /** Close the media processor.
     */
    public void wrapup() {
        if (_player != null) {
            _player.stop();
        }
    }

    private JFrame _frame;

    private Container _container;

    /** The player. */
    private Player _player;

    /** Start time for an audio clip. */
    private Time _startTime = new Time(0.0);
}
