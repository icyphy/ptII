/* An actor that outputs frames from a video file.

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

import java.net.URL;

import javax.media.Buffer;
import javax.media.ConfigureCompleteEvent;
import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.control.FrameGrabbingControl;
import javax.media.control.FramePositioningControl;
import javax.media.protocol.DataSource;

import ptolemy.actor.lib.Source;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// MovieReader
/**
   This actor loads a video file (MPEG, AVI, or Quicktime files only), and
   outputs each frame as a JMFImageToken.

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.1
*/

public class MovieReader extends Source implements ControllerListener {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MovieReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.OBJECT);
        fileOrURL = new FileParameter(this, "fileOrURL");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by File Attribute.
     *  @see FileParameter
     */
    public FileParameter fileOrURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** An attempt is made to acquire the file name.  If it is
     *  successful, create the DataSource that encapsulates the file.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the URL is null, or
     *  invalid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileOrURL) {
            URL url = fileOrURL.asURL();
            if (url == null) {
                throw new IllegalActionException("URLToken was null");
            } else {
                try {
                    _dataSource = Manager.createDataSource(url);
                } catch (Exception error) {
                    throw new IllegalActionException("Invalid URL");
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** The controller listener.  This method controls the
     *  initializing of the player.  It also senses when the
     *  file is done playing, in which case it closes the
     *  player.
     *  @param event The controller event.
     */
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof ConfigureCompleteEvent ||
                event instanceof RealizeCompleteEvent ||
                event instanceof PrefetchCompleteEvent) {
            synchronized (_waitSync) {
                _stateTransitionOK = true;
                _waitSync.notifyAll();
            }
        } else if (event instanceof ResourceUnavailableEvent) {
            synchronized (_waitSync) {
                _stateTransitionOK = false;
                _waitSync.notifyAll();
            }
        } else if (event instanceof EndOfMediaEvent) {
            _player.close();
            _playerOpen = false;
        }
    }

    /** Send a JMFImageToken out through the output port.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, new JMFImageToken(_frame));
    }

    /** An attempt is made to acquire both the frame grabbing and
     *  frame positioning controls.  If both succeed, the first
     *  frame is acquired.
     *  @exception IllegalActionException If the either frame
     *  grabbing control or frame positioning control cannot
     *  be acquired, or if a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        try {
            _player = Manager.createPlayer(_dataSource);
        } catch (Exception e) {
            throw new IllegalActionException(null, e,
                    "Failed to create a player for the data source. "
                    + "Note that you may need to run jmfinit, which is found "
                    + "in the JMF directory, for example c:/Program Files/"
                    + "JMF2.1.1/bin.  The original exception was: "
                    + _dataSource);
        }

        _player.addControllerListener(this);

        _player.realize();

        if (!_waitForState(Controller.Realized)) {
            throw new IllegalActionException(null,
                    "Failed to realize player");
        }

        _framePositioningControl =
            (FramePositioningControl)_player.getControl(
                    "javax.media.control.FramePositioningControl");

        if (_framePositioningControl == null) {
            throw new IllegalActionException(null,
                    "Failed to get Frame Poisitioning Control");
        }

        _frameGrabbingControl =
            (FrameGrabbingControl)_player.getControl(
                    "javax.media.control.FrameGrabbingControl");

        if (_frameGrabbingControl == null) {
            throw new IllegalActionException(null,
                    "Failed to get Frame Grabbing Control");
        }

        _player.prefetch();

        if (!_waitForState(Controller.Prefetched)) {
            throw new IllegalActionException(null,
                    "Failed to prefetch player");
        }

        //load first frame

        _frame = _frameGrabbingControl.grabFrame();
        _framePositioningControl.skip(1);

    }

    /** If the player is no longer open, then disconnect the
     *  datasource.  If the player is still open, acquire the next
     *  frame.
     *  @return false if the player is no longer open, otherwise
     *  return super.postfire().
     */
    public boolean postfire() throws IllegalActionException {
        if (_playerOpen == false) {
           _dataSource.disconnect();
           return false;
        } else {
            _frame = _frameGrabbingControl.grabFrame();
            _framePositioningControl.skip(1);
            return super.postfire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Block until the processor has transitioned to the given state.
     *  @return false if the transition failed.
     */
    protected boolean _waitForState(int state) throws IllegalActionException {
        synchronized (_waitSync) {
            try {
                while (_player.getState() != state && _stateTransitionOK)
                    _waitSync.wait();
            } catch (Exception e) {
                throw new IllegalActionException(null, e,
                        "Failed block the processor until it state"
                        + " transition completed.");
            }
        }
        return _stateTransitionOK;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The datasource that encapsulates the video file.
    private DataSource _dataSource;

    // The individual frame to be sent to the output port.
    private Buffer _frame;

    // The Frame grabbing control class that allows individual frames
    // to be acquired from the file.
    private FrameGrabbingControl _frameGrabbingControl;

    // The Frame positioning control class that allows control over
    // which frame is the current one.
    private FramePositioningControl _framePositioningControl;

    // The player.
    private Player _player;

    // Boolean that keeps track of whether the player is open or not.
    private boolean _playerOpen = true;

    // Boolean that keeps track of whether the player initialization
    // has gone through smoothly.
    private boolean _stateTransitionOK = true;

    // Object to allow synchronization in this actor.
    private Object _waitSync = new Object();

}




