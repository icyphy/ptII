package ptolemy.actor.lib.jmf;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Sink;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.BorderLayout;
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
//// VideoPlayer
/**
   This actor accepts an ObjectToken that contains a DataSource.
   This is typically obtained from the output of the StreamLoader
   actor.  This actor will play Datasources containing a AVI, Quicktime,
   or MPEG video file.  After the model is runned, a window will pop up
   allowing control of playing, rate of playback, and volume control.

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.1
*/

public class VideoPlayer extends Sink implements ControllerListener {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VideoPlayer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.OBJECT);
    }

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
        _container.setLayout(new BorderLayout());
        Component controlPanel = _player.getControlPanelComponent();
        Component videoPanel = _player.getVisualComponent();
        _container.add(videoPanel, BorderLayout.CENTER);
        _container.add(controlPanel, BorderLayout.SOUTH);
        _container.validate();
        _frame.pack();
        _frame.show();
        
        _player.start();
        return super.postfire();
    }
    
    
    /** The container that contains the control panel components. */ 
    private Container _container;
    
    /** The JFrame where the the container is put. */
    private JFrame _frame;

    /** The player. */
    private Player _player;

    /** Start time for the video clip. */
    private Time _startTime = new Time(0.0);
}
